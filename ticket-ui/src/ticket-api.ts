import type { Ticket } from "../../tickets";


function isTicket(value : unknown) : value is Ticket{
    
    return (
        typeof value === "object" &&
        value !== null &&

        "id" in value &&
        typeof value.id === "number" &&
        
        "title" in value &&
        typeof value.title === "string" &&
        
        "status" in value &&
        (value.status === "OPEN" || value.status === "CLOSED")
    );
}


export async function fetchTickets(
    signal?: AbortSignal
): Promise<Ticket[]> {
    
     const response = await fetch(
         "/api/tickets",
         {signal} 
    );

    if(!response.ok){
        throw new Error(`HTTP 요청 실패: ${response.status}`);
    }

    const data : unknown = await response.json();
    
    if(!Array.isArray(data)){
        throw new Error("응답이 배열이 아닙니다.");
    }

    return data.map((item : unknown): Ticket => {

        if(!isTicket(item)){
             throw new Error("티켓 데이터 형식이 올바르지 않습니다.");
        }

        return item;

    });
}//function


export async function createTicket(title:string): Promise<Ticket>{
    const response = await fetch("/api/tickets",{
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({ title }),    //{ title }은 { title: title }의 축약 표현
    });

    if(!response.ok){
        throw new Error(`티켓 등록 실패: ${response.status}`);
    }

    const data :unknown = await response.json();

    if(!isTicket(data)){
        throw new Error("등록 응답의 티켓 형식이 올바르지 않습니다.")
    }

    return data;
}//function

export async function completeTicket(ticketId:number) :Promise<Ticket> {

    const response = await fetch(
        `/api/tickets/${ticketId}/complete`,
        {
            method: "PATCH",
        }
    );

    if(!response.ok){
        throw new Error(`티켓 완료 처리 실패: ${response.status}`)
    }

    const data:unknown = await response.json();

    if(!isTicket(data)){
        throw new Error("완료 응답의 티켓 형식이 올바르지 않습니다.");
    }

    return data;
}//function