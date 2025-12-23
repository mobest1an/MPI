package com.iverpa.mpi.controller;

import com.iverpa.mpi.controller.dto.requests.DeleteFromQueueRequest;
import com.iverpa.mpi.controller.dto.responses.QueueViewResponse;
import com.iverpa.mpi.controller.dto.responses.SummonedRecruitResponse;
import com.iverpa.mpi.dao.UserService;
import com.iverpa.mpi.model.Summon;
import com.iverpa.mpi.model.User;
import com.iverpa.mpi.service.ElectronicQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/commissar/queue")
@RequiredArgsConstructor
public class CommissarElectronicQueueController {

    private final ElectronicQueueService electronicQueueService;
    private final UserService userService;

    /**
     * Получить очередь призывников
     */
    @GetMapping
    public List<QueueViewResponse> getQueue() {
        return electronicQueueService.getQueue();
    }

    /**
     * Вызвать призывника из очереди
     */
    @PostMapping("/summon")
    public void summonRecruit(@RequestBody DeleteFromQueueRequest request) {
        User user = userService.findByUsername(request.username());
        electronicQueueService.summon(user);
    }

    /**
     * Получить текущего вызванного призывника
     */
    @GetMapping("/current")
    public SummonedRecruitResponse getCurrentSummoned() {
        Summon summon = electronicQueueService.getCurrentSummoned();
        if (summon == null) {
            return null;
        }
        return new SummonedRecruitResponse(
                summon.getId(),
                summon.getUser().getUsername()
        );
    }

    /**
     * Проверить, есть ли вызванный призывник
     */
    @GetMapping("/has-summoned")
    public boolean hasSummonedRecruit() {
        return electronicQueueService.hasSummonedRecruit();
    }
}
