package com.example.ticket_api;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=always",
    "spring.sql.init.data-locations=classpath:test-data.sql",
    "spring.sql.init.encoding=UTF-8"
})
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.ANY
)
@DirtiesContext(
    classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD
)
class TicketOptimisticLockTest {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void rejectsUpdateUsingStaleVersion() {
        EntityManager first = entityManagerFactory.createEntityManager();
        EntityManager second = entityManagerFactory.createEntityManager();

        try {
            first.getTransaction().begin();
            second.getTransaction().begin();

            // 두 작업이 변경 전의 같은 티켓을 조회
            TicketEntity firstTicket =
                first.find(TicketEntity.class, 1L);

            TicketEntity secondTicket =
                second.find(TicketEntity.class, 1L);

            assertNotNull(firstTicket);
            assertNotNull(secondTicket);

            Long originalVersion = firstTicket.getVersion();
            assertNotNull(originalVersion);
            assertEquals(originalVersion, secondTicket.getVersion());

            // A가 먼저 수정하고 커밋
            firstTicket.complete();
            first.getTransaction().commit();

            assertEquals(
                Long.valueOf(originalVersion + 1),
                firstTicket.getVersion()
            );

            // B는 여전히 오래된 버전을 가진 상태
            secondTicket.complete();

            assertThrows(
                OptimisticLockException.class,
                () -> second.flush()    // 여기서 UPDATE 실행 → “수정할 행이 있어야 하는데 없네. 조회 이후 다른 작업이 변경했거나 삭제했구나.”
            );

            second.getTransaction().rollback();

            // 새로운 영속성 컨텍스트로 최종 DB 상태 확인
            EntityManager verifier =
                entityManagerFactory.createEntityManager();

            try {
                TicketEntity actual =
                    verifier.find(TicketEntity.class, 1L);

                assertEquals("CLOSED", actual.getStatus());
                assertEquals(
                    Long.valueOf(originalVersion + 1),
                    actual.getVersion()
                );
            } finally {
                verifier.close();
            }
        } finally {
            if (first.getTransaction().isActive()) {
                first.getTransaction().rollback();
            }
            if (second.getTransaction().isActive()) {
                second.getTransaction().rollback();
            }

            first.close();
            second.close();
        }
    }
}