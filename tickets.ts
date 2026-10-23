
export type Ticket ={
    id: number;
    title: string;
    status: "OPEN" | "CLOSED"
    version:number;
}

export function filterTickets(
    tickets: Ticket[], 
    searchText: string | null | undefined,
    showCompleted : boolean
): Ticket[] {
    const normalizedText  = (searchText ?? "").trim(); //(searchText ?? "") null과 undefined를 빈 검색어로 처리한다
    
    const filteredTickets  = tickets.filter(ticket=>{
        const matchesTitle = ticket.title.includes(normalizedText);
        const matchesStatus = showCompleted || ticket.status === "OPEN"
        
        return matchesTitle && matchesStatus;

    });
    return filteredTickets    
}

export function findOpenTickets(
    tickets: Ticket[], 
    searchText: string | null | undefined
): Ticket[] {

    const normalizedText  = (searchText ?? "").trim(); //(searchText ?? "") null과 undefined를 빈 검색어로 처리한다

    return tickets.filter(ticket =>
        ticket.status === "OPEN" && ticket.title.includes(normalizedText)
    );
}


export function findOpenTicketTitles(
    tickets: Ticket[],
    searchText: string | null | undefined
): string[] {
    return findOpenTickets(tickets, searchText).map(ticket=> ticket.title)    
}
