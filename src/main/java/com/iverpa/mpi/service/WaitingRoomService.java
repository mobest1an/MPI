package com.iverpa.mpi.service;

import com.iverpa.mpi.controller.dto.responses.WaitingRoomResponse;
import com.iverpa.mpi.dao.SummonService;
import com.iverpa.mpi.model.RecruitStatus;
import com.iverpa.mpi.model.Summon;
import com.iverpa.mpi.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WaitingRoomService {

    private final SummonService summonService;

    /**
     * Комиссар отправляет призывника в зал ожидания конвоирования
     */
    @Transactional
    public void sendToWaitingRoom(User user, String militaryBranch) {
        Summon summon = summonService.findByUserId(user.getId());
        
        if (summon.getStatus() != RecruitStatus.SUMMONED) {
            throw new IllegalStateException("Призывник не вызван комиссаром");
        }
        
        summon.setMilitaryBranch(militaryBranch);
        summon.setStatus(RecruitStatus.WAITING_ESCORT);
        summonService.save(summon);
    }

    /**
     * Проверить, находится ли призывник в зале ожидания или дальше
     */
    public boolean exists(User user) {
        Summon summon = summonService.findByUserId(user.getId());
        return summon.getStatus() == RecruitStatus.WAITING_ESCORT 
            || summon.getStatus() == RecruitStatus.IN_CONVOY
            || summon.getStatus() == RecruitStatus.DONE;
    }

    /**
     * Получить список призывников в зале ожидания конвоирования
     */
    public List<WaitingRoomResponse> getWaitingRoom() {
        return summonService.findAllByStatus(RecruitStatus.WAITING_ESCORT).stream()
                .map(summon -> new WaitingRoomResponse(
                        summon.getId(),
                        summon.getUser().getUsername(),
                        summon.getMilitaryBranch()
                ))
                .toList();
    }
}
