import { readErrorMessage } from "./api-error";

export type TicketAdviceResult = {
    category: string;
    checks: string[];
};

export async function fetchStructuredTicketAdvice(
    title: string
): Promise<TicketAdviceResult> {
    const timeoutSignal = AbortSignal.timeout(150_000); //150초
    try{
        const response = await fetch("/api/ai/ticket-advice/structured", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ title }),
            signal: timeoutSignal,
        });

        if (!response.ok) {
            //throw new Error(`AI 도움말 요청 실패: ${response.status}`);
            const x = await readErrorMessage(response, `AI 도움말 요청 실패: ${response.status}`);
            throw new Error(x);
        }

        const data: unknown = await response.json();

        if (
            typeof data !== "object" ||
            data === null ||
            !("category" in data) ||
            typeof data.category !== "string" ||
            data.category.trim() === "" ||
            !("checks" in data) ||
            !Array.isArray(data.checks) ||  /*  checks가 실제 배열인지 검사해. 배열임을 확인 */
            data.checks.length !== 2 ||
            !data.checks.every(             /*  “모든 항목이 문자열이고, 공백뿐인 값도 아닌가?” */
                (check: unknown) =>
                    typeof check === "string" && check.trim() !== ""
            )
        ) {
            throw new Error("AI 도움말 응답 형식이 올바르지 않습니다.");
        }

        return {
            category: data.category,
            checks: data.checks,
        };
    } catch (error: unknown) {
        if (timeoutSignal.aborted) {
            throw new Error(
                "AI 답변을 기다리는 시간이 초과되었습니다. 다시 요청해 주세요.",
                {cause : error}
            );
        }

        throw error;
    }
}