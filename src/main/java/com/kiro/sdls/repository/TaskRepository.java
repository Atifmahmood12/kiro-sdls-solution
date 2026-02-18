package com.kiro.sdls.repository;

import com.kiro.sdls.model.Task;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class TaskRepository {

    private final ConcurrentHashMap<UUID, Task> tasks = new ConcurrentHashMap<>();

    public Task save(Task task) {
        tasks.put(task.getId(), task);
        return task;
    }

    public Optional<Task> findById(UUID id) {
        return Optional.ofNullable(tasks.get(id));
    }
}
