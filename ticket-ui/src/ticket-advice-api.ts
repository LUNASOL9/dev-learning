export async function fetchTicketAdvice(title: string): Promise<string> {
    const response = await fetch("/api/ai/ticket-advice", {
           method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({title}),
    });

    if (!response.ok) {
        throw new Error(`AI 도움말 요청 실패: ${response.status}`);
    }

    const data: unknown = await response.json();

    if(typeof data != "object" ||
        data === null ||
        !("advice" in data) ||
        typeof data.advice != "string" ||
        data.advice.trim() === ""
    ){
        throw new Error("AI 도움말 응답 형식이 올바르지 않습니다.")
    }


    // 응답 검증과 반환은 다음 단계에서 함께 작성하자.
    return data.advice;
}