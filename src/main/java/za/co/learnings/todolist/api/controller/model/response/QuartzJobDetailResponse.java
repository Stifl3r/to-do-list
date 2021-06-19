package za.co.learnings.todolist.api.controller.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class QuartzJobDetailResponse {
    private String name;
    private String group;
    private String description;
    private boolean concurrentExectionDisallowed;  // misspelling is actually in Quartz object :)
    private boolean persistJobDataAfterExecution;
    private boolean durable;
    private boolean requestsRecovery;
    private Date previousRun;
    private Date nextRun;
    private List<QuartzTriggerResponse> triggers;
}
