package com.example.ticket_api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
/*
    여기서는 **`@RestController`가 아니라 @Controller**를 사용.
    반환 문자열을 응답 본문으로 보내는 대신,
    forward:를 통해 서버 내부에서 index.html로 요청을 전달
*/
@Controller
public class FrontendController {

    // 상세 화면 직접 접속·새로고침 시 React 시작 페이지를 제공합니다.
    @GetMapping("/tickets/{ticketId}")
    public String ticketDetail() {
        return "forward:/index.html";
    }

}
