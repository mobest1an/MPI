package com.iverpa.mpi.service;

import com.iverpa.mpi.dao.SummonService;
import com.iverpa.mpi.model.User;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;

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
}
