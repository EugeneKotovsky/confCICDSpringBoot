package ru.netology.todo.task;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TaskService {

    private final AtomicLong nextId = new AtomicLong();
    private final ConcurrentMap<Long, Task> tasks = new ConcurrentHashMap<>();

    public Task create(TaskRequest request) {
        long id = nextId.incrementAndGet();
        Task task = new Task(id, request.title(), request.description(), request.completed());
        tasks.put(id, task);
        return task;
    }

    public List<Task> findAll() {
        return tasks.values().stream()
                .sorted(Comparator.comparingLong(Task::id))
                .toList();
    }

    public Task update(long id, TaskRequest request) {
        findById(id);
        Task updatedTask = new Task(id, request.title(), request.description(), request.completed());
        tasks.put(id, updatedTask);
        return updatedTask;
    }

    public void delete(long id) {
        if (tasks.remove(id) == null) {
            throw new TaskNotFoundException(id);
        }
    }

    private Task findById(long id) {
        Task task = tasks.get(id);
        if (task == null) {
            throw new TaskNotFoundException(id);
        }
        return task;
    }
}
