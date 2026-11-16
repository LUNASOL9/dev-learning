package com.example.ticket_api.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import io.netty.handler.timeout.ReadTimeoutException;
import tools.jackson.databind.exc.MismatchedInputException;

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




    private boolean isReadTimeout(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if(current instanceof ReadTimeoutException){
                return true;
            }
            current = current.getCause();
        }
        return false;
    }




    public TicketAdviceResult suggestStructured(String title){

        TicketAdviceResult result;

        try{
            result= chatClient.prompt()
                .system("""
                    너는 업무 티켓을 검토하는 지원 담당자야.
                    원인을 단정하지 말고 먼저 확인할 사항을 제안해.

                    category에는 문제 분류를 짧은 한국어로 작성해.
                    checks에는 먼저 확인할 사항을 한국어로 정확히 두 개 작성해.

                    각 확인 사항에는 확인할 대상과 구체적인 행동을 포함해.
                    제목만으로 원인을 알 수 없다면 문제 구간을 좁히는 점검을 우선해.
                    업데이트, 재설치, 설정 변경보다는 로그와 현재 상태 확인을 먼저 제안해.
                    각 항목은 번호 없이 문장으로 작성해.

                    "성능 검사", "상태 점검"처럼 확인 방법이 없는 표현으로 끝내지 마.
                    무엇을 열거나 조회하고, 어떤 값이나 현상을 확인할지 설명해.

                    나쁜 예: 서버 리소스 사용량 확인
                    좋은 예: 문제가 발생한 시간대의 서버 모니터링에서 CPU와 메모리 사용률이 평소보다 높았는지 확인하세요.

                    예시는 구체성의 기준이며, 모든 티켓에 서버 점검을 제안하라는 뜻은 아니야.
                    """)
                .user("업무 티켓 제목: " + title)
                .options(
                    OllamaChatOptions.builder().disableThinking()
                )
                .call()
                // Spring AI는 DTO 구조에 맞는 응답 형식을 모델에 안내하고, 받은 응답을 Java 객체로 변환. 공식 문서에서도 record를 이렇게 받는 방법을 설명
                .entity(TicketAdviceResult.class);  // 지정한 Java 타입으로 변환해 받기
        }catch(ResourceAccessException exception){

            if(isReadTimeout(exception)){
                throw new AiResponseTimeoutException(exception);
            }

            throw new  AiServiceUnavailableException(exception);
            //아래의 검증·반환은 실행되지 않음
        }catch(MismatchedInputException exception) {
            // 모델 응답을 TicketAdviceResult로 변환하지 못한 경우
            // 원래 변환 예외를 cause로 보관한다.
            throw new InvalidAiResponseException(
                "AI 응답을 지정한 형식으로 변환하지 못했습니다.",
                exception
            );
        }

        adviceValidator.validate(result);
        return result;

    }//suggestStructured




}
