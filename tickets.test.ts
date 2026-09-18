import assert = require("node:assert/strict");

import { findOpenTickets, findOpenTicketTitles } from "./tickets";
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


console.log("티켓 검색 테스트 통과");