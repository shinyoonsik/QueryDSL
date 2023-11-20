package com.study.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.dto.MemberSearchCondition;
import com.study.querydsl.dto.MemberTeamDTO;
import com.study.querydsl.dto.QMemberTeamDTO;
import com.study.querydsl.entity.Member;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.study.querydsl.entity.QMember.*;
import static com.study.querydsl.entity.QTeam.team;
import static io.micrometer.common.util.StringUtils.isEmpty;

// 순수 JPA레포지토리
@Repository
@RequiredArgsConstructor
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory; // QueryDSL을 사용하려면 JPAQueryFactory객체가 필요

    @Transactional
    public void save(Member member) {
        em.persist(member);
    }

    @Transactional(readOnly = true)
    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    @Transactional(readOnly = true)
    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    @Transactional(readOnly = true)
    public List<Member> findAll_queryDSL() {
        return queryFactory
                .selectFrom(member)
                .fetch();
    }

    @Transactional(readOnly = true)
    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name=:name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }

    @Transactional(readOnly = true)
    public List<Member> findByName_queryDSL(String name) {
        return queryFactory
                .selectFrom(member)
                .where(member.name.eq(name))
                .fetch();
    }

    public List<MemberTeamDTO> search(MemberSearchCondition condition) {
        return queryFactory
                .select(new QMemberTeamDTO(
                        member.id,
                        member.name,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .fetch();
    }

    private BooleanExpression usernameEq(String username) {
        return isEmpty(username) ? null : member.name.eq(username);
    }
    private BooleanExpression teamNameEq(String teamName) {
        return isEmpty(teamName) ? null : team.name.eq(teamName);
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe == null ? null : member.age.goe(ageGoe);
    }
    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe == null ? null : member.age.loe(ageLoe);
    }
}
