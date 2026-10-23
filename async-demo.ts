import type {Ticket} from "./tickets.js"

function featchTickets(shouldFail = false): Promise<Ticket[]> {

    return new Promise<Ticket[]> ((resolve, reject) =>{

        setTimeout(()=>{
            if(shouldFail){
                reject(new Error("서버에서 티켓을 가져오지 못했습니다."))
                return;//성공 처리 코드로 내려가지 않게
            }

            resolve([
                {id: 1, title: "로그인 오류", status:"OPEN", version:0},
                {id: 2, title: "결제 내역 확인", status:"CLOSED", version:0}
            ]);

        },1000); //setTimeout


    }); //return new Promise
}//function


async function main() :Promise<void> {
    console.log("조회 시작");    //T = 0초

    try{
        const ticket = await featchTickets(true);
        console.log("조회 완료", ticket);

    }catch (error){
        if(error instanceof Error){
            console.error("조회 실패:", error.message);
            //throw error;
        }else{
            console.error("알 수 없는 오류", error);
        }
    }finally{
        console.log("조회 처리 종료");
    }

    //const pendingTickets = featchTickets(); //T = 0초 setTimeout(1초) 타이머를 백그라운드에 구동 시작하고, 안 기다리고 즉시 Promise 객체를 반환받음
    //console.log("2. 요청 후 다음 코드 실행"); //T = 0초
    //const tickets = await pendingTickets; //T = 0~1 await 여기서 main() 함수의 실행이 1초 동안 대기(Pause) 상태가 됨
    //T = 1초	resolve([...]) 호출	백그라운드 타이머 완료, Promise가 Fulfilled 됨
    //console.log("3. 조회 완료", tickets); //대기가 풀리면서 마지막 로그 출력

}


main().catch((error)=>{
    console.log("조회 실패", error);
    process.exitCode =1;
});

//console.log("4. main 바깥 코드 실행");