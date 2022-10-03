package za.co.learnings.todolist.api.service;

import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
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
import za.co.learnings.todolist.api.jsreport.JsReportBaseData;
import za.co.learnings.todolist.api.jsreport.JsReportClient;
import za.co.learnings.todolist.api.jsreport.JsReportRequest;
import za.co.learnings.todolist.api.repository.EmployeeRepository;
import za.co.learnings.todolist.api.repository.TaskRepository;
import za.co.learnings.todolist.api.testmodel.TaskEditRequestBuilder;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.TRUE;
import static java.time.LocalDateTime.now;
import static java.util.List.of;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static za.co.learnings.todolist.api.controller.model.StatusType.BACK_LOG;
import static za.co.learnings.todolist.api.controller.model.StatusType.TESTING;
import static za.co.learnings.todolist.api.testmodel.EmployeeBuilder.anEmployee;
import static za.co.learnings.todolist.api.testmodel.TaskBuilder.aTask;
import static za.co.learnings.todolist.api.testmodel.TaskCreateRequestBuilder.aTaskCreateRequest;
import static za.co.learnings.todolist.api.testmodel.TaskEditRequestBuilder.aTaskEditRequest;

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

    @MockBean
    private JsReportClient jsReportClient;

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
    public void getAllTasksWhenOverdueIsTrueShouldReturnListOfTasks() {
        //Given
        var task = aTask().build();
        var expected = new java.util.ArrayList<>(of(new TaskModel(task)));
        expected.clear();

        given(taskRepository.findTasksByFilter(now()))
                .willReturn(Collections.emptyList());


        //when
        var actual = taskService.getAllTasks(true);

        //Then
        assertEquals(expected.size(), actual.size());
    }

    @Test
    public void getAllTasksWhenOverdueIsFalseShouldReturnListOfTasks() {
        //Given
        var task = aTask().build();
        var expected = of(new TaskModel(task));

        given(taskRepository.findAllByOrderByTaskIdAsc())
                .willReturn(of(task));

        //when
        var actual = taskService.getAllTasks(false);

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

    @Test
    public void createTaskWhenNameIsNullShouldReturnInvalidField() {
        //Given
        var request = aTaskCreateRequest()
                .withName(null)
                .build();

        //When
        var thrown = catchThrowable(() -> taskService.createTask(request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Name cannot be null or empty", thrown.getMessage());
    }

    @Test
    public void createTaskWhenNameIsEmptyShouldReturnInvalidField() {
        //Given
        var request = aTaskCreateRequest()
                .withName(" ")
                .build();

        //When
        var thrown = catchThrowable(() -> taskService.createTask(request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Name cannot be null or empty", thrown.getMessage());
    }

    @Test
    public void createTaskWhenReporterIdIsNullShouldReturnInvalidField() {
        //Given
        var request = aTaskCreateRequest()
                .withReporterId(null)
                .build();

        //When
        var thrown = catchThrowable(() -> taskService.createTask(request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("ReporterId cannot be null", thrown.getMessage());
    }

    @Test
    public void createTaskWhenReporterIdDoesNotExistShouldReturnNotFound() {
        //Given
        var request = aTaskCreateRequest()
                .withReporterId(5)
                .build();
        var reporter = anEmployee().build();
        given(employeeRepository.findById(reporter.getEmployeeId()))
                .willReturn(Optional.of(reporter));

        //When
        var thrown = catchThrowable(() -> taskService.createTask(request));

        //Then
        assertThat(thrown).isInstanceOf(NotFoundException.class);
        assertEquals("Provided reporterId does not exist", thrown.getMessage());
    }

    @Test
    public void createTaskWhenAssigneeIdDoesNotExistShouldReturnNotFound() {
        //Given
        var request = aTaskCreateRequest()
                .withAssigneeId(5)
                .build();
        var assignee = anEmployee().build();
        given(employeeRepository.findById(assignee.getEmployeeId()))
                .willReturn(Optional.of(assignee));

        //When
        var thrown = catchThrowable(() -> taskService.createTask(request));

        //Then
        assertThat(thrown).isInstanceOf(NotFoundException.class);
        assertEquals("Provided assigneeId does not exist", thrown.getMessage());
    }

    @Test
    public void createTaskShouldReturnSuccess() throws NotFoundException, InvalidFieldException {
        //Given
        var request = aTaskCreateRequest()
                .withAssigneeId(5)
                .build();

        var reporter = anEmployee().build();
        given(employeeRepository.findById(reporter.getEmployeeId()))
                .willReturn(Optional.of(reporter));

        var assignee = anEmployee()
                .withEmployeeId(request.getAssigneeId())
                .build();
        given(employeeRepository.findById(assignee.getEmployeeId()))
                .willReturn(Optional.of(assignee));

        var task = aTask()
                .withReporter(reporter)
                .withAssignee(assignee)
                .withStatus(BACK_LOG.getValue())
                .withDeadline(request.getDeadline())
                .withCreateDate(null)
                .build();
        var expected = new TaskModel(task);

        given(taskRepository.save(task))
                .willReturn(task);

        //When
        var actual =  taskService.createTask(request);

        //Then
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringActualNullFields()
                .ignoringExpectedNullFields()
                .isEqualTo(expected);
    }

    @Test
    public void createTaskWhenAssigneeIsNullShouldReturnSuccess() throws NotFoundException, InvalidFieldException {
        //Given
        var request = aTaskCreateRequest()
                .withAssigneeId(null)
                .build();

        var reporter = anEmployee().build();
        given(employeeRepository.findById(reporter.getEmployeeId()))
                .willReturn(Optional.of(reporter));

        var assignee = anEmployee()
                .withEmployeeId(request.getAssigneeId())
                .build();
        given(employeeRepository.findById(assignee.getEmployeeId()))
                .willReturn(Optional.of(assignee));

        var task = aTask()
                .withReporter(reporter)
                .withAssignee(null)
                .withStatus(BACK_LOG.getValue())
                .withDeadline(request.getDeadline())
                .withCreateDate(null)
                .build();
        var expected = new TaskModel(task);

        given(taskRepository.save(task))
                .willReturn(task);

        //When
        var actual =  taskService.createTask(request);

        //Then
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringActualNullFields()
                .ignoringExpectedNullFields()
                .isEqualTo(expected);
    }

    @Test
    public void editTaskWhenIdIsNullShouldReturnInvalidField() {
        //Given
        var request = aTaskEditRequest().build();

        //When
        var thrown = catchThrowable(()-> taskService.editTask(null, request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Id cannot be null", thrown.getMessage());
    }

    @Test
    public void editTaskWhenIdDoesNotExistShouldReturnNotFound() {
        //Given
        var request = aTaskEditRequest().build();
        var task = aTask().build();
        given(taskRepository.findById(task.getTaskId()))
                .willReturn(Optional.of(task));

        //When
        var thrown = catchThrowable(() -> taskService.editTask(999, request));

        //Then
        assertThat(thrown).isInstanceOf(NotFoundException.class);
        Assertions.assertEquals("Provided id does not exist", thrown.getMessage());
    }

    @Test
    public void editTaskWhenNameIsNullShouldReturnInvalidField() {
        //Given
        var request = aTaskEditRequest()
                .withName(null)
                .build();
        var task = aTask().build();
        given(taskRepository.findById(task.getTaskId()))
                .willReturn(Optional.of(task));

        //When
        var thrown = catchThrowable(() -> taskService.editTask(task.getTaskId(), request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Name cannot be null or empty", thrown.getMessage());

    }

    @Test
    public void editTaskWhenNameIsBlankShouldReturnInvalidField() {
        //Given
        var request = aTaskEditRequest()
                .withName("")
                .build();
        var task = aTask().build();
        given(taskRepository.findById(task.getTaskId()))
                .willReturn(Optional.of(task));

        //When
        var thrown = catchThrowable(() -> taskService.editTask(task.getTaskId(), request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Name cannot be null or empty", thrown.getMessage());

    }

    @Test
    public void editTaskWhenAssigneeIdDoesNotExistShouldReturnNotFound() {
        //Given
        var request = aTaskEditRequest()
                .withAssigneeId(5)
                .build();
        var task = aTask().build();
        var assignee = anEmployee().build();
        given(taskRepository.findById(task.getTaskId()))
                .willReturn(Optional.of(task));
        given(employeeRepository.findById(999))
                .willReturn(Optional.empty());

        //When
        var thrown = catchThrowable(() -> taskService.editTask( task.getTaskId() ,request));

        //Then
        assertThat(thrown).isInstanceOf(NotFoundException.class);
        assertEquals("Provided assigneeId does not exist", thrown.getMessage());
    }


    @Test
    public void editTaskWhenAssigneeIdIsNullShouldReturnSuccess() throws NotFoundException, InvalidFieldException {
        //Given
        var request = aTaskEditRequest()
                .withAssigneeId(null)
                .withStatus(TESTING)
                .build();
        var task = aTask()
                .withAssignee(null)
                .withDeadline(request.getDeadline())
                .withStatusUpdate(null)
                .build();
        given(taskRepository.findById(task.getTaskId()))
                .willReturn(Optional.of(task));


        var expected = new TaskModel(task);

        given(taskRepository.save(task))
                .willReturn(task);

        //When
        var actual =  taskService.editTask(task.getTaskId() ,request);

        //Then
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringActualNullFields()
                .ignoringExpectedNullFields()
                .isEqualTo(expected);
    }

    @Test
    public void editTaskWhenShouldReturnSuccess() throws NotFoundException, InvalidFieldException {
        //Given
        var assignee = anEmployee().build();
        var request = aTaskEditRequest()
                .withAssigneeId(assignee.getEmployeeId())
                .withStatus(TESTING)
                .build();

        var task = aTask()
                .withAssignee(assignee)
                .withDeadline(request.getDeadline())
                .withStatusUpdate(null)
                .build();
        given(taskRepository.findById(task.getTaskId()))
                .willReturn(Optional.of(task));
        given(employeeRepository.findById(task.getTaskId()))
                .willReturn(Optional.of(assignee));


        var expected = new TaskModel(task);

        given(taskRepository.save(task))
                .willReturn(task);

        //When
        var actual =  taskService.editTask(task.getTaskId() ,request);

        //Then
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringActualNullFields()
                .ignoringExpectedNullFields()
                .isEqualTo(expected);
    }

//    @Test
//    public void getTasksForReportShouldReturnSuccess() {
//        var task = aTask().build();
//        var tasks = List.of(task);
//
//        var list = tasks.stream()
//                .map(taskModel -> {
//                    List<String> cols = new ArrayList<>();
//                    cols.add(taskModel.getName());
//                    cols.add(taskModel.getDescription());
//                    cols.add(taskModel.getStatus());
//                    cols.add(taskModel.getDeadline().toString());
//                    cols.add(taskModel.getAssignee() == null ?
//                            null : taskModel.getAssignee().getLastname() + " " + taskModel.getAssignee().getFirstname());
//
//                    return cols;
//                }).collect(toList());
//
//        list.add(List.of("NUMBEROFROWS:" + (list.size() + 2)));
//
//        var data = new JsReportBaseData();
//        data.setRows(list);
//        data.setDataHeadings(List.of("Name", "Description", "Status", "Due Date", "Assignee"));
//
//        given(taskRepository.findTasksByFilter(now()))
//                .willReturn(Collections.emptyList());
//
//        var csvRequest = new JsReportRequest<JsReportBaseData>();
//        given(jsReportClient.createCSVRequest(data))
//                .willReturn(csvRequest);
//
//        var outputStream = new ByteArrayOutputStream();
//        outputStream.write(1);
//        given(jsReportClient.sendAndWriteToBuffer(csvRequest))
//                .willReturn(outputStream);
//
//
//        //When
//        var actual = taskService.getTasksForReport();
//    }

}
