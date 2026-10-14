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

        String normalizedTitle = title.strip();

        if (normalizedTitle.length() > 50) {
            throw new InvalidTicketTitleException("티켓 제목은 50자 이하여야 합니다.");
        }

        Ticket ticket = new Ticket(
            nextId++,
            normalizedTitle,
            "OPEN"
        );

        tickets.add(ticket);
        return ticket;
    }

    public synchronized Ticket complete(long ticketId) {
        return changeStatus(ticketId, "CLOSED");
    }

    public synchronized Ticket reopen(long ticketId) {    // 지정한 티켓을 OPEN으로 변경하고 반환
        return changeStatus(ticketId, "OPEN");
    }

    private Ticket changeStatus(long ticketId, String status) {

        for (int i = 0; i < tickets.size(); i++) {
            Ticket ticket = tickets.get(i);

            if (ticket.id() == ticketId) {
                Ticket updatedTicket = new Ticket(
                    ticket.id(),
                    ticket.title(),
                    status
                );

                tickets.set(i, updatedTicket);
                return updatedTicket;
            }
        }

        throw new TicketNotFoundException();
    }

}//class
