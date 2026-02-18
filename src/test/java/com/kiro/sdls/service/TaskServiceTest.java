package com.kiro.sdls.service;

import com.kiro.sdls.model.Task;
import com.kiro.sdls.model.TaskStatus;
import com.kiro.sdls.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskServiceTest {

    private TaskRepository taskRepository;
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskRepository = new TaskRepository();
        taskService = new TaskService(taskRepository);
    }

    @Test
    void testCreateTask() {
        Task result = taskService.createTask("Test Task", "Test Description");

        assertNotNull(result.getId());
        assertEquals("Test Task", result.getTitle());
        assertEquals("Test Description", result.getDescription());
        assertEquals(TaskStatus.PENDING, result.getStatus());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void testGetTaskById() {
        Task created = taskService.createTask("Test Task", "Test Description");

        Task result = taskService.getTaskById(created.getId());

        assertEquals(created.getId(), result.getId());
        assertEquals("Test Task", result.getTitle());
    }

    @Test
    void testGetTaskByIdNotFound() {
        UUID id = UUID.randomUUID();

        assertThrows(NoSuchElementException.class, () -> taskService.getTaskById(id));
    }
}
