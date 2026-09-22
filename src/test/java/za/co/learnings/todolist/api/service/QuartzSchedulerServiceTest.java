package za.co.learnings.todolist.api.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import za.co.learnings.todolist.api.controller.model.QuartzJobHistoryFilterDto;
import za.co.learnings.todolist.api.controller.model.SortOrder;
import za.co.learnings.todolist.api.controller.model.request.CronTriggerRequest;
import za.co.learnings.todolist.api.controller.model.request.TriggerRequest;
import za.co.learnings.todolist.api.controller.model.response.QuartzResponse;
import za.co.learnings.todolist.api.exception.InvalidFieldException;
import za.co.learnings.todolist.api.exception.NotFoundException;
import za.co.learnings.todolist.api.job.JobConfiguration;
import za.co.learnings.todolist.api.job.TestJob;
import za.co.learnings.todolist.api.repository.QrtzJobHistoryRepository;
import za.co.learnings.todolist.api.repository.entity.QuartzJobHistory;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.never;
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
                .willReturn(List.of("test"));
        given(scheduler.getJobKeys(GroupMatcher.jobGroupEquals("test")))
                .willReturn(Set.of(jobKey));

        //When
        var actual = quartzSchedulerService.getJobKeys();

        //Then
        assertEquals(expected, actual);
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

    @Test
    public void getJobHistoryWhenSortOrderIsDescShouldSortDescAndMapResults() {
        //Given
        var jobId = "TestFibonacciJob";
        var paging = new QuartzJobHistoryFilterDto();
        var sortOrder = new SortOrder();
        sortOrder.setDirection(Sort.Direction.DESC);
        paging.setSortOrder(sortOrder);

        var history = new QuartzJobHistory();
        history.setJobName("job-1");
        history.setJobGroup(jobId);
        history.setFireTime(new Date());
        given(qrtzJobHistoryRepository.findByFilter(any(Pageable.class), eq(jobId)))
                .willReturn(new PageImpl<>(List.of(history)));

        //When
        var actual = quartzSchedulerService.getJobHistory(jobId, paging);

        //Then
        assertEquals("job-1", actual.getContent().get(0).getJobName());
        assertThat(actual.getContent().get(0).getFireTime()).isNotNull();
        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(qrtzJobHistoryRepository).findByFilter(pageableCaptor.capture(), eq(jobId));
        assertEquals(Sort.Direction.DESC, pageableCaptor.getValue().getSort().getOrderFor("fireTime").getDirection());
    }

    @Test
    public void getSchedulerInformationShouldMapMetaDataAndSkipJobsWithoutTriggers() throws SchedulerException {
        //Given
        var metaData = Mockito.mock(SchedulerMetaData.class);
        given(metaData.getVersion()).willReturn("2.5.0");
        given(metaData.getSchedulerName()).willReturn("quartzScheduler");
        given(metaData.getSchedulerInstanceId()).willReturn("instance-1");
        given(metaData.getThreadPoolSize()).willReturn(5);
        given(metaData.getNumberOfJobsExecuted()).willReturn(3);
        given(scheduler.getMetaData()).willReturn(metaData);

        var scheduledJob = new JobKey("scheduled", "group");
        var idleJob = new JobKey("idle", "group");
        given(scheduler.getJobGroupNames()).willReturn(List.of("group"));
        given(scheduler.getJobKeys(GroupMatcher.jobGroupEquals("group")))
                .willReturn(new LinkedHashSet<>(List.of(scheduledJob, idleJob)));
        willReturn(List.of(simpleTrigger(scheduledJob))).given(scheduler).getTriggersOfJob(scheduledJob);
        willReturn(List.of()).given(scheduler).getTriggersOfJob(idleJob);

        //When
        var actual = quartzSchedulerService.getSchedulerInformation();

        //Then
        assertEquals("2.5.0", actual.getVersion());
        assertEquals("quartzScheduler", actual.getSchedulerName());
        assertEquals("instance-1", actual.getInstanceId());
        assertEquals(5, actual.getNumberOfThreads());
        assertEquals(3, actual.getNumberOfJobsExecuted());
        assertThat(actual.getSimpleJobDetail()).hasSize(1);
        assertThat(actual.getSimpleJobDetail().get(0)).startsWith("group.scheduled - next run:");
    }

    // Regression test: a single response object used to be shared by every job in a group,
    // so all entries ended up as copies of the last job.
    @Test
    public void getAvailableJobsWhenGroupHasMultipleJobsShouldReturnEachJob() throws SchedulerException {
        //Given
        var firstJob = jobDetail("first", "group");
        var secondJob = jobDetail("second", "group");
        given(scheduler.getJobGroupNames()).willReturn(List.of("group"));
        given(scheduler.getJobKeys(GroupMatcher.jobGroupEquals("group")))
                .willReturn(new LinkedHashSet<>(List.of(firstJob.getKey(), secondJob.getKey())));
        given(scheduler.getJobDetail(firstJob.getKey())).willReturn(firstJob);
        given(scheduler.getJobDetail(secondJob.getKey())).willReturn(secondJob);
        var trigger = simpleTrigger(firstJob.getKey());
        willReturn(List.of(trigger)).given(scheduler).getTriggersOfJob(firstJob.getKey());
        willReturn(List.of()).given(scheduler).getTriggersOfJob(secondJob.getKey());

        //When
        var actual = quartzSchedulerService.getAvailableJobs();

        //Then
        assertThat(actual).extracting("name").containsExactly("first", "second");
        assertEquals(trigger.getNextFireTime(), actual.get(0).getNextRun());
        assertThat(actual.get(1).getNextRun()).isNull();
    }

    @Test
    public void getJobDetailWhenJobIdDoesNotExistShouldReturnNotFound() throws SchedulerException {
        //Given
        given(scheduler.getJobKeys(GroupMatcher.jobGroupEquals("missing")))
                .willReturn(Set.of());

        //When
        var thrown = catchThrowable(() -> quartzSchedulerService.getJobDetail("missing"));

        //Then
        assertThat(thrown).isInstanceOf(NotFoundException.class);
        assertEquals("Specified Job Id does not exist", thrown.getMessage());
    }

    // Regression test: the job detail used to be dereferenced before its null check,
    // so a vanished job surfaced as a NullPointerException instead of NotFound.
    @Test
    public void getJobDetailWhenJobDetailIsMissingShouldReturnNotFound() throws SchedulerException {
        //Given
        var jobKey = new JobKey("job-1", "group");
        given(scheduler.getJobKeys(GroupMatcher.jobGroupEquals("group")))
                .willReturn(Set.of(jobKey));
        given(scheduler.getJobDetail(jobKey)).willReturn(null);

        //When
        var thrown = catchThrowable(() -> quartzSchedulerService.getJobDetail("group"));

        //Then
        assertThat(thrown).isInstanceOf(NotFoundException.class);
    }

    @Test
    public void getJobDetailShouldMapJobAndTriggers() throws SchedulerException, NotFoundException {
        //Given
        var job = jobDetail("job-1", "group");
        given(scheduler.getJobKeys(GroupMatcher.jobGroupEquals("group")))
                .willReturn(Set.of(job.getKey()));
        given(scheduler.getJobDetail(job.getKey())).willReturn(job);
        willReturn(List.of(simpleTrigger(job.getKey()), cronTrigger(job.getKey())))
                .given(scheduler).getTriggersOfJob(job.getKey());

        //When
        var actual = quartzSchedulerService.getJobDetail("group");

        //Then
        assertEquals("job-1", actual.getName());
        assertEquals("group", actual.getGroup());
        assertEquals("test job", actual.getDescription());
        assertThat(actual.getTriggers()).hasSize(2);
        assertEquals("SimpleTriggerImpl", actual.getTriggers().get(0).getTriggerType());
        assertEquals("CronTriggerImpl", actual.getTriggers().get(1).getTriggerType());
        assertEquals(CRON_EXPRESSION, actual.getTriggers().get(1).getCronExpression());
    }

    @Test
    public void getJobDetailWhenJobHasNoTriggersShouldNotSetTriggers() throws SchedulerException, NotFoundException {
        //Given
        var job = jobDetail("job-1", "group");
        given(scheduler.getJobKeys(GroupMatcher.jobGroupEquals("group")))
                .willReturn(Set.of(job.getKey()));
        given(scheduler.getJobDetail(job.getKey())).willReturn(job);
        willReturn(List.of()).given(scheduler).getTriggersOfJob(job.getKey());

        //When
        var actual = quartzSchedulerService.getJobDetail("group");

        //Then
        assertThat(actual.getTriggers()).isNull();
    }

    @Test
    public void deleteJobDetailShouldReturnDeleteResponse() throws SchedulerException {
        //Given
        given(scheduler.deleteJob(new JobKey("job-1", "group"))).willReturn(true);

        //When
        var actual = quartzSchedulerService.deleteJobDetail("job-1", "group");

        //Then
        assertEquals(QuartzResponse.ResponseType.DELETE, actual.getType());
        assertEquals("job-1", actual.getName());
        assertEquals("group", actual.getGroup());
        assertThat(actual.isResult()).isTrue();
    }

    @Test
    public void triggerJobWhenIntervalIsOnceOffShouldScheduleOnceOffTrigger() throws Exception {
        //Given
        var request = triggerRequest("onceOff");
        request.setDateTime(LocalDateTime.now(ZoneOffset.UTC).plusDays(1));
        var jobDetail = givenExistingJob(request.getJobId());

        var trigger = Mockito.mock(Trigger.class);
        given(jobConfiguration.onceOffTrigger(anyString(), eq("test_Group"), anyString(), eq(jobDetail.getKey()), any(Date.class)))
                .willReturn(trigger);

        //When
        quartzSchedulerService.triggerJob(request);

        //Then
        verify(scheduler).scheduleJob(trigger);
    }

    @Test
    public void triggerJobWhenOnceOffDateIsInThePastShouldReturnInvalidField() throws SchedulerException {
        //Given
        var request = triggerRequest("onceOff");
        request.setDateTime(LocalDateTime.now(ZoneOffset.UTC).minusDays(1));
        givenExistingJob(request.getJobId());

        //When
        var thrown = catchThrowable(() -> quartzSchedulerService.triggerJob(request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        assertEquals("Date time provided is in the past", thrown.getMessage());
    }

    @Test
    public void triggerJobWhenIntervalIsCronShouldScheduleCronTrigger() throws Exception {
        //Given
        var request = triggerRequest("cron");
        request.setCronExpression(CRON_EXPRESSION);
        var jobDetail = givenExistingJob(request.getJobId());

        var trigger = cronTrigger(jobDetail.getKey());
        given(jobConfiguration.cronTrigger(eq(CRON_EXPRESSION), anyString(), eq("test_Group"), anyString(), eq(jobDetail.getKey()), any(Date.class)))
                .willReturn(trigger);

        //When
        quartzSchedulerService.triggerJob(request);

        //Then
        verify(scheduler).scheduleJob(trigger);
    }

    @Test
    public void triggerJobWhenCronExpressionIsInvalidShouldReturnInvalidField() throws SchedulerException {
        //Given
        var request = triggerRequest("cron");
        request.setCronExpression("not a cron");
        givenExistingJob(request.getJobId());

        //When
        var thrown = catchThrowable(() -> quartzSchedulerService.triggerJob(request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        assertEquals("Incorrect Cron Expression has been provided", thrown.getMessage());
    }

    @Test
    public void triggerJobWhenIntervalIsUnknownShouldReturnInvalidField() throws SchedulerException {
        //Given
        var request = triggerRequest("weekly");
        givenExistingJob(request.getJobId());

        //When
        var thrown = catchThrowable(() -> quartzSchedulerService.triggerJob(request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        assertEquals("Invalid interval provided", thrown.getMessage());
    }

    @Test
    public void editTriggerWhenTriggerDoesNotExistShouldReturnNotFound() {
        //Given
        var request = cronTriggerRequest("0 0 6 * * ?");

        //When
        var thrown = catchThrowable(() -> quartzSchedulerService.editTrigger(request));

        //Then
        assertThat(thrown).isInstanceOf(NotFoundException.class);
        assertEquals("Specified trigger does not exist", thrown.getMessage());
    }

    // Regression test: editing a simple trigger used to blow up with a ClassCastException.
    @Test
    public void editTriggerWhenTriggerIsNotCronShouldReturnInvalidField() throws SchedulerException {
        //Given
        var request = cronTriggerRequest("0 0 6 * * ?");
        given(scheduler.getTrigger(TRIGGER_KEY)).willReturn(simpleTrigger(new JobKey("job-1", "group")));

        //When
        var thrown = catchThrowable(() -> quartzSchedulerService.editTrigger(request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        assertEquals("Only cron triggers can be edited", thrown.getMessage());
        verify(scheduler, never()).rescheduleJob(any(), any());
    }

    @Test
    public void editTriggerWhenCronExpressionIsInvalidShouldReturnInvalidField() throws SchedulerException {
        //Given
        var request = cronTriggerRequest("not a cron");
        given(scheduler.getTrigger(TRIGGER_KEY)).willReturn(cronTrigger(new JobKey("job-1", "group")));

        //When
        var thrown = catchThrowable(() -> quartzSchedulerService.editTrigger(request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        assertEquals("Incorrect Cron Expression has been provided", thrown.getMessage());
    }

    @Test
    public void editTriggerShouldRescheduleWithNewCronExpression() throws Exception {
        //Given
        var request = cronTriggerRequest("0 0 6 * * ?");
        request.setDescription("morning run");
        given(scheduler.getTrigger(TRIGGER_KEY)).willReturn(cronTrigger(new JobKey("job-1", "group")));

        //When
        quartzSchedulerService.editTrigger(request);

        //Then
        var triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
        verify(scheduler).rescheduleJob(eq(TRIGGER_KEY), triggerCaptor.capture());
        var rescheduled = (CronTrigger) triggerCaptor.getValue();
        assertEquals("0 0 6 * * ?", rescheduled.getCronExpression());
        assertEquals("morning run", rescheduled.getDescription());
    }

    @Test
    public void getAllJobTriggersWhenJobIdDoesNotExistShouldReturnNotFound() throws SchedulerException {
        //Given
        given(scheduler.getJobKeys(GroupMatcher.jobGroupEquals("missing")))
                .willReturn(Set.of());

        //When
        var thrown = catchThrowable(() -> quartzSchedulerService.getAllJobTriggers("missing"));

        //Then
        assertThat(thrown).isInstanceOf(NotFoundException.class);
    }

    @Test
    public void getAllJobTriggersShouldMapTriggersWithState() throws SchedulerException, NotFoundException {
        //Given
        var jobKey = new JobKey("job-1", "group");
        given(scheduler.getJobKeys(GroupMatcher.jobGroupEquals("group")))
                .willReturn(Set.of(jobKey));
        var simple = simpleTrigger(jobKey);
        var cron = cronTrigger(jobKey).getTriggerBuilder()
                .withIdentity("cron-trigger", "group")
                .build();
        willReturn(List.of(simple, cron)).given(scheduler).getTriggersOfJob(jobKey);
        given(scheduler.getTriggerState(simple.getKey())).willReturn(Trigger.TriggerState.NORMAL);
        given(scheduler.getTriggerState(cron.getKey())).willReturn(Trigger.TriggerState.PAUSED);

        //When
        var actual = quartzSchedulerService.getAllJobTriggers("group");

        //Then
        assertThat(actual).extracting("triggerType").containsExactly("Simple", "CRON");
        assertThat(actual).extracting("state").containsExactly("NORMAL", "PAUSED");
        assertEquals(CRON_EXPRESSION, actual.get(1).getCronExpression());
    }

    @Test
    public void getAllJobTriggersWhenJobHasNoTriggersShouldReturnEmptyList() throws SchedulerException, NotFoundException {
        //Given
        var jobKey = new JobKey("job-1", "group");
        given(scheduler.getJobKeys(GroupMatcher.jobGroupEquals("group")))
                .willReturn(Set.of(jobKey));
        willReturn(List.of()).given(scheduler).getTriggersOfJob(jobKey);

        //When
        var actual = quartzSchedulerService.getAllJobTriggers("group");

        //Then
        assertThat(actual).isEmpty();
    }

    @Test
    public void pauseActiveTriggerWhenTriggerDoesNotExistShouldReturnNotFound() {
        //When
        var thrown = catchThrowable(() -> quartzSchedulerService.pauseActiveTrigger(cronTriggerRequest(null)));

        //Then
        assertThat(thrown).isInstanceOf(NotFoundException.class);
    }

    @ParameterizedTest
    @MethodSource("existingTriggers")
    public void pauseActiveTriggerShouldPauseTrigger(Trigger trigger) throws SchedulerException, NotFoundException {
        //Given
        given(scheduler.getTrigger(TRIGGER_KEY)).willReturn(trigger);

        //When
        quartzSchedulerService.pauseActiveTrigger(cronTriggerRequest(null));

        //Then
        verify(scheduler).pauseTrigger(TRIGGER_KEY);
    }

    @Test
    public void resumePausedTriggerWhenTriggerDoesNotExistShouldReturnNotFound() {
        //When
        var thrown = catchThrowable(() -> quartzSchedulerService.resumePausedTrigger(cronTriggerRequest(null)));

        //Then
        assertThat(thrown).isInstanceOf(NotFoundException.class);
    }

    @ParameterizedTest
    @MethodSource("existingTriggers")
    public void resumePausedTriggerShouldResumeTrigger(Trigger trigger) throws SchedulerException, NotFoundException {
        //Given
        given(scheduler.getTrigger(TRIGGER_KEY)).willReturn(trigger);

        //When
        quartzSchedulerService.resumePausedTrigger(cronTriggerRequest(null));

        //Then
        verify(scheduler).resumeTrigger(TRIGGER_KEY);
    }

    @Test
    public void deleteTriggerWhenTriggerDoesNotExistShouldReturnNotFound() {
        //When
        var thrown = catchThrowable(() -> quartzSchedulerService.deleteTrigger(TRIGGER_KEY.getName(), TRIGGER_KEY.getGroup()));

        //Then
        assertThat(thrown).isInstanceOf(NotFoundException.class);
    }

    @ParameterizedTest
    @MethodSource("existingTriggers")
    public void deleteTriggerShouldUnscheduleTrigger(Trigger trigger) throws SchedulerException, NotFoundException {
        //Given
        given(scheduler.getTrigger(TRIGGER_KEY)).willReturn(trigger);

        //When
        quartzSchedulerService.deleteTrigger(TRIGGER_KEY.getName(), TRIGGER_KEY.getGroup());

        //Then
        verify(scheduler).unscheduleJob(TRIGGER_KEY);
    }

    private static final String CRON_EXPRESSION = "0 0 12 * * ?";
    private static final TriggerKey TRIGGER_KEY = new TriggerKey("trigger-1", "group");

    static List<Trigger> existingTriggers() {
        var jobKey = new JobKey("job-1", "group");
        return List.of(simpleTrigger(jobKey), cronTrigger(jobKey));
    }

    private static Trigger simpleTrigger(JobKey jobKey) {
        return TriggerBuilder.newTrigger()
                .withIdentity(TRIGGER_KEY.getName(), TRIGGER_KEY.getGroup())
                .forJob(jobKey)
                .startAt(new Date(System.currentTimeMillis() + 60_000))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                .build();
    }

    private static CronTrigger cronTrigger(JobKey jobKey) {
        return TriggerBuilder.newTrigger()
                .withIdentity(TRIGGER_KEY.getName(), TRIGGER_KEY.getGroup())
                .forJob(jobKey)
                .withSchedule(CronScheduleBuilder.cronSchedule(CRON_EXPRESSION))
                .build();
    }

    private static JobDetail jobDetail(String name, String group) {
        return JobBuilder.newJob(TestJob.class)
                .withIdentity(name, group)
                .withDescription("test job")
                .storeDurably()
                .build();
    }

    private JobDetail givenExistingJob(String jobId) throws SchedulerException {
        var jobDetail = jobDetail("job-1", jobId);
        given(scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobId)))
                .willReturn(Set.of(jobDetail.getKey()));
        given(scheduler.getJobDetail(jobDetail.getKey())).willReturn(jobDetail);
        return jobDetail;
    }

    private static TriggerRequest triggerRequest(String interval) {
        var request = new TriggerRequest();
        request.setJobId("test");
        request.setInterval(interval);
        return request;
    }

    private static CronTriggerRequest cronTriggerRequest(String cronExpression) {
        var request = new CronTriggerRequest();
        request.setName(TRIGGER_KEY.getName());
        request.setGroup(TRIGGER_KEY.getGroup());
        request.setCronExpression(cronExpression);
        return request;
    }
}
