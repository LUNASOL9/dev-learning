package com.example.ticket_api;

import static org.junit.jupiter.api.Assertions.*;


import org.junit.jupiter.api.Test;

public class TicketEntityTest {

    @Test
    void completesAndReopensTicket() {
        TicketEntity ticket = new TicketEntity("로그인 오류", "OPEN");

        assertNull(ticket.getId());
        assertEquals("OPEN", ticket.getStatus());
        ticket.complete();
        assertEquals("CLOSED", ticket.getStatus());
        ticket.reopen();
        assertEquals("OPEN",ticket.getStatus());
        assertEquals("로그인 오류",ticket.getTitle());


    }
}//class

