package za.co.learnings.todolist.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.learnings.todolist.api.controller.model.EmployeeModel;
import za.co.learnings.todolist.api.controller.model.TaskModel;
import za.co.learnings.todolist.api.controller.model.request.EmployeeCreateRequest;
import za.co.learnings.todolist.api.controller.model.request.EmployeeEditRequest;
import za.co.learnings.todolist.api.exception.InvalidFieldException;
import za.co.learnings.todolist.api.exception.NotFoundException;
import za.co.learnings.todolist.api.service.EmployeeService;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@Tag(name = "Employees")
@RestController
@RequestMapping("api/employees")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Operation(description = "Get a list of employees")
    @GetMapping()
    public List<EmployeeModel> getListOfEmployees() {
        return employeeService.getAllEmployees();
    }

    @Operation(description = "Get a employee by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    @GetMapping("/{id}")
    public EmployeeModel getEmployeeById(@PathVariable Integer id) throws NotFoundException, InvalidFieldException {
        return employeeService.getEmployeeById(id);
    }

    @Operation(description = "Get employee tasks ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    @GetMapping("/{id}/tasks")
    public List<TaskModel> getEmployeeTasks(@PathVariable Integer id) throws NotFoundException, InvalidFieldException {
        return employeeService.getEmployeeTasks(id);
    }

    @Operation(description = "Create a new employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    @PostMapping()
    public ResponseEntity<EmployeeModel> createEmployee(@RequestBody EmployeeCreateRequest request) throws InvalidFieldException {
        var result = employeeService.createEmployee(request);
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
    public EmployeeModel editEmployee(@PathVariable Integer id, @RequestBody EmployeeEditRequest request) throws NotFoundException, InvalidFieldException {
        return employeeService.editEmployee(id, request);
    }
}
