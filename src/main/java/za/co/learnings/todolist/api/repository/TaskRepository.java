package za.co.learnings.todolist.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.learnings.todolist.api.repository.entity.Employee;
import za.co.learnings.todolist.api.repository.entity.Task;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    List<Task> findAllByAssignee(Employee employee);
}
