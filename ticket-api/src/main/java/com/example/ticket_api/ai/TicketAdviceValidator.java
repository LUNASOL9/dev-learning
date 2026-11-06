package com.example.ticket_api.ai;

public class TicketAdviceValidator {

    public void validate(TicketAdviceResult result) {

        if(result == null ||
           result.category() == null ||
           result.category().isBlank())
        {
            throw new IllegalStateException("AI 응답의 분류가 비어 있습니다.");
        }

         if(result.checks() == null ||
            result.checks().size() != 2)
        {
            throw new IllegalStateException(   "AI 확인 사항은 정확히 두 개여야 합니다.");
        }

        for(String check : result.checks()){
            if(check == null || check.isBlank()){
                throw new IllegalStateException(
                    "AI 확인 사항에 빈 내용이 있습니다."
                );
            }
        }
    }

}//class