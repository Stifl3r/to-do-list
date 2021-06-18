package za.co.learnings.todolist.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import za.co.learnings.todolist.api.repository.entity.Employee;
import za.co.learnings.todolist.api.repository.entity.Task;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    List<Task> findAllByAssignee(Employee employee);

    List<Task> findAllByOrderByTaskIdAsc();

    @Query("select t" +
           "  from Task t " +
           " where t.status not in ('COMPLETED', 'DEPLOYED_TO_PRODUCTION') " +
           "   and t.statusUpdate > t.deadline " +
           " order by t.taskId asc ")
    List<Task> findTasksByFilter();
}
