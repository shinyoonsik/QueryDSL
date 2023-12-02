package com.study.querydsl.repository;

import com.study.querydsl.dto.MemberSearchCondition;
import com.study.querydsl.dto.MemberTeamDTO;
import com.study.querydsl.entity.Member;
import com.study.querydsl.entity.Team;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MemberRepositoryTest {

    static MemberRepositoryRepository memberRepository;
    static TeamRepository teamRepository;

    static String name1 = "eclipse";
    static String name2 = "hoola";

    @BeforeAll
    public static void init(@Autowired MemberRepositoryRepository myMemberRepository, @Autowired TeamRepository teamRepository){
        memberRepository = myMemberRepository;
        teamRepository = teamRepository;

        Team teamA = new Team("teamA");
        teamRepository.save(teamA);

        Team teamB = new Team("teamA");
        teamRepository.save(teamB);

        for(int i=0; i<20; i++){
            Member member = new Member("member10" + i, i);

            if(i % 2 == 0) member.setTeam(teamA);
            else member.setTeam(teamB);

            memberRepository.save(member);
        }
    }


    @Test
    @DisplayName("Spring Data JPA조회 테스트")
    @Disabled
    void test(){
        List<Member> results = memberRepository.findByName("eclipse");
        assertThat(results).extracting("name").containsExactly(name1);
    }

    @Test
    @DisplayName("Spring Data JPA조회 테스트2")
    @Disabled
    void test1(){
        List<Member> results = memberRepository.findAll();
        assertThat(results.size() > 0).isTrue();
    }

    @Test
    @DisplayName("Custom Repo 테스트")
    @Disabled
    void test3(){
        MemberSearchCondition condition = new MemberSearchCondition(name1);
        List<MemberTeamDTO> result = memberRepository.search(condition);

        assertThat(result).extracting("username").containsExactly(name1);
    }

    @Test
    @DisplayName("Paging처리 테스트")
    void test4(){
        PageRequest page = PageRequest.of(0, 10);
        MemberSearchCondition memberSearchCondition = new MemberSearchCondition();

        Page<MemberTeamDTO> result = memberRepository.searchComplex(memberSearchCondition, page);

        for (MemberTeamDTO memberTeamDTO : result) {
            System.out.println("전체 개수" + result.getContent().size());
            System.out.println("memberTeamDTO = " + memberTeamDTO);
        }
    }
}