package com.study.querydsl.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueryDSLConfig {

    /*
     * @PersistenceContext로 주입된 EntityManager는 스레드 안전한 프록시입니다.
     * 스프링은 해당 프록시를 통해 요청이나 트랜잭션별로 적절한 EntityManager 인스턴스를 제공합니다.
     * JPAQueryFactory를 EntityManager와 함께 사용하는 것은 안전하며, 스프링이 올바른 EntityManager 처리를 보장합니다.
     */

    @PersistenceContext
    private EntityManager em;

    @Bean
    public JPAQueryFactory getJpaQueryFactory(){
        return new JPAQueryFactory(em);
    }
}
