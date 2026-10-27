// @vitest-environment jsdom

import { afterEach, expect, test, vi } from "vitest";
import { cleanup, render, screen } from "@testing-library/react";
import "@testing-library/jest-dom/vitest";
import TicketForm from "./TicketForm";
import userEvent from "@testing-library/user-event";

// 각 테스트가 끝나면 렌더링했던 컴포넌트를 정리합니다.
afterEach(() => {
    cleanup();
});




test("제목이 비어 있으면 등록 버튼이 비활성화된다", () => {
    // 실제 서버 등록 대신 사용할 가짜 콜백입니다.
    const onCreate = vi.fn<(title: string) => Promise<void>>();

    // 실제 TicketForm을 테스트용 DOM에 렌더링합니다.
    // disabled=false로 설정하여 부모가 폼을 막은 상황을 제외합니다.
    render(
        <TicketForm
            disabled={false}
            onCreate={onCreate}
        />
    );

    // 화면에서 '티켓 등록'이라는 이름의 버튼을 찾습니다.
    const submitButton = screen.getByRole("button", {
        name: "티켓 등록",
    });

    // 실제 렌더링된 버튼이 비활성화 상태인지 검사합니다.
    expect(submitButton).toBeDisabled();
});




test("정상 제목을 입력하면 등록 버튼이 활성화된다", async () => {
    // 입력·클릭 같은 사용자 동작을 실행할 준비입니다.
    const user = userEvent.setup();
    const onCreate = vi.fn<(title: string) => Promise<void>>();

    render(
        <TicketForm
            disabled={false}
            onCreate={onCreate}
        />
    );

    // '새 티켓 제목'이라는 라벨이 연결된 입력창을 찾습니다.
    const titleInput = screen.getByRole("textbox", {
        name: "새 티켓 제목",
    });

    const submitButton = screen.getByRole("button", {
        name: "티켓 등록",
    });

    // 입력 전에는 비활성화되어 있어야 합니다.
    expect(submitButton).toBeDisabled();

    // 사용자가 입력창에 글자를 입력하는 동작을 재현합니다.
    await user.type(titleInput, "로그인 오류");

    // 입력 후에는 활성화되어 있어야 합니다.
    expect(submitButton).toBeEnabled();
});




test("등록하면 앞뒤 공백을 제거한 제목을 전달한다", async () => {
    
    // 입력·클릭 같은 사용자 동작을 실행할 준비입니다.
    const user = userEvent.setup();

    const onCreate = vi.fn<(title: string) => Promise<void>>()
    .mockResolvedValue(undefined); // 이 가짜 함수가 호출되면, 비동기 작업이 성공한 것으로 응답해라

    render(
        <TicketForm
            disabled={false}
            onCreate={onCreate}
        />
    );
    
    // '새 티켓 제목'이라는 라벨이 연결된 입력창을 찾습니다.
    const titleInput = screen.getByRole("textbox", {
        name: "새 티켓 제목",
    });    
    // 사용자가 입력창에 글자를 입력하는 동작을 재현합니다.
    await user.type(titleInput, "  로그인 오류  ");

    const submitButton = screen.getByRole("button", {
        name: "티켓 등록",
    });
    //등록 버튼을 클릭    
    await user.click(submitButton);

    expect(onCreate).toHaveBeenCalledTimes(1); // onCreate가 정확히 한 번 호출됐는가?
    expect(onCreate).toHaveBeenCalledWith("로그인 오류"); // 호출할 때 "로그인 오류"를 인수로 전달했는가?

    // 등록 성공 후 입력창이 비워졌는지 확인
    expect(titleInput).toHaveValue("");

});



test("등록에 실패하면 오류를 표시하고 입력을 유지한다", async () => {
    
    // 입력·클릭 같은 사용자 동작을 실행할 준비입니다.
    const user = userEvent.setup();

    const onCreate = vi.fn<(title: string) => Promise<void>>()
    .mockRejectedValue(new Error("티켓 등록에 실패했습니다.")); // 이 가짜 함수가 호출되면, 티켓 등록에 실패했습니다. 비동기 실패로 응답해라

    render(
        <TicketForm
            disabled={false}
            onCreate={onCreate}
        />
    );
    
    // '새 티켓 제목'이라는 라벨이 연결된 입력창을 찾습니다.
    const titleInput = screen.getByRole("textbox", {
        name: "새 티켓 제목",
    });    
    // 사용자가 입력창에 글자를 입력하는 동작을 재현합니다.
    await user.type(titleInput, "프린터 연결 오류");

    const submitButton = screen.getByRole("button", {
        name: "티켓 등록",
    });
    //등록 버튼을 클릭    
    await user.click(submitButton);

    const alert = await screen.findByRole("alert");
    expect(alert).toHaveTextContent("티켓 등록에 실패했습니다.");

    expect(onCreate).toHaveBeenCalledTimes(1); // onCreate가 정확히 한 번 호출됐는가?
    expect(titleInput).toHaveValue("프린터 연결 오류"); // 입력한 제목 유지

});




test("제목이 50자를 넘으면 등록할 수 없다", async () => {

    // 입력·클릭 같은 사용자 동작을 실행할 준비입니다.
    const user = userEvent.setup();

    const onCreate = vi.fn<(title: string) => Promise<void>>()
    .mockResolvedValue(undefined); // 이 가짜 함수가 호출되면, 비동기 작업이 성공한 것으로 응답해라

    render(
        <TicketForm
            disabled={false}
            onCreate={onCreate}
        />
    );

        // '새 티켓 제목'이라는 라벨이 연결된 입력창을 찾습니다.
    const titleInput = screen.getByRole("textbox", {
        name: "새 티켓 제목",
    });    
    // 사용자가 입력창에 글자를 입력하는 동작을 재현합니다.
    await user.type(titleInput, "가".repeat(51));

    const submitButton = screen.getByRole("button", {
        name: "티켓 등록",
    });
    expect(submitButton).toBeDisabled(); //비활성화된 버튼 검사    
    await user.click(submitButton); // 등록 버튼을 클릭    

    expect(onCreate).not.toHaveBeenCalled(); // 등록 콜백이 한 번도 호출되지 않았어야 한다
    
});




test("제목이 정확히 50자이면 등록할 수 있다", async () => {

    // 입력·클릭 같은 사용자 동작을 실행할 준비입니다.
    const user = userEvent.setup();

    const onCreate = vi.fn<(title: string) => Promise<void>>()
    .mockResolvedValue(undefined); // 이 가짜 함수가 호출되면, 비동기 작업이 성공한 것으로 응답해라

    render(
        <TicketForm
            disabled={false}
            onCreate={onCreate}
        />
    );

        // '새 티켓 제목'이라는 라벨이 연결된 입력창을 찾습니다.
    const titleInput = screen.getByRole("textbox", {
        name: "새 티켓 제목",
    });    
    // 사용자가 입력창에 글자를 입력하는 동작을 재현합니다.
    await user.type(titleInput, "가".repeat(50));

    const submitButton = screen.getByRole("button", {
        name: "티켓 등록",
    });
    expect(submitButton).toBeEnabled(); // 등록 버튼이 활성화되어 있는지 검사
    await user.click(submitButton); // 등록 버튼을 클릭    

    expect(onCreate).toHaveBeenCalledTimes(1); // onCreate가 정확히 한 번 호출됐는가?
    expect(onCreate).toHaveBeenCalledWith("가".repeat(50)); // 호출할 때 "가".repeat(50)를 인수로 전달했는가?

    // 등록 성공 후 입력창이 비워졌는지 확인
    expect(titleInput).toHaveValue("");    
});