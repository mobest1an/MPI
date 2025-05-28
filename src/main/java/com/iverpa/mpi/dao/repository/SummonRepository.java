package com.iverpa.mpi.dao.repository;

import com.iverpa.mpi.model.Summon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SummonRepository extends JpaRepository<Summon, Long> {

    Optional<Summon> findSummonByUserId(Long userId);
}
