package za.co.learnings.todolist.api.controller.model;

import lombok.Getter;
import lombok.Setter;
import za.co.learnings.todolist.api.repository.entity.Employee;

@Getter
@Setter
public class EmployeeModel {
    private Integer employeeId;
    private String firstname;
    private String lastname;
    private String department;
    private String email;

    public EmployeeModel(Employee employee) {
        this.employeeId = employee.getEmployeeId();
        this.firstname =  employee.getFirstname();
        this.lastname = employee.getLastname();
        this.department = employee.getDepartment();
        this.email = employee.getEmail();
    }
}
