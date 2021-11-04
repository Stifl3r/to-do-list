package za.co.learnings.todolist.api.service;

import org.springframework.stereotype.Service;
import za.co.learnings.todolist.api.controller.model.TaskModel;
import za.co.learnings.todolist.api.controller.model.request.TaskCreateRequest;
import za.co.learnings.todolist.api.controller.model.request.TaskEditRequest;
import za.co.learnings.todolist.api.exception.InvalidFieldException;
import za.co.learnings.todolist.api.exception.NotFoundException;
import za.co.learnings.todolist.api.jsreport.JsReportBaseData;
import za.co.learnings.todolist.api.jsreport.JsReportClient;
import za.co.learnings.todolist.api.repository.EmployeeRepository;
import za.co.learnings.todolist.api.repository.TaskRepository;
import za.co.learnings.todolist.api.repository.entity.Employee;
import za.co.learnings.todolist.api.repository.entity.Task;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toList;
import static za.co.learnings.todolist.api.controller.model.StatusType.BACK_LOG;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;
    private final JsReportClient jsReportClient;

    public TaskService(TaskRepository taskRepository,
                       JsReportClient jsReportClient,
                       EmployeeRepository employeeRepository) {
        this.taskRepository = taskRepository;
        this.jsReportClient = jsReportClient;
        this.employeeRepository = employeeRepository;
    }

    public List<TaskModel> getAllTasks(Boolean overdue) {
        var results = overdue != null && overdue ?
                taskRepository.findTasksByFilter(now())
                : taskRepository.findAllByOrderByTaskIdAsc();
        return results.stream()
                .map(TaskModel::new)
                .collect(toList());
    }

    public TaskModel getTaskById(Integer id) throws InvalidFieldException, NotFoundException {
        if (id == null) {
            throw new InvalidFieldException("Id cannot be null");
        }
        var task = taskRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Provided id does not exist"));

        return new TaskModel(task);
    }

    public TaskModel createTask(TaskCreateRequest request) throws InvalidFieldException, NotFoundException {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new InvalidFieldException("Name cannot be null or empty");
        }

        if (request.getReporterId() == null) {
            throw new InvalidFieldException("ReporterId cannot be null");
        }
        var reporter = employeeRepository.findById(request.getReporterId())
                .orElseThrow(()-> new NotFoundException("Provided reporterId does not exist"));

        Employee assignee;
        if (request.getAssigneeId() != null) {
            assignee = employeeRepository.findById(request.getAssigneeId())
                    .orElseThrow(()-> new NotFoundException("Provided assigneeId does not exist"));
        } else {
            assignee = null;
        }

        var task = new Task();
        task.setName(request.getName());
        task.setDescription(request.getDescription());
        task.setCreateDate(now());
        task.setDeadline(request.getDeadline());
        task.setStatus(BACK_LOG.getValue());
        task.setReporter(reporter);
        task.setAssignee(assignee);

        taskRepository.save(task);
        return new TaskModel(task);
    }

    public TaskModel editTask(Integer id, TaskEditRequest request) throws InvalidFieldException, NotFoundException {
        if (id == null) {
            throw new InvalidFieldException("Id cannot be null");
        }
        var task = taskRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Provided id does not exist"));

        //TODO change into nullable patches(send changes that are needed only)
        if (request.getName() == null || request.getName().isBlank()) {
            throw new InvalidFieldException("Name cannot be null or empty");
        }

        Employee assignee;
        if (request.getAssigneeId() != null) {
            assignee = employeeRepository.findById(request.getAssigneeId())
                    .orElseThrow(()-> new NotFoundException("Provided reporterId does not exist"));
        } else {
            assignee = null;
        }

        task.setName(request.getName());
        task.setDescription(request.getDescription());
        task.setDeadline(request.getDeadline());
        task.setStatus(request.getStatus().getValue());
        task.setStatusUpdate(now());
        task.setAssignee(assignee);

        taskRepository.save(task);
        return new TaskModel(task);
    }


    public ByteArrayInputStream getTasksForReport() {
        var tasks = getAllTasks(true);
        var list = tasks.stream()
                .map(taskModel -> {
                    List<String> cols = new ArrayList<>();
                    cols.add(taskModel.getName());
                    cols.add(taskModel.getDescription());
                    cols.add(taskModel.getStatus());
                    cols.add(taskModel.getDeadline().toString());
                    cols.add(taskModel.getAssignee() == null ?
                            null : taskModel.getAssignee().getLastname() + " " + taskModel.getAssignee().getFirstname());

                    return cols;
                }).collect(toList());

        list.add(List.of("NUMBEROFROWS:" + (list.size() + 2)));

        var data = new JsReportBaseData();
        data.setRows(list);
        data.setDataHeadings(List.of("Name", "Description", "Status", "Due Date", "Assignee"));
        var jsRequest = jsReportClient.createCSVRequest(data);
        var result = jsReportClient.sendAndWriteToBuffer(jsRequest);
        var bos = (ByteArrayOutputStream) result;
        return new ByteArrayInputStream(bos.toByteArray());
    }
}
