package za.co.learnings.todolist.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.learnings.todolist.api.controller.model.TaskModel;
import za.co.learnings.todolist.api.controller.model.request.TaskCreateRequest;
import za.co.learnings.todolist.api.controller.model.request.TaskEditRequest;
import za.co.learnings.todolist.api.exception.InvalidFieldException;
import za.co.learnings.todolist.api.exception.NotFoundException;
import za.co.learnings.todolist.api.service.TaskService;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@Tag(name = "Tasks")
@RestController
@RequestMapping("api/tasks")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(description = "Get a list of tasks")
    @GetMapping()
    public List<TaskModel> getListOfTasks(@RequestParam(required = false) Boolean overdue ) {
        return taskService.getAllTasks(overdue);
    }

    @Operation(description = "Get a task by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    @GetMapping("/{id}")
    public TaskModel getTaskById(@PathVariable Integer id) throws NotFoundException, InvalidFieldException {
        return taskService.getTaskById(id);
    }

    @Operation(description = "Create a new task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    @PostMapping()
    public ResponseEntity<TaskModel> createTask(@RequestBody TaskCreateRequest request) throws InvalidFieldException, NotFoundException {
       var result = taskService.createTask(request);
       return ResponseEntity
               .status(CREATED)
               .body(result);
    }

    @Operation(description = "Edit an existing task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    @PatchMapping("/{id}")
    public TaskModel editTask(@PathVariable Integer id, @RequestBody TaskEditRequest request) throws NotFoundException, InvalidFieldException {
        return taskService.editTask(id, request);
    }
}
