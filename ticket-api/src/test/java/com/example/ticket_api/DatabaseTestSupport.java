package com.example.ticket_api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

abstract class DatabaseTestSupport {

    private EmbeddedDatabase database;

    protected TicketRepository repository;

    @BeforeEach
    void setUpDatabase() {
        database = new EmbeddedDatabaseBuilder()
            .generateUniqueName(true)
            .setType(EmbeddedDatabaseType.H2)
            .setScriptEncoding("UTF-8")
            .addScript("classpath:schema.sql")
            .addScript("classpath:test-data.sql")
            .build();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(database);
        repository = new TicketRepository(jdbcTemplate);
    }

    @AfterEach
    void tearDownDatabase() {
        if (database != null) {
            database.shutdown();
        }
    }
}