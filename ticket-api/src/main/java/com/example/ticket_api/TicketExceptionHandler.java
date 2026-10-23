package com.example.ticket_api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.dao.OptimisticLockingFailureException;

@RestControllerAdvice 
public class TicketExceptionHandler {
    
    public record ErrorResponse(String message) {
    }

    @ExceptionHandler(InvalidTicketTitleException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTitle(
            InvalidTicketTitleException exception
    ) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST) //400
            .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(TicketNotFoundException.class)//없는 티켓
    public ResponseEntity<ErrorResponse> handleNotFound(
            TicketNotFoundException exception
    ) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)   //404
            .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler({
        OptimisticLockingFailureException.class,
        TicketVersionConflictException.class
    })
    public ResponseEntity<ErrorResponse> handleOptimisticLock(
            RuntimeException exception
    ) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)    //409
            .body(new ErrorResponse(
                "다른 요청이 티켓을 변경했습니다. 목록을 새로 조회한 뒤 다시 시도해 주세요."
            ));
    }

    //티켓 버전 null 또는 음수 → 잘못된 요청 → 400
    @ExceptionHandler(InvalidTicketVersionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidVersion(
            InvalidTicketVersionException exception
    ) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(exception.getMessage()));
    }

}
