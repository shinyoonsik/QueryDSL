package com.study.querydsl.controller;

import com.study.querydsl.entity.Member;
import com.study.querydsl.entity.Team;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


/**
 * paging테스트를 위한 클래스
 */
@Profile("local")
@Component
@RequiredArgsConstructor
public class InitMember {

    private final InitMemberService initMemberService;

    @PostConstruct
    public void init(){
        initMemberService.init();
    }

    @Component
    static class InitMemberService{

        @PersistenceContext
        private EntityManager em;

//        @Transactional // entityManager는 트랜잭션내에서 사용되어야 한다
        public void init(){
            Team teamA = new Team("teamA");
            Team teamB = new Team("teamB");
            Member member1 = em.find(Member.class, 1L);
            System.out.println(member1);
//            em.persist(teamA);
//            em.persist(teamB);
//
//            for(int i=0; i<100; i++){
//                Team selectedTeam = (i % 2 == 0 ? teamA : teamB);
//                Member member = new Member("member" + i, i, selectedTeam);
//                em.persist(member);
//            }
        }
    }
}
