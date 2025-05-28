package com.iverpa.mpi.service;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import com.iverpa.mpi.controller.dto.responses.QueueViewResponse;
import com.iverpa.mpi.model.Role;
import com.iverpa.mpi.model.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * @author erik.karapetyan
 */
@Service
@RequiredArgsConstructor
public class WaitingRoomService {

    private final ConcurrentLinkedQueue<User> queue = new ConcurrentLinkedQueue<>();

    @PostConstruct
    public void init() {
        queue.add(new User(
           3L,
           "test",
           "test",
                Set.of(Role.RECRUIT)
        ));
    public void send(User user) {
        if (!queue.contains(user)) {
            queue.add(user);
        }
    }

    public void remove() {
        queue.clear();
    }

    public Queue<QueueViewResponse> getWaitingRoom() {
        return queue.stream().map(
                QueueViewResponse::new
        ).collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
    }
}
