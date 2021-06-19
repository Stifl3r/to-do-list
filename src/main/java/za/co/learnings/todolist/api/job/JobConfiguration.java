package za.co.learnings.todolist.api.job;

import org.quartz.*;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

import static org.quartz.DateBuilder.futureDate;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

@Component
public class JobConfiguration {

    public JobDetail buildJobDetail(Class clazz, String group, String description) {
        return JobBuilder.newJob(clazz)
                .withIdentity(UUID.randomUUID().toString(), group)
                .withDescription(description)
                .storeDurably(true)  // https://stackoverflow.com/questions/25624977/how-to-save-quartz-executed-jobs
                .build();
    }

    public Trigger onceOffTrigger(String identity, String group, String description, JobKey jobKey, Date startDate) {
        return TriggerBuilder
                .newTrigger()
                .withIdentity(identity, group)
                .withDescription(description)
                .forJob(jobKey)
                .startAt(startDate)
                .withSchedule(simpleSchedule())
                .build();
    }

    public Trigger fireOnce(String identity, String group, String description, JobKey jobKey, Date startDate) {
        return TriggerBuilder
                .newTrigger()
                .withIdentity(identity, group)
                .withDescription(description)
                .forJob(jobKey)
                .startAt(startDate)
                .withSchedule(simpleSchedule())
                .build();
    }

    public CronTrigger cronTrigger(String cronExpression, String identity, String group, String description, JobKey jobKey, Date startDate) {
        return TriggerBuilder
                .newTrigger()
                .withIdentity(identity, group)
                .withDescription(description)
                .forJob(jobKey)
                .startAt(futureDate(500, DateBuilder.IntervalUnit.MILLISECOND))
                .withSchedule(CronScheduleBuilder.cronSchedule( cronExpression))
                .build();
    }
}
