package com.example.ticket_api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
//import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.annotation.DirtiesContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
/* JDBC -> JPA hibernate 주석
@WebMvcTest(TicketController.class)
@Import({
    TicketService.class,
    TicketRepository.class,
    TestDatabaseConfig.class
    })
*/

/*JPA hibernate----- */
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;


@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=always",
    "spring.sql.init.data-locations=classpath:test-data.sql",
    "spring.sql.init.encoding=UTF-8"
})
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(//   실제 파일 DB 대신 테스트용 메모리 H2 DB 사용
    replace = AutoConfigureTestDatabase.Replace.ANY
)
/*-----JPA hibernate */

@DirtiesContext(    //테스트 메서드가 끝날 때 다음 테스트를 위해 컨텍스트를 완전히 초기화
    classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD
)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsTicketList() throws Exception {
        mockMvc.perform(get("/api/tickets"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(
                MediaType.APPLICATION_JSON
            ))
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].title").value("로그인 오류"))
            .andExpect(jsonPath("$[0].status").value("OPEN"))
            .andExpect(jsonPath("$[1].status").value("CLOSED"));
    }

    @Test
    void createsTicketAndIncludesItInList() throws Exception {
        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title": "  프린터 연결 오류  "}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(4))
            .andExpect(jsonPath("$.title").value("프린터 연결 오류"))
            .andExpect(jsonPath("$.status").value("OPEN"));

        mockMvc.perform(get("/api/tickets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(4))
            .andExpect(jsonPath("$[3].id").value(4))
            .andExpect(jsonPath("$[3].title").value("프린터 연결 오류"));
    }

    @Test
    void rejectsBlankTitle() throws Exception {
        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title": "   "}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(
                MediaType.APPLICATION_JSON
            ))
            .andExpect(jsonPath("$.message").value("티켓 제목을 입력해 주세요."));

        mockMvc.perform(get("/api/tickets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void rejectsMissingTitle() throws Exception {
        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("티켓 제목을 입력해 주세요."));

        mockMvc.perform(get("/api/tickets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void completesTicketAndKeepsStatusInList() throws Exception {
        mockMvc.perform(patch("/api/tickets/1/complete"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("로그인 오류"))
            .andExpect(jsonPath("$.status").value("CLOSED"));

        mockMvc.perform(get("/api/tickets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].status").value("CLOSED"))
            .andExpect(jsonPath("$[2].status").value("OPEN"));
    }

    @Test
    void completingTwiceKeepsTicketClosed() throws Exception {
        mockMvc.perform(patch("/api/tickets/1/complete"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CLOSED"));

        mockMvc.perform(patch("/api/tickets/1/complete"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("CLOSED"));

        mockMvc.perform(get("/api/tickets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void returnsNotFoundForMissingTicket() throws Exception {
        mockMvc.perform(patch("/api/tickets/999/complete"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(
                MediaType.APPLICATION_JSON
            ))
            .andExpect(jsonPath("$.message").value("티켓을 찾을 수 없습니다."));

        mockMvc.perform(get("/api/tickets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].status").value("OPEN"))
            .andExpect(jsonPath("$[2].status").value("OPEN"));
    }

    @Test
    void rejectsTitleLongerThan50Characters() throws Exception {
        String title = "가".repeat(51);
        String requestBody = """
            {"title": "%s"}
            """.formatted(title);

        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(
                MediaType.APPLICATION_JSON
            ))
            .andExpect(jsonPath("$.message")
                .value("티켓 제목은 50자 이하여야 합니다."));

        mockMvc.perform(get("/api/tickets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void reopensClosedTicketAndKeepsStatusInList() throws Exception {
        mockMvc.perform(patch("/api/tickets/2/reopen"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.title").value("결제 내역 확인"))
            .andExpect(jsonPath("$.status").value("OPEN"));

        mockMvc.perform(get("/api/tickets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[1].status").value("OPEN"));
    }

    @Test
    void returnsNotFoundWhenReopeningMissingTicket() throws Exception{
        mockMvc.perform(patch("/api/tickets/999/reopen"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(
                MediaType.APPLICATION_JSON
            ))
            .andExpect(jsonPath("$.message").value("티켓을 찾을 수 없습니다."));

        mockMvc.perform(get("/api/tickets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[1].status").value("CLOSED"));
    }

}//class
