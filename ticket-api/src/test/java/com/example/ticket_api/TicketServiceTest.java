package com.example.ticket_api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class TicketServiceTest {

    @Test
    void createsTicketWithTrimmedTitle() {
        // 준비
        TicketService service = new TicketService();

        // 실행
        Ticket created = service.create("  프린터 연결 오류  ");

        // 검증
        assertEquals(4L, created.id());
        assertEquals("프린터 연결 오류", created.title());
        assertEquals("OPEN", created.status());

        assertEquals(4, service.findAll().size());
        assertTrue(service.findAll().contains(created));
    }

    @Test
    void completesTicket() {
        // 준비
        TicketService service = new TicketService();

        // 실행
        Ticket completed = service.complete(1L);

        // 검증
        assertEquals(1L, completed.id());
        assertEquals("로그인 오류", completed.title());
        assertEquals("CLOSED", completed.status());

        assertEquals(completed, service.findAll().get(0));
        assertEquals("OPEN", service.findAll().get(2).status());
    } 

    @Test
    void rejectsBlankTitleWithoutAddingTicket() {
        TicketService service = new TicketService();

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.create("   ")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(3, service.findAll().size());
    }

    @Test
    void rejectsMissingTicketWithoutChangingList() {
        TicketService service = new TicketService();
        var before = service.findAll();

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.complete(999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(before, service.findAll());
    }
    
}
