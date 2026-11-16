import { afterEach, expect, test, vi } from "vitest";
import { fetchStructuredTicketAdvice } from "./ticket-advice-api";
import type { TicketAdviceResult } from "./ticket-advice-api";

afterEach(() => {
    // 테스트에서 바꾼 전역 fetch를 원래대로 복구 (전역 변수 및 객체)
    vi.unstubAllGlobals();
    // vi.spyOn()으로 감시하거나 대체한 함수 복구
    vi.restoreAllMocks();

});

test("구조화된 도움말을 요청하고 분류와 확인 사항을 반환한다", async () => {
    const responseAdvice: TicketAdviceResult = {
        category: "프린터",
        checks: [
            "케이블 연결 상태를 확인하세요.",
            "드라이버 설치 상태를 확인하세요.",
        ],
    };

    // 서버에서 받을 JSON 응답을 준비
    const fetchMock = vi.fn<typeof fetch>().mockResolvedValue(
        new Response(JSON.stringify(responseAdvice), {
            status: 200,
            headers: {
                "Content-Type": "application/json",
            },
        })
    );

    vi.stubGlobal("fetch", fetchMock);

    // 우리가 작성한 실제 API 함수를 실행
    const result = await fetchStructuredTicketAdvice("프린터 연결 오류");

    expect(fetchMock).toHaveBeenCalledTimes(1);

    expect(fetchMock).toHaveBeenCalledWith(
        "/api/ai/ticket-advice/structured",
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                title: "프린터 연결 오류",
            }),
            signal: expect.any(AbortSignal),    //signal에 AbortSignal 객체가 전달됐는지 확인
        }
    );

    // 반환 객체의 필드와 배열 내용이 같은지 확인
    expect(result).toEqual(responseAdvice);
});

test("구조화된 응답에 공백 확인 사항이 있으면 오류를 던진다", async () => {
    const invalidAdvice: TicketAdviceResult = {
        category: "프린터",
        checks: [
            "케이블 연결 상태를 확인하세요.", "    "
        ],
    };

    // 서버에서 받을 JSON 응답을 준비
    const fetchMock = vi.fn<typeof fetch>().mockResolvedValue(
        new Response(JSON.stringify(invalidAdvice), {
            status: 200,
            headers: {
                "Content-Type": "application/json",
            },
        })
    );

    vi.stubGlobal("fetch", fetchMock);

    await expect(fetchStructuredTicketAdvice("프린터 연결 오류"))
    .rejects.toThrow("AI 도움말 응답 형식이 올바르지 않습니다.");
});

test("구조화된 도움말 요청이 실패하면 HTTP 오류를 던진다", async () => {
    // 1. 본문이 없고 HTTP 상태가 503인 Response 준비
        const fetchMock = vi.fn<typeof fetch>().mockResolvedValue(
        new Response(null ,{status: 503})
    );

    // 2. fetchMock을 전역 fetch에 연결
    vi.stubGlobal("fetch", fetchMock);

    // 3. fetchStructuredTicketAdvice() 호출
    // 4. "AI 도움말 요청 실패: 503"으로 거절되는지 확인
     await expect(fetchStructuredTicketAdvice("프린터 연결 오류"))
        .rejects.toThrow("AI 도움말 요청 실패: 503");
});

test("구조화된 응답의 확인 사항이 한 개이면 오류를 던진다", async () => {
    const invalidAdvice = {
        category: "프린터",
        checks: ["케이블 연결 상태를 확인하세요."],
    };

    const fetchMock = vi.fn<typeof fetch>().mockResolvedValue(
        new Response(JSON.stringify(invalidAdvice), {
            status: 200,
            headers: {
                "Content-Type": "application/json",
            },
        })
    );

    vi.stubGlobal("fetch", fetchMock);

    await expect(fetchStructuredTicketAdvice("프린터 연결 오류"))
        .rejects.toThrow("AI 도움말 응답 형식이 올바르지 않습니다.");
});

test("AI 응답 검증에 실패하면 서버 안내 메시지로 오류를 던진다", async () => {

    const message = "AI 답변을 올바르게 생성하지 못했습니다. 다시 요청해 주세요.";

    const fetchMock = vi.fn<typeof fetch>().mockResolvedValue(
        new Response(JSON.stringify({ message }), {
            status: 502,
            headers: {
                "Content-Type": "application/json",
            },
        })
    );

    vi.stubGlobal("fetch", fetchMock);

    await expect(fetchStructuredTicketAdvice("프린터 연결 오류")).rejects.toThrow(message);
});

test("오류 응답이 HTML이면 기본 오류 메시지를 사용한다", async () => {

    const fetchMock = vi.fn<typeof fetch>().mockResolvedValue(
        new Response("<html><body>Bad Gateway</body></html>", {
            status: 502,
            headers: {
                "Content-Type": "text/html",
            },
        })
    );

    vi.stubGlobal("fetch", fetchMock);
    await expect(fetchStructuredTicketAdvice("프린터 연결 오류")).rejects.toThrow("AI 도움말 요청 실패: 502");

});




test("브라우저 대기 시간이 초과되면 안내하고 원래 오류를 보존한다", async () => {
    //시간 초과 오류를 만든다
    const timeoutError = new DOMException(
        // 브라우저에서 발생하는 시간 초과(TimeoutError) 예외를 직접 생성하는 코드
        "테스트용 시간 초과",
        "TimeoutError"
    );

    // 요청을 중단시킬 수 있는 도구를 만든다.
    //AbortController는 진행 중인 작업을 중단시킬 수 있는 브라우저 API
    const controller = new AbortController();

    // 미리 요청을 중단시킨다. 단순히 요청을 취소하는 것이 아니라 TimeoutError를 이유로 요청을 중단
    // 이제 controller.signal.aborted는 true가 된다.
    controller.abort(timeoutError);

    // 실제 코드에서 AbortSignal.timeout(150_000)을 호출하면,
    // 150초짜리 타이머를 만드는 대신 위에서 중단시킨 신호를 돌려준다.
    const timeoutSpy = vi.spyOn(AbortSignal, "timeout")
        .mockReturnValue(controller.signal);

    // 실제 서버에는 요청하지 않는다.
    // fetch가 호출되면 시간 초과 오류로 실패하도록 준비한다.
    const fetchMock = vi.fn<typeof fetch>()
        .mockRejectedValue(timeoutError);

    vi.stubGlobal("fetch", fetchMock);

    const request = fetchStructuredTicketAdvice("프린터 연결 오류");

    await expect(request).rejects.toThrow(
        "AI 답변을 기다리는 시간이 초과되었습니다. 다시 요청해 주세요."
    );

    // 새 오류의 cause에 원래 오류가 그대로 보관됐는지 확인 error.cause === timeoutError
    await expect(request).rejects.toHaveProperty(
        "cause",
        timeoutError
    );

    // 실제 코드가 150초 제한을 설정했는지 확인
    expect(timeoutSpy).toHaveBeenCalledWith(150_000);

    // 준비한 신호를 fetch에도 전달했는지 확인
    expect(fetchMock).toHaveBeenCalledWith(
        "/api/ai/ticket-advice/structured",
        expect.objectContaining({
            signal: controller.signal,
        })
    );
});


test("시간 초과가 아닌 네트워크 오류는 그대로 전달한다", async () => {
    const networkError = new TypeError("Failed to fetch");

    // abort()를 호출하지 않았으므로 중단되지 않은 신호
    const controller = new AbortController();

    vi.spyOn(AbortSignal, "timeout")
        .mockReturnValue(controller.signal);

    // 네트워크 오류로 요청이 실패하는 상황
    const fetchMock = vi.fn<typeof fetch>()
        .mockRejectedValue(networkError);

    vi.stubGlobal("fetch", fetchMock);

    await expect(fetchStructuredTicketAdvice("프린터 연결 오류"))
        .rejects.toBe(networkError);
});