package com.iverpa.mpi.dao.repository;

import com.iverpa.mpi.model.Convoy;
import com.iverpa.mpi.model.RecruitStatus;
import com.iverpa.mpi.model.Summon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SummonRepository extends JpaRepository<Summon, Long> {

    Optional<Summon> findByUserId(Long userId);

    Optional<Summon> findByUserUsername(String username);

    List<Summon> findAllByStatus(RecruitStatus status);

    List<Summon> findAllByConvoy(Convoy convoy);

    List<Summon> findAllByIdInAndStatus(List<Long> ids, RecruitStatus status);
}
