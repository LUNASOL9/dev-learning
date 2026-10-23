package com.example.ticket_api;

public class InvalidTicketVersionException extends RuntimeException{
    public InvalidTicketVersionException() {
        super("티켓 버전은 0 이상의 숫자로 입력해 주세요.");
    }
}//class
