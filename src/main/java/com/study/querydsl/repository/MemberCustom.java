package com.study.querydsl.repository;

import com.study.querydsl.dto.MemberSearchCondition;
import com.study.querydsl.dto.MemberTeamDTO;

import java.util.List;

public interface MemberCustom {
    List<MemberTeamDTO> search(MemberSearchCondition condition);
}
