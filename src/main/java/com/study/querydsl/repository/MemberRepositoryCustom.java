package com.study.querydsl.repository;

import com.study.querydsl.dto.MemberSearchCondition;
import com.study.querydsl.dto.MemberTeamDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDTO> search(MemberSearchCondition condition);
    Page<MemberTeamDTO> searchComplex(MemberSearchCondition condition, Pageable pageable);
}
