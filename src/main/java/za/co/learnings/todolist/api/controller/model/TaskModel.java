package za.co.learnings.todolist.api.controller.model;

import lombok.Getter;
import lombok.Setter;
import za.co.learnings.todolist.api.repository.entity.Task;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaskModel {
    private Integer taskId;
    private String name;
    private String description;
    private LocalDateTime issueDate;
    private LocalDateTime deadline;
    private String status;
    private LocalDateTime statusUpdate;
    private EmployeeModel assignee;
    private EmployeeModel reporter;

    public TaskModel(Task task) {
        this.taskId = task.getTaskId();
        this.name = task.getName();
        this.description = task.getDescription();
        this.issueDate = task.getCreateDate();
        this.deadline = task.getDeadline();
        this.status = task.getStatus();
        this.statusUpdate = task.getStatusUpdate();
        this.assignee = task.getAssignee() == null ?
                null :
                new EmployeeModel(task.getAssignee());
        this.reporter = task.getReporter() == null ?
                null :
                new EmployeeModel(task.getReporter());
    }
}
