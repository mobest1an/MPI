package com.iverpa.mpi.dao;

import com.iverpa.mpi.dao.repository.SummonRepository;
import com.iverpa.mpi.model.Convoy;
import com.iverpa.mpi.model.RecruitStatus;
import com.iverpa.mpi.model.Summon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SummonService {

    private final SummonRepository summonRepository;

    public Summon findByUserId(Long userId) {
        return summonRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("Summon not found for user: " + userId));
    }

    public Summon findByUsername(String username) {
        return summonRepository.findByUserUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Summon not found for user: " + username));
    }

    public List<Summon> findAllByStatus(RecruitStatus status) {
        List<Summon> result = summonRepository.findAllByStatus(status);
        log.info("findAllByStatus({}): найдено {} записей", status, result.size());
        return result;
    }

    public List<Summon> findAllByConvoy(Convoy convoy) {
        return summonRepository.findAllByConvoy(convoy);
    }

    public List<Summon> findAllByIdsAndStatus(List<Long> ids, RecruitStatus status) {
        return summonRepository.findAllByIdInAndStatus(ids, status);
    }

    public Summon updateStatus(Long userId, RecruitStatus status) {
        Summon summon = findByUserId(userId);
        summon.setStatus(status);
        return summonRepository.save(summon);
    }

    public Summon updateStatus(Summon summon, RecruitStatus status) {
        summon.setStatus(status);
        return summonRepository.save(summon);
    }

    public Summon save(Summon summon) {
        return summonRepository.save(summon);
    }

    public void saveAll(List<Summon> summons) {
        summonRepository.saveAll(summons);
    }
}
