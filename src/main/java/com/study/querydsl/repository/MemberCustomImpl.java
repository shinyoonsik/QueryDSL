package com.study.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.dto.MemberSearchCondition;
import com.study.querydsl.dto.MemberTeamDTO;
import com.study.querydsl.dto.QMemberTeamDTO;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;

import java.util.List;

import static com.study.querydsl.entity.QMember.*;
import static com.study.querydsl.entity.QTeam.*;

@RequiredArgsConstructor
public class MemberCustomImpl implements MemberCustom {

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
}
