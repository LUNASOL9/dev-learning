package com.example.ticket_api.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import tools.jackson.databind.exc.MismatchedInputException;

class TicketAdviceServiceTest {

    @Test
    void wrapsEmptyModelResponseAsInvalidAiResponse() {
        /*  service.suggestStructured(...)
            → 가짜 모델이 "" 반환
            → 실제 ChatClient가 ""를 DTO로 변환 시도
            → MismatchedInputException 발생
            → 서비스의 catch 실행
            → InvalidAiResponseException으로 바꿔 던짐
            → 메시지와 원인 예외 검증 */


        // 1. 실제 Ollama 대신 사용할 가짜 모델
        ChatModel model = mock(ChatModel.class);

        // ChatClient가 모델의 옵션을 읽을 수 있도록 빈 기본 옵션을 반환
        when(model.getOptions()).thenReturn(ChatOptions.builder().build());

        // 2. 모델이 빈 문자열을 답했다고 설정
        ChatResponse emptyResponse = new ChatResponse(
            List.of(new Generation(new AssistantMessage("")))
        );

        //“model.call()에 어떤 질문이 들어오든, emptyResponse를 반환해.”
        when(model.call(any(Prompt.class)))
            .thenReturn(emptyResponse);

        // 3. 실제 ChatClient와 실제 서비스를 생성
        TicketAdviceService service = new TicketAdviceService(
            ChatClient.builder(model)
        );

        // 4. 실제 변환 과정에서 발생한 오류가
        //    우리 예외로 바뀌는지 확인
        InvalidAiResponseException exception = assertThrows(
            InvalidAiResponseException.class,
            () -> service.suggestStructured("프린터 연결 오류")
        );

        assertEquals(
            "AI 응답을 지정한 형식으로 변환하지 못했습니다.",
            exception.getMessage()
        );

        // 5. 원래 변환 오류가 cause에 보존됐는지 확인
        assertInstanceOf(
            MismatchedInputException.class,
            exception.getCause()
        );
    }




    @Test
    void returnsValidStructuredAdvice() {
        String responseJson = """
                            {
                                "category": "프린터",
                                "checks": [
                                    "케이블 연결 상태를 확인하세요.",
                                    "드라이버 설치 상태를 확인하세요."
                                ]
                            }
                            """;
        // 1. 실제 Ollama 대신 사용할 가짜 모델
        ChatModel model = mock(ChatModel.class);

        // ChatClient가 모델의 옵션을 읽을 수 있도록 빈 기본 옵션을 반환
        when(model.getOptions()).thenReturn(ChatOptions.builder().build());

        ChatResponse validResponse = new ChatResponse(
            List.of(new Generation(new AssistantMessage(responseJson)))
        );

        when(model.call(any(Prompt.class)))
        .thenReturn(validResponse);

        // 3. 실제 ChatClient와 실제 서비스를 생성
        TicketAdviceService service = new TicketAdviceService(
            ChatClient.builder(model)
        );

        TicketAdviceResult adviceResult = service.suggestStructured("프린터 연결 오류");

        assertEquals("프린터", adviceResult.category());
        assertEquals(
            List.of(
                "케이블 연결 상태를 확인하세요.",
                "드라이버 설치 상태를 확인하세요."
            ),
            adviceResult.checks()
        );

    }

}//class
