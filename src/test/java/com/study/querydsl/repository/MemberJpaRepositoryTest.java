package com.study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.config.QueryDSLConfig;
import com.study.querydsl.entity.Member;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    JPAQueryFactory queryFactory;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    void basicTest(){
        String name1 = "member1";
        int age = 10;
        Member member1 = new Member(name1, age);
        memberJpaRepository.save(member1);

        Optional<Member> foundMember = memberJpaRepository.findById(member1.getId());
        foundMember.ifPresent(member -> assertThat(member.getName()).isEqualTo(name1));

        List<Member> memberList = memberJpaRepository.findAll();
        assertThat(memberList.size() > 0).isTrue();

        List<Member> memberList2 = memberJpaRepository.findByName(name1);
        assertThat(memberList2).containsExactly(member1);
    }

    @Test
    void queryDSL_test(){
        String name1 = "member1";
        int age = 10;
        Member member1 = new Member(name1, age);
        memberJpaRepository.save(member1);

        List<Member> result = memberJpaRepository.findByName_queryDSL(name1);
        assertThat(result).containsExactly(member1);

        List<Member> result2 = memberJpaRepository.findAll_queryDSL();
        assertThat(result2).containsExactly(member1);
    }

}