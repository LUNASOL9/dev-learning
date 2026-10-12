package com.example.ticket_api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice 
public class TicketExceptionHandler {
    
    public record ErrorResponse(String message) {
    }

    @ExceptionHandler(InvalidTicketTitleException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTitle(
            InvalidTicketTitleException exception
    ) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(TicketNotFoundException.class)//없는 티켓
    public ResponseEntity<ErrorResponse> handleNotFound(
            TicketNotFoundException exception
    ) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(exception.getMessage()));
    }

}
