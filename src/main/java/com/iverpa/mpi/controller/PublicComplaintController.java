package com.iverpa.mpi.controller;

import com.iverpa.mpi.controller.dto.requests.SubmitComplaintRequest;
import com.iverpa.mpi.controller.dto.responses.ActiveConvoyResponse;
import com.iverpa.mpi.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicComplaintController {

    private final ComplaintService complaintService;

    /**
     * Получить список активных конвоев (для анонимных пользователей)
     */
    @GetMapping("/convoys")
    public List<ActiveConvoyResponse> getActiveConvoys() {
        return complaintService.getActiveConvoys().stream()
                .map(convoy -> new ActiveConvoyResponse(
                        convoy.getId(),
                        convoy.getEscort().getUsername()
                ))
                .toList();
    }

    /**
     * Подать жалобу на конвой (анонимно)
     */
    @PostMapping("/complaint")
    public void submitComplaint(@RequestBody SubmitComplaintRequest request) {
        complaintService.submitComplaint(request.convoyId());
    }
}
