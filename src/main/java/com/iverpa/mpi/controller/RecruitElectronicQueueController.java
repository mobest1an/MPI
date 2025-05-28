package com.iverpa.mpi.controller;

import com.iverpa.mpi.controller.dto.requests.JoinRecruitRequest;
import com.iverpa.mpi.dao.UserService;
import com.iverpa.mpi.model.User;
import com.iverpa.mpi.service.ElectronicQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recruit/queue")
@RequiredArgsConstructor
public class RecruitElectronicQueueController {

    private final UserService userService;
    private final ElectronicQueueService electronicQueueService;

    @PostMapping("/join")
    public void joinRecruit(@RequestBody JoinRecruitRequest request) {
        User user = userService.findByUsername(request.username());
        electronicQueueService.join(user);
    }
}
