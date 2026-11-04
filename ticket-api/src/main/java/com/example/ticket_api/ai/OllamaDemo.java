package com.example.ticket_api.ai;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("ai-demo")
public class OllamaDemo implements CommandLineRunner{

    private final TicketAdviceService ticketAdviceService;

    public OllamaDemo(TicketAdviceService service){
        this.ticketAdviceService = service;
    }

    @Override
    public void run(String... arg){

        String answer = ticketAdviceService.suggest("프린터 연결 오류");

        System.out.println("[Ollama → Java] " + answer);

    }// run
}// class
