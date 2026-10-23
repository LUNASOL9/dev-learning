package com.example.ticket_api;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//Spring Data JPA에서 조회 결과의 정렬 순서를 지정
import org.springframework.data.domain.Sort;
@Service 
public class TicketService {
    //Service → JPA Repository → Hibernate → JDBC → H2
    /*  jpa hibernate*/ 
    private final TicketJpaRepository ticketJpaRepository;

    public TicketService(TicketJpaRepository ticketJpaRepository) {
        this.ticketJpaRepository = ticketJpaRepository;
    }

    private Ticket toTicket(TicketEntity entity) {
        return new Ticket(        
            entity.getId(),
            entity.getTitle(),
            entity.getStatus(),
            entity.getVersion()

        );
    }
    
    @Transactional(readOnly = true) /*읽기 전용 트랜잭션 */
    public List<Ticket> findAll() {
        return ticketJpaRepository.findAll(Sort.by("id"))   //데이터베이스에서 모든 티켓을 조회하고, id를 기준으로 정렬
            .stream()   //조회한 리스트를 Stream으로 변환
                .map(this::toTicket)    //조회한 각 데이터를 toTicket() 메서드에 전달해서 변환
                    .toList();
    
    }

    @Transactional
    public synchronized Ticket create(String title) {

        if (title == null || title.isBlank()) {
            throw new InvalidTicketTitleException();
        }

        String normalizedTitle = title.strip();

        if (normalizedTitle.length() > 50) {
            throw new InvalidTicketTitleException("티켓 제목은 50자 이하여야 합니다.");
        }

        TicketEntity saved = ticketJpaRepository.save(
            new TicketEntity(normalizedTitle, "OPEN")
        );

        return toTicket(saved);
    }

    @Transactional
    public Ticket complete(long ticketId, Long requestedVersion) {
        TicketEntity entity = ticketJpaRepository.findById(ticketId)
            .orElseThrow(() -> new TicketNotFoundException());

        validateVersion(entity, requestedVersion);//버전 다르면 예외

        entity.complete();
        ticketJpaRepository.flush();    // UPDATE와 버전 증가를 DB에 반영. 아직 커밋 전이므로 롤백 가능.

        return toTicket(entity);
    }

    @Transactional
    public Ticket reopen(long ticketId, Long requestedVersion) {
        TicketEntity entity = ticketJpaRepository.findById(ticketId)
            .orElseThrow(() -> new TicketNotFoundException());

        validateVersion(entity, requestedVersion);//버전 다르면 예외

        entity.reopen();
        ticketJpaRepository.flush();

        return toTicket(entity);
    }
    // save()나 update()가 없다.
    // 트랜잭션 안에서 조회한 관리 상태의 TicketEntity entity 엔티티를 변경했으므로, 
    // 정상적인 트랜잭션 종료 과정에서 변경 감지로 UPDATE가 실행

    private  void validateVersion(TicketEntity entity, Long requestedVersion ) {
        if (requestedVersion == null || requestedVersion < 0) {
            throw new InvalidTicketVersionException();
        }

        if (!entity.getVersion().equals(requestedVersion)) {
            throw new TicketVersionConflictException();
        }
    }


    
    /*
        JDBC
        // Service가 사용할 저장소를 보관하는 필드
        private final TicketRepository ticketRepository;

        // 밖에서 전달받은 저장소를 필드에 저장
        public TicketService(TicketJpaRepository  ticketJpaRepository){
            this.ticketRepository = ticketJpaRepository;
        }    

        public synchronized List<Ticket> findAll() {
            return ticketRepository.findAll();
        }

        @Transactional
        public synchronized Ticket create(String title) {

            if (title == null || title.isBlank()) {
                throw new InvalidTicketTitleException();
            }

            String normalizedTitle = title.strip();

            if (normalizedTitle.length() > 50) {
                throw new InvalidTicketTitleException("티켓 제목은 50자 이하여야 합니다.");
            }

            return ticketRepository.insert(normalizedTitle, "OPEN");
        }

        @Transactional
        public synchronized Ticket complete(long ticketId) {
            return changeStatus(ticketId, "CLOSED");
        }

        @Transactional
        public synchronized Ticket reopen(long ticketId) {    // 지정한 티켓을 OPEN으로 변경하고 반환
            return changeStatus(ticketId, "OPEN");
        }

        private Ticket changeStatus(long ticketId, String status) {
            Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new TicketNotFoundException());

                    Ticket updatedTicket = new Ticket(
                        ticket.id(),
                        ticket.title(),
                        status
                    );

                    boolean updated = ticketRepository.update(updatedTicket);

                    if(!updated){
                        throw new TicketNotFoundException();
                    }
                    return  updatedTicket;
        }
    */

    
}//class
