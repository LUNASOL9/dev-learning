package com.example.ticket_api.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class TicketAdviceService {

    private final TicketAdviceValidator adviceValidator = new TicketAdviceValidator();

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
                .content();  //answer 문자열로 받기
    }//suggest




    public TicketAdviceResult suggestStructured(String title){

        TicketAdviceResult result = chatClient.prompt()
                .system("""
                    너는 업무 티켓을 검토하는 지원 담당자야.
                    원인을 단정하지 말고 먼저 확인할 사항을 제안해.

                    category에는 문제 분류를 짧은 한국어로 작성해.
                    checks에는 먼저 확인할 사항을 한국어로 정확히 두 개 작성해.
                """)
                .user("업무 티켓 제목: " + title)
                .call()
                // Spring AI는 DTO 구조에 맞는 응답 형식을 모델에 안내하고, 받은 응답을 Java 객체로 변환. 공식 문서에서도 record를 이렇게 받는 방법을 설명
                .entity(TicketAdviceResult.class);  // 지정한 Java 타입으로 변환해 받기

        adviceValidator.validate(result);

        return result;

    }//suggestStructured




}
