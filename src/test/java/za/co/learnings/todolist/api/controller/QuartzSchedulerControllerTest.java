package za.co.learnings.todolist.api.controller;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import za.co.learnings.todolist.api.controller.model.response.QuartzJobDetailResponse;
import za.co.learnings.todolist.api.controller.model.response.QuartzJobHistoryResponse;
import za.co.learnings.todolist.api.controller.model.request.TriggerRequest;
import za.co.learnings.todolist.api.controller.model.response.QuartzResponse;
import za.co.learnings.todolist.api.exception.InvalidFieldException;
import za.co.learnings.todolist.api.exception.NotFoundException;
import za.co.learnings.todolist.api.service.QuartzSchedulerService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
public class QuartzSchedulerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QuartzSchedulerService quartzSchedulerService;

    @Test
    public void triggerJobShouldReturnOk() throws Exception {
        var request = new TriggerRequest();
        request.setJobId("TestFibonacciJob");
        request.setInterval("fireNow");

        willDoNothing().given(quartzSchedulerService).triggerJob(any(TriggerRequest.class));

        this.mockMvc.perform(post("/api/scheduler/triggerJob")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    // Regression test: GenericControllerAdvice previously excluded this controller,
    // so this exception surfaced as a bare 500 instead of a structured 400.
    @Test
    public void triggerJobWhenInvalidFieldExceptionThrownShouldReturnBadRequest() throws Exception {
        var request = new TriggerRequest();
        request.setJobId("TestFibonacciJob");
        request.setInterval("not-a-real-interval");

        willThrow(new InvalidFieldException("Invalid interval provided", -1))
                .given(quartzSchedulerService).triggerJob(any(TriggerRequest.class));

        this.mockMvc.perform(post("/api/scheduler/triggerJob")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message").value("Invalid interval provided"));
    }

    // Regression test: same GenericControllerAdvice gap, for the not-found path.
    @Test
    public void triggerJobWhenNotFoundExceptionThrownShouldReturnNotFound() throws Exception {
        var request = new TriggerRequest();
        request.setJobId("NoSuchJob");
        request.setInterval("fireNow");

        willThrow(new NotFoundException("Selected jobId does not exist", -1))
                .given(quartzSchedulerService).triggerJob(any(TriggerRequest.class));

        this.mockMvc.perform(post("/api/scheduler/triggerJob")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].message").value("Selected jobId does not exist"));
    }

    @Test
    public void getAllJobsShouldReturnOk() throws Exception {
        var job = new QuartzJobDetailResponse();
        job.setName("job-1");
        job.setGroup("TestFibonacciJob");

        given(quartzSchedulerService.getAvailableJobs())
                .willReturn(List.of(job));

        this.mockMvc.perform(get("/api/scheduler/jobs"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].group").value("TestFibonacciJob"));
    }

    @Test
    public void getJobByIdShouldReturnOk() throws Exception {
        var job = new QuartzJobDetailResponse();
        job.setName("job-1");
        job.setGroup("TestFibonacciJob");

        given(quartzSchedulerService.getJobDetail("TestFibonacciJob"))
                .willReturn(job);

        this.mockMvc.perform(get("/api/scheduler/jobs/TestFibonacciJob"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.group").value("TestFibonacciJob"));
    }

    // Regression test: path variable used to be {id} while the method parameter was
    // bound as jobId, causing MissingPathVariableException (500) on every call.
    @Test
    public void getJobHistoryShouldReturnOkWhenPagingParamsAreOmitted() throws Exception {
        var historyResponse = new QuartzJobHistoryResponse();
        historyResponse.setJobGroup("TestFibonacciJob");

        given(quartzSchedulerService.getJobHistory(eq("TestFibonacciJob"), any()))
                .willReturn(new PageImpl<>(List.of(historyResponse)));

        this.mockMvc.perform(get("/api/scheduler/jobs/TestFibonacciJob/history"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].jobGroup").value("TestFibonacciJob"));
    }

    @Test
    public void getJobHistoryShouldReturnOkWhenPagingParamsAreProvided() throws Exception {
        given(quartzSchedulerService.getJobHistory(eq("TestFibonacciJob"), any()))
                .willReturn(new PageImpl<>(Collections.emptyList()));

        this.mockMvc.perform(get("/api/scheduler/jobs/TestFibonacciJob/history")
                        .param("pageIndex", "0")
                        .param("pageSize", "5"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void getJobKeysShouldReturnOk() throws Exception {
        given(quartzSchedulerService.getJobKeys())
                .willReturn(List.of(new JobKey("job-1", "TestFibonacciJob")));

        this.mockMvc.perform(get("/api/scheduler/jobKeys"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].group").value("TestFibonacciJob"));
    }

    @Test
    public void deleteJobShouldReturnAccepted() throws Exception {
        var response = new QuartzResponse();
        response.setType(QuartzResponse.ResponseType.DELETE);
        response.setName("job-1");
        response.setGroup("TestFibonacciJob");
        response.setResult(true);

        given(quartzSchedulerService.deleteJobDetail("job-1", "TestFibonacciJob"))
                .willReturn(response);

        this.mockMvc.perform(delete("/api/scheduler/deleteJob")
                        .param("name", "job-1")
                        .param("group", "TestFibonacciJob"))
                .andDo(print())
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.group").value("TestFibonacciJob"));
    }
}
