import { afterEach, expect, test, vi } from "vitest";
import { fetchTicketAdvice } from "./ticket-advice-api";

afterEach(() => {
    // 테스트에서 바꾼 전역 fetch를 원래대로 복구
    vi.unstubAllGlobals();
});

test("티켓 제목을 전송하고 AI 도움말을 반환한다", async () => {

    const advice = "프린터 케이블과 드라이버를 확인하세요.";

    // 실제 서버 대신 돌려줄 HTTP 응답 준비
    const fetchMock = vi.fn<typeof fetch>().mockResolvedValue(
        new Response(JSON.stringify({ advice }), {
            status: 200,
            headers: {
                "Content-Type": "application/json",
            },
        })
    );

    // 실제 네트워크 통신 대신 가짜 fetch 사용
    vi.stubGlobal("fetch", fetchMock);

    // 우리가 작성한 실제 함수를 실행
    const result = await fetchTicketAdvice("프린터 연결 오류");

    // 요청이 정확히 한 번 발생했는지 확인
    expect(fetchMock).toHaveBeenCalledTimes(1);

    // URL, 메서드, 헤더, 요청 본문 확인
    expect(fetchMock).toHaveBeenCalledWith(
        "/api/ai/ticket-advice",
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                title: "프린터 연결 오류",
            }),
        }
    );

    // 응답 객체에서 advice 문자열을 꺼내 반환했는지 확인
    expect(result).toBe(advice);
});




test("응답에 advice가 없으면 오류를 던진다", async () => {

    const message = "잘못된 응답";

    // 실제 서버 대신 돌려줄 HTTP 응답 준비
    const fetchMock = vi.fn<typeof fetch>().mockResolvedValue(
        new Response(JSON.stringify({ message }), {
            status: 200,
            headers: {
                "Content-Type": "application/json",
            },
        })
    );

    // 실제 네트워크 통신 대신 가짜 fetch 사용
    vi.stubGlobal("fetch", fetchMock);

    // 정상 반환하면 실패하고, 지정한 메시지의 Error로 거절되면 통과합니다.
    await expect(fetchTicketAdvice("프린터 연결 오류"))
        .rejects.toThrow("AI 도움말 응답 형식이 올바르지 않습니다.");
        // .rejects Promise가 실패 상태로 끝나야 합니다.
        // .toThrow(message) 실패 원인이 오류이고, 오류 메시지에 message 문자열이 포함되어야 합니다.

});

test("서버가 503을 반환하면 요청 실패 오류를 던진다", async () => {
    const fetchMock = vi.fn<typeof fetch>().mockResolvedValue(
        new Response(null, {
            status: 503,
        })/*응답 본문이 없는 HTTP 응답 */
    );

    vi.stubGlobal("fetch", fetchMock);

    await expect(fetchTicketAdvice("프린터 연결 오류"))
        .rejects.toThrow("AI 도움말 요청 실패: 503");
});