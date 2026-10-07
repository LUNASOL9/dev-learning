import type { Ticket } from "../../tickets";
import {Link} from "react-router";

type TicketItemProps = {
    ticket:Ticket;
    onComplete: (TicketId: number) => void;
    disabled: boolean;
    isCompleting: boolean;
};

//-export default function TicketItem({ticket}: TicketItemProps){ //props {ticket}는  부모 컴포넌트가 전달한 데이터를 담은 객체
export default function TicketItem({ticket, onComplete,  disabled,  isCompleting,}: TicketItemProps){
    
    const statusLabel = ticket.status === "OPEN" ? "미처리" : "처리 완료";
    
    return (
        <li>
            <Link to={`/tickets/${ticket.id}`}>
                {ticket.title}
            </Link> 
            {" - "} 
            {statusLabel}

            {ticket.status === "OPEN" && (
            
            <button type="button" 
                    onClick={()=> void onComplete(ticket.id)}
                    disabled={disabled}
            >
                  {isCompleting ? "처리 중..." : "처리 완료"}
            </button>

            )}
            
        
        
        </li>
    );
}