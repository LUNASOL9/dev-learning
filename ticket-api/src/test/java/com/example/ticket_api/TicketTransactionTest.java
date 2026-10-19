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