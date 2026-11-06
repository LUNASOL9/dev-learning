package com.example.ticket_api.ai;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketAdviceValidatorTest {

    @Test
    void acceptsValidAdvice() {
        TicketAdviceValidator validator = new TicketAdviceValidator();

        TicketAdviceResult result = new TicketAdviceResult(
            "프린터",
            List.of(
                "케이블 연결 상태를 확인하세요.",
                "드라이버 설치 상태를 확인하세요."
            )
        );

        assertDoesNotThrow(() -> validator.validate(result));   //실행 중 예외가 발생하지 않아야 통과
    }




    @Test
    void rejectsBlankCategory() {
        TicketAdviceValidator validator = new TicketAdviceValidator();

        // 1. category는 "   "로 지정
        // 2. checks는 정상적인 확인 사항 두 개로 구성
        TicketAdviceResult result = new TicketAdviceResult(
            "   ", List.of(
                    "케이블 연결 상태를 확인하세요.",
                    "드라이버 설치 상태를 확인하세요."
            )
        );

        // 3. validate(result)가 IllegalStateException을 던지는지 확인
        IllegalStateException exception = assertThrows(
            IllegalStateException.class , () -> validator.validate(result)
        ) ;

            assertEquals(
            "AI 응답의 분류가 비어 있습니다.",
            exception.getMessage()
        );
    }




    @Test
    void rejectsAdviceWithOnlyOneCheck() {

        // 1. 검증기 생성
        TicketAdviceValidator validator = new TicketAdviceValidator();

        // 2. category는 "프린터", checks는 정상 문장 한 개로 DTO 생성
        TicketAdviceResult result = new TicketAdviceResult(
            "프린터",
            List.of(
                    "케이블 연결 상태를 확인하세요."
            )
        );

        // 3. validate()가 IllegalStateException을 던지는지 확인
        IllegalStateException exception = assertThrows(
            IllegalStateException.class , () -> validator.validate(result)
        ) ;

        assertEquals("AI 확인 사항은 정확히 두 개여야 합니다.", exception.getMessage());
    }




    @Test
    void rejectsBlankCheck() {

        TicketAdviceValidator validator = new TicketAdviceValidator();

        TicketAdviceResult result = new TicketAdviceResult(
            "프린터",
            List.of(
                "케이블 연결 상태를 확인하세요.",
                "   "
            )
        );

        IllegalStateException exception = assertThrows(
            IllegalStateException.class , () -> validator.validate(result)
        ) ;

        assertEquals("AI 확인 사항에 빈 내용이 있습니다.", exception.getMessage());

    }




    @Test
    void rejectsMissingChecks() {
        TicketAdviceValidator validator = new TicketAdviceValidator();
        TicketAdviceResult result = new TicketAdviceResult(
            "프린터",
            null
        );

        IllegalStateException exception = assertThrows(
            IllegalStateException.class , () -> validator.validate(result)
        ) ;

        assertEquals("AI 확인 사항은 정확히 두 개여야 합니다.", exception.getMessage());
    }

}//class