package com.example.ticket_api.ai;

public class InvalidAiResponseException extends RuntimeException{

    // 기존 검증기에서 사용하는 생성자
    public InvalidAiResponseException(String message){
        super(message);
    }

    // 변환 실패의 원인 예외까지 보관하는 생성자
    public InvalidAiResponseException(String message, Throwable cause) {
        super(message, cause);
    }

}
