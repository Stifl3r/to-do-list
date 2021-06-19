package za.co.learnings.todolist.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.web.bind.annotation.*;
import za.co.learnings.todolist.api.controller.model.response.QuartzTriggerResponse;
import za.co.learnings.todolist.api.controller.model.request.CronTriggerRequest;
import za.co.learnings.todolist.api.exception.InvalidFieldException;
import za.co.learnings.todolist.api.exception.NotFoundException;
import za.co.learnings.todolist.api.service.QuartzSchedulerService;

import javax.transaction.Transactional;
import java.text.ParseException;
import java.util.List;

@Tag(name = "Quartz Triggers ")
@RestController
@ApiResponses(value = {
        @ApiResponse(responseCode ="200", description = "OK"),
        @ApiResponse(responseCode ="500", description = "Internal server error")
})
@RequestMapping("/api/scheduler/jobs/{id}/triggers")
@Slf4j
@Transactional
public class QuartzTriggerController {

    private final QuartzSchedulerService quartzSchedulerService;

    public QuartzTriggerController(QuartzSchedulerService quartzSchedulerService) {
        this.quartzSchedulerService = quartzSchedulerService;
    }

    @Operation(description = "Retrieves triggers for specific job")
    @GetMapping()
    public List<QuartzTriggerResponse> getAllJobTriggers(@PathVariable String jobId) throws SchedulerException, NotFoundException {
        var triggers = quartzSchedulerService.getAllJobTriggers(jobId);
        return triggers;
    }

    @Operation(description = "Edits existing trigger")
    @PatchMapping()
    public void editTrigger(@RequestBody CronTriggerRequest request) throws SchedulerException, ParseException, NotFoundException, InvalidFieldException {
        quartzSchedulerService.editTrigger(request);
    }

    @Operation(description = "Pause an active trigger")
    @PostMapping("/pause")
    public void pauseTrigger(@RequestBody CronTriggerRequest request) throws SchedulerException, NotFoundException {
        quartzSchedulerService.pauseActiveTrigger(request);
    }

    @Operation(description = "Resume a paused trigger")
    @PostMapping("/resume")
    public void resumeTrigger(@RequestBody CronTriggerRequest request) throws SchedulerException, NotFoundException {
        quartzSchedulerService.resumePausedTrigger(request);
    }

    @Operation(description = "Delete a trigger")
    @DeleteMapping()
    public void deleteTrigger(@RequestParam String name, @RequestParam String group) throws SchedulerException, NotFoundException {
        quartzSchedulerService.deleteTrigger(name, group);
    }
}
