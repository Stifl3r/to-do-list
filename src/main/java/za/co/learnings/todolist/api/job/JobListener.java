package za.co.learnings.todolist.api.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.stereotype.Component;
import za.co.learnings.todolist.api.controller.model.QuartzJobContextDto;
import za.co.learnings.todolist.api.repository.entity.QuartzJobHistory;

@Component
@Slf4j
public class JobListener implements org.quartz.JobListener {

    private final  QuartzPersistence quartzPersistence;

    public JobListener(final QuartzPersistence quartzPersistence) {
        this.quartzPersistence = quartzPersistence;
    }
    public static final String LISTENER_NAME = "GenericJobListener";

    @Override
    public String getName() {
        return LISTENER_NAME;
    }

    // Run this if job is about to be executed.
    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        var jobName = context.getJobDetail().getKey().toString();
        log.info("jobToBeExecuted");
        log.info("Job : " + jobName + " is going to start...");
    }

    // No idea when will run this?
    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        log.info("jobExecutionVetoed");
    }

    //Run this after job has been executed
    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        log.info("jobWasExecuted");
        var quartzJobHistory = new QuartzJobHistory();

        quartzJobHistory.setJobName(context.getJobDetail().getKey().getName());
        quartzJobHistory.setJobGroup(context.getJobDetail().getKey().getGroup());
        quartzJobHistory.setJobDescription(context.getJobDetail().getDescription());
        quartzJobHistory.setJobRunTime(context.getJobRunTime());
        quartzJobHistory.setTriggerName(context.getTrigger().getKey().getName());
        quartzJobHistory.setTriggerGroup(context.getTrigger().getKey().getGroup());
        quartzJobHistory.setTriggerDescription(context.getTrigger().getDescription());
        var triggerType = context.getTrigger() instanceof CronTriggerImpl ?
                "CRON" :
                "Simple";
        quartzJobHistory.setTriggerType(triggerType);
        quartzJobHistory.setFireTime(context.getFireTime());
        quartzJobHistory.setNextFireTime(context.getNextFireTime());
        quartzJobHistory.setPreviousFireTime(context.getPreviousFireTime());
        quartzJobHistory.setScheduledFireTime(context.getScheduledFireTime());
        if (context.getResult() instanceof QuartzJobContextDto) {
            QuartzJobContextDto x = (QuartzJobContextDto) context.getResult();
            quartzJobHistory.setResult(x.getResult());
            quartzJobHistory.setUuid(x.getUuid());
        } else {
            quartzJobHistory.setResult("success");
        }
        quartzPersistence.persist(quartzJobHistory);

        var jobName = context.getJobDetail().getKey().toString();
        log.info("Job : " + jobName + " is finished...");
    }
}
