package ru.netology.todo.task;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(long id) {
        super("Task with id %d was not found".formatted(id));
    }
}
