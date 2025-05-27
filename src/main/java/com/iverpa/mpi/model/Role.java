package com.iverpa.mpi.model;

import lombok.Getter;

@Getter
public enum Role {
    RECRUIT("recruit"),
    ESCORT("escort"),
    COMMISSAR("commissar"),
    ADMIN("admin");

    private final String name;

    Role(String name) {
        this.name = name;
    }
}
