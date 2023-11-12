package com.study.querydsl;


import com.querydsl.core.QueryFactory;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.entity.Member;
import com.study.querydsl.entity.QMember;
import com.study.querydsl.entity.Team;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.*;
import static com.study.querydsl.entity.QMember.member;
import static com.study.querydsl.entity.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(value = false)
public class QueryDSLTest {

    @PersistenceUnit
    EntityManagerFactory emf;
    
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
        Member member4 = new Member("member4", 21, teamB);
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

    @Test
    void testAggregationFunction(){
        Tuple result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetchOne();

        assertThat(result.get(member.age.avg())).isNotNull();
        assertThat(result.get(member.count())).isEqualTo(4L);
        assertThat(result.get(0, Long.class)).isEqualTo(4L);
        assertThat(result.get(member.age.sum())).isEqualTo(61);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라
     * select t.name, avg(m.age)
     * from TEAM t
     * join MEMBER m on t.TEAM_ID = m.TEAM_ID
     * group by t.name
     */
    @Test
    void groupingTest(){
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(team)
                .join(member).on(team.eq(member.team))
                .groupBy(team.name) // group by규칙??
                .fetch();

        assertThat(result.size()).isGreaterThan(0);
    }

    /**
     * 연관관계가 있는 경우
     * 팀A에 소속된 모든 멤버
     */
    @Test
    void joinTest(){
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("name")
                .containsExactly("member1", "member2");
    }

    /**
     * 연관관계가 없는 조인: 세타 조인 = 크로스 조인
     * 카티시안 프로덕트의 경우의 수 발생
     */
    @Test
    void testThetaJoin(){
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team) // 경우의 수: n x m
                .where(member.name.eq(team.name)) // 여기서 거름
                .fetch();

        assertThat(result)
                .extracting("name")
                .containsExactly("teamA", "teamB");
    }

    @Test
    void left_join_test(){
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();

        for( Tuple tuple : result){
            System.out.println("tuple= " + tuple);
        }
    }

    @Test
    void fetchJoinNo(){
        em.flush();
        em.clear();

        Member foundMember = queryFactory
                .selectFrom(member)
                .where(member.name.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(foundMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isFalse();

        System.out.println(foundMember.getTeam()); // 사용할때, team을 조회함. lazy loading

        boolean loaded2 = emf.getPersistenceUnitUtil().isLoaded(foundMember.getTeam());
        assertThat(loaded2).as("페치 조인 미적용").isTrue();
    }

    @Test
    void fetchJoinUse(){
        em.flush();
        em.clear();

        Member result = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.name.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(result.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isTrue();
    }

    /**
     * SubQuery
     */
    @Test
    @DisplayName("평균나이보다 나이가 많은 멤버")
    void test_subQueryGoe(){

        QMember subMember = new QMember("subMember");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                                select(subMember.age.avg())
                                        .from(subMember)
                        )
                ).fetch();

        assertThat(result).isNotNull();
        assertThat(result.size() > 0).isTrue();
        assertThat(result).extracting("age").containsExactly(20, 21);
    }

    /**
     * SubQuery
     */
    @Test
    @DisplayName("나이가 가장 많은 멤버")
    void test_subQuery(){

        QMember subMember = new QMember("subMember");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                                select(subMember.age.max())
                                        .from(subMember)
                        )
                ).fetch();

        assertThat(result).isNotNull();
        assertThat(result)
                .extracting("age")
                .containsExactly(21);
    }

    @Test
    @DisplayName("in 쿼리 테스트")
    void in절_테스트(){
        QMember subMember = new QMember("subMember");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        select(subMember.age)
                                .from(subMember)
                                .where(subMember.age.goe(11))

                ))
                .fetch();

        assertThat(result).isNotNull();
        assertThat(result).extracting("age").containsExactly(11, 20, 21);
    }

    @Test
    @DisplayName("select절 subQuery")
    void test_select_subQuery(){
        QMember subMember = new QMember("subMember");

        List<Tuple> result = queryFactory
                .select(member.name, select(subMember.age.max())
                        .from(subMember)
                )
                .from(member).fetch();

        for(Tuple element : result){
            System.out.println(element);
        }
    }

    @Test
    @DisplayName("case 예제")
    void case_테스트(){
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("20살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for(String age : result){
            System.out.println(age);
        }

        List<String> result2 = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 10)).then("0~10살")
                        .when(member.age.between(11, 20)).then("11~20살")
                        .otherwise("old")
                )
                .from(member)
                .fetch();

        for(String r : result2){
            System.out.println(r);
        }
    }

    @Test
    @DisplayName("상수 더하기")
    void 상수더하기_test(){
        List<Tuple> result = queryFactory
                .select(member.name, Expressions.constant("Const"))
                .from(member)
                .fetch();

        for(Tuple  tuple: result){
            System.out.println(tuple);
        }
    }

    @Test
    @DisplayName("조합하기")
    void 테스트_조합하기(){
        List<String> result = queryFactory
                .select(member.name.concat("_").concat(member.age.stringValue()))
                .from(member)
                .fetch();

        for(String t : result){
            System.out.println(t);
        }
    }
}
