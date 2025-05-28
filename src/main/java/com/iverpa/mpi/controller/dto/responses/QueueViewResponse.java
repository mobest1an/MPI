package com.iverpa.mpi.controller.dto.responses;

import com.iverpa.mpi.model.User;

public record QueueViewResponse(String username) {

    public QueueViewResponse(User user) {
        this(user.getUsername());
    }
}
