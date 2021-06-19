package za.co.learnings.todolist.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.learnings.todolist.api.controller.model.response.QuartzInformationResponse;
import za.co.learnings.todolist.api.controller.model.response.QuartzJobDetailResponse;
import za.co.learnings.todolist.api.controller.model.QuartzJobHistoryFilterDto;
import za.co.learnings.todolist.api.controller.model.request.TriggerRequest;
import za.co.learnings.todolist.api.controller.model.response.QuartzJobHistoryResponse;
import za.co.learnings.todolist.api.controller.model.response.QuartzResponse;
import za.co.learnings.todolist.api.exception.InvalidFieldException;
import za.co.learnings.todolist.api.exception.NotFoundException;
import za.co.learnings.todolist.api.service.QuartzSchedulerService;

import javax.transaction.Transactional;
import java.text.ParseException;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@Tag(name = "Quartz Scheduler")
@RestController
@ApiResponses(value = {
        @ApiResponse(responseCode ="200", description = "OK"),
        @ApiResponse(responseCode ="500", description = "Internal server error")
})
@RequestMapping("/api/scheduler")
@Slf4j
@Transactional
public class QuartzSchedulerController {

    private final QuartzSchedulerService quartzSchedulerService;

    public QuartzSchedulerController(QuartzSchedulerService quartzSchedulerService) {
        this.quartzSchedulerService = quartzSchedulerService;
    }

    @Operation(description = "Trigger a specific job")
    @PostMapping("/triggerJob")
    @ApiResponses(value = {
            @ApiResponse(responseCode ="400", description = "Bad Request"),
            @ApiResponse(responseCode ="404", description = "Not Found")
    })
    public void triggerJob(@RequestBody TriggerRequest request) throws SchedulerException, NotFoundException, InvalidFieldException, ParseException {
        quartzSchedulerService.triggerJob(request);
    }

    @Operation(description = "Retrieves general information about the Quartz scheduler")
    @GetMapping(value = "/jobs")
    public List<QuartzJobDetailResponse> getAllJobs() throws SchedulerException {
        return quartzSchedulerService.getAvailableJobs();
    }

    @Operation(description = "Retrieves general information about the Quartz scheduler")
    @GetMapping(value = "/jobs/{jobId}")
    public QuartzJobDetailResponse getJobById(@PathVariable String jobId) throws SchedulerException, NotFoundException {
        return quartzSchedulerService.getJobDetail(jobId);
    }

    @Operation(description = "Retrieves general information about the Quartz scheduler")
    @GetMapping(value = "/jobs/{jobId}/history")
    public Page<QuartzJobHistoryResponse> getJobHistory(@PathVariable String jobId, QuartzJobHistoryFilterDto paging) {
        return quartzSchedulerService.getJobHistory(jobId, paging);
    }


    @Operation(description = "Retrieves general information about the Quartz scheduler")
    @GetMapping(value = "/information")
    @ResponseBody
    @ResponseStatus(OK)
    public ResponseEntity<QuartzInformationResponse> getSchedulerInformation() {
        try {
            return new ResponseEntity<>(quartzSchedulerService.getSchedulerInformation(), OK);
        } catch (SchedulerException e) {
            return new ResponseEntity<>(BAD_REQUEST);
        }
    }

    @Operation(description = "Retrieves job key information from the Quartz scheduler")
    @GetMapping(value = "jobKeys")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<JobKey>> getJobKeys() {
        try {
            return new ResponseEntity<>(quartzSchedulerService.getJobKeys(), HttpStatus.OK);
        } catch (SchedulerException e) {
            return new ResponseEntity<>(BAD_REQUEST);
        }
    }

    @Operation(description = "For a given name and group, deletes the job/trigger(s) from the Quartz scheduler")
    @DeleteMapping(value = "/deleteJob")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<QuartzResponse> deleteJob(@RequestParam String name, @RequestParam String group) {
        try {
            return new ResponseEntity<>(quartzSchedulerService.deleteJobDetail(name, group), HttpStatus.ACCEPTED);
        } catch (SchedulerException e) {
            return new ResponseEntity<>(BAD_REQUEST);
        }
    }
}
