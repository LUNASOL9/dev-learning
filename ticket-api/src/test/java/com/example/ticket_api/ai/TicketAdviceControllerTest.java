package com.example.ticket_api.ai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.List;

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
                .andExpect(status().isBadRequest());// HTTP 400 확인

        // 서비스가 호출되지 않았는지 확인
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




    @Test
    void returnsStructuredAdviceForTrimmedTitle() throws Exception {

        TicketAdviceResult result = new TicketAdviceResult(
            "프린터",
            List.of("케이블 연결 확인", "드라이버 상태 확인")
        );
        when(adviceService.suggestStructured("프린터 연결 오류"))
            .thenReturn(result);


        mockMvc.perform(post("/api/ai/ticket-advice/structured")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"title" : " 프린터 연결 오류 "}
                                    """))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.category").value("프린터"))
                            .andExpect(jsonPath("$.checks.length()").value(2))
                            .andExpect(jsonPath("$.checks[0]").value("케이블 연결 확인"))
                            .andExpect(jsonPath("$.checks[1]").value("드라이버 상태 확인"))
                            ;

        verify(adviceService).suggestStructured("프린터 연결 오류");
    }




    @Test
    void rejectsBlankTitleForStructuredAdvice() throws Exception {

        // 1. 새 API에 공백 제목 전송
        mockMvc.perform(post("/api/ai/ticket-advice/structured")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"title" : " "}
                                    """))
                            // 2. HTTP 400 확인
                            .andExpect(status().isBadRequest());

        // 3. 서비스가 호출되지 않았는지 확인   잘못된 입력은 모델을 호출하기 전에 거절해야 한다
        verifyNoInteractions(adviceService);
    }




    @Test
    void returnsBadGatewayWhenAiResponseIsInvalid() throws Exception {
        // 서비스에서 AI 응답 검증에 실패한 상황을 준비
        when(adviceService.suggestStructured("프린터 연결 오류"))
            .thenThrow(new InvalidAiResponseException(
                "AI 확인 사항은 정확히 두 개여야 합니다."
            ));

        mockMvc.perform(post("/api/ai/ticket-advice/structured")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": "프린터 연결 오류"}
                        """))
            .andExpect(status().isBadGateway()) //502
            .andExpect(content().contentTypeCompatibleWith(
                MediaType.APPLICATION_JSON
            ))
            .andExpect(jsonPath("$.message").value(
                "AI 답변을 올바르게 생성하지 못했습니다. 다시 요청해 주세요."
            ));
    }




    @Test
    void returnsServiceUnavailableWhenAiConnectionFails() throws Exception {
        // 실제 연결 대신, 서비스가 연결 실패 예외를 던지는 상황을 준비
        when(adviceService.suggestStructured("프린터 연결 오류"))
            .thenThrow(new AiServiceUnavailableException(
                new ResourceAccessException("테스트용 연결 실패")
            ));

        mockMvc.perform(post("/api/ai/ticket-advice/structured")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": "프린터 연결 오류"}
                        """))
            .andExpect(status().isServiceUnavailable())//503
            .andExpect(content().contentTypeCompatibleWith(
                MediaType.APPLICATION_JSON
            ))
            .andExpect(jsonPath("$.message").value(
                "AI 서비스에 연결하지 못했습니다. 잠시 후 다시 시도해 주세요."
            ));

        verify(adviceService).suggestStructured("프린터 연결 오류");
    }

}//class