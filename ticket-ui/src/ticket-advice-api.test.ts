import { afterEach, expect, test, vi } from "vitest";
import { fetchStructuredTicketAdvice } from "./ticket-advice-api";
import type { TicketAdviceResult } from "./ticket-advice-api";

afterEach(() => {
    // 테스트에서 바꾼 전역 fetch를 원래대로 복구
    vi.unstubAllGlobals();
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