package com.example.ticket_api.ai;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.ticket_api.Ticket;
import com.example.ticket_api.TicketService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TicketToolsTest {

    @Test
    void returnsOnlyOpenTickets() {
        TicketService service = mock(TicketService.class);

        Ticket openTicket = new Ticket(36L, "화면 조회 속도 저하", "OPEN", 0L);
        Ticket closedTicket = new Ticket(37L, "프린터 연결 오류", "CLOSED", 1L);

        // 1. service.findAll()이 위 두 티켓을 반환하도록 설정
        when(service.findAll()).thenReturn(List.of(openTicket, closedTicket));

        TicketTools tools = new TicketTools(service);

        // 2. tools.listOpenTickets()를 호출해서 결과 저장
        List<Ticket> result = tools.listOpenTickets();

        // 3. 결과가 List.of(openTicket)과 같은지 검사
        assertEquals(List.of(openTicket), result);

        // 4. service.findAll()이 한 번 호출됐는지 검사
        verify(service).findAll();
    }

    @Test
    void returnsEmptyListWhenNoOpenTicketsExist() {

        // 1. 가짜 TicketService 생성
        TicketService service = mock(TicketService.class);

        // 2. findAll()이 CLOSED 티켓 한 개만 반환하도록 설정
        Ticket closedTicket = new Ticket(37L, "프린터 연결 오류", "CLOSED", 1L);
        when(service.findAll()).thenReturn(List.of(closedTicket));

        // 3. 실제 TicketTools의 listOpenTickets() 호출
        TicketTools tools = new TicketTools(service);
        List<Ticket> result = tools.listOpenTickets();

        // 4. 결과가 List.of()와 같은지 검사
        assertEquals(List.of(), result);

        // 5. findAll()이 한 번 호출됐는지 검사
        verify(service).findAll();
    }

    @Test
    void returnsAllTicketsRegardlessOfStatus() {
        // 1. 가짜 TicketService 생성
        TicketService service = mock(TicketService.class);

        // 2. OPEN 티켓과 CLOSED 티켓을 각각 하나 생성
        Ticket openTicket = new Ticket(36L, "화면 조회 속도 저하", "OPEN", 0L);
        Ticket closedTicket = new Ticket(37L, "프린터 연결 오류", "CLOSED", 1L);

        // 3. 두 티켓을 담은 목록을 expected 변수에 저장
        List<Ticket> expected = List.of(openTicket, closedTicket);

        //service.findAll()이 expected를 반환하도록 설정
        when(service.findAll()).thenReturn(expected);

        // 4. 실제 TicketTools의 listTickets() 호출
        TicketTools tools = new TicketTools(service);
        List<Ticket> result = tools.listTickets();

        // 5. 반환된 목록이 expected와 같은지 검사
        assertEquals(expected, result);

        // 6. service.findAll()이 한 번 호출됐는지 검사
        verify(service).findAll();

    }

}