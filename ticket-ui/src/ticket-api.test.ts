import { afterEach, expect, test, vi } from "vitest";
import { completeTicket } from "./ticket-api";
import type { Ticket } from "../../tickets";

// 이 테스트의 검사 대상은 실제 completeTicket()입니다.
// fetch만 가짜로 바꾸므로 Spring 서버나 DB는 호출하지 않습니다.
// 요청 구성과 응답 처리를 검사하며, DB의 상태 변경이나 버전 증가는 검사하지 않습니다.

// 테스트가 성공하거나 실패한 뒤에도 원래 fetch를 복원합니다.
// 가짜 fetch 설정이 다음 테스트에 영향을 주지 않도록 정리하는 단계입니다.
afterEach(() => {
    vi.unstubAllGlobals();
});

test("완료 요청에 조회한 버전을 보내고 응답 티켓을 반환한다", async () => {
    // 1. 서버가 성공 응답을 보냈다고 가정할 데이터를 준비합니다.
    // version: 4는 우리가 지정한 값입니다. 이 테스트에서 3 + 1을 계산하지 않습니다.
    const responseTicket: Ticket = {
        id: 1,
        title: "로그인 오류",
        status: "CLOSED",
        version: 4,
    };

    // 2. vi.fn()은 호출 횟수와 전달받은 인수를 기록하는 가짜 함수를 만듭니다.
    // <typeof fetch>는 실제 fetch와 같은 함수 타입을 사용한다는 뜻입니다.
    // mockResolvedValue()는 호출 시 아래 Response로 이행되는 Promise를 반환하도록 합니다.
    // 요청의 버전을 검사하거나 증가시키지 않고, 준비한 응답을 그대로 돌려줍니다.
    const fetchMock = vi.fn<typeof fetch>().mockResolvedValue(
        // 응답 객체를 JSON 문자열로 만들어 HTTP 응답 형태로 감쌉니다.
        // 나중에 실제 코드의 response.json()이 이 문자열을 객체로 읽습니다.
        new Response(JSON.stringify(responseTicket), {
            status: 200,
            headers: {
                "Content-Type": "application/json",
            },
        })
    );

    // 전역 fetch를 교체합니다. 이후 실제 코드에서 fetch()를 부르면 fetchMock이 실행됩니다.
    vi.stubGlobal("fetch", fetchMock);

    // 3. 실제 completeTicket()에 티켓 ID 1과 화면에서 조회한 버전 3을 전달합니다.
    // 내부의 요청 구성, response.ok 확인, JSON 읽기, isTicket 검사, 반환은 실제 코드입니다.
    // result는 요청 내용이 아니라 함수가 반환한 응답 티켓입니다.
    const result = await completeTicket(1, 3);

    // 4. 위 실행 중 남은 호출 기록을 검사합니다. 아래 expect는 요청을 새로 보내지 않습니다.
    // completeTicket() 한 번 실행에 fetch가 정확히 한 번 호출됐는지 확인합니다.
    // 실제 버튼의 중복 클릭 방지 기능까지 검사하는 것은 아닙니다.
    expect(fetchMock).toHaveBeenCalledTimes(1);

    // fetch가 아래 주소와 요청 설정을 인수로 받아 호출됐는지 확인합니다.
    // URL, PATCH 방식, JSON 헤더, 본문의 버전 3 중 하나라도 다르면 실패합니다.
    // 예: 실제 코드가 전달받은 버전 3을 무시하고 항상 0을 보내는 실수를 잡습니다.
    expect(fetchMock).toHaveBeenCalledWith(
        "/api/tickets/1/complete",
        {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ version: 3 }),
        }
    );

    // 5. 실제 함수가 읽고 검사한 뒤 반환한 티켓의 필드 값들을 준비한 응답과 비교합니다.
    // 같은 객체 참조인지가 아니라 id, title, status, version의 값이 같은지 검사합니다.
    // 실제 함수에서 return을 빼먹거나 응답의 version을 덮어쓰면 실패합니다.
    // 서버가 버전을 4로 증가시켰다는 증거는 아닙니다. 4는 위에서 준비한 값입니다.
    expect(result).toEqual(responseTicket);
});




test("완료 요청이 충돌하면 서버 안내 메시지로 오류를 던진다", async () => {
    // 1. 서버가 반환할 충돌 안내 메시지입니다.
    const message =
        "다른 요청이 티켓을 변경했습니다. 목록을 새로 조회한 뒤 다시 시도해 주세요.";

    // 2. fetch 자체는 응답을 받았지만, HTTP 상태는 409인 상황입니다.
    const fetchMock = vi.fn<typeof fetch>().mockResolvedValue(
        new Response(JSON.stringify({ message }), {
            status: 409,
            headers: {
                "Content-Type": "application/json",
            },
        })
    );

    vi.stubGlobal("fetch", fetchMock);

    // 3. 실제 completeTicket()이 응답을 처리한 결과를 검사합니다.
    // 정상 반환하면 실패하고, 지정한 메시지의 Error로 거절되면 통과합니다.
    await expect(completeTicket(1, 3))
        .rejects.toThrow(message);
        // .rejects Promise가 실패 상태로 끝나야 합니다. 
        // .toThrow(message) 실패 원인이 오류이고, 오류 메시지에 message 문자열이 포함되어야 합니다.
});




test("완료 응답에 버전이 없으면 오류를 던진다", async () => {

    const invalidTicket = {
        id: 1,
        title: "로그인 오류",
        status: "CLOSED",
    };

    const fetchMock = vi.fn<typeof fetch>().mockResolvedValue(
        // 응답 객체를 JSON 문자열로 만들어 HTTP 응답 형태로 감쌉니다.
        // 나중에 실제 코드의 response.json()이 이 문자열을 객체로 읽습니다.
        new Response(JSON.stringify(invalidTicket), {
            status: 200,
            headers: {
                "Content-Type": "application/json",
            },
        })
    );

    vi.stubGlobal("fetch", fetchMock);
    
    await expect(completeTicket(1, 3))
        .rejects.toThrow("완료 응답의 티켓 형식이 올바르지 않습니다.");
})