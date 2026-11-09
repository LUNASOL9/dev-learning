import { useState } from "react";
import { fetchStructuredTicketAdvice  } from "./ticket-advice-api";
import type { TicketAdviceResult } from "./ticket-advice-api";

type TicketAdviceProps = {
    title : string;
};

export default function TicketAdvice({title}: TicketAdviceProps){
    const [advice, setAdvice] = useState<TicketAdviceResult | null>(null); /* (null) 최초 시작값 | TicketAdviceResult | null*/
    const [isLoading, setIsLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");

    async function handleRequestAdvice() {
        if (isLoading) {
            return;
        }

        setIsLoading(true);
        setAdvice(null);
        setErrorMessage("");

        try {
            const result = await fetchStructuredTicketAdvice(title);
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
                <div>
                    <h3>분류: {advice.category}</h3>

                    <h3>먼저 확인할 사항</h3>
                    <ul>
                        {advice.checks.map((check, index)=>(
                           <li key= {index}>{check}</li>
                        ))}
                    </ul>
                </div>
            )}

        </section>
    );
}