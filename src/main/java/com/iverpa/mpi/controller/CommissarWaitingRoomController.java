package com.iverpa.mpi.controller;

import com.iverpa.mpi.controller.dto.requests.JoinRecruitRequest;
import com.iverpa.mpi.controller.dto.responses.QueueViewResponse;
import com.iverpa.mpi.dao.SummonService;
import com.iverpa.mpi.dao.UserService;
import com.iverpa.mpi.model.User;
import com.iverpa.mpi.service.WaitingRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/commissar/room")
@RequiredArgsConstructor
public class CommissarWaitingRoomController {

    private final WaitingRoomService waitingRoomService;
    private final UserService userService;
    private final SummonService summonService;

    @PostMapping("/add")
    public void sendRecruitToWaitingRoom(@RequestBody JoinRecruitRequest request) {
        User user = userService.findByUsername(request.username());
        waitingRoomService.send(user);
    }

    @GetMapping("/exists/{username}")
    public Boolean userExistsInWaitingRoom(@PathVariable String username) {
        User user = userService.findByUsername(username);
        return waitingRoomService.exists(user);
    }

    @GetMapping("/summoned")
    public List<QueueViewResponse> getSummonedUsers() {
        return summonService.findAllByCommissarSummoned();
    }
}
