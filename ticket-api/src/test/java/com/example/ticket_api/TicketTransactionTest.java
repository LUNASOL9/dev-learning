package com.example.ticket_api;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=always",
    "spring.sql.init.data-locations=classpath:test-data.sql",
    "spring.sql.init.encoding=UTF-8"
})
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.ANY
)
class TicketTransactionTest {

    @Autowired
    private TicketService service;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private TicketJpaRepository repository;

    @Test
    void rollsBackInsertWhenCreationFails() {
        
        Long before = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM tickets",
            Long.class
        );

        //규칙(매뉴얼)을 미리 등록(Stubbing)
        doAnswer(invocation -> {//  Mockito.doAnswer()로 Repository의 동작을 가로채기
            // 1. 파라미터로 넘어온 TicketEntity를 꺼냄
            TicketEntity ticket = invocation.getArgument(0);
            
            // 2. 인터페이스 대신 EntityManager로 실제 DB에 저장 요청
            entityManager.persist(ticket); 
            
            // 3. 변경 사항을 DB에 즉시 반영
            entityManager.flush();         

            // 4. 의도한 예외 발생
            throw new IllegalStateException("저장 직후 강제 실패");
        }).when(repository).save(any(TicketEntity.class)); //repository.save()가 호출되면, 내가 지정한 동작을 대신 실행해라.

        //실제로 서비스 메서드를 호출
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> service.create("롤백 확인")
        );

        assertEquals("저장 직후 강제 실패", exception.getMessage());

        Long after = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM tickets",
            Long.class
        );

        assertEquals(before, after);
    }
}//class




/*              JDBC -> JPA hibernate 주석
    package com.example.ticket_api;

    import static org.junit.jupiter.api.Assertions.assertEquals;
    import static org.junit.jupiter.api.Assertions.assertThrows;

    import javax.sql.DataSource;

    import org.junit.jupiter.api.Test;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.context.TestConfiguration;
    import org.springframework.context.annotation.Bean;
    import org.springframework.jdbc.core.JdbcTemplate;
    import org.springframework.jdbc.datasource.DataSourceTransactionManager;
    import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
    import org.springframework.transaction.PlatformTransactionManager;
    import org.springframework.transaction.annotation.EnableTransactionManagement;

    @SpringJUnitConfig(classes = {
        TicketService.class,
        TestDatabaseConfig.class,
        TicketTransactionTest.TransactionConfig.class
    })
    class TicketTransactionTest {

        @Autowired
        private TicketService service;

        @Autowired
        private JdbcTemplate jdbcTemplate;

        @Test
        void rollsBackInsertWhenCreationFails() {
            Long before = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tickets",
                Long.class
            );

            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.create("롤백 확인")
            );

            assertEquals("저장 직후 강제 실패", exception.getMessage());

            Long after = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tickets",
                Long.class
            );

            assertEquals(before, after);
        }

        @TestConfiguration(proxyBeanMethods = false)
        @EnableTransactionManagement
        static class TransactionConfig {

            @Bean
            PlatformTransactionManager transactionManager(
                DataSource dataSource
            ) {
                return new DataSourceTransactionManager(dataSource);
            }

            @Bean
            TicketRepository ticketRepository(JdbcTemplate jdbcTemplate) {
                return new TicketRepository(jdbcTemplate) {

                    @Override
                    public Ticket insert(String title, String status) {
                        super.insert(title, status);
                        throw new IllegalStateException("저장 직후 강제 실패");
                    }
                };
            }
        }
    }
*/