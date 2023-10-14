package com.study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.entity.Hello;
import com.study.querydsl.entity.QHello;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(value = false) // or @Commit으로 DB에 test흔적남겨서 확인하기
class QueryDslApplicationTests {

    @PersistenceContext
    EntityManager em;

    @Test
    void contextLoads() {
        // TODO: 테스트 성공여부 질의함!! 미확인

        Hello hello = new Hello();
        em.persist(hello);

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QHello qHello = QHello.hello;
        Hello result = jpaQueryFactory
                .selectFrom(qHello)
                .fetchOne();

        assertThat(result).isEqualTo(hello);
        assertThat(result.getId()).isEqualTo(hello.getId());
    }

}
