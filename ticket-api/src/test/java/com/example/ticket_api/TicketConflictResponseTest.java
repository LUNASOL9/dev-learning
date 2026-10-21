package com.example.ticket_api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Controller와 HTTP 요청·응답 처리에 필요한 Spring 환경을 준비합니다.
// 실제 DB와 JPA 환경을 실행하는 테스트는 아닙니다.
@WebMvcTest(TicketController.class)

// 충돌 예외를 HTTP 409 응답으로 바꿀 실제 예외 처리기를 등록합니다.
@Import(TicketExceptionHandler.class)
class TicketConflictResponseTest {

    // 실제 서버 포트를 열지 않고 Controller에 요청을 보내는 테스트 도구입니다.
    @Autowired
    private MockMvc mockMvc;

    // 실제 Service 대신 Mockito가 만든 가짜 Service를 등록합니다.
    // Controller에는 이 가짜 Service가 주입됩니다.
    @MockitoBean
    private TicketService service;

    @Test
    void returnsConflictWhenCompletingStaleTicket() throws Exception {

        // 1. 가짜 Service의 동작 규칙을 미리 등록합니다.
        // complete(1L)이 호출되면 반환값 대신 충돌 예외를 던집니다.
        // 이 줄에서 실제 완료 처리나 DB 수정이 실행되지는 않습니다.
        when(service.complete(1L))
            .thenThrow(new OptimisticLockingFailureException(
                "테스트용 충돌"
            ));

        // 2. 티켓 완료 API로 PATCH 요청을 보냅니다.
        // Controller → 가짜 Service → 예외 발생 → 예외 처리기 순으로 실행됩니다.
        mockMvc.perform(patch("/api/tickets/1/complete"))

            // 3. HTTP 상태 코드가 409 Conflict인지 검증합니다. 결과 검증
            .andExpect(status().isConflict())

            // 4. 응답 본문이 JSON 형식인지 검증합니다.
            .andExpect(content().contentTypeCompatibleWith(
                MediaType.APPLICATION_JSON
            ))

            // 5. JSON의 message가 사용자 안내 메시지인지 검증합니다.
            // 예외의 내부 메시지인 "테스트용 충돌"을 그대로 반환하면 실패합니다.
            .andExpect(jsonPath("$.message").value(
                "다른 요청이 티켓을 변경했습니다. 목록을 새로 조회한 뒤 다시 시도해 주세요."
            ));
    }

    @Test
    void returnsConflictWhenReopeningStaleTicket() throws Exception {

        //가짜 서비스에게 "이럴 때 에러를 뿜어라"고 미리 명령을 심어둡니다.
        when(service.reopen(2L))
            .thenThrow(new OptimisticLockingFailureException(
                "테스트용 충돌"
            ));

        //실제 서버 포트를 열지 않고 MockMvc로 PATCH 요청 처리를 실행합니다.
        mockMvc.perform(patch("/api/tickets/2/reopen"))
            .andExpect(status().isConflict())
            .andExpect(content().contentTypeCompatibleWith(
                MediaType.APPLICATION_JSON
            ))
            .andExpect(jsonPath("$.message").value(
                "다른 요청이 티켓을 변경했습니다. 목록을 새로 조회한 뒤 다시 시도해 주세요."
            ));
    }
}