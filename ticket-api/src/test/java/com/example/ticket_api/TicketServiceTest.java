package com.example.ticket_api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TicketServiceTest extends DatabaseTestSupport {

    @Test
    void createsTicketWithTrimmedTitle() {
        // 준비
        TicketService service = new TicketService(repository);

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
        TicketService service = new TicketService(repository);

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
        TicketService service = new TicketService(repository);

        InvalidTicketTitleException exception = assertThrows(
            InvalidTicketTitleException.class,
            () -> service.create("   ")
        );

        assertEquals("티켓 제목을 입력해 주세요.", exception.getMessage());
        assertEquals(3, service.findAll().size());
    }

    @Test
    void rejectsMissingTicketWithoutChangingList() {
        TicketService service = new TicketService(repository);
        var before = service.findAll();

        TicketNotFoundException exception = assertThrows(
            TicketNotFoundException.class,
            () -> service.complete(999L)
        );

        assertEquals("티켓을 찾을 수 없습니다.", exception.getMessage());
        assertEquals(before, service.findAll());
    }

    @Test
    void acceptsTitleWith50Characters() {
        TicketService service = new TicketService(repository);
        String title = "가".repeat(50);

        Ticket created = service.create(title);

        assertEquals(title, created.title());
        assertTrue(service.findAll().contains(created));
    }

    @Test
    void rejectsTitleWith51Characters() {
        TicketService service = new TicketService(repository);
        String title = "가".repeat(51);

        InvalidTicketTitleException exception = assertThrows(
            InvalidTicketTitleException.class,
            () -> service.create(title)
        );

        assertEquals(
            "티켓 제목은 50자 이하여야 합니다.",
            exception.getMessage()
        );
        assertEquals(3, service.findAll().size());
    }

    @Test
    void acceptsTitleWith50CharactersAfterStripping() {
        TicketService service = new TicketService(repository);

        String expectedTitle = "가".repeat(50);
        String inputTitle = "  " + expectedTitle + "  ";

        Ticket created = service.create(inputTitle);

        assertEquals(expectedTitle, created.title());
        assertTrue(service.findAll().contains(created));
    }

    @Test
    void reopensClosedTicket() {
        TicketService service = new TicketService(repository);
        Ticket reopenTicket = service.reopen(2L);
        assertEquals(2L, reopenTicket.id());
        assertEquals("결제 내역 확인", reopenTicket.title());
        assertEquals("OPEN", reopenTicket.status());
        assertEquals(3, service.findAll().size());
        assertEquals(reopenTicket, service.findAll().get(1));
    }

    @Test
    void rejectsReopeningMissingTicket() {
        TicketService service = new TicketService(repository);
        var before = service.findAll();

        TicketNotFoundException exception = assertThrows(
            TicketNotFoundException.class,
            () -> service.reopen(999L)
        );

        assertEquals("티켓을 찾을 수 없습니다.", exception.getMessage());
        assertEquals(before, service.findAll());
    }

    @Test
    void reopeningOpenTicketKeepsItOpen() {
        TicketService service = new TicketService(repository);
        var before = service.findAll();
        Ticket reopenTicket = service.reopen(1L);

        assertEquals(1L, reopenTicket.id());
        assertEquals("OPEN", reopenTicket.status());
        assertEquals(before, service.findAll());
    }
}
