package com.study.querydsl;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.dto.MemberDto;
import com.study.querydsl.dto.UserDto;
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
import org.springframework.util.ObjectUtils;

import java.util.List;

import static com.querydsl.core.types.Projections.bean;
import static com.querydsl.core.types.Projections.fields;
import static com.querydsl.jpa.JPAExpressions.select;
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
    void before() {
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
    void 테스트_JPQL() {
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
    void 테스트_queryDSL() {
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
    void testName() {
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
    void testName2() {
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
    void sortTest() {
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
    void paginTest() {
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
    void testAggregationFunction() {
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
    void groupingTest() {
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
    void joinTest() {
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
    void testThetaJoin() {
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
    void left_join_test() {
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple= " + tuple);
        }
    }

    @Test
    void fetchJoinNo() {
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
    void fetchJoinUse() {
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
    void test_subQueryGoe() {

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
    void test_subQuery() {

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
    void in절_테스트() {
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
    void test_select_subQuery() {
        QMember subMember = new QMember("subMember");

        List<Tuple> result = queryFactory
                .select(member.name, select(subMember.age.max())
                        .from(subMember)
                )
                .from(member).fetch();

        for (Tuple element : result) {
            System.out.println(element);
        }
    }

    @Test
    @DisplayName("case 예제")
    void case_테스트() {
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("20살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String age : result) {
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

        for (String r : result2) {
            System.out.println(r);
        }
    }

    @Test
    @DisplayName("상수 더하기")
    void 상수더하기_test() {
        List<Tuple> result = queryFactory
                .select(member.name, Expressions.constant("Const"))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println(tuple);
        }
    }

    @Test
    @DisplayName("조합하기")
    void 테스트_조합하기() {
        List<String> result = queryFactory
                .select(member.name.concat("_").concat(member.age.stringValue()))
                .from(member)
                .fetch();

        for (String t : result) {
            System.out.println(t);
        }
    }

    @Test
    void tuple_projection() {
        // Tuple자료구조를 repository를 넘어서는 설계는 좋지 않다
        // why, 하부 구현기술(JPA, QueryDSL)을 service로직에서 알 필요도 없으며, coupling이 높아진다
        // 만약, 넘어선다면 Tuple에 문제가 생기면 영향력이 repository를 넘어 직접적인 의존성이 있는 곳까지 퍼져나간다.
        // 그러므로, Tuple을 사용하게 되더라도 repository안에서만 사용하고 바깥계층으로는 DTO로 변환해서 전달
        List<Tuple> result = queryFactory
                .select(member.name, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    @DisplayName("DTO를 활용하 Projection by bean")
    void UserDto() {
        // getter, setter를 통해 값이 DTO에 주입됨
        List<MemberDto> result = queryFactory
                .select(bean(MemberDto.class,
                        member.name,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    @DisplayName("DTO를 활용한 Projection by fields")
    void UserDto2(){
        // getter, setter사용하지않고 바로 필드에 주입됨
        List<MemberDto> result = queryFactory
                .select(fields(MemberDto.class,
                        member.name,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * query
     * 나이는 최대 나이로 출력
     */
    @Test
    @DisplayName("별칭이 다른 DTO Projection")
    void userDTO3(){
        QMember subMember = new QMember("memberSub");

        List<UserDto> result = queryFactory
                .select(fields(UserDto.class,
                        member.name.as("username"),
                        ExpressionUtils.as(JPAExpressions
                                .select(subMember.age.max())
                                .from(subMember), "age")
                ))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    @Test
    @DisplayName("BooleanBuilder를 사용한 동적쿼리")
    void dynamicQuery_BooleanBuilder(){
        String username = "member1";
        Integer ageParam = null;

        List<Member> result = searchMember1(username, ageParam);

        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String username, Integer ageParam) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if(!ObjectUtils.isEmpty(username)){
            booleanBuilder.and(member.name.eq(username));
        }

        if(!ObjectUtils.isEmpty(ageParam)){
            booleanBuilder.and(member.age.eq(ageParam));
        }

        return queryFactory
                .selectFrom(member)
                .where(booleanBuilder)
                .fetch();
    }

    @Test
    @DisplayName("BooleanExpression을 사용한 동적쿼리")
    void dynamicQuery_BooleanExpression(){
        String username = "member1";
        Integer ageParam = 10;

//        List<Member> result = searchMember2(username, ageParam);
        List<Member> result = searchMember2(username, ageParam);

        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String username, Integer ageParam) {
        return queryFactory
                .selectFrom(member)
                .where(usernameEq(username),
                        ageEq(ageParam)) // where에 null이 들어가면 무시된다. 따라서 ageEq()이 null을 리턴하면 조건에서 무시됨 + 조건을 나열하면(,) 조건끼리는 and이다
                .fetch();
    }

    private BooleanExpression usernameEq(String username) {
        return username != null ? member.name.eq(username) : null;
    }

    private BooleanExpression ageEq(Integer ageParam) {
        return ageParam != null ? member.age.eq(ageParam) : null;
    }

    /**
     * 조건을 or로 조합하는 방법
     */
    @Test
    @DisplayName("동적쿼리 or조건 조합")
    void dynamicQuery_BooleanExpression2(){
        String name = "member1";
        Integer age = 10;

        List<Member> result = searchMember3(name, age);

        assertThat(result).isNotNull();
    }

    private List<Member> searchMember3(String name, Integer age) {
        return queryFactory
                .selectFrom(member)
                .where(nameEqOrAgeEq(name, age))
                .fetch();
    }

    private BooleanBuilder nameEqOrAgeEq(String name, Integer age){
        return nameEq(name).or(ageEq2(age));
    }

    private BooleanBuilder nameEq(String name){
        return name != null ? new BooleanBuilder(member.name.eq(name)) : new BooleanBuilder();
    }

    private BooleanBuilder ageEq2(Integer age){
        return age != null ? new BooleanBuilder(member.age.eq(age)) : new BooleanBuilder();
    }

    @Test
    void bulkUpdate(){
        // update결과에 영향을 받은 row수
        // bulk연산은 영속성 컨텍스트를 거치지않고 바로 DB에 쿼리를 날림
        long resultCount = queryFactory
                .update(member)
                .set(member.name, "비회원")
                .where(member.age.gt(10))
                .execute();

        System.out.println("resultCount = " + resultCount);

//        영속성 컨텍스트 초기화
//        em.flush();
//        em.clear();

        // DB에서 값을 가져와도 영속성 컨텍스트에 엔티티가 존재하면 영속성 컨텍스트안에 있는
        // 엔티티가 우선한다. 즉, DB에서 가져온 내용을 버림
        List<Member> result = queryFactory
                .selectFrom(member)
                .fetch();

        for (Member member1 : result) {
            System.out.println("member1 = " + member1);

        }
    }

    @Test
    void bulkAdd(){
        long count = queryFactory
                .update(member)
                .set(member.age, member.age.add(1))
                .execute();

        assertThat(count > 0).isTrue();
    }
}
