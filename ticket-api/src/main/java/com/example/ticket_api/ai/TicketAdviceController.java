package com.example.ticket_api.ai;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ai")
public class TicketAdviceController {

    private final TicketAdviceService ticketAdviceService;

    public TicketAdviceController(TicketAdviceService ticketAdviceService) {
        this.ticketAdviceService = ticketAdviceService;
    }

    public record AdviceRequest(String title) {}
    public record AdviceResponse(String advice) {}

    @PostMapping("/ticket-advice")
    public AdviceResponse suggest(@RequestBody AdviceRequest request) {
        String title = request.title();

        if (title == null || title.isBlank() || title.strip().length() > 50) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "티켓 제목은 1~50자로 입력해 줘."
            );
        }

        String advice = ticketAdviceService.suggest(title.strip());
        return new AdviceResponse(advice);// 기존: 문자열을 AdviceResponse로(record) 감싸서 반환
    }




    @PostMapping("/ticket-advice/structured")
    public TicketAdviceResult suggestStructured(@RequestBody AdviceRequest request){
        String title = request.title();

        if(title == null || title.isBlank() || title.strip().length() > 50){
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,             "티켓 제목은 1~50자로 입력해 줘."
            );
        }
        return ticketAdviceService.suggestStructured(request.title.strip()); // 신규: 서비스가 만든 DTO를 그대로 반환
    }

}//class