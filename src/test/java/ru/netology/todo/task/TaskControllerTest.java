package ru.netology.todo.task;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsAndReturnsTasks() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Buy milk","description":"Two litres","completed":false}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/tasks/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.completed").value(false));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Buy milk"))
                .andExpect(jsonPath("$[0].description").value("Two litres"));
    }

    @Test
    void updatesAnExistingTask() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" + "\"title\":\"Read book\",\"completed\":false}"))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Read Spring Boot","description":"Chapter 3","completed":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Read Spring Boot"))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void deletesAnExistingTaskAndReturnsNotFoundForSecondDeletion() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" + "\"title\":\"Remove me\",\"completed\":false}"))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/tasks/1"))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/tasks/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", containsString("Task with id 1")));
    }

    @Test
    void rejectsAnInvalidTask() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" + "\"title\":\"   \",\"completed\":false}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundWhenUpdatingMissingTask() throws Exception {
        mockMvc.perform(put("/tasks/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" + "\"title\":\"Unknown\",\"completed\":false}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", containsString("Task with id 42")));
    }
}
