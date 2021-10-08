package za.co.learnings.todolist.api.testmodel;

import za.co.learnings.todolist.api.controller.model.request.EmployeeCreateRequest;

public class EmployeeCreateRequestBuilder implements Builder<EmployeeCreateRequest>{
    private String firstname = "John";
    private String lastname = "Smith";
    private String department = "IOT";
    private String email = "a@b.com";

    @Override
    public EmployeeCreateRequest build() {
        var employeeCreateRequest = new EmployeeCreateRequest();
        employeeCreateRequest.setFirstname(firstname);
        employeeCreateRequest.setLastname(lastname);
        employeeCreateRequest.setDepartment(department);
        employeeCreateRequest.setEmail(email);
        return employeeCreateRequest;
    }

    public static EmployeeCreateRequestBuilder anEmployeeCreateRequest() {
        return new EmployeeCreateRequestBuilder();
    }

    public EmployeeCreateRequestBuilder withFirstname(String firstname) {
        this.firstname = firstname;
        return this;
    }

    public EmployeeCreateRequestBuilder withLastname(String lastname) {
        this.lastname = lastname;
        return this;
    }

    public EmployeeCreateRequestBuilder withDepartment(String department) {
        this.department = department;
        return this;
    }

    public EmployeeCreateRequestBuilder withEmail(String email) {
        this.email = email;
        return this;
    }
}
