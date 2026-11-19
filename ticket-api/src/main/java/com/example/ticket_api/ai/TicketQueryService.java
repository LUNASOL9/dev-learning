package com.example.ticket_api.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;

@Service
public class TicketQueryService {

    private final ChatClient chatClient;
    private final TicketTools ticketTools;

    public TicketQueryService(
        ChatClient.Builder builder,
        TicketTools ticketTools
    ) {
        this.chatClient = builder.build();
        this.ticketTools = ticketTools;
    }

    public String answer(String question) {
        return chatClient.prompt()
            .system("""
                너는 업무 티켓 조회를 돕는 담당자야.
                현재 티켓에 관한 질문에는 반드시 도구로 조회한 결과를 사용해.
                티켓 번호, 제목, 상태를 임의로 만들지 마.
                OPEN은 미처리, CLOSED는 처리 완료를 의미해.
                질문의 조건에 맞는 티켓을 한국어로 안내해.
                해당하는 티켓이 없다면 없다고 알려줘.
                """)
            .user(question)
            .tools(ticketTools)
            .options(
                OllamaChatOptions.builder().disableThinking()
            )
            .call()
            .content();
    }
}