import { Link, useParams } from "react-router";
import type { Ticket } from "../../tickets";

type TicketDetailProps  = {
    tickets: Ticket[];
    isLoading: boolean;
    errorMessage: string;
}

export default function TicketDetail({
    tickets, isLoading, errorMessage,
}:TicketDetailProps){
    const { ticketId } = useParams();
    const id = Number(ticketId);

    if(!Number.isSafeInteger(id) || id <= 0){
        return (
            <main>
                <p>올바르지 않은 티켓 번호입니다.</p>
                <Link to="/"> 목록 으로 </Link>
            </main>
        );
    }//if

    if (isLoading) {
        return <p role="status">티켓을 불러오는 중입니다.</p>;
    }

    if (errorMessage) {
        return (
        <main>
            <p role="alert">{errorMessage}</p>
            <Link to="/">목록으로 돌아가 재시도</Link>
        </main>
        );
    }


    const ticket = tickets.find(ticket => ticket.id === id);

    if(!ticket){
        return (
        <main>
            <p>현재 목록에서 티켓을 찾을 수 없습니다.</p>
            <Link to="/">목록으로</Link>
        </main>
      );
    }


    return(
        <main>
            <h1>{ticket.title}</h1>
            <p>번호 :{ticket.id }</p>
            <p>
                상태{ticket.status ==="OPEN" ? "미처리": "처리 완료"}
            </p>

            <Link to="/">목록으로</Link>
        </main>
    )
}//TicketDetail