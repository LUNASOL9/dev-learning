package com.example.ticket_api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
@RequestMapping ("/api/tickets")
public class TicketController {
    
    @GetMapping
    public List<Ticket> findAll(){

        return List.of(
            new Ticket(1L, "로그인 오류", "OPEN"),
            new Ticket(2L, "결제 내역 확인", "CLOSED"),
            new Ticket(3L, "화면 속도 저하", "OPEN")
        );

    };
}
