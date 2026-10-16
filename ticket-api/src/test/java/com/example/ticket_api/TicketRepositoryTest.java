package com.example.ticket_api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TicketRepositoryTest {
    
    @Test
    void findsExistingTicketById() {
        TicketRepository repository = new TicketRepository();
        // 1. ID가 2L인 티켓 조회
        var result = repository.findById(2L);
        
        // 2. 결과가 존재하는지 확인
        assertTrue(result.isPresent());//값이 있는지 확인
        
        Ticket ticket = result.orElseThrow();//result.orElseThrow() 담긴 티켓 꺼내기. 비어 있으면 예외 발생
        assertEquals(2L, ticket.id());
        assertEquals("결제 내역 확인", ticket.title()); 
    }

    @Test 
    void returnsEmptyWhenTicketDoesNotExist() {
        TicketRepository repository = new TicketRepository();
        var result = repository.findById(999L);
        
        // 결과가 비어 있는지 검증
        assertTrue(result.isEmpty());
    }

    @Test 
    void insertsTicketAndFindsItById() {
        TicketRepository repository = new TicketRepository();

        Ticket created = repository.insert("프린터 연결 오류", "OPEN");
        assertEquals(4L, created.id());
        assertEquals("프린터 연결 오류", created.title());
        assertEquals("OPEN", created.status());
        assertEquals(
            created,
            repository.findById(created.id()).orElseThrow()
        );//orElseThrow()는 티켓이 있으면 꺼내고, 없으면 예외를 던져 테스트를 실패
        assertEquals(4, repository.findAll().size());
    }

    @Test
    void assignsSequentialIdsToNewTickets() {
        TicketRepository repository = new TicketRepository();

        Ticket first = repository.insert("첫 번째 티켓", "OPEN");
        Ticket second = repository.insert("두 번째 티켓", "OPEN");

        assertEquals(4L, first.id());
        assertEquals(5L, second.id());
        
        assertEquals(5, repository.findAll().size());
    }

    @Test 
    void updatesExistingTicket() {
        TicketRepository repository = new TicketRepository();
        Ticket testTicket = new Ticket(2L, "결제 내역 확인", "OPEN");

        assertTrue(repository.update(testTicket));
        assertEquals(testTicket, repository.findById(2L).orElseThrow());
        assertEquals(3, repository.findAll().size());
        assertEquals("로그인 오류",repository.findById(1L).orElseThrow().title());
        
    }

    @Test
    void returnsFalseWhenUpdatingMissingTicket() {
        TicketRepository repository = new TicketRepository();

        // 1. 수정 전 전체 목록을 before 변수에 보관
        var before =repository.findAll();

        // 2. ID 999L, 제목 "없는 티켓", 상태 "OPEN"인 Ticket 생성
        Ticket missingTicket = new Ticket(999L, "없는 티켓", "OPEN");

        // 3. update() 결과가 false인지 확인
        assertFalse(repository.update(missingTicket));

        // 4. 수정 후 전체 목록이 before와 같은지 확인
        assertEquals(before, repository.findAll());

        // 5. findById(999L)의 결과가 비어 있는지 확인
        assertTrue(repository.findById(999L).isEmpty());
    }

}//class
