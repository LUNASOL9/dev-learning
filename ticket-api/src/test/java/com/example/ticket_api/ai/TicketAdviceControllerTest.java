package com.example.ticket_api.ai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(TicketAdviceController.class)
class TicketAdviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketAdviceService adviceService;

    @Test
    void rejectsBlankTitleWithoutCallingAdviceService() throws Exception {
        mockMvc.perform(post("/api/ai/ticket-advice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "   "}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(adviceService);
    }

    @Test
    void returnsAdviceForTrimmedTitle() throws Exception{

        String advice = "프린터 케이블 연결을 확인해.";

        // ① 서비스의 동작을 가짜로 설정
        when(adviceService.suggest("프린터 연결 오류")).thenReturn(advice);

        // ② HTTP POST 요청 실행
        mockMvc.perform(post("/api/ai/ticket-advice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title" : " 프린터 연결 오류 "}
                                """))
                        .andExpect(status().isOk())  // ③ HTTP 응답 검증
                        .andExpect(jsonPath("$.advice").value(advice));  // ④ JSON 응답의 advice 값 검증

                        verify(adviceService).suggest("프린터 연결 오류");      // ⑤ 서비스 메서드가 정확히 한 번 호출됐는지 검증

    }




}//class