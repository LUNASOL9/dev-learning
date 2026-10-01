import { useState } from "react";

type TicketFormProps = {
    disabled: boolean;
    onCreate: (title: string) => void;
}


export default function TicketForm ({
  disabled,
  onCreate,
}: TicketFormProps){
  
    const [newTitle, setNewTitle] = useState("");
    const [formError, setFormError] = useState("");

    function handleSubmit(){
        if(disabled){
            return;
        }

        const title = newTitle.trim();

        if (title.length === 0) {
            setFormError("제목을 입력해 주세요.");
            return;
        }

        onCreate(title);

        setFormError("");
        setNewTitle("");        

    }//handleSubmit

    return(
        <form onSubmit={event=>{
            event.preventDefault();
            handleSubmit();
        }}
        >
        
            <label htmlFor="new-ticket-title">새 티켓 제목</label>
            <input id="new-ticket-title" 
                value={newTitle} 
                onChange={event => setNewTitle(event.target.value)}
                disabled={disabled}
            />
            
            <button type="submit" disabled={disabled}>
                티켓 등록
            </button>

            {formError && <p role="alert">{formError}</p>}

        </form>
    );
    

}//TicketForm