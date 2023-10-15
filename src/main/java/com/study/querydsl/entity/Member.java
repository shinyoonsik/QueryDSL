package com.study.querydsl.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA는 리플렉션을 사용하기 때문에 기본 생성자가 필요하다
@ToString(of = {"id", "name", "age"}) // ToString항목에 연관관계 정보를 넣지 말자. 무한 루프 발생
public class Member {
    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String name;
    private int age;

    // 외래키와 맵핑된 객체를 연관관계의 주인으로 간주하는게 편하다
    @JoinColumn(name = "team_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;

    public Member(String name){
        this(name, 0);
    }

    public Member(String name, int age) {
        this(name, age, null);
    }


    public Member(String name, int age, Team team) {
        this.name = name;
        this.age = age;
        if (team != null) {
            changeTeam(team);
        }
    }

    private void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }

}
