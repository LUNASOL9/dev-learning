import { useState } from "react";
import { fetchTicketAdvice } from "./ticket-advice-api";

type TicketAdviceProps = {
    title : string;
};

export default function TicketAdvice({title}: TicketAdviceProps){
    const [advice, setAdvice] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");

    async function handleRequestAdvice() {
        if (isLoading) {
            return;
        }

        setIsLoading(true);
        setAdvice("");
        setErrorMessage("");

        try {
            const result = await fetchTicketAdvice(title);
            setAdvice(result);
        } catch (error: unknown) {
            setErrorMessage(
                error instanceof Error
                    ? error.message
                    : "AI 도움말을 가져오지 못했습니다."
            );
        } finally {
            setIsLoading(false);
        }
    }






    return (
        <section>
            <h2>AI 도움말</h2>
            <p>"{title}"에 대해 먼저 확인할 사항을 물어보세요.</p>
                <button type="button"
                        disabled={isLoading}
                        onClick={()=> void handleRequestAdvice()}
                >
                    {isLoading ? "AI 답변 생성 중…" : "AI 도움말 요청"}
                </button>

            {isLoading && (
                <p role="status">AI가 확인할 사항을 정리하고 있어요.</p>
            )}

            {errorMessage && (
                <p role="alert">{errorMessage}</p>
            )}

            {advice && (
                <p style={{ whiteSpace: "pre-wrap" }}>{advice}</p>
            )}

        </section>
    );
}