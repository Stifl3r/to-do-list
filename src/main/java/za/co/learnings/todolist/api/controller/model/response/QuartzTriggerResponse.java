package za.co.learnings.todolist.api.controller.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;
import java.util.TimeZone;


@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class QuartzTriggerResponse {
    private String name;
    private String group;
    private String description;
    private String calendarName;
    private Date nextFireTime;
    private Date previousFireTime;
    private Date startTime;
    private Date endTime;
    private Date finalFireTime;
    private int priority;
    private int misfireInstruction;

    private String triggerType;

    private long repeatInterval;
    private int repeatCount;
    private long timesTriggered;

    private TimeZone timeZone;
    private String cronExpression;
    private String expressionSummary;

    private String state;
}
