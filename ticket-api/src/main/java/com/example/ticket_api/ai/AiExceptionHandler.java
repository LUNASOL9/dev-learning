package com.example.ticket_api.ai;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AiExceptionHandler {
// ① TicketAdviceController.java
//    suggestStructured(request)
//    │
//    │ 제목 검증 후 서비스 호출
//    ▼
// ② TicketAdviceService.java
//    suggestStructured(title)
//    │
//    │ ChatClient로 Ollama 호출
//    │ 응답을 TicketAdviceResult로 변환
//    │ adviceValidator.validate(result) 호출
//    ▼
// ③ TicketAdviceValidator.java
//    validate(result)
//    │
//    │ 예: 확인 사항이 한 개뿐이면
//    │ throw new InvalidAiResponseException(...)
//    ▼
// ④ 예외가 호출 경로를 거슬러 올라감
//    Validator → Service → Controller
//    │
//    │ 중간에서 catch하지 않았으므로 Spring MVC까지 전달
//    ▼
// ⑤ AiExceptionHandler.java
//    handleInvalidAiResponse(exception)
//    │
//    │ Spring이 @ExceptionHandler를 찾아 실행
//    ▼
// ⑥ HTTP 502 + JSON 안내 메시지 반환
    public record ErrorResponse(String message) {}

    //모델 응답 내용을 사용할 수 없음
    @ExceptionHandler (InvalidAiResponseException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAiResponse(
            InvalidAiResponseException exception) {

        return ResponseEntity
            .status(HttpStatus.BAD_GATEWAY)//502
            .body(new ErrorResponse(
                "AI 답변을 올바르게 생성하지 못했습니다. 다시 요청해 주세요."
            ));
    }



    //모델 연결 문제로 AI 기능을 제공할 수 없음     예) LLM 서버가 꺼져 있음
    @ExceptionHandler(AiServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleAiServiceUnavailable(
            AiServiceUnavailableException exception) {

        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE) //503
            .body(new ErrorResponse(exception.getMessage()));
    }

}//class