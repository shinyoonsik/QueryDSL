package com.study.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.dto.MemberSearchCondition;
import com.study.querydsl.dto.MemberTeamDTO;
import com.study.querydsl.dto.QMemberTeamDTO;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.ObjectUtils;

import java.util.List;

import static com.study.querydsl.entity.QMember.member;
import static com.study.querydsl.entity.QTeam.team;

@RequiredArgsConstructor
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
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
                .where(memberNameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()))
                .fetch();
    }

    private BooleanExpression memberNameEq(String memberName) {
        return StringUtils.isBlank(memberName) ? null : member.name.eq(memberName);
    }

    private BooleanExpression teamNameEq(String teamName) {
        return StringUtils.isBlank(teamName) ? null : team.name.eq(teamName);
    }

    private BooleanExpression ageGoe(Integer age) {
        return ObjectUtils.isEmpty(age) ? null : member.age.goe(age);
    }

    @Override
    public Page<MemberTeamDTO> searchComplex(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDTO> content = queryFactory
                .select(new QMemberTeamDTO(
                        member.id,
                        member.name,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(memberNameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()))
                .offset(pageable.getOffset()) // 몇번째 record부터 가져와
                .limit(pageable.getPageSize()) // offset부터 가져오는 최대 개수 == 한 페이지에 보여줄 컨텐츠의 개수
                .fetch();

        long count = queryFactory
                .select(member.count())
                .from(member)
                .leftJoin(member.team, team)
                .where(memberNameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()))
                .fetchOne();

        return new PageImpl<>(content,pageable, count);
    }
}
