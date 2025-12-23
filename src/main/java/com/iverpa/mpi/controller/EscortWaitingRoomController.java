package com.iverpa.mpi.controller;

import com.iverpa.mpi.controller.dto.requests.CreateConvoyRequest;
import com.iverpa.mpi.controller.dto.responses.ConvoyResponse;
import com.iverpa.mpi.controller.dto.responses.WaitingRoomResponse;
import com.iverpa.mpi.dao.UserService;
import com.iverpa.mpi.model.User;
import com.iverpa.mpi.security.AuthorizedUser;
import com.iverpa.mpi.service.ConvoyService;
import com.iverpa.mpi.service.WaitingRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/escort")
@RequiredArgsConstructor
public class EscortWaitingRoomController {

    private final WaitingRoomService waitingRoomService;
    private final ConvoyService convoyService;
    private final UserService userService;

    /**
     * Получить список призывников в зале ожидания
     */
    @GetMapping("/room")
    public List<WaitingRoomResponse> getWaitingRoom() {
        return waitingRoomService.getWaitingRoom();
    }

    /**
     * Получить активный конвой текущего конвоира
     */
    @GetMapping("/convoy")
    public ConvoyResponse getActiveConvoy(@AuthenticationPrincipal AuthorizedUser authorizedUser) {
        User escort = userService.findByUsername(authorizedUser.username());
        return convoyService.getActiveConvoy(escort).orElse(null);
    }

    /**
     * Проверить, есть ли активный конвой
     */
    @GetMapping("/convoy/exists")
    public boolean hasActiveConvoy(@AuthenticationPrincipal AuthorizedUser authorizedUser) {
        User escort = userService.findByUsername(authorizedUser.username());
        return convoyService.hasActiveConvoy(escort);
    }

    /**
     * Создать конвой из выбранных призывников
     */
    @PostMapping("/convoy/create")
    public ConvoyResponse createConvoy(
            @AuthenticationPrincipal AuthorizedUser authorizedUser,
            @RequestBody CreateConvoyRequest request
    ) {
        User escort = userService.findByUsername(authorizedUser.username());
        return convoyService.createConvoy(escort, request.summonIds());
    }

    /**
     * Распустить конвой (призывники доставлены)
     */
    @PostMapping("/convoy/dismiss")
    public void dismissConvoy(@AuthenticationPrincipal AuthorizedUser authorizedUser) {
        User escort = userService.findByUsername(authorizedUser.username());
        convoyService.dismissConvoy(escort);
    }
}
