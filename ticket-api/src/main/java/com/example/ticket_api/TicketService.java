package com.example.ticket_api;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service 
public class TicketService {
    // Service가 사용할 저장소를 보관하는 필드
    private final TicketRepository ticketRepository;

    // 밖에서 전달받은 저장소를 필드에 저장
    public TicketService(TicketRepository ticketRepository){
        this.ticketRepository = ticketRepository;
    }


    public synchronized List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    @Transactional
    public synchronized Ticket create(String title) {

        if (title == null || title.isBlank()) {
            throw new InvalidTicketTitleException();
        }

        String normalizedTitle = title.strip();

        if (normalizedTitle.length() > 50) {
            throw new InvalidTicketTitleException("티켓 제목은 50자 이하여야 합니다.");
        }

        return ticketRepository.insert(normalizedTitle, "OPEN");
    }

    @Transactional
    public synchronized Ticket complete(long ticketId) {
        return changeStatus(ticketId, "CLOSED");
    }

    @Transactional
    public synchronized Ticket reopen(long ticketId) {    // 지정한 티켓을 OPEN으로 변경하고 반환
        return changeStatus(ticketId, "OPEN");
    }

    private Ticket changeStatus(long ticketId, String status) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new TicketNotFoundException());

                Ticket updatedTicket = new Ticket(
                    ticket.id(),
                    ticket.title(),
                    status
                );

                boolean updated = ticketRepository.update(updatedTicket);

                if(!updated){
                    throw new TicketNotFoundException();
                }
                return  updatedTicket;
    }

}//class
