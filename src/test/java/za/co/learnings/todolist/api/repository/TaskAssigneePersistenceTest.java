package za.co.learnings.todolist.api.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import za.co.learnings.todolist.api.repository.entity.Employee;
import za.co.learnings.todolist.api.repository.entity.Task;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

// Regression coverage for Task.assignee's @JoinColumn(updatable = false), which silently
// dropped assignee changes from every UPDATE statement while still reporting success via
// the in-memory entity. Mock-based service/controller tests can't catch a mapping-level
// bug like this, so this exercises the real repositories against the real database.
@SpringBootTest
@ActiveProfiles("local")
@Transactional
public class TaskAssigneePersistenceTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void editingAssigneeShouldPersistAcrossReload() {
        //Given
        var reporter = new Employee();
        reporter.setFirstname("Ada");
        reporter.setLastname("Lovelace");
        reporter.setDepartment("Research");
        reporter.setEmail("ada@example.com");
        reporter = employeeRepository.save(reporter);

        var assignee = new Employee();
        assignee.setFirstname("Grace");
        assignee.setLastname("Hopper");
        assignee.setDepartment("Engineering");
        assignee.setEmail("grace@example.com");
        assignee = employeeRepository.save(assignee);

        var task = new Task();
        task.setName("Persist assignee regression test");
        task.setDescription("Guards against @JoinColumn(assignee, updatable = false) regressions");
        task.setCreateDate(LocalDateTime.now());
        task.setDeadline(LocalDateTime.now().plusDays(1));
        task.setStatus("BACK_LOG");
        task.setStatusUpdate(LocalDateTime.now());
        task.setReporter(reporter);
        task.setAssignee(null);
        task = taskRepository.save(task);
        entityManager.flush();
        entityManager.clear();

        //When
        var toEdit = taskRepository.findById(task.getTaskId()).orElseThrow();
        toEdit.setAssignee(assignee);
        taskRepository.save(toEdit);
        entityManager.flush();
        entityManager.clear();

        //Then
        var reloaded = taskRepository.findById(task.getTaskId()).orElseThrow();
        assertThat(reloaded.getAssignee()).isNotNull();
        assertThat(reloaded.getAssignee().getEmployeeId()).isEqualTo(assignee.getEmployeeId());
    }

    @Test
    public void clearingAssigneeShouldPersistAcrossReload() {
        //Given
        var reporter = new Employee();
        reporter.setFirstname("Katherine");
        reporter.setLastname("Johnson");
        reporter.setDepartment("Research");
        reporter.setEmail("katherine@example.com");
        reporter = employeeRepository.save(reporter);

        var assignee = new Employee();
        assignee.setFirstname("Dorothy");
        assignee.setLastname("Vaughan");
        assignee.setDepartment("Engineering");
        assignee.setEmail("dorothy@example.com");
        assignee = employeeRepository.save(assignee);

        var task = new Task();
        task.setName("Unassign regression test");
        task.setDescription("Guards against @JoinColumn(assignee, updatable = false) regressions");
        task.setCreateDate(LocalDateTime.now());
        task.setDeadline(LocalDateTime.now().plusDays(1));
        task.setStatus("BACK_LOG");
        task.setStatusUpdate(LocalDateTime.now());
        task.setReporter(reporter);
        task.setAssignee(assignee);
        task = taskRepository.save(task);
        entityManager.flush();
        entityManager.clear();

        //When
        var toEdit = taskRepository.findById(task.getTaskId()).orElseThrow();
        toEdit.setAssignee(null);
        taskRepository.save(toEdit);
        entityManager.flush();
        entityManager.clear();

        //Then
        var reloaded = taskRepository.findById(task.getTaskId()).orElseThrow();
        assertThat(reloaded.getAssignee()).isNull();
    }
}
