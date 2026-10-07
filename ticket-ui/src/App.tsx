import { useEffect ,useState } from "react";

import type {Ticket} from "../../tickets";
import { filterTickets } from "../../tickets";
import TicketItem   from "./TicketItem";
import TicketForm from "./TicketForm";
import { fetchTickets, createTicket, completeTicket } from "./ticket-api";

import {Routes, Route, Link} from "react-router";
import TicketDetail from "./TicketDetail";

//const tickets : Ticket[] = [
/*
const initialTickets : Ticket[] = [
  { id: 1, title: "로그인 오류", status: "OPEN" },
  { id: 2, title: "결제 내역 확인", status: "CLOSED" },
  { id: 3, title: "화면 속도 저하", status: "OPEN" },
];
*/


function App(){

  const [completingId, setCompletingId] = useState<number | null>(null);
  const [completeError, setCompleteError] = useState("");

  const[isSaving, setIsSaving] = useState(false);
  const [formKey, setFormKey] = useState(0); // 티켓 등록

  const [searchText, setSearchText] = useState(""); //검색
  const [showCompleted, setShowCompleted] = useState(false);  //완료된 티켓도 보기
  const [tickets, setTickets] = useState<Ticket[]>([]);

  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  const isBusy = isLoading || isSaving || completingId != null;//조회·등록·완료 요청 중 하나라도 진행 중인지
  const filteredTickets = filterTickets(tickets, searchText, showCompleted);

  useEffect(()=>{
    const controller = new AbortController();
    const signal = controller.signal;

    async function loadInitialTickets() {
      try {
        const loadedTickets = await fetchTickets(signal);
        
        if(!signal.aborted){
          setTickets(loadedTickets);
        }

      } catch (error : unknown){

        if(!signal.aborted){
          setErrorMessage(
           error instanceof Error ? error.message : "알 수 없는 오류가 발생했습니다."
          );
        }

      } finally {
          if(!signal.aborted){
            setIsLoading(false);
          }
      }

      
    }//loadInitialTickets
    
    void loadInitialTickets();
    return () => {
        controller.abort();
      };
  },[]);




  async function handleCreate(title:string){ //새 티켓

    setIsSaving(true);

    try{
      const createdTicket = await createTicket(title);

      setTickets(previousTickets => [
        createdTicket,
        ...previousTickets,
      ]);

      setSearchText("");


    }finally{
      setIsSaving(false);
    }

    /*
    setTickets(previousTickets =>{
      const nextId = previousTickets.reduce((maxId, ticket)=> Math.max(maxId, ticket.id), 0)+1;

      const newTicket: Ticket = {
        id: nextId,
        title,
        status: "OPEN",
      }
      return [newTicket, ...previousTickets]
    });
    */

    setSearchText("");
  }//handleCreate




  async function handleLoad() { //서버에서 불러오기
    setIsLoading(true);
    setErrorMessage("");
    setCompleteError("");

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
    setShowCompleted(false);
    setCompleteError("");
   
    setFormKey(previousKey => previousKey + 1);
}
  
async function handleComplete(ticketId:number) :Promise<void>{ // 처리 완료

  if(isBusy){
    return;
  }

    setCompletingId(ticketId);
    setCompleteError("");

    try{
      const completedTicket = await completeTicket(ticketId);

      setTickets( previousTickets =>
        previousTickets.map(ticket =>
          ticket.id === completedTicket.id
        ? completedTicket
        : ticket
        )
      );


    }catch(error:unknown){
      setCompleteError(
        error instanceof Error ? error.message : "티켓 완료 처리 중 오류가 발생했습니다."
      );
    }finally{
      setCompletingId(null);
    }


  /*
  setTickets(previousTickets =>
    
    previousTickets.map(ticket => 
      ticket.id === ticketId ? { ...ticket, status: "CLOSED"} 
      : ticket
    )
  );
  */
}// handleComplete


  return (
    <Routes>
      <Route
        path="/"
        element={
          <main>
            <h1>업무 티켓 관리</h1>
            
            <TicketForm
              key={formKey}
              disabled={isBusy}
              onCreate={handleCreate}
            />
            {isSaving && <p role="status">티켓을 등록하는 중입니다.</p>}

            <button type="button" onClick={handleLoad} disabled={isBusy}>
                {isLoading ? "불러오는 중..." : "서버에서 불러오기"}
            </button>

            {errorMessage && <p role="alert">{errorMessage}</p>}
            {completeError && <p role="alert">{completeError}</p>}

            <button type="button" onClick={handleReset} disabled={isBusy}>
              입력 및 검색 초기화
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
          
              { isLoading ? (
                <p role="status">티켓을 불러오는 중입니다.</p>              
              ): filteredTickets.length === 0 ? (              
                <p> 검색 조건에 맞는 티켓이 없습니다.</p>              
              ) : (
                <ul>
                  {filteredTickets.map(ticket=>(
                    <TicketItem key={ticket.id} 
                                ticket={ticket} 
                                onComplete={handleComplete}
                                disabled={isBusy}
                                isCompleting={completingId === ticket.id}
                    />
                  ))}
                </ul>              
              )}
          </main>
      }
      />


      <Route
        path="/tickets/:ticketId"
        element={<TicketDetail
                tickets={tickets}
                isLoading={isLoading}
                errorMessage={errorMessage}
        />}
      />


      <Route
        path="*"
        element={
          <main>
            <h1>페이지를 찾을 수 없습니다.</h1>
            <Link to ="/">목록으로</Link>
          </main>
        }
      />



    </Routes>
  );
};

export default App;