package za.co.learnings.todolist.api.job;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.quartz.CronTrigger;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.SimpleTrigger;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import za.co.learnings.todolist.api.controller.model.QuartzJobContextDto;
import za.co.learnings.todolist.api.exception.FailedDependencyException;

import java.net.URI;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpMethod.POST;

/**
 * Covers the job building blocks: trigger/job factories and the job implementations.
 */
public class JobsTest {

    private final JobConfiguration jobConfiguration = new JobConfiguration();
    private final JobKey jobKey = new JobKey("job-1", "TestFibonacciJob");

    @Test
    public void buildJobDetailShouldCreateDurableJob() {
        //When
        var actual = jobConfiguration.buildJobDetail(TestJob.class, "TestFibonacciJob", "fibonacci");

        //Then
        assertEquals(TestJob.class, actual.getJobClass());
        assertEquals("TestFibonacciJob", actual.getKey().getGroup());
        assertEquals("fibonacci", actual.getDescription());
        assertThat(actual.isDurable()).isTrue();
    }

    @Test
    public void onceOffTriggerShouldStartAtGivenDate() {
        //Given
        var startDate = new Date(System.currentTimeMillis() + 60_000);

        //When
        var actual = jobConfiguration.onceOffTrigger("trigger-1", "group", "once off", jobKey, startDate);

        //Then
        assertThat(actual).isInstanceOf(SimpleTrigger.class);
        assertEquals("trigger-1", actual.getKey().getName());
        assertEquals("once off", actual.getDescription());
        assertEquals(jobKey, actual.getJobKey());
        assertEquals(startDate, actual.getStartTime());
    }

    @Test
    public void fireOnceShouldStartAtGivenDate() {
        //Given
        var startDate = new Date(System.currentTimeMillis() + 500);

        //When
        var actual = jobConfiguration.fireOnce("trigger-1", "group", "fire now", jobKey, startDate);

        //Then
        assertThat(actual).isInstanceOf(SimpleTrigger.class);
        assertEquals(jobKey, actual.getJobKey());
        assertEquals(startDate, actual.getStartTime());
    }

    @Test
    public void cronTriggerShouldUseCronExpression() {
        //When
        var actual = jobConfiguration.cronTrigger("0 0 12 * * ?", "trigger-1", "group", "cron", jobKey, new Date());

        //Then
        assertThat(actual).isInstanceOf(CronTrigger.class);
        assertEquals("0 0 12 * * ?", actual.getCronExpression());
        assertEquals(jobKey, actual.getJobKey());
    }

    @Test
    public void testJobShouldStoreFibonacciResult() throws Exception {
        //Given
        var context = mock(JobExecutionContext.class);

        //When
        new TestJob().executeInternal(context);

        //Then
        var captor = ArgumentCaptor.forClass(Object.class);
        verify(context).setResult(captor.capture());
        var result = (QuartzJobContextDto) captor.getValue();
        assertThat(result.getResult()).endsWith("0 1 1 2 3 5 8 13 21 34 ");
        assertThat(result.getUuid()).isNotBlank();
    }

    @Test
    public void overdueTasksJobWhenBatchServiceReturnsOkShouldSucceed() {
        //Given
        var restTemplate = mock(RestTemplate.class);
        given(restTemplate.exchange(any(URI.class), eq(POST), any(HttpEntity.class), eq(Object.class)))
                .willReturn(ResponseEntity.ok().build());

        //When / Then
        assertThatCode(() -> new OverdueTasksJob(restTemplate).executeInternal(mock(JobExecutionContext.class)))
                .doesNotThrowAnyException();
        verify(restTemplate).exchange(eq(URI.create("http://localhost:8081/batch/overdueTasksTrigger")),
                eq(POST), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    public void overdueTasksJobWhenBatchServiceReturnsNonOkShouldFailWithFailedDependency() {
        //Given
        var restTemplate = mock(RestTemplate.class);
        given(restTemplate.exchange(any(URI.class), eq(POST), any(HttpEntity.class), eq(Object.class)))
                .willReturn(ResponseEntity.accepted().build());

        //When
        var thrown = catchThrowable(() -> new OverdueTasksJob(restTemplate).executeInternal(mock(JobExecutionContext.class)));

        //Then
        assertThat(thrown).isInstanceOf(FailedDependencyException.class)
                .hasMessage("Down stream services failed");
    }

    @Test
    public void overdueTasksJobWhenBatchServiceIsUnreachableShouldFailWithFailedDependency() {
        //Given
        var restTemplate = mock(RestTemplate.class);
        given(restTemplate.exchange(any(URI.class), eq(POST), any(HttpEntity.class), eq(Object.class)))
                .willThrow(new ResourceAccessException("connection refused"));

        //When
        var thrown = catchThrowable(() -> new OverdueTasksJob(restTemplate).executeInternal(mock(JobExecutionContext.class)));

        //Then
        assertThat(thrown).isInstanceOf(FailedDependencyException.class);
    }
}
