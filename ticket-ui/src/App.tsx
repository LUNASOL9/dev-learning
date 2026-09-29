import { useState } from "react";

import type {Ticket} from "../../tickets";
import { filterTickets } from "../../tickets";
import TicketItem   from "./TicketItem";

import { fetchTickets } from "./ticket-api";

//const tickets : Ticket[] = [
const initialTickets : Ticket[] = [
  { id: 1, title: "로그인 오류", status: "OPEN" },
  { id: 2, title: "결제 내역 확인", status: "CLOSED" },
  { id: 3, title: "화면 속도 저하", status: "OPEN" },
];





function App(){
  
  const [searchText, setSearchText] = useState(""); //검색
  const [showCompleted, setShowCompleted] = useState(false);  //완료된 티켓도 보기
  const [tickets, setTickets] = useState<Ticket[]>(initialTickets);

  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const filteredTickets = filterTickets(tickets, searchText, showCompleted);

  async function handleLoad() { //서버에서 불러오기
    setIsLoading(true);
    setErrorMessage("");

    try{
      const loadedTickets = await fetchTickets();
      setTickets(loadedTickets)

    }catch(error : unknown){
      setErrorMessage(
        error instanceof Error ? error.message : "알 수 없는 오류가 발생했습니다."
      );


    }finally{
      setIsLoading(false);
    }
  }


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

      <button type="button" onClick={handleLoad} disabled={isLoading}>
          {isLoading ? "불러오는 중..." : "서버에서 불러오기"}
      </button>
      {errorMessage && <p role="alert">{errorMessage}</p>}

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