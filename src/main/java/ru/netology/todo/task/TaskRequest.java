package ru.netology.todo.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskRequest(
        @NotBlank(message = "Title must not be blank")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,
        boolean completed
) {
}
