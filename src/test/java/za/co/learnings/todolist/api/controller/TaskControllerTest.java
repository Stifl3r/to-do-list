package za.co.learnings.todolist.api.controller;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import za.co.learnings.todolist.api.controller.model.TaskModel;
import za.co.learnings.todolist.api.controller.model.request.TaskCreateRequest;
import za.co.learnings.todolist.api.service.TaskService;
import za.co.learnings.todolist.api.testmodel.TaskBuilder;
import za.co.learnings.todolist.api.testmodel.TaskCreateRequestBuilder;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static za.co.learnings.todolist.api.testmodel.TaskBuilder.aTask;
import static za.co.learnings.todolist.api.testmodel.TaskCreateRequestBuilder.aTaskCreateRequest;

@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    @Test
    public void getListOfTasksShouldReturnTaskList() throws Exception {
        var expected = aTask().build();

        given(taskService.getAllTasks(null))
                .willReturn(List.of(new TaskModel(expected)));

        this.mockMvc.perform(get("/api/tasks"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void getTaskByIdShouldReturnTaskModel() throws Exception {
        var expected = aTask().build();

        given(taskService.getTaskById(1))
                .willReturn(new TaskModel(expected));

        var actual = this.mockMvc.perform(get("/api/tasks/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        var actualResponseBody = actual.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(new TaskModel(expected)));
    }

    @Test
    public void createTaskShouldReturnSuccess() throws Exception {
        var request = aTaskCreateRequest().build();

        var expected = aTask().build();

        given(taskService.createTask(any(TaskCreateRequest.class)))
                .willReturn(new TaskModel(expected));

        var response = this.mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        assertThat(response.getContentAsString()).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(new TaskModel(expected)));
    }
}
