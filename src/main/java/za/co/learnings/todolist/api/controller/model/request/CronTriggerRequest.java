package za.co.learnings.todolist.api.controller.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CronTriggerRequest {
    private String cronExpression;
    private String name;
    private String group;
    private String description;
}
