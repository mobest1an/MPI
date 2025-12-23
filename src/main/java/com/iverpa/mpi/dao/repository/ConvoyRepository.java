package com.iverpa.mpi.dao.repository;

import com.iverpa.mpi.model.Convoy;
import com.iverpa.mpi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConvoyRepository extends JpaRepository<Convoy, Long> {

    Optional<Convoy> findByEscort(User escort);

    boolean existsByEscort(User escort);
}
