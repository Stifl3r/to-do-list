package za.co.learnings.todolist.api.controller.model.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaskCreateRequest {
    private String name;
    private String description;
    private LocalDateTime deadline;
    private Integer assigneeId;
    private Integer reporterId;

}
