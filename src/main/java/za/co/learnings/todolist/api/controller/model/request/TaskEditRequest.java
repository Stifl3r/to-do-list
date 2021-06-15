package za.co.learnings.todolist.api.controller.model.request;

import lombok.Getter;
import lombok.Setter;
import za.co.learnings.todolist.api.controller.model.StatusType;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaskEditRequest {
    private String name;
    private String description;
    private LocalDateTime deadline;
    private StatusType status;
    private Integer assigneeId;
}
