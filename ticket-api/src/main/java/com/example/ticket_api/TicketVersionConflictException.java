package com.example.ticket_api;

public class TicketVersionConflictException extends RuntimeException {

    public TicketVersionConflictException() {
        super(
            "다른 요청이 티켓을 변경했습니다. 목록을 새로 조회한 뒤 다시 시도해 주세요."
        );
    }
}
