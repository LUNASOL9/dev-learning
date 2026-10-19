package com.example.ticket_api;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class TicketRepository {

    private static final RowMapper<Ticket> TICKET_ROW_MAPPER =
        (rs, rowNum) -> new Ticket(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("status")
        );

    private final JdbcTemplate jdbcTemplate;

    public TicketRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Ticket> findAll() {
        String sql = """
            SELECT id, title, status
            FROM tickets
            ORDER BY id
            """;

        return jdbcTemplate.query(sql, TICKET_ROW_MAPPER);
    }

    public Optional<Ticket> findById(long ticketId) {
        String sql = """
            SELECT id, title, status
            FROM tickets
            WHERE id = ?
            """;

        List<Ticket> results = jdbcTemplate.query(
            sql,
            TICKET_ROW_MAPPER,
            ticketId
        );

        return results.stream().findFirst();
    }

    public Ticket insert(String title, String status) {
        String sql = """
            INSERT INTO tickets (title, status)
            VALUES (?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                sql,
                new String[] {"id"}
            );

            statement.setString(1, title);
            statement.setString(2, status);

            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();

        if (generatedId == null) {
            throw new IllegalStateException(
                "생성된 티켓 ID를 가져오지 못했습니다."
            );
        }

        return new Ticket(generatedId.longValue(), title, status);
    }

    public boolean update(Ticket updatedTicket) {
        String sql = """
            UPDATE tickets
            SET title = ?, status = ?
            WHERE id = ?
            """;

        int updatedRows = jdbcTemplate.update(
            sql,
            updatedTicket.title(),
            updatedTicket.status(),
            updatedTicket.id()
        );

        return updatedRows == 1;
    }
}