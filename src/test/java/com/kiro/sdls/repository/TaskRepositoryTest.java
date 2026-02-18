package com.kiro.sdls.repository;

import com.kiro.sdls.model.Task;
import com.kiro.sdls.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskRepositoryTest {

    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository = new TaskRepository();
    }

    @Test
    void testSaveAndFindById() {
        UUID id = UUID.randomUUID();
        Task task = new Task(id, "Test Task", "Description", TaskStatus.PENDING, LocalDateTime.now());

        taskRepository.save(task);

        Optional<Task> found = taskRepository.findById(id);
        assertTrue(found.isPresent());
        assertEquals("Test Task", found.get().getTitle());
        assertEquals("Description", found.get().getDescription());
    }

    @Test
    void testFindByIdNotFound() {
        UUID id = UUID.randomUUID();
        Optional<Task> found = taskRepository.findById(id);
        assertTrue(found.isEmpty());
    }
}
