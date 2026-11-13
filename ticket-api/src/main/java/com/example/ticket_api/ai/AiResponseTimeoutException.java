package com.example.ticket_api.ai;

public class AiResponseTimeoutException extends RuntimeException{

    public AiResponseTimeoutException (Throwable cause) {
        super(
            "AI 응답 대기 시간이 초과되었습니다. 잠시 후 다시 시도해 주세요.",
            cause
        );
    }
}//class
