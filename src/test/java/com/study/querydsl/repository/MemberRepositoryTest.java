package com.study.querydsl.repository;

import com.study.querydsl.entity.Member;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MemberRepositoryTest {

    static MemberRepository memberRepository;
    static String name1 = "eclipse";
    static String name2 = "hoola";

    @BeforeAll
    public static void init(@Autowired MemberRepository myMemberRepository){
        memberRepository = myMemberRepository;

        Member member1 = new Member(name1, 10);
        Member result1 = memberRepository.save(member1);

        assertThat(result1.getName()).isEqualTo(name1);

        Member member2 = new Member(name2, 10);
        Member result2 = memberRepository.save(member2);

        assertThat(result2.getName()).isEqualTo(name2);
    }


    @Test
    @DisplayName("Spring Data JPA조회 테스트")
    void test(){
        List<Member> results = memberRepository.findByName("eclipse");
        assertThat(results).extracting("name").containsExactly(name1);
    }

    @Test
    @DisplayName("Spring Data JPA조회 테스트2")
    void test1(){
        List<Member> results = memberRepository.findAll();
        assertThat(results.size() > 0).isTrue();
    }
}