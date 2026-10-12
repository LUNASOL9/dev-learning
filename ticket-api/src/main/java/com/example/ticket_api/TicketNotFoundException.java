package com.example.ticket_api;

public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException() {
        super("티켓을 찾을 수 없습니다.");
    }
}