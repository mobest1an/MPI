package com.iverpa.mpi.controller;

import com.iverpa.mpi.controller.dto.responses.QueueViewResponse;
import com.iverpa.mpi.service.WaitingRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Queue;

/**
 * @author erik.karapetyan
 */
@RestController
@RequestMapping("/api/v1/escort/room")
@RequiredArgsConstructor
public class EscortWaitingRoomController {

    private final WaitingRoomService waitingRoomService;

    @GetMapping
    public Queue<QueueViewResponse> getWaitingRoom() {
        return waitingRoomService.getWaitingRoom();
    }

    @PostMapping("/delete")
    public void removeFromWaitingRoom() {
        waitingRoomService.remove();
    }
}
