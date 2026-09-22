package za.co.learnings.todolist.api.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import za.co.learnings.todolist.api.controller.model.QuartzJobContextDto;
import za.co.learnings.todolist.api.repository.entity.QuartzJobHistory;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class JobListenerTest {

    private QuartzPersistence quartzPersistence;
    private JobListener jobListener;
    private JobExecutionContext context;

    private final JobDetail jobDetail = JobBuilder.newJob(TestJob.class)
            .withIdentity("job-1", "TestFibonacciJob")
            .withDescription("fibonacci")
            .build();

    @BeforeEach
    public void setUp() {
        quartzPersistence = mock(QuartzPersistence.class);
        jobListener = new JobListener(quartzPersistence);
        context = mock(JobExecutionContext.class);
        given(context.getJobDetail()).willReturn(jobDetail);
    }

    @Test
    public void getNameShouldReturnListenerName() {
        assertEquals(JobListener.LISTENER_NAME, jobListener.getName());
    }

    @Test
    public void jobToBeExecutedAndVetoedShouldOnlyLog() {
        assertThatCode(() -> {
            jobListener.jobToBeExecuted(context);
            jobListener.jobExecutionVetoed(context);
        }).doesNotThrowAnyException();
        verifyNoInteractions(quartzPersistence);
    }

    @Test
    public void jobWasExecutedWithCronTriggerAndJobResultShouldPersistHistory() {
        //Given
        var trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger-1", "TestFibonacciJob_Group")
                .withDescription("cron trigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 12 * * ?"))
                .build();
        givenExecutedWith(trigger);

        var result = new QuartzJobContextDto();
        result.setResult("0 1 1 2");
        result.setUuid("uuid-1");
        given(context.getResult()).willReturn(result);

        //When
        jobListener.jobWasExecuted(context, null);

        //Then
        var history = capturePersistedHistory();
        assertEquals("job-1", history.getJobName());
        assertEquals("TestFibonacciJob", history.getJobGroup());
        assertEquals("fibonacci", history.getJobDescription());
        assertEquals(42L, history.getJobRunTime());
        assertEquals("trigger-1", history.getTriggerName());
        assertEquals("TestFibonacciJob_Group", history.getTriggerGroup());
        assertEquals("cron trigger", history.getTriggerDescription());
        assertEquals("CRON", history.getTriggerType());
        assertEquals("0 1 1 2", history.getResult());
        assertEquals("uuid-1", history.getUuid());
        assertEquals(FIRE_TIME, history.getFireTime());
    }

    @Test
    public void jobWasExecutedWithSimpleTriggerAndNoResultShouldRecordSuccess() {
        //Given
        var trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger-1", "TestFibonacciJob_Group")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                .build();
        givenExecutedWith(trigger);

        //When
        jobListener.jobWasExecuted(context, null);

        //Then
        var history = capturePersistedHistory();
        assertEquals("Simple", history.getTriggerType());
        assertEquals("success", history.getResult());
    }

    private static final Date FIRE_TIME = new Date(1_700_000_000_000L);

    private void givenExecutedWith(Trigger trigger) {
        given(context.getTrigger()).willReturn(trigger);
        given(context.getJobRunTime()).willReturn(42L);
        given(context.getFireTime()).willReturn(FIRE_TIME);
    }

    private QuartzJobHistory capturePersistedHistory() {
        var captor = ArgumentCaptor.forClass(QuartzJobHistory.class);
        verify(quartzPersistence).persist(captor.capture());
        return captor.getValue();
    }
}
