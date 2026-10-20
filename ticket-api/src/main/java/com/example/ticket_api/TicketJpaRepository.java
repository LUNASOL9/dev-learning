package com.example.ticket_api;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketJpaRepository 
        extends JpaRepository<TicketEntity, Long> /*TicketEntity 저장·조회할 엔티티   Long 엔티티의 기본 키 타입*/{ 
    
}
