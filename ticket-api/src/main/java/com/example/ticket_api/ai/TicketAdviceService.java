package com.example.ticket_api.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class TicketAdviceService {

    private final ChatClient chatClient;

    public  TicketAdviceService(ChatClient.Builder builder){
        this.chatClient = builder.build();
    }

    public String suggest(String title) {
    return chatClient.prompt()
            .system("너는 업무 티켓을 검토하는 지원 담당자야. " /* .system 매번 유지할 지침 */
                    + "원인을 단정하지 말고, 먼저 확인할 사항 두 가지를 한국어로 제안해.")
            .user("업무 티켓 제목: " + title)
            .call()
            .content();
    }










}
