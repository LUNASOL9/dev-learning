import assert = require("node:assert/strict");

import { findOpenTickets, findOpenTicketTitles, filterTickets, } from "./tickets";
import type { Ticket } from "./tickets";


const tickets: Ticket[] = [
    {id: 1, title: "로그인 오류", status: "OPEN"},
    {id: 2, title: "결제 내역 확인", status: "CLOSED"},
    {id: 3, title: "화면 속도 저하", status: "OPEN"},
    {id: 4, title: "로그인 오류 해결 완료", status: "CLOSED"},
];



assert.deepEqual(
    findOpenTickets(tickets, "로그인").map(ticket => ticket.id), [1]
);

assert.deepEqual(
    findOpenTickets(tickets, " 로그인 ").map(ticket => ticket.id), [1]
);

assert.deepEqual(
    findOpenTickets(tickets, " ").map(ticket => ticket.id), [1, 3]
);

assert.deepEqual(
    findOpenTickets([], "로그인"), []
);

assert.deepEqual(
    findOpenTickets(tickets, null).map(ticket => ticket.id), [1, 3]
);

assert.deepEqual(
    findOpenTickets(tickets, undefined).map(ticket => ticket.id), [1, 3]
);


assert.deepEqual(
    findOpenTicketTitles(tickets, "로그인"), ["로그인 오류"]
);


assert.deepEqual(
    findOpenTicketTitles(tickets, "없는검색어"), []
);

assert.deepEqual(
    filterTickets(tickets, "", false).map(ticket => ticket.id), [1, 3],   "체크 해제 시 OPEN만 반환"
);

assert.deepEqual(
    filterTickets(tickets,"",true).map(ticket=> ticket.id), [1,2,3,4],   "체크 선택 시 모든 상태 반환"
)

assert.deepEqual(
    filterTickets(tickets," 로그인 ",true).map(ticket=> ticket.id), [1,4],   "앞뒤 공백을 제거하고 완료 티켓도 검색"
)

assert.deepEqual(
    filterTickets(tickets,"결제",false).map(ticket=> ticket.id), [],     "체크 해제 시 제목이 일치해도 CLOSED는 제외"
)

console.log("티켓 검색 테스트 통과");