import { useState } from "react";

import type {Ticket} from "../../tickets";
//import { findOpenTickets } from "../../tickets";
import TicketItem   from "./TicketItem";

//const tickets : Ticket[] = [
const initialTickets : Ticket[] = [
  { id: 1, title: "로그인 오류", status: "OPEN" },
  { id: 2, title: "결제 내역 확인", status: "CLOSED" },
  { id: 3, title: "화면 속도 저하", status: "OPEN" },
];


function filterTickets(
    tickets: Ticket[], 
    searchText: string | null | undefined,
    showCompleted : boolean
): Ticket[] {
    const normalizedText  = (searchText ?? "").trim(); //(searchText ?? "") null과 undefined를 빈 검색어로 처리한다
    
    const filteredTickets  = tickets.filter(ticket=>{
        const matchesTitle = ticket.title.includes(normalizedText);
        const matchesStatus = showCompleted || ticket.status === "OPEN"
        
        return matchesTitle && matchesStatus;

    });
    return filteredTickets
    
}



function App(){
  
  const [searchText, setSearchText] = useState("");
  const [showCompleted, setShowCompleted] = useState(false);  
  const [tickets, setTickets] = useState<Ticket[]>(initialTickets)

  const filteredTickets = filterTickets(tickets, searchText, showCompleted);


function handleReset(){ //초기 상태로 복원
    setSearchText("");
    setTickets(initialTickets);
    setShowCompleted(false);
}
  
function handleComplete(ticketId:number){ // 처리 완료

  setTickets(previousTickets =>
    
    previousTickets.map(ticket => 
      ticket.id === ticketId ? { ...ticket, status: "CLOSED"} 
      : ticket
    )

  );
}


  return (
    <main>
      <h1>업무 티켓 관리</h1>
      <button type="button" onClick={handleReset}>
        초기 상태로 복원
      </button>
        <label htmlFor="ticket-search">티켓 검색</label>
        <input
          id="ticket-search"
          value = {searchText}
          onChange={event => setSearchText(event.target.value)}
          placeholder="제목을 입력하세요"
        />

        <label>
          <input type="checkbox" 
                 checked={showCompleted}
                 onChange={event=>setShowCompleted(event.target.checked)}
          /> 완료된 티켓도 보기
        </label>
      
      <p>검색 결과 {filteredTickets.length}건</p>
    
        {filteredTickets.length === 0 ? (
          <p> 검색 조건에 맞는 티켓이 없습니다.</p>
        ) : (
          <ul>
            {filteredTickets.map(ticket=>(
              <TicketItem key={ticket.id} 
                          ticket={ticket} 
                          onComplete={handleComplete}
              />
            ))}
          </ul>
        
        )}

    </main>
  );
};

export default App;