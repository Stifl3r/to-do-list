package za.co.learnings.todolist.api.testmodel;

import za.co.learnings.todolist.api.controller.model.StatusType;
import za.co.learnings.todolist.api.controller.model.request.TaskCreateRequest;
import za.co.learnings.todolist.api.controller.model.request.TaskEditRequest;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

public class TaskEditRequestBuilder implements Builder<TaskEditRequest>{

    private String name = "Todo List rest api";
    private String description = "Build rest api that will";
    private LocalDateTime deadline = now().plusDays(3);
    private Integer assigneeId = 2;
    private StatusType statusType = StatusType.BACK_LOG;

    @Override
    public TaskEditRequest build() {
        var request = new TaskEditRequest();
        request.setName(name);
        request.setDescription(description);
        request.setDeadline(deadline);
        request.setStatus(statusType);
        request.setAssigneeId(assigneeId);
        return request;
    }

    public static TaskEditRequestBuilder aTaskEditRequest() {
        return new TaskEditRequestBuilder();
    }

    public TaskEditRequestBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public TaskEditRequestBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public TaskEditRequestBuilder withDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
        return this;
    }

    public TaskEditRequestBuilder withAssigneeId(Integer assigneeId) {
        this.assigneeId = assigneeId;
        return this;
    }

    public TaskEditRequestBuilder withStatus(StatusType statusType) {
        this.statusType = statusType;
        return this;
    }
}
