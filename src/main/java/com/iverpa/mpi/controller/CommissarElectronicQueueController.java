package com.iverpa.mpi.controller;

import com.iverpa.mpi.controller.dto.requests.DeleteFromQueueRequest;
import com.iverpa.mpi.controller.dto.responses.QueueViewResponse;
import com.iverpa.mpi.dao.UserService;
import com.iverpa.mpi.model.User;
import com.iverpa.mpi.service.ElectronicQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Queue;

@RestController
@RequestMapping("/api/v1/commissar/queue")
@RequiredArgsConstructor
public class CommissarElectronicQueueController {

    private final ElectronicQueueService electronicQueueService;
    private final UserService userService;

    @GetMapping
    public Queue<QueueViewResponse> getQueue() {
        return electronicQueueService.getQueue();
    }

    @PostMapping("/delete")
    public void deleteFromQueue(@RequestBody DeleteFromQueueRequest request) {
        User user = userService.findByUsername(request.username());
        electronicQueueService.delete(user);
    }
}
