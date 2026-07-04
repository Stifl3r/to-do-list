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
import za.co.learnings.todolist.api.controller.model.request.CronTriggerRequest;
import za.co.learnings.todolist.api.controller.model.response.QuartzTriggerResponse;
import za.co.learnings.todolist.api.exception.InvalidFieldException;
import za.co.learnings.todolist.api.exception.NotFoundException;
import za.co.learnings.todolist.api.service.QuartzSchedulerService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
public class QuartzTriggerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QuartzSchedulerService quartzSchedulerService;

    // Regression test: the class-level @RequestMapping used to declare {id} while
    // getAllJobTriggers bound the parameter as jobId, causing a MissingPathVariableException
    // (500) on every call to this endpoint.
    @Test
    public void getAllJobTriggersShouldReturnOk() throws Exception {
        var trigger = new QuartzTriggerResponse();
        trigger.setName("trigger-1");
        trigger.setGroup("TestFibonacciJob_Group");
        trigger.setTriggerType("CRON");

        given(quartzSchedulerService.getAllJobTriggers("TestFibonacciJob"))
                .willReturn(List.of(trigger));

        this.mockMvc.perform(get("/api/scheduler/jobs/TestFibonacciJob/triggers"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("trigger-1"))
                .andExpect(jsonPath("$[0].triggerType").value("CRON"));
    }

    @Test
    public void getAllJobTriggersWhenJobDoesNotExistShouldReturnNotFound() throws Exception {
        given(quartzSchedulerService.getAllJobTriggers("NoSuchJob"))
                .willThrow(new NotFoundException("Specified Job Id does not exist"));

        this.mockMvc.perform(get("/api/scheduler/jobs/NoSuchJob/triggers"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].message").value("Specified Job Id does not exist"));
    }

    @Test
    public void editTriggerShouldReturnOk() throws Exception {
        var request = new CronTriggerRequest();
        request.setName("trigger-1");
        request.setGroup("TestFibonacciJob_Group");
        request.setCronExpression("0 30 10 * * ?");
        request.setDescription("updated");

        willDoNothing().given(quartzSchedulerService).editTrigger(any(CronTriggerRequest.class));

        this.mockMvc.perform(patch("/api/scheduler/jobs/TestFibonacciJob/triggers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void editTriggerWhenCronExpressionIsInvalidShouldReturnBadRequest() throws Exception {
        var request = new CronTriggerRequest();
        request.setName("trigger-1");
        request.setGroup("TestFibonacciJob_Group");
        request.setCronExpression("not-a-cron");

        willThrow(new InvalidFieldException("Incorrect Cron Expression has been provided", -1))
                .given(quartzSchedulerService).editTrigger(any(CronTriggerRequest.class));

        this.mockMvc.perform(patch("/api/scheduler/jobs/TestFibonacciJob/triggers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message").value("Incorrect Cron Expression has been provided"));
    }

    @Test
    public void pauseTriggerShouldReturnOk() throws Exception {
        var request = new CronTriggerRequest();
        request.setName("trigger-1");
        request.setGroup("TestFibonacciJob_Group");

        willDoNothing().given(quartzSchedulerService).pauseActiveTrigger(any(CronTriggerRequest.class));

        this.mockMvc.perform(post("/api/scheduler/jobs/TestFibonacciJob/triggers/pause")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void resumeTriggerShouldReturnOk() throws Exception {
        var request = new CronTriggerRequest();
        request.setName("trigger-1");
        request.setGroup("TestFibonacciJob_Group");

        willDoNothing().given(quartzSchedulerService).resumePausedTrigger(any(CronTriggerRequest.class));

        this.mockMvc.perform(post("/api/scheduler/jobs/TestFibonacciJob/triggers/resume")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void resumeTriggerWhenTriggerDoesNotExistShouldReturnNotFound() throws Exception {
        var request = new CronTriggerRequest();
        request.setName("no-such-trigger");
        request.setGroup("TestFibonacciJob_Group");

        willThrow(new NotFoundException("Specified trigger does not exist"))
                .given(quartzSchedulerService).resumePausedTrigger(any(CronTriggerRequest.class));

        this.mockMvc.perform(post("/api/scheduler/jobs/TestFibonacciJob/triggers/resume")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].message").value("Specified trigger does not exist"));
    }

    @Test
    public void deleteTriggerShouldReturnOk() throws Exception {
        willDoNothing().given(quartzSchedulerService).deleteTrigger("trigger-1", "TestFibonacciJob_Group");

        this.mockMvc.perform(delete("/api/scheduler/jobs/TestFibonacciJob/triggers")
                        .param("name", "trigger-1")
                        .param("group", "TestFibonacciJob_Group"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
