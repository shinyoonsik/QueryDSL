package com.study.querydsl.repository;

import com.study.querydsl.dto.MemberSearchCondition;
import com.study.querydsl.dto.MemberTeamDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MemberRepositoryTest {

    static MemberRepository memberRepository;
    static String name1 = "eclipse";
    static String name2 = "hoola";

    @BeforeAll
    public static void init(@Autowired MemberRepository myMemberRepository){
        memberRepository = myMemberRepository;

        com.study.querydsl.entity.Member member1 = new com.study.querydsl.entity.Member(name1, 10);
        com.study.querydsl.entity.Member result1 = memberRepository.save(member1);

        assertThat(result1.getName()).isEqualTo(name1);

        com.study.querydsl.entity.Member member2 = new com.study.querydsl.entity.Member(name2, 10);
        com.study.querydsl.entity.Member result2 = memberRepository.save(member2);

        assertThat(result2.getName()).isEqualTo(name2);
    }


    @Test
    @DisplayName("Spring Data JPA조회 테스트")
    void test(){
        List<com.study.querydsl.entity.Member> results = memberRepository.findByName("eclipse");
        assertThat(results).extracting("name").containsExactly(name1);
    }

    @Test
    @DisplayName("Spring Data JPA조회 테스트2")
    void test1(){
        List<com.study.querydsl.entity.Member> results = memberRepository.findAll();
        assertThat(results.size() > 0).isTrue();
    }

    @Test
    @DisplayName("Custom Repo 테스트")
    void test3(){
        MemberSearchCondition condition = new MemberSearchCondition(name1);
        List<MemberTeamDTO> result = memberRepository.search(condition);

        assertThat(result).extracting("username").containsExactly(name1);
    }
}