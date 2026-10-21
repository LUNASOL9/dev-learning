package com.example.ticket_api;

import jakarta.persistence.*;

@Entity // JPA 엔티티로 지정
@Table(name = "tickets") // 매핑할 DB 테이블 이름
public class TicketEntity {

    @Id // 이 필드를 기본 키로 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // INSERT 시 DB가 ID를 발급합니다. 1, 2, 3…
    // 롤백 등으로 번호가 건너뛸 수 있으므로 연속된 값은 보장하지 않습니다.
    private Long id;

    @Column(nullable = false, length = 50)
    private String title;
    @Column(nullable = false, length = 10)
    private String status;

    //“내가 조회한 이후 다른 요청이 먼저 수정했는지”를 확인하는 방법 낙관적 잠금
    @Version
    @Column(nullable = false)
    private Long version;//Hibernate가 저장·수정 과정에서 관리.

    protected TicketEntity() {} 
    public TicketEntity(String title, String status) {
        this.title = title; 
        this.status = status; 
    } 



    public Long getVersion() {
        return version;
    }
    public Long getId(){
        return id;
    }
    public String getTitle(){
        return title;
    }
    
    public String getStatus(){
        return status;
    }
    public void complete(){
        this.status = "CLOSED";
    }
    public void reopen(){
        this.status = "OPEN";
    }
}