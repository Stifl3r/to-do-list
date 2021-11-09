package za.co.learnings.todolist.api.testmodel;

import za.co.learnings.todolist.api.controller.model.request.EmployeeEditRequest;

public class EmployeeEditRequestBuilder implements Builder<EmployeeEditRequest>{
    private String firstname = "John";
    private String lastname = "Smith";
    private String department = "IOT";
    private String email = "a@b.com";

    @Override
    public EmployeeEditRequest build() {
        var employeeEditRequest = new EmployeeEditRequest();
        employeeEditRequest.setFirstname(firstname);
        employeeEditRequest.setLastname(lastname);
        employeeEditRequest.setDepartment(department);
        employeeEditRequest.setEmail(email);
        return employeeEditRequest;
    }

    public static EmployeeEditRequestBuilder anEmployeeEditRequest() {
        return new EmployeeEditRequestBuilder();
    }

    public EmployeeEditRequestBuilder withFirstname(String firstname) {
        this.firstname = firstname;
        return this;
    }

    public EmployeeEditRequestBuilder withLastname(String lastname) {
        this.lastname = lastname;
        return this;
    }

    public EmployeeEditRequestBuilder withDepartment(String department) {
        this.department = department;
        return this;
    }

    public EmployeeEditRequestBuilder withEmail(String email) {
        this.email = email;
        return this;
    }
}
