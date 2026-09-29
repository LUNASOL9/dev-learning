import type { Ticket } from "../../tickets";

type TodoResponse = {
    id: number,
    title: string;
    completed: boolean;
}

function isTodoResponse(value : unknown) : value is TodoResponse{
    
    return (
        typeof value === "object" &&
        value !== null &&

        "id" in value &&
        typeof value.id === "number" &&
        
        "title" in value &&
        typeof value.title === "string" &&
        
        "completed" in value &&
        typeof value.completed === "boolean"
    );
}


export async function fetchTickets(): Promise<Ticket[]> {
    
     const response = await fetch(
        "https://jsonplaceholder.typicode.com/todos?userId=1"
    );

    if(!response.ok){
        throw new Error(`HTTP 요청 실패: ${response.status}`);
    }

    const data : unknown = await response.json();
    
    if(!Array.isArray(data)){
        throw new Error("응답이 배열이 아닙니다.");
    }

    return data.map((item : unknown): Ticket => {

        if(!isTodoResponse(item)){
             throw new Error("티켓 데이터 형식이 올바르지 않습니다.");
        }

        return {
            id: item.id,
            title: item.title,
            status: item.completed ? "CLOSED" : "OPEN",
        };

    });
}//function