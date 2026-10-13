import { useState } from "react";

type TicketFormProps = {
    disabled: boolean;
    onCreate: (title: string) => Promise<void>;
}


export default function TicketForm ({
  disabled,
  onCreate,
}: TicketFormProps){
  
    const [newTitle, setNewTitle] = useState("");
    const [formError, setFormError] = useState("");
    const titleLength = newTitle.trim().length;
    const isTitleTooLong = titleLength > 50;

    const isSubmitDisabled = disabled || titleLength === 0 || isTitleTooLong;

    async function handleSubmit(){
        if(disabled){
            return;
        }

        const title = newTitle.trim();

        if (title.length === 0) {
            setFormError("제목을 입력해 주세요.");
            return;
        }

        if (title.length > 50) {
            setFormError("티켓 제목은 50자 이하여야 합니다.");
            return;
        }


        setFormError("");

        try{
            await onCreate(title);
            setNewTitle("");
        }catch(error: unknown){
            setFormError(
                error instanceof Error ? error.message:"티켓 등록 중 오류가 발생했습니다."
            );
        }


    }//handleSubmit

    return(
        <form onSubmit={event=>{
            event.preventDefault();
            void handleSubmit();
        }}
        >
        
            <label htmlFor="new-ticket-title">새 티켓 제목</label>
            <input id="new-ticket-title" 
                value={newTitle} 
                onChange={event => setNewTitle(event.target.value)}
                disabled={disabled}
            />
            <p style={{color : isTitleTooLong ? "red" : undefined}}>{titleLength}/50</p>
            <button type="submit" disabled={isSubmitDisabled}>
                티켓 등록
            </button>

            {formError && <p role="alert">{formError}</p>}

        </form>
    );
    

}//TicketForm