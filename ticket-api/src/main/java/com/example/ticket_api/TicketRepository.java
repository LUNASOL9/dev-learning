package com.example.ticket_api;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository 
public class TicketRepository {
    private final List<Ticket> tickets = new ArrayList<>(List.of(
        new Ticket(1L, "로그인 오류", "OPEN"),
        new Ticket(2L, "결제 내역 확인", "CLOSED"),
        new Ticket(3L, "화면 속도 저하", "OPEN")
    ));

    private long nextId = 4L;

    public synchronized List<Ticket> findAll() {
        return List.copyOf(tickets);
    }

    //Optional<Ticket>은 티켓이 있을 수도, 없을 수도 있다는 것을 표현하는 반환 타입
    public synchronized Optional<Ticket> findById(long ticketId) {

        for (int i = 0; i < tickets.size(); i++) {
            Ticket ticket = tickets.get(i);

            if (ticket.id() == ticketId) { 
                //찾았을 때:               
                return Optional.of(ticket);
            }
        }
        //찾지 못했을 때:
        return Optional.empty(); 
    }


    public synchronized Ticket insert(String title, String status) {
        
        Ticket newTicket = new Ticket(
            nextId++,
            title,
            status
        );

        tickets.add(newTicket);
        return newTicket;
    }

    public synchronized boolean update(Ticket updatedTicket) {

        for(int index=0;  index < tickets.size();  index++){

            if(tickets.get(index).id() == updatedTicket.id()){
                tickets.set(index, updatedTicket);
                return true;
            }
        }

        return false;
    }

}//class
