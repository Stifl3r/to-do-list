package za.co.learnings.todolist.api.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import za.co.learnings.todolist.api.controller.model.EmployeeModel;
import za.co.learnings.todolist.api.controller.model.TaskModel;
import za.co.learnings.todolist.api.exception.InvalidFieldException;
import za.co.learnings.todolist.api.exception.NotFoundException;
import za.co.learnings.todolist.api.repository.EmployeeRepository;
import za.co.learnings.todolist.api.repository.TaskRepository;
import za.co.learnings.todolist.api.testmodel.TaskBuilder;

import java.util.List;
import java.util.Optional;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static za.co.learnings.todolist.api.testmodel.EmployeeBuilder.anEmployee;
import static za.co.learnings.todolist.api.testmodel.EmployeeCreateRequestBuilder.anEmployeeCreateRequest;
import static za.co.learnings.todolist.api.testmodel.EmployeeEditRequestBuilder.anEmployeeEditRequest;
import static za.co.learnings.todolist.api.testmodel.TaskBuilder.aTask;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EmployeeService.class)
@ActiveProfiles("local")
public class EmployeeServiceTest {

    @Autowired
    private EmployeeService employeeService;

    @MockBean
    private EmployeeRepository employeeRepository;

    @MockBean
    private TaskRepository taskRepository;

    @Test
    public void getAllEmployeesShouldReturnListOfEmployees() {
        //Given
        var employee = anEmployee()
                .withEmployeeId(1)
                .build();
        var employee2 = anEmployee()
                .withEmployeeId(2)
                .build();
        var expected = of(new EmployeeModel(employee),
                new EmployeeModel(employee2));

        given(employeeRepository.findAll())
                .willReturn(of(employee,employee2));

        //When
        var actual = employeeService.getAllEmployees();

        //Then
        assertEquals(expected.size(), actual.size());
        assertThat(actual.get(0))
                .usingRecursiveComparison()
                .ignoringActualNullFields()
                .ignoringExpectedNullFields()
                .isEqualTo(expected.get(0));
    }

    @Test
    public void getEmployeeByIdShouldReturnEmployeeModel() throws NotFoundException, InvalidFieldException {
        //Given
        var employee = anEmployee().build();
        var expected = new EmployeeModel(employee);

        given(employeeRepository.findById(employee.getEmployeeId()))
                .willReturn(Optional.of(employee));

        //When
        var actual = employeeService.getEmployeeById(1);

        //Then
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringActualNullFields()
                .ignoringExpectedNullFields()
                .isEqualTo(expected);
    }

    @Test
    public void getEmployeeByIdWhenIdIsNullShouldReturnInvalidField(){
        //Given
        //When
        var thrown = catchThrowable(() -> employeeService.getEmployeeById(null));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Id cannot be null", thrown.getMessage());
    }

    @Test
    public void getEmployeeByIdWhenIdDoesNotExistShouldReturnNotFound() {
        //Given
        var employee = anEmployee().build();

        given(employeeRepository.findById(employee.getEmployeeId()))
                .willReturn(Optional.of(employee));

        //When
        var thrown = catchThrowable(() ->
            employeeService.getEmployeeById(999)
        );

        assertThat(thrown).isInstanceOf(NotFoundException.class);
        Assertions.assertEquals("Provided id does not exist", thrown.getMessage());
    }

    @Test
    public void createEmployeeWhenFirstnameIsNullShouldReturnInvalidField(){
        //Given
        var request = anEmployeeCreateRequest()
                .withFirstname(null)
                .build();

        //When
        var thrown = catchThrowable(()-> employeeService.createEmployee(request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Firstname cannot be null or empty", thrown.getMessage());
    }

    @Test
    public void createEmployeeWhenFirstnameIsBlankShouldReturnInvalidField(){
        //Given
        var request = anEmployeeCreateRequest()
                .withFirstname("")
                .build();

        //When
        var thrown = catchThrowable(()-> employeeService.createEmployee(request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Firstname cannot be null or empty", thrown.getMessage());
    }

    @Test
    public void createEmployeeWhenLastnameIsNullShouldReturnInvalidField(){
        //Given
        var request = anEmployeeCreateRequest()
                .withLastname(null)
                .build();

        //When
        var thrown = catchThrowable(()-> employeeService.createEmployee(request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Lastname cannot be null or empty", thrown.getMessage());
    }

    @Test
    public void createEmployeeWhenLastnameIsBlankShouldReturnInvalidField(){
        //Given
        var request = anEmployeeCreateRequest()
                .withLastname("")
                .build();

        //When
        var thrown = catchThrowable(()-> employeeService.createEmployee(request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Lastname cannot be null or empty", thrown.getMessage());
    }

    @Test
    public void createEmployeeWhenDepartmentIsNullShouldReturnInvalidField(){
        //Given
        var request = anEmployeeCreateRequest()
                .withDepartment(null)
                .build();

        //When
        var thrown = catchThrowable(()-> employeeService.createEmployee(request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Department cannot be null or empty", thrown.getMessage());
    }

    @Test
    public void createEmployeeWhenDepartmentIsBlankShouldReturnInvalidField(){
        //Given
        var request = anEmployeeCreateRequest()
                .withDepartment("")
                .build();

        //When
        var thrown = catchThrowable(()-> employeeService.createEmployee(request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Department cannot be null or empty", thrown.getMessage());
    }

    @Test
    public void createEmployeeWhenEmailIsNullShouldReturnInvalidField(){
        //Given
        var request = anEmployeeCreateRequest()
                .withEmail(null)
                .build();

        //When
        var thrown = catchThrowable(()-> employeeService.createEmployee(request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Email cannot be null or empty", thrown.getMessage());
    }

    @Test
    public void createEmployeeWhenEmailIsEmptyShouldReturnInvalidField(){
        //Given
        var request = anEmployeeCreateRequest()
                .withEmail("")
                .build();

        //When
        var thrown = catchThrowable(()-> employeeService.createEmployee(request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Email cannot be null or empty", thrown.getMessage());
    }

    @Test
    public void createEmployeeWhenEmailIsInvalidShouldReturnInvalidField(){
        //Given
        var request = anEmployeeCreateRequest()
                .withEmail("test")
                .build();

        //When
        var thrown = catchThrowable(()-> employeeService.createEmployee(request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Email address format is incorrect", thrown.getMessage());
    }

    @Test
    public void createEmployeeShouldReturnSuccess() throws InvalidFieldException {
        //Given
        var request = anEmployeeCreateRequest().build();
        var employee = anEmployee()
                .withDepartment("IOT")
                .build();

        var expected = new EmployeeModel(employee);

        given(employeeRepository.save(employee))
                .willReturn(employee);

        //When
        var actual = employeeService.createEmployee(request);

        //Then
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringActualNullFields()
                .ignoringExpectedNullFields()
                .isEqualTo(expected);
    }

    @Test
    public void editEmployeeWhenIdIsNullShouldReturnInvalidField() {
        //Given
        var request = anEmployeeEditRequest()
                .build();

        //When
        var thrown = catchThrowable(()-> employeeService.editEmployee(null, request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Id cannot be null", thrown.getMessage());
    }

    @Test
    public void editEmployeeWhenIdDoesNotExistShouldReturnNotFound() {
        //Given
        var request = anEmployeeEditRequest()
                .build();
        var employee = anEmployee().build();
        given(employeeRepository.findById(employee.getEmployeeId()))
                .willReturn(Optional.of(employee));

        //When
        var thrown = catchThrowable(() ->
                employeeService.editEmployee(999, request)
        );

        //Then
        assertThat(thrown).isInstanceOf(NotFoundException.class);
        Assertions.assertEquals("Provided id does not exist", thrown.getMessage());
    }

    @Test
    public void editEmployeeWhenFirstnameIsNullShouldReturnInvalidField(){
        //Given
        var employee = anEmployee().build();
        given(employeeRepository.findById(employee.getEmployeeId()))
                .willReturn(Optional.of(employee));
        var request = anEmployeeEditRequest()
                .withFirstname(null)
                .build();

        //When
        var thrown = catchThrowable(()-> employeeService.editEmployee(1,request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Firstname cannot be null or empty", thrown.getMessage());
    }

    @Test
    public void editEmployeeWhenFirstnameIsBlankShouldReturnInvalidField(){
        //Given
        var employee = anEmployee().build();
        given(employeeRepository.findById(employee.getEmployeeId()))
                .willReturn(Optional.of(employee));
        var request = anEmployeeEditRequest()
                .withFirstname("")
                .build();

        //When
        var thrown = catchThrowable(()-> employeeService.editEmployee(1, request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Firstname cannot be null or empty", thrown.getMessage());
    }

    @Test
    public void editEmployeeWhenLastnameIsNullShouldReturnInvalidField(){
        //Given
        var employee = anEmployee().build();
        given(employeeRepository.findById(employee.getEmployeeId()))
                .willReturn(Optional.of(employee));
        var request = anEmployeeEditRequest()
                .withLastname(null)
                .build();

        //When
        var thrown = catchThrowable(()-> employeeService.editEmployee(1,request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Lastname cannot be null or empty", thrown.getMessage());
    }

    @Test
    public void editEmployeeWhenLastnameIsBlankShouldReturnInvalidField(){
        //Given
        var employee = anEmployee().build();
        given(employeeRepository.findById(employee.getEmployeeId()))
                .willReturn(Optional.of(employee));
        var request = anEmployeeEditRequest()
                .withLastname("")
                .build();

        //When
        var thrown = catchThrowable(()-> employeeService.editEmployee(1, request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Lastname cannot be null or empty", thrown.getMessage());
    }

    @Test
    public void editEmployeeWhenDepartmentIsNullShouldReturnInvalidField(){
        //Given
        var employee = anEmployee().build();
        given(employeeRepository.findById(employee.getEmployeeId()))
                .willReturn(Optional.of(employee));
        var request = anEmployeeEditRequest()
                .withDepartment(null)
                .build();

        //When
        var thrown = catchThrowable(()-> employeeService.editEmployee(1, request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Department cannot be null or empty", thrown.getMessage());
    }

    @Test
    public void editEmployeeWhenDepartmentIsBlankShouldReturnInvalidField(){
        //Given
        var employee = anEmployee().build();
        given(employeeRepository.findById(employee.getEmployeeId()))
                .willReturn(Optional.of(employee));
        var request = anEmployeeEditRequest()
                .withDepartment("")
                .build();

        //When
        var thrown = catchThrowable(()-> employeeService.editEmployee(1, request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Department cannot be null or empty", thrown.getMessage());
    }

    @Test
    public void editEmployeeWhenEmailIsNullShouldReturnInvalidField(){
        //Given
        var employee = anEmployee().build();
        given(employeeRepository.findById(employee.getEmployeeId()))
                .willReturn(Optional.of(employee));
        var request = anEmployeeEditRequest()
                .withEmail(null)
                .build();

        //When
        var thrown = catchThrowable(()-> employeeService.editEmployee(1, request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Email cannot be null or empty", thrown.getMessage());
    }

    @Test
    public void editEmployeeWhenEmailIsBlankShouldReturnInvalidField(){
        //Given
        var employee = anEmployee().build();
        given(employeeRepository.findById(employee.getEmployeeId()))
                .willReturn(Optional.of(employee));
        var request = anEmployeeEditRequest()
                .withEmail("")
                .build();

        //When
        var thrown = catchThrowable(()-> employeeService.editEmployee(1, request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Email cannot be null or empty", thrown.getMessage());
    }

    @Test
    public void editEmployeeWhenEmailIsIncorrectShouldReturnInvalidField(){
        //Given
        var employee = anEmployee().build();
        given(employeeRepository.findById(employee.getEmployeeId()))
                .willReturn(Optional.of(employee));
        var request = anEmployeeEditRequest()
                .withEmail("test")
                .build();

        //When
        var thrown = catchThrowable(()-> employeeService.editEmployee(1, request));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Email address format is incorrect", thrown.getMessage());
    }

    @Test
    public void editEmployeeShouldReturnSuccess() throws NotFoundException, InvalidFieldException {
        //Given
        var employee = anEmployee()
                .withDepartment("IOT")
                .build();
        given(employeeRepository.findById(employee.getEmployeeId()))
                .willReturn(Optional.of(employee));
        var request = anEmployeeEditRequest().build();
        var expected = new EmployeeModel(employee);

        //When
        var actual = employeeService.editEmployee(employee.getEmployeeId(), request);

        //Then
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringActualNullFields()
                .ignoringExpectedNullFields()
                .isEqualTo(expected);
    }

    @Test
    public void getEmployeeTasksWhenIdIsNullShouldReturnInvalidField() {
        //Given
        //When
        var thrown = catchThrowable(()-> employeeService.getEmployeeTasks(null));

        //Then
        assertThat(thrown).isInstanceOf(InvalidFieldException.class);
        Assertions.assertEquals("Id cannot be null", thrown.getMessage());
    }

    @Test
    public void getEmployeeTasksWhenIdDoesNotExistShouldReturnNotFound() {
        //Given
        var employee = anEmployee().build();
        given(employeeRepository.findById(employee.getEmployeeId()))
                .willReturn(Optional.of(employee));

        //When
        var thrown = catchThrowable(() ->
                employeeService.getEmployeeTasks(999)
        );

        //Then
        assertThat(thrown).isInstanceOf(NotFoundException.class);
        Assertions.assertEquals("Provided id does not exist", thrown.getMessage());
    }

    @Test
    public void getEmployeeTasksShouldReturnSuccess() throws NotFoundException, InvalidFieldException {
        //Given
        var employee = anEmployee().build();
        given(employeeRepository.findById(employee.getEmployeeId()))
                .willReturn(Optional.of(employee));
        var tasks = List.of(aTask().build());
        given(taskRepository.findAllByAssignee(employee))
                .willReturn(tasks);
        var expected = List.of(new TaskModel(aTask()
                //TODO Find correct comparor
                .withDeadline(null)
                .withCreateDate(null)
                .withStatusUpdate(null)
                .build()));

        //When
        var actual = employeeService.getEmployeeTasks(employee.getEmployeeId());

        //Then
        assertEquals(expected.size(), actual.size());
        assertThat(actual.get(0))
                .usingRecursiveComparison()
                .ignoringActualNullFields()
                .ignoringExpectedNullFields()
                .isEqualTo(expected.get(0));
    }
}
