import type { Ticket } from "../../tickets";

type TicketItemProps = {
    ticket:Ticket
};


export default function TicketItem({ticket}: TicketItemProps){ //props는 부모 컴포넌트가 전달한 데이터를 담은 객체
    
    const statusLabel = ticket.status === "OPEN" ? "미처리" : "처리 완료";
 
    
    
    return (
        <li>
            {ticket.title} - {statusLabel}
        </li>
    );
}