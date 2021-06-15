package za.co.learnings.todolist.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.learnings.todolist.api.repository.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
}
