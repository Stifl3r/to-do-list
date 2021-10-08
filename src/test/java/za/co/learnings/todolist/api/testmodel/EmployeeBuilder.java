package za.co.learnings.todolist.api.testmodel;

import za.co.learnings.todolist.api.repository.entity.Employee;

public class EmployeeBuilder implements Builder<Employee> {

    private Integer employeeId = 1;
    private String firstname = "John";
    private String lastname = "Smith";
    private String department = "FinTech";
    private String email = "a@b.com";

    @Override
    public Employee build() {
        var employee =  new Employee();
        employee.setEmployeeId(employeeId);
        employee.setFirstname(firstname);
        employee.setLastname(lastname);
        employee.setDepartment(department);
        employee.setEmail(email);
        return employee;
    }

    public static EmployeeBuilder anEmployee() {
        return new EmployeeBuilder();
    }

    public EmployeeBuilder withEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
        return this;
    }

    public EmployeeBuilder withFirstname(String firstname) {
        this.firstname = firstname;
        return this;
    }

    public EmployeeBuilder withLastname(String lastname) {
        this.lastname = lastname;
        return this;
    }

    public EmployeeBuilder withDepartment(String department) {
        this.department = department;
        return this;
    }

    public EmployeeBuilder withEmail(String email) {
        this.email = email;
        return this;
    }
}
