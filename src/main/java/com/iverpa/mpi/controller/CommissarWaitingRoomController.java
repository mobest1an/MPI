package com.iverpa.mpi.controller;

import com.iverpa.mpi.controller.dto.requests.SendToWaitingRoomRequest;
import com.iverpa.mpi.dao.UserService;
import com.iverpa.mpi.model.User;
import com.iverpa.mpi.service.WaitingRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/commissar/room")
@RequiredArgsConstructor
public class CommissarWaitingRoomController {

    private final WaitingRoomService waitingRoomService;
    private final UserService userService;

    /**
     * Отправить призывника в зал ожидания конвоирования
     */
    @PostMapping("/send")
    public void sendRecruitToWaitingRoom(@RequestBody SendToWaitingRoomRequest request) {
        User user = userService.findByUsername(request.username());
        waitingRoomService.sendToWaitingRoom(user, request.militaryBranch());
    }

    /**
     * Проверить, находится ли призывник в зале ожидания или дальше
     */
    @GetMapping("/exists/{username}")
    public Boolean userExistsInWaitingRoom(@PathVariable String username) {
        User user = userService.findByUsername(username);
        return waitingRoomService.exists(user);
    }
}
