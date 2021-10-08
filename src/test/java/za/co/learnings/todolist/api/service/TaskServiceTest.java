package za.co.learnings.todolist.api.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import za.co.learnings.todolist.api.controller.model.TaskModel;
import za.co.learnings.todolist.api.exception.InvalidFieldException;
import za.co.learnings.todolist.api.exception.NotFoundException;
import za.co.learnings.todolist.api.repository.EmployeeRepository;
import za.co.learnings.todolist.api.repository.TaskRepository;

import java.util.Optional;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static za.co.learnings.todolist.api.testmodel.TaskBuilder.aTask;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TaskService.class)
@ActiveProfiles("local")
public class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @MockBean
    private TaskRepository taskRepository;

    @MockBean
    private EmployeeRepository employeeRepository;

    @Test
    public void getAllTasksShouldReturnListOfTasks() {
        //Given
        var task = aTask().build();
        var expected = of(new TaskModel(task));

        given(taskRepository.findAllByOrderByTaskIdAsc())
                .willReturn(of(task));

        //when
        var actual = taskService.getAllTasks(null);

        //Then
        assertEquals(expected.size(), actual.size());
        assertThat(actual.get(0))
                .usingRecursiveComparison()
                .ignoringActualNullFields()// remove these
                .ignoringExpectedNullFields()// if needs be
                .isEqualTo(expected.get(0));
    }

    @Test
    public void getTaskByIdShouldReturnTaskModel() throws NotFoundException, InvalidFieldException {
        //Given
        var task = aTask().build();
        var expected = new TaskModel(task);

        given(taskRepository.findById(task.getTaskId()) )
                .willReturn(Optional.of(task));

        //when
        var actual = taskService.getTaskById(task.getTaskId());

        //Then
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringActualNullFields()
                .ignoringExpectedNullFields()
                .isEqualTo(expected);
    }

    @Test
    public void getTaskByIdWhenIdDoesNotExistShouldReturnNotFound() {

        //Given
        var task = aTask().build();

        given(taskRepository.findById(task.getTaskId()))
                .willReturn(Optional.of(task));

        //when
        var thrown = catchThrowable(() -> {
            taskService.getTaskById(2);
        });

        //Then
        assertThat(thrown).isInstanceOf(NotFoundException.class);
        assertEquals("Provided id does not exist", thrown.getMessage());
    }

    @Test
    public void getTaskByIdWhenValidationFailsShouldReturnInvalidField() {

        //Given
        //when
        var thrown = catchThrowable(() -> {
            taskService.getTaskById(null);
        });

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        assertEquals("Id cannot be null", thrown.getMessage());
    }
}
