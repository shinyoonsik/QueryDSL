package com.study.querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<com.study.querydsl.entity.Member, Long>, MemberCustom {
    List<com.study.querydsl.entity.Member> findByName(String name);
}
