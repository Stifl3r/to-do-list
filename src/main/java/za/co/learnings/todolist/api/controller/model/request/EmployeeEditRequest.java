package za.co.learnings.todolist.api.controller.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeEditRequest {
    private String firstname;
    private String lastname;
    private String department;
    private String email;
}
