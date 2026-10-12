package com.example.ticket_api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;


@Service 
public class TicketService {
private final List<Ticket> tickets = new ArrayList<>(List.of(
        new Ticket(1L, "로그인 오류", "OPEN"),
        new Ticket(2L, "결제 내역 확인", "CLOSED"),
        new Ticket(3L, "화면 속도 저하", "OPEN")
    ));

    private long nextId = 4L;

    public synchronized List<Ticket> findAll() {
        return List.copyOf(tickets);
    }

    public synchronized Ticket create(String title) {
        if (title == null || title.isBlank()) {
            throw new InvalidTicketTitleException();
        }

        Ticket ticket = new Ticket(
            nextId++,
            title.strip(),
            "OPEN"
        );

        tickets.add(ticket);
        return ticket;
    }

    public synchronized Ticket complete(long ticketId) {
        for (int i = 0; i < tickets.size(); i++) {
            Ticket ticket = tickets.get(i);

            if (ticket.id() == ticketId) {
                Ticket completedTicket = new Ticket(
                    ticket.id(),
                    ticket.title(),
                    "CLOSED"
                );

                tickets.set(i, completedTicket);
                return completedTicket;
            }
        }

        throw new TicketNotFoundException();
    }

}//class
