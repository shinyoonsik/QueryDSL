package com.study.querydsl.controller;

import com.study.querydsl.dto.MemberSearchCondition;
import com.study.querydsl.dto.MemberTeamDTO;
import com.study.querydsl.repository.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HelloController {

    private final MemberJpaRepository memberJpaRepository;

    @GetMapping
    public String hello(){
        return "hello";
    }

    @GetMapping("/v1/members")
    public List<MemberTeamDTO> searchMemberV1(MemberSearchCondition condition){
        return memberJpaRepository.search(condition);
    }
}
