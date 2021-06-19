package za.co.learnings.todolist.api.controller.model.response;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import za.co.learnings.todolist.api.repository.entity.QuartzJobHistory;

import java.time.LocalDateTime;

import static za.co.learnings.todolist.api.util.DateUtil.toLocalDateTime;


@Setter
@Getter
public class QuartzJobHistoryResponse {
    private String jobName;
    private String jobGroup;
    private String jobDescription;
    private Long jobRunTime;
    private String triggerName;
    private String triggerGroup;
    private String triggerDescription;
    private String triggerType;
    private String result;
    private LocalDateTime scheduledFireTime;
    private LocalDateTime fireTime;
    private LocalDateTime previousFireTime;
    private LocalDateTime nextFireTime;

    public static QuartzJobHistoryResponse mapToResponse(@NonNull QuartzJobHistory job) {
        var newJob = new QuartzJobHistoryResponse();
        newJob.setJobName(job.getJobName());
        newJob.setJobGroup(job.getJobGroup());
        newJob.setJobDescription(job.getJobDescription());
        newJob.setJobRunTime(job.getJobRunTime());
        newJob.setTriggerName(job.getTriggerName());
        newJob.setTriggerGroup(job.getTriggerGroup());
        newJob.setTriggerDescription(job.getTriggerDescription());
        newJob.setTriggerType(job.getTriggerType());
        newJob.setResult(job.getResult());
        newJob.setScheduledFireTime(toLocalDateTime(job.getScheduledFireTime()));
        newJob.setFireTime(toLocalDateTime(job.getFireTime()));
        newJob.setPreviousFireTime(toLocalDateTime(job.getPreviousFireTime()));
        newJob.setNextFireTime(toLocalDateTime(job.getNextFireTime()));

        return newJob;
    }
}
