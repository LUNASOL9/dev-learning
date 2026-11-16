import { useState, useEffect } from "react";
import { fetchStructuredTicketAdvice  } from "./ticket-advice-api";
import type { TicketAdviceResult } from "./ticket-advice-api";

type TicketAdviceProps = {
    title : string;
};

export default function TicketAdvice({title}: TicketAdviceProps){
    const [advice, setAdvice] = useState<TicketAdviceResult | null>(null); /* (null) 최초 시작값 | TicketAdviceResult | null*/
    const [isLoading, setIsLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");
    const [isWaitingLong, setIsWaitingLong] = useState(false);

    //30초 전에 요청이 끝나면 정리 함수가 타이머 취소
    //다른 화면으로 이동해 컴포넌트가 사라질 때도 타이머 취소
    useEffect(()=>{
        if(!isLoading){
            return;
        }

        const timerId = window.setTimeout(()=>{
            setIsWaitingLong(true);
        }, 30_000);//30초

        return () =>{
            window.clearTimeout(timerId);
        }

    },[isLoading]); //isLoading이 true가 되면 30초 타이머 시작


    async function handleRequestAdvice() {
        if (isLoading) {
            return;
        }

        setIsWaitingLong(false);
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
                <p role="status">
                    {isWaitingLong? "응답을 기다린 지 30초가 지났어요. 조금 더 기다려 주세요." : "AI가 확인할 사항을 정리하고 있어요."}</p>
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