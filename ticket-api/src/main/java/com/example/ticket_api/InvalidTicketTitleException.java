package com.example.ticket_api;

public class InvalidTicketTitleException extends RuntimeException {
    
    public  InvalidTicketTitleException(){
        super("티켓 제목을 입력해 주세요.");
    }
}
