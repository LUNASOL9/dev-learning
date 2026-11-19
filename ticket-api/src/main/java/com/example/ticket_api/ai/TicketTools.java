package com.example.ticket_api.ai;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.example.ticket_api.Ticket;
import com.example.ticket_api.TicketService;

@Component
public class TicketTools {

    private final TicketService ticketService;

    public TicketTools(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Tool(description = """
                        현재 저장된 모든 티켓을 조회합니다.
                        미처리와 처리 완료 티켓을 모두 반환합니다.
                        전체 티켓 목록을 묻는 질문에 사용하세요.
                        """)
    public List<Ticket> listTickets() {
        List<Ticket> tickets = ticketService.findAll();

        return tickets;
    }

    @Tool(description = """
                        현재 미처리 상태인 티켓만 조회합니다.
                        OPEN 상태의 티켓만 반환합니다.
                        미처리 티켓 목록을 묻는 질문에 사용하세요.
                        """)
    public List<Ticket> listOpenTickets() {
        List<Ticket> tickets = ticketService.findAll().stream()
            .filter(ticket -> "OPEN".equals(ticket.status()))
            .toList();

        return tickets;
    }
}