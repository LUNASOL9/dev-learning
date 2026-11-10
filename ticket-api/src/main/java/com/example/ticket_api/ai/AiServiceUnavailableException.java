package com.example.ticket_api.ai;

public class AiServiceUnavailableException extends RuntimeException {

    public AiServiceUnavailableException(Throwable cause) {
        super(
            "AI 서비스에 연결하지 못했습니다. 잠시 후 다시 시도해 주세요.", /* 첫 번째 인수: 우리 서비스에서 사용할 안내 메시지/ 두 번째 인수: 실패 원인인 원래 예외를 보관*/
            cause
        );
    }
}