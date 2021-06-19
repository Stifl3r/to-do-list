package za.co.learnings.todolist.api.controller.model.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class TriggerResponse {
    private LocalDateTime previousFireTime;
    private LocalDateTime nextFireTime;
    private String triggerType;
    private String description;
    private String group;
    private String name;
    private String state;
}
