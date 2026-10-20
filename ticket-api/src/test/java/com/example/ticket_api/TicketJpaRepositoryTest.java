package com.example.ticket_api;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=always"
})

//테스트용 데이터베이스를 설정
@AutoConfigureTestDatabase(
    //테스트용 데이터베이스로 대체
    replace = AutoConfigureTestDatabase.Replace.ANY
)
class TicketJpaRepositoryTest {

    @Autowired
    private TicketJpaRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAndFindsTicket() {
        //티켓 객체 생성
        TicketEntity ticket = new TicketEntity("JPA 저장 확인", "OPEN");
        assertNull(ticket.getId());

        //데이터베이스에 저장
        TicketEntity saved = repository.saveAndFlush(ticket);
        
        Long savedId = saved.getId();
        assertNotNull(savedId);

        //영속성 컨텍스트 초기화
        entityManager.clear();

        TicketEntity found = repository.findById(savedId).orElseThrow();

        assertEquals(savedId, found.getId());
        assertEquals("JPA 저장 확인", found.getTitle());
        assertEquals("OPEN", found.getStatus());        
    }

    @Test/*변경 감지 Test*/
    void updatesStatusThroughDirtyChecking() {
        TicketEntity saved = repository.saveAndFlush(
            new TicketEntity("변경 감지 확인", "OPEN")
        );
        Long savedId = saved.getId();

        entityManager.clear();

        TicketEntity found = repository.findById(savedId)
            .orElseThrow();

        found.complete();
        entityManager.flush();  //변경 감지 여기서 Hibernate가 변경을 감지하고 UPDATE SQL을 실행.
        entityManager.clear();

        TicketEntity reloaded = repository.findById(savedId)
            .orElseThrow();
        assertEquals("CLOSED", reloaded.getStatus());
    }

    @Test
    void doesNotUpdateDetachedEntity() {
        TicketEntity saved = repository.saveAndFlush(
            new TicketEntity("분리된 엔티티 확인", "OPEN")
        );
        Long savedId = saved.getId();
        entityManager.clear(); // saved를 관리 대상에서 분리
        
        saved.complete(); // 분리된 객체의 상태 변경
        entityManager.flush();
        entityManager.clear();

        TicketEntity reloaded = repository.findById(savedId).orElseThrow();        
        assertEquals("CLOSED", saved.getStatus());
        assertEquals("OPEN", reloaded.getStatus());
        
    }







}//class