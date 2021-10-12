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
import za.co.learnings.todolist.api.exception.InvalidFieldException;
import za.co.learnings.todolist.api.exception.NotFoundException;
import za.co.learnings.todolist.api.repository.EmployeeRepository;
import za.co.learnings.todolist.api.repository.TaskRepository;

import java.util.Optional;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static za.co.learnings.todolist.api.testmodel.EmployeeBuilder.anEmployee;
import static za.co.learnings.todolist.api.testmodel.EmployeeCreateRequestBuilder.anEmployeeCreateRequest;

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
}
