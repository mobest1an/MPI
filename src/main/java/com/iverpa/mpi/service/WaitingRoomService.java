package com.iverpa.mpi.service;

import com.iverpa.mpi.controller.dto.responses.QueueViewResponse;
import com.iverpa.mpi.model.User;
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
