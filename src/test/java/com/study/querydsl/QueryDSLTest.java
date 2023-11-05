package com.study.querydsl;


import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.entity.Member;
import com.study.querydsl.entity.Team;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.querydsl.core.types.ExpressionUtils.count;
import static com.study.querydsl.entity.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QueryDSLTest {
    
    @PersistenceContext
    private EntityManager em;

    @Autowired
    private JPAQueryFactory queryFactory;

    @BeforeEach
    void before(){
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

    @Test
    @DisplayName("QueryDSL 검색조건 쿼리")
    void testName(){
        // when
        Member foundMember = queryFactory
                .selectFrom(member)
                .where(member.name.eq("member1")
                        .and(member.age.eq(10))
                )
                .fetchOne();

        // then
        assertThat(foundMember).isNotNull();
    }

    @Test
    @DisplayName("QueryDSL 검색조건 쿼리2")
    void testName2(){
        List<Member> memberList1 = queryFactory
                .selectFrom(member)
                .where(member.name.ne("member1"))
                .fetch();

        assertThat(memberList1).isNotNull();

        List<Member> memberList2 = queryFactory
                .selectFrom(member)
                .where(member.age.between(10, 30),
                        member.name.like("member%"))
                .fetch();

        assertThat(memberList2.size() > 0).isTrue();
    }

    @Test
    void sortTest(){
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.name.asc().nullsLast())
                .fetch();

        assertThat(result.get(0).getName()).isEqualTo("member5");
        assertThat(result.get(2).getName()).isNull();
    }

    @Test
    void paginTest(){
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.name.desc())
                .offset(1) // 조회의 시작점(테이블 조회의 시작row). 대부분의 DB는 zero-base이다
                .limit(2)
                .fetch();

        assertThat(result.size()).isEqualTo(2);

        Long resultCount = queryFactory
                .select(member.count())
                .from(member)
                .fetchOne();

        assertThat(resultCount > 0).isTrue();
    }
}
