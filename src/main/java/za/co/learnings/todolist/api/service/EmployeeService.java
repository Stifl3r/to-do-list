package za.co.learnings.todolist.api.service;

import org.springframework.stereotype.Service;
import za.co.learnings.todolist.api.controller.model.EmployeeModel;
import za.co.learnings.todolist.api.controller.model.TaskModel;
import za.co.learnings.todolist.api.controller.model.request.EmployeeCreateRequest;
import za.co.learnings.todolist.api.controller.model.request.EmployeeEditRequest;
import za.co.learnings.todolist.api.exception.InvalidFieldException;
import za.co.learnings.todolist.api.exception.NotFoundException;
import za.co.learnings.todolist.api.repository.EmployeeRepository;
import za.co.learnings.todolist.api.repository.TaskRepository;
import za.co.learnings.todolist.api.repository.entity.Employee;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           TaskRepository taskRepository) {
        this.employeeRepository = employeeRepository;
        this.taskRepository = taskRepository;
    }

    public List<EmployeeModel> getAllEmployees() {
        var results = employeeRepository.findAll();
        return results.stream()
                .map(EmployeeModel::new)
                .collect(toList());
    }

    public EmployeeModel getEmployeeById(Integer id) throws InvalidFieldException, NotFoundException {
        if (id == null) {
            throw new InvalidFieldException("Id cannot be null");
        }
        var employee = employeeRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Provided id does not exist"));

        return new EmployeeModel(employee);
    }

    public EmployeeModel createEmployee(EmployeeCreateRequest request) throws InvalidFieldException {
        if (request.getFirstname() == null || request.getFirstname().isBlank()) {
            throw new InvalidFieldException("Firstname cannot be null or empty");
        }

        if (request.getLastname() == null || request.getLastname().isBlank()) {
            throw new InvalidFieldException("Lastname cannot be null or empty");
        }

        if (request.getDepartment() == null || request.getDepartment().isBlank()) {
            throw new InvalidFieldException("Department cannot be null or empty");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new InvalidFieldException("Email cannot be null or empty");
        }

        if (!isValidEmailAddress(request.getEmail())) {
            throw new InvalidFieldException("Email address format is incorrect");
        }

        var employee =  new Employee();
        employee.setFirstname(request.getFirstname());
        employee.setLastname(request.getLastname());
        employee.setDepartment(request.getDepartment());

        employeeRepository.save(employee);
        return new EmployeeModel(employee);
    }

    public EmployeeModel editEmployee(Integer id, EmployeeEditRequest request) throws InvalidFieldException, NotFoundException {
        if (id == null) {
            throw new InvalidFieldException("Id cannot be null");
        }

        var employee = employeeRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Provided id does not exist"));

        if (request.getLastname() == null || request.getLastname().isBlank()) {
            throw new InvalidFieldException("Lastname cannot be null or empty");
        }

        if (request.getDepartment() == null || request.getDepartment().isBlank()) {
            throw new InvalidFieldException("Name cannot be null or empty");
        }

        if (!isValidEmailAddress(request.getEmail())) {
            throw new InvalidFieldException("Email address format is incorrect");
        }

        employee.setFirstname(request.getFirstname());
        employee.setLastname(request.getLastname());
        employee.setDepartment(request.getDepartment());
        employee.setEmail(request.getEmail());

        employeeRepository.save(employee);
        return new EmployeeModel(employee);
    }

    public List<TaskModel> getEmployeeTasks(Integer id) throws InvalidFieldException, NotFoundException {
        if (id == null) {
            throw new InvalidFieldException("Id cannot be null");
        }

        var employee = employeeRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Provided id does not exist"));

        var results = taskRepository.findAllByAssignee(employee);

        return results.stream()
                .map(TaskModel::new)
                .collect(toList());
    }

    //TODO Enhance validator
    // 1@com is considered to be a valid address
    private static boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }
}
