import { Ticket } from "./tickets";//import가 있으므로 export {}는 제거해도 됩니다. 이미 이 파일은 모듈입니다.
import { findOpenTickets } from "./tickets";

//export {} //는 이 파일을 모듈로 만들어, 다른 실습 파일의 main과 이름이 충돌하지 않게 합니다.


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

async function main() : Promise<void> {

    console.log("조회 시작");

    const response = await fetch(
        "https://jsonplaceholder.typicode.com/todos/1"
    );

    console.log("HTTP 상태:", response.status);
    console.log("성공 여부", response.ok);
    
    if(!response.ok){
        throw new Error(`HTTP 요청 실패: ${response.status}`)
    }

    const data : unknown = await response.json(); //응답 본문을 읽고 JSON으로 해석할 때까지
    /*
        const data : unknown = {
            id: 1,
            title: "잘못된 응답 실습",
            completed: "false", //"false"는 문자열이므로 정상적인 boolean 값이 아닙니다.
        }
    */
    if(!isTodoResponse(data)){
        throw new Error("응답 데이터 형식이 올바르지 않습니다");
    }

    const ticket: Ticket = {
        id: data.id,
        title: data.title,
        status: data.completed ? "CLOSED" : "OPEN",
        version:0
    }

    const openTickets = findOpenTickets([ticket] ,"");

    console.log("미처리 티켓:",openTickets);
    //console.log("변환한 티켓", ticket);
    //console.log("제목:", data.title);
    //console.log("완료 여부:", data.completed);
    //console.log("응답 데이터:", data);
}

main().catch((error: unknown) => {

  if (error instanceof Error) {
    console.error("조회 실패:", error.message);
  } else {
    console.error("알 수 없는 오류:", error);
  }

  process.exitCode = 1;
});
