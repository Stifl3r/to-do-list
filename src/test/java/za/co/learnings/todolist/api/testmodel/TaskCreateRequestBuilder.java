package za.co.learnings.todolist.api.testmodel;

import za.co.learnings.todolist.api.controller.model.request.TaskCreateRequest;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

public class TaskCreateRequestBuilder implements Builder<TaskCreateRequest>{

    private String name = "Todo List rest api";
    private String description = "Build rest api that will";
    private LocalDateTime deadline = now().plusDays(3);
    private String status = "Testing";

    @Override
    public TaskCreateRequest build() {
        return null;
    }
}
