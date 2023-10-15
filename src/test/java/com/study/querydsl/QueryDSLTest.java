package com.study.querydsl;


import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.entity.Member;
import com.study.querydsl.entity.QMember;
import com.study.querydsl.entity.Team;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QueryDSLTest {
    
    @PersistenceContext
    private EntityManager em;

    private JPAQueryFactory queryFactory;

    @BeforeEach
    void before(){
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 11, teamA);
        Member member3 = new Member("member3", 20, teamB);
        Member member4 = new Member("member4", 20, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    @DisplayName("JPQL select 테스트")
    void 테스트_JPQL(){
        // given
        String memberName = "member1";

        // when
        String jpql = "select m from Member m where name = :memberName";
        Member foundMember = em.createQuery(jpql, Member.class)
                .setParameter("memberName", memberName)
                .getSingleResult();

        // then
        assertThat(foundMember.getName()).isEqualTo(memberName);
    }

    @Test
    @DisplayName("QueryDSL select테스트")
    void 테스트_queryDSL(){
        // given
        QMember member = QMember.member;
        String memberName = "member1";

        // when
        Member foundMember = queryFactory
                .select(member)
                .from(member)
                .where(member.name.eq(memberName))
                .fetchOne();

        // then
        assertThat(foundMember.getName())
                .isEqualTo(memberName);
    }
}
