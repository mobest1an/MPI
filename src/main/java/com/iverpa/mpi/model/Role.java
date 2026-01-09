package com.iverpa.mpi.model;

import lombok.Getter;

@Getter
public enum Role {
    RECRUIT("recruit"),
    ESCORT("escort"),
    COMMISSAR("commissar"),
    ADMIN("admin"),
    MILITARY_POLICE("military_police");

    private final String name;

    Role(String name) {
        this.name = name;
    }
}
