import { useState } from "react";

import type {Ticket} from "../../tickets";
import { findOpenTickets } from "../../tickets";
import TicketItem   from "./TicketItem";


const tickets : Ticket[] = [
  { id: 1, title: "로그인 오류", status: "OPEN" },
  { id: 2, title: "결제 내역 확인", status: "CLOSED" },
  { id: 3, title: "화면 속도 저하", status: "OPEN" },
];


function App(){

const [searchText, setSearchText] = useState("");
const filteredTickets = findOpenTickets(tickets, searchText);

  return (
    <main>
      <h1>업무 티켓 관리</h1>
        <label htmlFor="ticket-search">미처리 티켓 검색</label>
        <input
          id="ticket-search"
          value = {searchText}
          onChange={event => setSearchText(event.target.value)}
          placeholder="제목을 입력하세요"
        />
      
      <p>검색 결과 {filteredTickets.length}건</p>
    
      <ul>
        {filteredTickets.length === 0 ? (<p> 검색 조건에 맞는 미처리 티켓이 없습니다.</p>
        ) : (
          filteredTickets.map(ticket=>(
            <TicketItem key={ticket.id} ticket={ticket}/>           
          /*
            <li key={ticket.id}>
              {ticket.title} - {ticket.status}
            </li>
          */
        )))}
      </ul>

    </main>
  );
};

export default App;