// 테스트 환경을 브라우저와 유사한 가상 DOM(jsdom) 환경으로 설정합니다.
// @vitest-environment jsdom

import "@testing-library/jest-dom/vitest";
import {act, cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, expect, test, vi } from "vitest";
import TicketAdvice from "./TicketAdvice";
import { fetchTicketAdvice } from "./ticket-advice-api";

// 1. 모듈 모킹(Mocking)
// 실제 서버와 통신하는 API 함수를 가짜(Mock) 함수로 대체합니다.
// 네트워크 요청 없이 테스트를 빠르고 안정적으로 수행하기 위함입니다.
vi.mock("./ticket-advice-api", () => ({
    fetchTicketAdvice: vi.fn(),
}));

// 2. 테스트 후 정리 작업 (Cleanup)
// 각 테스트 케이스가 끝난 후 실행되어 이전 테스트의 영향이 다음 테스트에 미치지 않도록 방지합니다.
afterEach(() => {
    cleanup(); // 렌더링된 컴포넌트의 DOM 흔적을 정리합니다.
    vi.resetAllMocks(); // 목(Mock) 함수의 호출 기록과 반환값 설정을 초기화합니다.
});

// 3. 테스트 케이스 작성
test("도움말을 요청하면 티켓 제목을 전달하고 답변을 표시한다", async () => {
    // 사용자 상호작용(클릭, 입력 등)을 실제 브라우저처럼 시뮬레이션하기 위해 설정합니다.
    const user = userEvent.setup();

    // 4. 모킹된 API의 응답 설정
    // 가짜 API 함수가 호출되었을 때 성공적으로 반환할 데이터(Resolved Value)를 지정합니다.
    vi.mocked(fetchTicketAdvice).mockResolvedValue(
        "데이터베이스 연결 로그를 확인하세요."
    );

    // 5. 컴포넌트 렌더링
    // 테스트 대상 컴포넌트에 title prop을 전달하여 가상 DOM에 렌더링합니다.
    render(<TicketAdvice title="데이터베이스 연결 오류" />);

    // 6. 사용자 이벤트 발생 (버튼 클릭)
    // 'AI 도움말 요청'이라는 이름표(Accessible Name)를 가진 버튼 요소를 찾아 클릭합니다.
    await user.click(
        screen.getByRole("button", {
            name: "AI 도움말 요청",
        })
    );

    // 7. API 호출 검증
    // API 함수가 정확히 1번 호출되었는지 검증합니다.
    expect(fetchTicketAdvice).toHaveBeenCalledTimes(1);

    // API 함수가 올바른 인자("데이터베이스 연결 오류")와 함께 호출되었는지 검증합니다.
    expect(fetchTicketAdvice).toHaveBeenCalledWith(
        "데이터베이스 연결 오류"
    );

    // 8. 화면 상태 변화 검증 (비동기)
    // 비동기 통신 후 화면에 API로부터 받은 답변 텍스트가 정상적으로 나타났는지 확인합니다.
    // (findByText는 요소가 나타날 때까지 비동기로 기다려줍니다.)
    expect(
        await screen.findByText("데이터베이스 연결 로그를 확인하세요.")
    ).toBeInTheDocument();

    // 9. 최종 컴포넌트 상태 검증
    // 요청이 끝난 후 버튼이 다시 활성화 상태(enabled)인지 확인합니다.
    expect(
        screen.getByRole("button", {
            name: "AI 도움말 요청",
        })
    ).toBeEnabled();
});




test("도움말 요청에 실패하면 오류를 표시하고 다시 요청할 수 있다", async () => {
    const user = userEvent.setup();

    // API 함수가 실패하는 상황을 준비
    vi.mocked(fetchTicketAdvice).mockRejectedValue(
        new Error("AI 도움말 요청 실패: 503")
    );

    render(<TicketAdvice title="데이터베이스 연결 오류" />);

    await user.click(
        screen.getByRole("button", {
            name: "AI 도움말 요청",
        })
    );

    // catch에서 설정한 오류 메시지가 화면에 나타나는지 확인
    expect(await screen.findByRole("alert")).toHaveTextContent(
        "AI 도움말 요청 실패: 503"
    );

    // finally에서 로딩 상태가 해제되어 버튼을 다시 누를 수 있는지 확인
    expect(
        screen.getByRole("button", {
            name: "AI 도움말 요청",
        })
    ).toBeEnabled();
});




/*
    버튼 클릭
    → Promise 대기 → 로딩 표시·버튼 비활성화 검사
    → resolveAdvice("답변") 호출
    → Promise 성공 → 답변 표시·버튼 활성화 검사
*/
test("도움말 요청 중에는 버튼을 비활성화한다", async () => {
    const user = userEvent.setup();

    // 나중에 우리가 직접 응답을 완료할 때 사용할 함수
    let resolveAdvice!: (value: string) => void;//“문자열을 받는 함수를 나중에 resolveAdvice에 넣을 거야.”

    // 아직 성공도 실패도 하지 않은, 대기 중인 Promise
    const pendingAdvice = new Promise<string>((resolve) => {
        resolveAdvice = resolve;
    });

    vi.mocked(fetchTicketAdvice).mockReturnValue(pendingAdvice);

    render(<TicketAdvice title="데이터베이스 연결 오류" />);

    await user.click(
        screen.getByRole("button", {
            name: "AI 도움말 요청",
        })
    );

    // 아직 응답을 완료하지 않았으므로 요청 중 상태여야 한다.
    const loadingButton = screen.getByRole("button", {
        name: "AI 답변 생성 중…",
    });

    expect(loadingButton).toBeDisabled();
    expect(screen.getByRole("status")).toHaveTextContent(
        "AI가 확인할 사항을 정리하고 있어요."
    );

    // 비활성화된 버튼을 다시 눌러도 추가 요청이 없어야 한다.
    await user.click(loadingButton);
    expect(fetchTicketAdvice).toHaveBeenCalledTimes(1);

    // 이제 우리가 직접 응답을 도착시킨다.
    await act(async () => {
        resolveAdvice("데이터베이스 연결 로그를 확인하세요.");
    });

    expect(
        await screen.findByText("데이터베이스 연결 로그를 확인하세요.")
    ).toBeInTheDocument();

    expect(
        screen.getByRole("button", {
            name: "AI 도움말 요청",
        })
    ).toBeEnabled();

    expect(screen.queryByRole("status")).not.toBeInTheDocument();
});