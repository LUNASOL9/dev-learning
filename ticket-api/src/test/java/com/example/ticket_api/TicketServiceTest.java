package com.example.ticket_api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

//Spring이 JPA 환경 준비
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import org.junit.jupiter.api.Test;

/*JPA hibernate----- */
@DataJpaTest(properties = {//엔티티·JPA Repository·테스트 트랜잭션 준비
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=always",
    "spring.sql.init.data-locations=classpath:test-data.sql",
    "spring.sql.init.encoding=UTF-8"
})

@AutoConfigureTestDatabase(//실제 파일 DB 대신 테스트용 메모리 H2 DB 사용
    replace = AutoConfigureTestDatabase.Replace.ANY
)

@Import(TicketService.class)//Service도 테스트 환경에 등록

@DirtiesContext(//테스트마다 Spring 환경을 다시 만들어 초기 상태로 시작
    classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD
)
/*-----JPA hibernate */




public class TicketServiceTest /* JDBC -> JPA hibernate 주석 
                                extends DatabaseTestSupport*/ {
    @Autowired
    private TicketService service;

    @Test
    void createsTicketWithTrimmedTitle() {
        // 준비
        //TicketService service = new TicketService(repository);

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
        //TicketService service = new TicketService(repository);

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
        //TicketService service = new TicketService(repository);

        InvalidTicketTitleException exception = assertThrows(
            InvalidTicketTitleException.class,
            () -> service.create("   ")
        );

        assertEquals("티켓 제목을 입력해 주세요.", exception.getMessage());
        assertEquals(3, service.findAll().size());
    }

    @Test
    void rejectsMissingTicketWithoutChangingList() {
        //TicketService service = new TicketService(repository);
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
        //TicketService service = new TicketService(repository);
        String title = "가".repeat(50);

        Ticket created = service.create(title);

        assertEquals(title, created.title());
        assertTrue(service.findAll().contains(created));
    }

    @Test
    void rejectsTitleWith51Characters() {
        //TicketService service = new TicketService(repository);
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
        //TicketService service = new TicketService(repository);

        String expectedTitle = "가".repeat(50);
        String inputTitle = "  " + expectedTitle + "  ";

        Ticket created = service.create(inputTitle);

        assertEquals(expectedTitle, created.title());
        assertTrue(service.findAll().contains(created));
    }

    @Test
    void reopensClosedTicket() {
        //TicketService service = new TicketService(repository);
        Ticket reopenTicket = service.reopen(2L);
        assertEquals(2L, reopenTicket.id());
        assertEquals("결제 내역 확인", reopenTicket.title());
        assertEquals("OPEN", reopenTicket.status());
        assertEquals(3, service.findAll().size());
        assertEquals(reopenTicket, service.findAll().get(1));
    }

    @Test
    void rejectsReopeningMissingTicket() {
        //TicketService service = new TicketService(repository);
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
        //TicketService service = new TicketService(repository);
        var before = service.findAll();
        Ticket reopenTicket = service.reopen(1L);

        assertEquals(1L, reopenTicket.id());
        assertEquals("OPEN", reopenTicket.status());
        assertEquals(before, service.findAll());
    }
}
