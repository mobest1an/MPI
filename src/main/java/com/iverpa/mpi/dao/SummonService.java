package com.iverpa.mpi.dao;

import com.iverpa.mpi.dao.repository.SummonRepository;
import com.iverpa.mpi.model.Summon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SummonService {

    private final SummonRepository summonRepository;

    public Summon findSummonByUserId(Long userId) {
        return summonRepository.findSummonByUserId(userId).orElseThrow();
    }
}
