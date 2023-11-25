package com.study.querydsl.dto;

import lombok.Data;

@Data
public class MemberSearchCondition {
    private String username;
    private String teamName;
    private Integer ageGoe;
    private Integer ageLoe;

    public MemberSearchCondition(String username) {
        this.username = username;
    }
}
