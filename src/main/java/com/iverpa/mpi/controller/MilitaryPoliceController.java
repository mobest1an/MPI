package com.iverpa.mpi.controller;

import com.iverpa.mpi.config.Passed;
import com.iverpa.mpi.controller.dto.responses.ActiveComplaintResponse;
import com.iverpa.mpi.controller.dto.responses.ComplaintGroupResponse;
import com.iverpa.mpi.dao.SummonService;
import com.iverpa.mpi.dao.UserService;
import com.iverpa.mpi.model.Complaint;
import com.iverpa.mpi.model.ComplaintStatus;
import com.iverpa.mpi.model.Convoy;
import com.iverpa.mpi.model.User;
import com.iverpa.mpi.security.AuthorizedUser;
import com.iverpa.mpi.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/military-police")
@RequiredArgsConstructor
@Passed
public class MilitaryPoliceController {

    private final ComplaintService complaintService;
    private final UserService userService;
    private final SummonService summonService;

    /**
     * Получить список жалоб, сгруппированных по конвоям
     */
    @GetMapping("/complaints")
    public List<ComplaintGroupResponse> getComplaints(@AuthenticationPrincipal AuthorizedUser authorizedUser) {
        User currentUser = userService.findByUsername(authorizedUser.username());
        List<Complaint> complaints = complaintService.getComplaintsForPolice();

        // Группируем по конвою
        Map<Convoy, List<Complaint>> groupedByConvoy = complaints.stream()
                .collect(Collectors.groupingBy(Complaint::getConvoy));

        return groupedByConvoy.entrySet().stream()
                .map(entry -> {
                    Convoy convoy = entry.getKey();
                    List<Complaint> convoyComplaints = entry.getValue();

                    int count = (int) convoyComplaints.stream()
                            .filter(c -> c.getStatus() == ComplaintStatus.NEW)
                            .count();

                    // Проверяем, взята ли жалоба другим пользователем
                    boolean takenByOther = convoyComplaints.stream()
                            .anyMatch(c -> c.getStatus() == ComplaintStatus.IN_PROGRESS
                                    && c.getAssignedTo() != null
                                    && !c.getAssignedTo().getId().equals(currentUser.getId()));

                    return new ComplaintGroupResponse(
                            convoy.getId(),
                            convoy.getEscort().getUsername(),
                            count,
                            takenByOther
                    );
                })
                .filter(r -> r.complaintsCount() > 0 || r.takenByOther())
                .toList();
    }

    /**
     * Получить активную жалобу текущего пользователя
     */
    @GetMapping("/complaints/active")
    public ActiveComplaintResponse getActiveComplaint(@AuthenticationPrincipal AuthorizedUser authorizedUser) {
        User currentUser = userService.findByUsername(authorizedUser.username());
        return complaintService.getActiveComplaint(currentUser)
                .map(complaint -> {
                    Convoy convoy = complaint.getConvoy();
                    List<String> recruitUsernames = summonService.findAllByConvoy(convoy).stream()
                            .map(summon -> summon.getUser().getUsername())
                            .toList();

                    return new ActiveComplaintResponse(
                            convoy.getId(),
                            convoy.getEscort().getUsername(),
                            recruitUsernames,
                            complaintService.getComplaintsCount(convoy)
                    );
                })
                .orElse(null);
    }

    /**
     * Взять жалобу в работу
     */
    @PostMapping("/complaints/{convoyId}/take")
    public void takeComplaint(
            @PathVariable Long convoyId,
            @AuthenticationPrincipal AuthorizedUser authorizedUser
    ) {
        User currentUser = userService.findByUsername(authorizedUser.username());
        complaintService.takeComplaint(convoyId, currentUser);
    }

    /**
     * Завершить жалобу
     */
    @PostMapping("/complaints/{convoyId}/complete")
    public void completeComplaint(
            @PathVariable Long convoyId,
            @AuthenticationPrincipal AuthorizedUser authorizedUser
    ) {
        User currentUser = userService.findByUsername(authorizedUser.username());
        complaintService.completeComplaint(convoyId, currentUser);
    }

    /**
     * Отменить жалобу (вернуть в NEW)
     */
    @PostMapping("/complaints/{convoyId}/cancel")
    public void cancelComplaint(
            @PathVariable Long convoyId,
            @AuthenticationPrincipal AuthorizedUser authorizedUser
    ) {
        User currentUser = userService.findByUsername(authorizedUser.username());
        complaintService.cancelComplaint(convoyId, currentUser);
    }
}
