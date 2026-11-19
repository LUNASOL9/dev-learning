package com.example.ticket_api.ai;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("ai-demo")
public class OllamaDemo implements CommandLineRunner{

    private final TicketQueryService ticketQueryService;

    public OllamaDemo(TicketQueryService ticketQueryService) {
        this.ticketQueryService = ticketQueryService;
    }

    @Override
    public void run(String... arg){
        String answer = ticketQueryService.answer(
            "현재 미처리 티켓의 번호와 제목을 알려줘."
        );

        System.out.println("[AI 티켓 조회] " + answer);

    }// run
}// class
