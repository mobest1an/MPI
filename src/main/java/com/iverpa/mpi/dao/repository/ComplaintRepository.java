package com.iverpa.mpi.dao.repository;

import com.iverpa.mpi.model.Complaint;
import com.iverpa.mpi.model.ComplaintStatus;
import com.iverpa.mpi.model.Convoy;
import com.iverpa.mpi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findAllByConvoy(Convoy convoy);

    List<Complaint> findAllByStatus(ComplaintStatus status);

    List<Complaint> findAllByStatusIn(List<ComplaintStatus> statuses);

    Optional<Complaint> findByAssignedToAndStatus(User assignedTo, ComplaintStatus status);

    int countByConvoy(Convoy convoy);

    void deleteAllByConvoy(Convoy convoy);
}
