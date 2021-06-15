package za.co.learnings.todolist.api.repository.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "Employee")
@Getter
@Setter
public class Employee {

    @Id
    @Column(name = "employeeid")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer employeeId;
    private String firstname;
    private String lastname;
    private String department;
}
