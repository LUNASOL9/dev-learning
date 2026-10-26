import type { Ticket } from "../../tickets";
import {Link} from "react-router";

type TicketItemProps = {
    ticket:Ticket;
    onComplete: (TicketId: number) => Promise<void>;
    onReopen: (Ticket:number) => Promise<void>;
    disabled: boolean;
    isCompleting: boolean;
    isReopening : boolean;
};

//-export default function TicketItem({ticket}: TicketItemProps){ //props {ticket}는  부모 컴포넌트가 전달한 데이터를 담은 객체
export default function TicketItem({ticket, onComplete, onReopen , disabled,  isCompleting, isReopening}: TicketItemProps){
    
    const statusLabel = ticket.status === "OPEN" ? "미처리" : "처리 완료";
    
    return (
        <li>
            <Link to={`/tickets/${ticket.id}`}>
                {ticket.title} {ticket.version}
            </Link> 
            {" - "} 
            {statusLabel}

            {ticket.status === "OPEN" ? 
            (            
                <button type="button" 
                        onClick={()=> void onComplete(ticket.id)}
                        disabled={disabled}
                >
                    {isCompleting ? "처리 중..." : "처리 완료"}
                </button>

            ) : (
                <button type="button" 
                    onClick={()=> void onReopen(ticket.id)}
                    disabled={disabled}
                >
                  {isReopening ? "처리 중..." : "다시 열기"}
                </button>) 
            }
            
        
        
        </li>
    );
}