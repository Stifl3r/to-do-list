package za.co.learnings.todolist.api.config;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import za.co.learnings.todolist.api.job.*;

@Component
public class QuartzConfig {

    private final JobConfiguration jobConfiguration;
    private final Scheduler scheduler;
    private final QuartzPersistence quartzPersistence;

    public QuartzConfig(final Scheduler scheduler,
                        final JobConfiguration jobConfiguration,
                        final QuartzPersistence quartzPersistence) {
        this.scheduler = scheduler;
        this.jobConfiguration = jobConfiguration;
        this.quartzPersistence = quartzPersistence;
    }

    @Autowired
    public  void configureJobs() throws SchedulerException {

        //test job
        var testJobDetail = jobConfiguration.buildJobDetail(TestJob.class, "TestFibonacciJob", "TestFibonacciJob that helps test quartz stability.");
        var existingTestJob = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(testJobDetail.getKey().getGroup()));
        if (existingTestJob.size() == 0) {
            scheduler.addJob(testJobDetail, true, true);
        }

        //OverdueTasksJob
        var overdueTasksJob = jobConfiguration.buildJobDetail(OverdueTasksJob.class, "OverdueTasksJob", "OverdueTasksJob that invokes batch job to check over due tasks and send notifications");
        var existingOverdueTasksJob = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(overdueTasksJob.getKey().getGroup()));
        if (existingOverdueTasksJob.size() == 0) {
            scheduler.addJob(overdueTasksJob, true, true);
        }

        scheduler.getListenerManager().addJobListener( new JobListener(quartzPersistence));
    }
}

