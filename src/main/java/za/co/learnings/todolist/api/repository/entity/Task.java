package za.co.learnings.todolist.api.repository.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer taskId;
    private String name;
    private String description;
    private LocalDateTime createDate;
    private LocalDateTime deadline;
    private String status;
    private LocalDateTime statusUpdate;

    @ManyToOne
    @JoinColumn(name = "assignee", insertable = true, updatable = false)
    private Employee assignee;

    @ManyToOne
    @JoinColumn(name = "reporter", insertable = true, updatable = false)
    private Employee reporter;
}
