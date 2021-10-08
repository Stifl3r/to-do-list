package za.co.learnings.todolist.api.testmodel;

import za.co.learnings.todolist.api.repository.entity.Task;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

public class TaskBuilder implements Builder<Task> {

    private Integer taskId = 1;
    private String name = "Todo List rest api";
    private String description = "Build rest api that will";
    private LocalDateTime createDate = now();
    private LocalDateTime deadline = now().plusDays(3);
    private String status = "Testing";
    private LocalDateTime statusUpdate = now();

    @Override
    public Task build() {
        var task = new Task();
        task.setTaskId(taskId);
        task.setName(name);
        task.setDescription(description);
        task.setCreateDate(createDate);
        task.setDeadline(deadline);
        task.setStatus(status);
        task.setStatusUpdate(statusUpdate);
        return task;
    }

    public static TaskBuilder aTask() {
        return new TaskBuilder();
    }

    public TaskBuilder withTaskId(Integer taskId) {
        this.taskId = taskId;
        return this;
    }

    public TaskBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public TaskBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public TaskBuilder withCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
        return this;
    }

    public TaskBuilder withDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
        return this;
    }

    public TaskBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    public TaskBuilder withStatusUpdate(LocalDateTime statusUpdate) {
        this.statusUpdate = statusUpdate;
        return this;
    }
}
