package za.co.learnings.todolist.api.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import za.co.learnings.todolist.api.controller.model.QuartzJobHistoryFilterDto;
import za.co.learnings.todolist.api.controller.model.request.TriggerRequest;
import za.co.learnings.todolist.api.exception.InvalidFieldException;
import za.co.learnings.todolist.api.exception.NotFoundException;
import za.co.learnings.todolist.api.job.JobConfiguration;
import za.co.learnings.todolist.api.repository.QrtzJobHistoryRepository;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.catchThrowable;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = QuartzSchedulerService.class)
@ActiveProfiles("local")
public class QuartzSchedulerServiceTest {

    @Autowired
    private QuartzSchedulerService quartzSchedulerService;

    @MockitoBean
    private Scheduler scheduler;

    @MockitoBean
    private QrtzJobHistoryRepository qrtzJobHistoryRepository;

    @MockitoBean
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
//        var thrown = catchThrowable(() -> quartzSchedulerService.triggerJob(request));
//
//        //Then
//        assertThat(thrown).isInstanceOf(NotFoundException.class);
//        assertEquals("Selected jobId does not exist", thrown.getMessage());
//    }

    @Test
    public void triggerJobShouldReturnSuccess() throws SchedulerException {
        //Given
        var request = new TriggerRequest();
        request.setJobId("test");
        request.setInterval("fireNow");

        var jobKey = new JobKey("test");
        given(scheduler.getJobKeys(GroupMatcher.jobGroupEquals(request.getJobId())))
                .willReturn(Set.of(jobKey));

        var jobDetail = Mockito.mock(JobDetail.class);
        given(jobDetail.getKey()).willReturn(jobKey);
        given(scheduler.getJobDetail(jobKey)).willReturn(jobDetail);

        var trigger = Mockito.mock(Trigger.class);
        given(jobConfiguration.fireOnce(anyString(), anyString(), anyString(), eq(jobKey), any(Date.class)))
                .willReturn(trigger);
        given(scheduler.scheduleJob(trigger)).willReturn(new Date());

        //When / Then
        assertDoesNotThrow(() -> quartzSchedulerService.triggerJob(request));
    }

    @Test
    public void getJobHistoryWhenPageIndexAndPageSizeAreNullShouldDefaultToFirstPageOfTen() {
        //Given
        var jobId = "TestFibonacciJob";
        var paging = new QuartzJobHistoryFilterDto();

        given(qrtzJobHistoryRepository.findByFilter(any(Pageable.class), eq(jobId)))
                .willReturn(new PageImpl<>(Collections.emptyList()));

        //When
        var actual = quartzSchedulerService.getJobHistory(jobId, paging);

        //Then
        assertThat(actual.getContent()).isEmpty();
        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(qrtzJobHistoryRepository).findByFilter(pageableCaptor.capture(), eq(jobId));
        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(10, pageableCaptor.getValue().getPageSize());
    }

    @Test
    public void getJobHistoryWhenPageIndexAndPageSizeAreProvidedShouldUseThem() {
        //Given
        var jobId = "TestFibonacciJob";
        var paging = new QuartzJobHistoryFilterDto();
        paging.setPageIndex(2);
        paging.setPageSize(5);

        given(qrtzJobHistoryRepository.findByFilter(any(Pageable.class), eq(jobId)))
                .willReturn(new PageImpl<>(Collections.emptyList()));

        //When
        quartzSchedulerService.getJobHistory(jobId, paging);

        //Then
        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(qrtzJobHistoryRepository).findByFilter(pageableCaptor.capture(), eq(jobId));
        assertEquals(2, pageableCaptor.getValue().getPageNumber());
        assertEquals(5, pageableCaptor.getValue().getPageSize());
    }
}
