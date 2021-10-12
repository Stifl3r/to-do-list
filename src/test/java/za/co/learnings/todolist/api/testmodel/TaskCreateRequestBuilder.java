package za.co.learnings.todolist.api.testmodel;

import za.co.learnings.todolist.api.controller.model.request.TaskCreateRequest;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

public class TaskCreateRequestBuilder implements Builder<TaskCreateRequest>{

    private String name = "Todo List rest api";
    private String description = "Build rest api that will";
    private LocalDateTime deadline = now().plusDays(3);
    private Integer assigneeId = 2;
    private Integer reporterId = 1;

    @Override
    public TaskCreateRequest build() {
        var request = new TaskCreateRequest();
        request.setName(name);
        request.setDescription(description);
        request.setDeadline(deadline);
        request.setReporterId(reporterId);
        request.setAssigneeId(assigneeId);
        return request;
    }

    public static TaskCreateRequestBuilder aTaskCreateRequest() {
        return new TaskCreateRequestBuilder();
    }

    public TaskCreateRequestBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public TaskCreateRequestBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public TaskCreateRequestBuilder withDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
        return this;
    }

    public TaskCreateRequestBuilder withAssigneeId(Integer assigneeId) {
        this.assigneeId = assigneeId;
        return this;
    }

    public TaskCreateRequestBuilder withReporterId(Integer reporterId) {
        this.reporterId = reporterId;
        return this;
    }
}
