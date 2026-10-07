package com.example.ticket_api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController 
@RequestMapping ("/api/tickets")
public class TicketController {
    
    
    private final List<Ticket> tickets = new ArrayList<>(List.of(
            new Ticket(1L, "로그인 오류", "OPEN"),
            new Ticket(2L, "결제 내역 확인", "CLOSED"),
            new Ticket(3L, "화면 속도 저하", "OPEN")
    ));

    @GetMapping
    public synchronized List<Ticket> findAll(){
        return List.copyOf(tickets);
    }


    private long nextId = 4L;
    @PostMapping 
    @ResponseStatus (HttpStatus.CREATED)
    public synchronized Ticket create(@RequestBody CreateTicketRequest request) {
        
        String title = request.title();

        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "티켓 제목을 입력해 주세요."
            );
        }

        Ticket ticket = new Ticket(
            nextId++,
            title.strip(),
            "OPEN"
        );

        tickets.add(ticket);

        return ticket;
    } //create


    @PatchMapping("/{ticketId}/complete")
    public synchronized Ticket complete(
        @PathVariable("ticketId") long ticketId
    ){

        for(int i =0; i< tickets.size(); i++){
            Ticket ticket = tickets.get(i);

            if(ticket.id() == ticketId){
                Ticket completedTicket  = new Ticket(
                    ticket.id(),
                    ticket.title(),
                    "CLOSED"
                );

                tickets.set(i, completedTicket );
                return completedTicket;
            }//if
        }//for

        throw new ResponseStatusException(
            HttpStatus.NOT_FOUND, //404
            "티켓을 찾을 수 없습니다."
        );
    }//complete


    
}//class
