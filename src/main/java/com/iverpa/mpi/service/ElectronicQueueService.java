package com.iverpa.mpi.service;

import com.iverpa.mpi.controller.dto.responses.QueueViewResponse;
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
public class ElectronicQueueService {

    private final SummonService summonService;

    /**
     * Призывник встаёт в очередь к комиссару
     */
    @Transactional
    public void join(User user) {
        Summon summon = summonService.findByUserId(user.getId());
        
        if (summon.getStatus() != RecruitStatus.NOT_STARTED) {
            throw new IllegalStateException("Призывник уже в очереди или прошёл этот этап");
        }
        
        summon.setStatus(RecruitStatus.IN_QUEUE);
        summonService.save(summon);
    }

    /**
     * Комиссар вызывает призывника из очереди
     */
    @Transactional
    public void summon(User user) {
        Summon summon = summonService.findByUserId(user.getId());
        
        if (summon.getStatus() != RecruitStatus.IN_QUEUE) {
            throw new IllegalStateException("Призывник не в очереди");
        }
        
        summon.setStatus(RecruitStatus.SUMMONED);
        summonService.save(summon);
    }

    /**
     * Получить очередь к комиссару
     */
    public List<QueueViewResponse> getQueue() {
        return summonService.findAllByStatus(RecruitStatus.IN_QUEUE).stream()
                .map(summon -> new QueueViewResponse(summon.getUser()))
                .toList();
    }

    /**
     * Получить текущего вызванного призывника (если есть)
     */
    public Summon getCurrentSummoned() {
        List<Summon> summoned = summonService.findAllByStatus(RecruitStatus.SUMMONED);
        return summoned.isEmpty() ? null : summoned.get(0);
    }

    /**
     * Проверить, есть ли вызванный призывник
     */
    public boolean hasSummonedRecruit() {
        return !summonService.findAllByStatus(RecruitStatus.SUMMONED).isEmpty();
    }

    /**
     * Проверить готовность комиссара принять призывника
     */
    public boolean isCommissarReady(String username) {
        Summon summon = summonService.findByUsername(username);
        // Комиссар готов, если призывник вызван (SUMMONED)
        return summon.getStatus() == RecruitStatus.SUMMONED;
    }

    /**
     * Призывник выходит из очереди
     */
    @Transactional
    public void leave(User user) {
        Summon summon = summonService.findByUserId(user.getId());

        if (summon.getStatus() != RecruitStatus.IN_QUEUE) {
            throw new IllegalStateException("Призывник не в очереди");
        }

        summon.setStatus(RecruitStatus.NOT_STARTED);
        summonService.save(summon);
    }

    /**
     * Комиссар отклоняет призывника (возвращает в начало)
     */
    @Transactional
    public void reject(User user) {
        Summon summon = summonService.findByUserId(user.getId());

        if (summon.getStatus() != RecruitStatus.SUMMONED) {
            throw new IllegalStateException("Призывник не вызван комиссаром");
        }

        summon.setStatus(RecruitStatus.NOT_STARTED);
        summon.setMilitaryBranch(null);
        summonService.save(summon);
    }

    /**
     * Получить статус призывника
     */
    public Summon getRecruitSummon(User user) {
        return summonService.findByUserId(user.getId());
    }
}
