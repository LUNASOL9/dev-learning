package com.example.ticket_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = {
    "spring.sql.init.mode=never"
})
@Import(TestDatabaseConfig.class)
class TicketApiApplicationTests {

    @Test
    void contextLoads() {
    }
}