package com.study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.entity.Member;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// 순수 JPA레포지토리
@Repository
@RequiredArgsConstructor
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory; // QueryDSL을 사용하려면 JPAQueryFactory객체가 필요


    @Transactional
    public void save(Member member){
        em.persist(member);
    }

    @Transactional(readOnly = true)
    public Optional<Member> findById(Long id){
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    @Transactional(readOnly = true)
    public List<Member> findAll(){
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    @Transactional(readOnly = true)
    public List<Member> findByName(String name){
        return em.createQuery("select m from Member m where m.name=:name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}
