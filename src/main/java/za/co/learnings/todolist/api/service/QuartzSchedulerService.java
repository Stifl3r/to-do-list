package za.co.learnings.todolist.api.service;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import za.co.learnings.todolist.api.controller.model.response.QuartzInformationResponse;
import za.co.learnings.todolist.api.controller.model.response.QuartzJobDetailResponse;
import za.co.learnings.todolist.api.controller.model.QuartzJobHistoryFilterDto;
import za.co.learnings.todolist.api.controller.model.response.QuartzTriggerResponse;
import za.co.learnings.todolist.api.controller.model.request.CronTriggerRequest;
import za.co.learnings.todolist.api.controller.model.request.TriggerRequest;
import za.co.learnings.todolist.api.controller.model.response.QuartzJobHistoryResponse;
import za.co.learnings.todolist.api.controller.model.response.QuartzResponse;
import za.co.learnings.todolist.api.exception.InvalidFieldException;
import za.co.learnings.todolist.api.exception.NotFoundException;
import za.co.learnings.todolist.api.job.JobConfiguration;
import za.co.learnings.todolist.api.repository.QrtzJobHistoryRepository;

import java.text.ParseException;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.*;

import static org.quartz.CronExpression.isValidExpression;
import static org.quartz.JobKey.jobKey;
import static org.quartz.impl.matchers.GroupMatcher.groupEquals;

@Service
@Slf4j
public class QuartzSchedulerService {

    private final Scheduler scheduler;
    private final QrtzJobHistoryRepository qrtzJobHistoryRepository;
    private final JobConfiguration jobConfiguration;

    public QuartzSchedulerService(final Scheduler scheduler,
                                  final QrtzJobHistoryRepository qrtzJobHistoryRepository,
                                  final JobConfiguration jobConfiguration) {
        this.scheduler = scheduler;
        this.qrtzJobHistoryRepository = qrtzJobHistoryRepository;
        this.jobConfiguration = jobConfiguration;
    }

    public QuartzInformationResponse getSchedulerInformation() throws SchedulerException {
        var schedulerMetaData = scheduler.getMetaData();
        var quartzInformation = new QuartzInformationResponse();
        quartzInformation.setVersion(schedulerMetaData.getVersion());
        quartzInformation.setSchedulerName(schedulerMetaData.getSchedulerName());
        quartzInformation.setInstanceId(schedulerMetaData.getSchedulerInstanceId());
        quartzInformation.setThreadPoolClass(schedulerMetaData.getThreadPoolClass());
        quartzInformation.setNumberOfThreads(schedulerMetaData.getThreadPoolSize());
        quartzInformation.setSchedulerClass(schedulerMetaData.getSchedulerClass());
        quartzInformation.setClustered(schedulerMetaData.isJobStoreClustered());
        quartzInformation.setJobStoreClass(schedulerMetaData.getJobStoreClass());
        quartzInformation.setNumberOfJobsExecuted(schedulerMetaData.getNumberOfJobsExecuted());
        quartzInformation.setInStandbyMode(schedulerMetaData.isInStandbyMode());
        quartzInformation.setStartTime(schedulerMetaData.getRunningSince());
        for (String groupName : scheduler.getJobGroupNames()) {
            List<String> simpleJobList = new ArrayList<>();
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                var jobName = jobKey.getName();
                var jobGroup = jobKey.getGroup();
                List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                if (triggers.size() == 0)
                    continue;
                var nextFireTime = triggers.get(0).getNextFireTime();
                var lastFireTime = triggers.get(0).getPreviousFireTime();
                simpleJobList.add(String.format("%1s.%2s - next run: %3s (previous run: %4s)", jobGroup, jobName, nextFireTime, lastFireTime));
            }
            if(!(simpleJobList == null))
            quartzInformation.addSimpleJobDetail(simpleJobList);
        }
        return quartzInformation;
    }

    public List<QuartzJobDetailResponse> getAvailableJobs() throws SchedulerException {
        var jobs = new ArrayList<QuartzJobDetailResponse>();

        for (String groupName : scheduler.getJobGroupNames()) {

            var jobDetail = new QuartzJobDetailResponse();
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                var jobGroup = jobKey.getGroup();
                var existingJobDetail = scheduler.getJobDetail(jobKey);
                List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                if (triggers.size() > 0) {
                    jobDetail.setPreviousRun(triggers.get(0).getPreviousFireTime());
                    jobDetail.setNextRun(triggers.get(0).getNextFireTime());
                }
                jobDetail.setGroup(jobGroup);
                jobDetail.setName(existingJobDetail.getKey().getName());
                jobDetail.setDescription(existingJobDetail.getDescription());
                jobs.add(jobDetail);
            }
        }
        return jobs;
    }

    public QuartzJobDetailResponse getJobDetail(String jobId) throws SchedulerException, NotFoundException {
        var existingJob =  scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobId));
        if (existingJob.size() == 0) {
            throw new NotFoundException("Specified Job Id does not exist");
        }
        var job =  scheduler.getJobDetail(existingJob.iterator().next());

        var jobDetail = scheduler.getJobDetail(job.getKey());
        if (jobDetail == null) {
            throw new NotFoundException("Specified Job Id does not exist");
        }
        var quartzJobDetail = new QuartzJobDetailResponse();
        BeanUtils.copyProperties(jobDetail, quartzJobDetail);
        var triggers = scheduler.getTriggersOfJob(job.getKey());
        if (!triggers.isEmpty()) {
            List<QuartzTriggerResponse> quartzTriggerResponses = new ArrayList<>();
            for (Trigger trigger : triggers) {
                var quartzTrigger = new QuartzTriggerResponse();
                BeanUtils.copyProperties(trigger, quartzTrigger);
                if (trigger instanceof SimpleTrigger) {
                    var simpleTrigger = (SimpleTrigger) trigger;
                    quartzTrigger.setTriggerType(simpleTrigger.getClass().getSimpleName());
                    quartzTrigger.setRepeatInterval(simpleTrigger.getRepeatInterval());
                    quartzTrigger.setRepeatCount(simpleTrigger.getRepeatCount());
                    quartzTrigger.setTimesTriggered(simpleTrigger.getTimesTriggered());
                } else if (trigger instanceof CronTrigger) {
                    var cronTrigger = (CronTrigger) trigger;
                    quartzTrigger.setTriggerType(cronTrigger.getClass().getSimpleName());
                    quartzTrigger.setTimeZone(cronTrigger.getTimeZone());
                    quartzTrigger.setCronExpression(cronTrigger.getCronExpression());
                    quartzTrigger.setExpressionSummary(cronTrigger.getExpressionSummary());
                }
                quartzTriggerResponses.add(quartzTrigger);
            }
            quartzJobDetail.setTriggers(quartzTriggerResponses);
        }
        return quartzJobDetail;
    }

    public Page<QuartzJobHistoryResponse> getJobHistory(String jobId, QuartzJobHistoryFilterDto paging) {
        var sortDirection = paging.getSortOrder() == null || paging.getSortOrder().getDirection() == Sort.Direction.ASC ?
                Sort.Direction.ASC :
                Sort.Direction.DESC;
        var pageRequest = PageRequest.of(paging.getPageIndex(), paging.getPageSize(), paging.getSort(Sort.by(sortDirection, "fireTime")));
        var historyList = qrtzJobHistoryRepository.findByFilter(pageRequest, jobId);

        log.info("we are here");
        return historyList.map(QuartzJobHistoryResponse::mapToResponse);
    }


    public List<JobKey> getJobKeys() throws SchedulerException {
        List<JobKey> jobKeys = new ArrayList<>();
        for (String group : scheduler.getTriggerGroupNames()) {
            jobKeys.addAll(scheduler.getJobKeys(groupEquals(group)));
        }
        return jobKeys;
    }

    public QuartzResponse deleteJobDetail(String name, String group) throws SchedulerException {
        var quartzResponse = new QuartzResponse();
        quartzResponse.setType(QuartzResponse.ResponseType.DELETE);
        quartzResponse.setName(name);
        quartzResponse.setGroup(group);

        var result = scheduler.deleteJob(jobKey(name, group));
        quartzResponse.setResult(result);
        quartzResponse.setStatus(String.format("%1s.%2s has been successfully deleted", group, name));

        return quartzResponse;
    }

    public void triggerJob(TriggerRequest request) throws SchedulerException, NotFoundException, InvalidFieldException, ParseException {
        if (request.getJobId() == null || request.getJobId().isBlank()) {
            throw new InvalidFieldException("Job id has not been supplied", -1);
        }
        var existingJob =  scheduler.getJobKeys(GroupMatcher.jobGroupEquals(request.getJobId()));
        if (existingJob.size() == 0) {
            throw new NotFoundException("Selected jobId does not exist", -1);
        }

        var jobKey =  existingJob.iterator().next();
        var jobDetail = scheduler.getJobDetail(jobKey);
        var trigger = getTrigger(request, jobDetail);
        var result = scheduler.scheduleJob(trigger);
        log.info(trigger.getDescription() + " scheduled with next fire time of: " +result);
    }

    public void editTrigger(CronTriggerRequest request) throws SchedulerException, NotFoundException, InvalidFieldException, ParseException {
        var existingTrigger = scheduler.getTrigger(new TriggerKey(request.getName(), request.getGroup()));
        if (existingTrigger == null) {
            throw new NotFoundException("Specified trigger does not exist");
        }
        if(!isValidExpression(request.getCronExpression())) {
            throw new InvalidFieldException("Incorrect Cron Expression has been provided", -1);
        }

        var newTrigger = new CronTriggerImpl();
        newTrigger = (CronTriggerImpl) existingTrigger;

        newTrigger.setCronExpression(request.getCronExpression());
        newTrigger.setDescription(request.getDescription());

        var result = scheduler.rescheduleJob(existingTrigger.getKey(), newTrigger);
        log.info(existingTrigger.getDescription() + " with cron " + ((CronTriggerImpl) existingTrigger).getCronExpression() + " is rescheduled with next fire time of: " + result);
    }

    private Trigger getTrigger(TriggerRequest request, JobDetail jobDetail) throws InvalidFieldException {
        switch (request.getInterval()) {
            case "onceOff": {
                //LocalDateTime interprets front end time as GMT
                var dateTime = Date.from(request.getDateTime().atZone( ZoneId.of("Z")).toInstant());
                if (dateTime.before( new Date())) {
                    throw new InvalidFieldException("Date time provided is in the past");
                }
                return jobConfiguration.onceOffTrigger(
                        UUID.randomUUID().toString(),
                        request.getJobId() + "_Group",
                        request.getJobId() + " once off trigger that will run at " + dateTime,
                        jobDetail.getKey(),
                        dateTime);
            }
            case "fireNow": {
                var HALF_A_SECOND = 500; //millisecs
                var date = Calendar.getInstance();
                var afterAddingSecs = new Date(date.getTimeInMillis() + (HALF_A_SECOND));
                return jobConfiguration.fireOnce(
                        request.getJobId() + "_OnceOff_Trigger",
                        request.getJobId() + "_Group",
                        jobDetail.getKey().getGroup() + " once off trigger",
                        jobDetail.getKey(),
                        afterAddingSecs);
            }
            case "cron": {
                if(!isValidExpression(request.getCronExpression())) {
                    throw new InvalidFieldException("Incorrect Cron Expression has been provided", -1);
                }
                return jobConfiguration.cronTrigger(
                        request.getCronExpression(),
                        UUID.randomUUID().toString(),
                        request.getJobId() + "_Group",
                        jobDetail.getKey().getGroup() + " cron trigger",
                        jobDetail.getKey(),
                        new Date());
            }
            default: {
                throw new InvalidFieldException("Invalid interval provided", -1);
            }
        }
    }

    public List<QuartzTriggerResponse> getAllJobTriggers(String jobId) throws SchedulerException, NotFoundException {
        var existingJob =  scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobId));
        if (existingJob.size() == 0) {
            throw new NotFoundException("Specified Job Id does not exist");
        }
        var job =  existingJob.iterator().next();
        var triggers = scheduler.getTriggersOfJob(job);
        List<QuartzTriggerResponse> quartzTriggerResponses = new ArrayList<>();
        if (!triggers.isEmpty()) {

            for (Trigger trigger : triggers) {
                var quartzTrigger = new QuartzTriggerResponse();
                BeanUtils.copyProperties(trigger, quartzTrigger);
                if (trigger instanceof SimpleTrigger) {
                    var simpleTrigger = (SimpleTrigger) trigger;
                    quartzTrigger.setTriggerType("Simple");
                    quartzTrigger.setRepeatInterval(simpleTrigger.getRepeatInterval());
                    quartzTrigger.setRepeatCount(simpleTrigger.getRepeatCount());
                    quartzTrigger.setTimesTriggered(simpleTrigger.getTimesTriggered());
                } else if (trigger instanceof CronTrigger) {
                    var cronTrigger = (CronTrigger) trigger;
                    quartzTrigger.setTriggerType("CRON");
                    quartzTrigger.setTimeZone(cronTrigger.getTimeZone());
                    quartzTrigger.setCronExpression(cronTrigger.getCronExpression());
                    quartzTrigger.setExpressionSummary(cronTrigger.getExpressionSummary());
                }
                var state = scheduler.getTriggerState(trigger.getKey());
                quartzTrigger.setState(state.toString());
                quartzTriggerResponses.add(quartzTrigger);
            }
        }
        return quartzTriggerResponses;
    }

    public void pauseActiveTrigger(CronTriggerRequest request) throws SchedulerException, NotFoundException {
        var existingTrigger = scheduler.getTrigger(new TriggerKey(request.getName(), request.getGroup()));
        if (existingTrigger == null) {
            throw new NotFoundException("Specified trigger does not exist");
        }
        scheduler.pauseTrigger(existingTrigger.getKey());
        var result = existingTrigger instanceof CronTriggerImpl ?
                " with cron " + ((CronTriggerImpl) existingTrigger).getCronExpression() :
                " with simple " + existingTrigger.getStartTime();
        log.info(existingTrigger.getDescription() + result + " has been paused");
    }

    public void resumePausedTrigger(CronTriggerRequest request) throws SchedulerException, NotFoundException {
        var existingTrigger = scheduler.getTrigger(new TriggerKey(request.getName(), request.getGroup()));
        if (existingTrigger == null) {
            throw new NotFoundException("Specified trigger does not exist");
        }

        scheduler.resumeTrigger(existingTrigger.getKey());
        var result = existingTrigger instanceof CronTriggerImpl ?
                " with cron " + ((CronTriggerImpl) existingTrigger).getCronExpression() :
                " with simple " + existingTrigger.getStartTime();
        log.info(existingTrigger.getDescription() + result+ " has been resumed");
    }

    public void deleteTrigger(String name, String group) throws SchedulerException, NotFoundException {
        var existingTrigger = scheduler.getTrigger(new TriggerKey(name, group));
        if (existingTrigger == null) {
            throw new NotFoundException("Specified trigger does not exist");
        }
        scheduler.unscheduleJob(existingTrigger.getKey());
        var result = existingTrigger instanceof CronTriggerImpl ?
                " with cron " + ((CronTriggerImpl) existingTrigger).getCronExpression() :
                " with simple " + existingTrigger.getStartTime();
        log.info(existingTrigger.getDescription() + result + " has been resumed");
    }
}
