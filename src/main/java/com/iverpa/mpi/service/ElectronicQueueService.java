package com.iverpa.mpi.service;

import com.iverpa.mpi.controller.dto.responses.QueueViewResponse;
import com.iverpa.mpi.dao.SummonService;
import com.iverpa.mpi.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ElectronicQueueService {

    private final SummonService summonService;
    private final ConcurrentLinkedQueue<User> queue = new ConcurrentLinkedQueue<>();

    @SneakyThrows
    public void join(User user) {
        try {
            summonService.findSummonByUserId(user.getId());
        } catch (NoSuchElementException e) {
            throw new IllegalAccessException("Cannot join queue without summon");
        }
        if (!queue.contains(user)) {
            queue.add(user);
        }
    }

    public Queue<QueueViewResponse> getQueue() {
        return queue.stream().map(
                QueueViewResponse::new
        ).collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
    }

    @Transactional
    public void delete(User user) {
        summonService.updateSummonStatus(user.getId(), true);
        queue.remove(user);
    }
}
