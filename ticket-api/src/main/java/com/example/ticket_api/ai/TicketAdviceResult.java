package com.example.ticket_api.ai;

import java.util.List;

public record TicketAdviceResult( /*DTO*/
    String category, 	/*문제 분류 하나*/
    List<String> checks /*먼저 확인할 사항 목록*/
) {
}