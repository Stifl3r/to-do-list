package za.co.learnings.todolist.api.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import za.co.learnings.todolist.api.controller.model.request.TriggerRequest;
import za.co.learnings.todolist.api.exception.InvalidFieldException;
import za.co.learnings.todolist.api.exception.NotFoundException;
import za.co.learnings.todolist.api.job.JobConfiguration;
import za.co.learnings.todolist.api.repository.QrtzJobHistoryRepository;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.catchThrowable;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = QuartzSchedulerService.class)
@ActiveProfiles("local")
public class QuartzSchedulerServiceTest {

    @Autowired
    private QuartzSchedulerService quartzSchedulerService;

    @MockBean
    private Scheduler scheduler;

    @MockBean
    private QrtzJobHistoryRepository qrtzJobHistoryRepository;

    @MockBean
    private JobConfiguration jobConfiguration;

    @Test
    public void getJobKeysShouldReturnSuccess() throws SchedulerException {
        //Given
        var jobKey = new JobKey("test");
        var expected = List.of(jobKey);

        given(scheduler.getTriggerGroupNames())
                .willReturn(Collections.emptyList());

    }

    @Test
    public void triggerJobWhenJobIdIsNullShouldReturnInvalidRequest() {
        //Given
        var request = new TriggerRequest();

        //When
        var thrown = catchThrowable(() -> quartzSchedulerService.triggerJob(request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        assertEquals("Job id has not been supplied", thrown.getMessage());
    }

    @Test
    public void triggerJobWhenJobIdIsBlankShouldReturnInvalidRequest() {
        //Given
        var request = new TriggerRequest();
        request.setJobId("");

        //When
        var thrown = catchThrowable(() -> quartzSchedulerService.triggerJob(request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        assertEquals("Job id has not been supplied", thrown.getMessage());
    }

    @Test
    public void triggerJobWhenJobIdDoesNotExistShouldReturnNotFound() throws SchedulerException {
        //Given
        var request = new TriggerRequest();
        request.setJobId("test");

        given(scheduler.getJobKeys(GroupMatcher.jobGroupEquals(request.getJobId())))
                .willReturn(Set.of());


        //When
        var thrown = catchThrowable(() -> quartzSchedulerService.triggerJob(request));

        //Then
        assertThat(thrown).isInstanceOf(NotFoundException.class);
        assertEquals("Selected jobId does not exist", thrown.getMessage());
    }

//    @Test
//    public void triggerJobShouldReturnSuccess() throws SchedulerException, NotFoundException, ParseException, InvalidFieldException {
//        //Given
//        var request = new TriggerRequest();
//        request.setJobId("test");
//
//        var jobKey = new JobKey("test");
//        given(scheduler.getJobKeys(GroupMatcher.jobGroupEquals(request.getJobId())))
//                .willReturn(Set.of(jobKey));
//
//        var jobDetail = scheduler.getJobDetail(jobKey);
//        given(scheduler.getJobDetail(jobKey))
//                .willReturn(jobDetail);
//
////        var trigger = scheduler.getTrigger(request, jobDetail)
//
//
//        //When
//        quartzSchedulerService.triggerJob(request);
////        var thrown = catchThrowable(() -> quartzSchedulerService.triggerJob(request));
//
//        //Then
////        assertThat(thrown).isInstanceOf(NotFoundException.class);
////        assertEquals("Selected jobId does not exist", thrown.getMessage());
//    }
}
