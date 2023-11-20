package com.study.querydsl.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueryDSLConfig {

    // 스프링이 주입해주는 엔티티 매니저는 프록시 엔티티 매니저이다.
    // 동작 시점에 트랜잭션 단위로 실제 엔티티 매니저가 할당된다.

    @Bean
    public JPAQueryFactory getJpaQueryFactory(EntityManager em){
        return new JPAQueryFactory(em);
    }
}
