package com.iverpa.mpi.controller;

import com.iverpa.mpi.controller.dto.requests.JoinRecruitRequest;
import com.iverpa.mpi.controller.dto.responses.RecruitStatusResponse;
import com.iverpa.mpi.dao.UserService;
import com.iverpa.mpi.model.Summon;
import com.iverpa.mpi.model.User;
import com.iverpa.mpi.security.AuthorizedUser;
import com.iverpa.mpi.service.ElectronicQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recruit")
@RequiredArgsConstructor
public class RecruitElectronicQueueController {

    private final UserService userService;
    private final ElectronicQueueService electronicQueueService;

    @PostMapping("/queue/join")
    public void joinRecruit(@RequestBody JoinRecruitRequest request) {
        User user = userService.findByUsername(request.username());
        electronicQueueService.join(user);
    }

    @GetMapping("/queue/ready/{username}")
    public Boolean commissarReady(@PathVariable String username) {
        return electronicQueueService.isCommissarReady(username);
    }

    @PostMapping("/queue/leave")
    public void leaveQueue(@AuthenticationPrincipal AuthorizedUser authorizedUser) {
        User user = userService.findByUsername(authorizedUser.username());
        electronicQueueService.leave(user);
    }

    @GetMapping("/status")
    public RecruitStatusResponse getStatus(@AuthenticationPrincipal AuthorizedUser authorizedUser) {
        User user = userService.findByUsername(authorizedUser.username());
        Summon summon = electronicQueueService.getRecruitSummon(user);
        return new RecruitStatusResponse(summon.getStatus(), summon.getMilitaryBranch());
    }
}
