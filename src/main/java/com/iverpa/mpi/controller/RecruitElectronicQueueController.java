package com.iverpa.mpi.controller;

import com.iverpa.mpi.controller.dto.requests.RecruitRequest;
import com.iverpa.mpi.dao.SummonService;
import com.iverpa.mpi.dao.UserService;
import com.iverpa.mpi.model.Summon;
import com.iverpa.mpi.model.User;
import com.iverpa.mpi.service.ElectronicQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recruit/queue")
@RequiredArgsConstructor
public class RecruitElectronicQueueController {

    private final UserService userService;
    private final ElectronicQueueService electronicQueueService;
    private final SummonService summonService;

    @PostMapping("/join")
    public void joinRecruit(@RequestBody RecruitRequest request) {
        User user = userService.findByUsername(request.username());
        electronicQueueService.join(user);
    }

    @GetMapping("/ready/{username}")
    public Boolean commissarReady(@PathVariable String username) {
        User user = userService.findByUsername(username);
        Summon summon = summonService.findSummonByUserId(user.getId());
        return summon.getCommissarSummoned();
    }
}
