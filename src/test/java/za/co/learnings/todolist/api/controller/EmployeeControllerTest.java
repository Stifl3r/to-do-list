package za.co.learnings.todolist.api.controller;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import za.co.learnings.todolist.api.controller.model.EmployeeModel;
import za.co.learnings.todolist.api.controller.model.TaskModel;
import za.co.learnings.todolist.api.controller.model.request.EmployeeCreateRequest;
import za.co.learnings.todolist.api.exception.InvalidFieldException;
import za.co.learnings.todolist.api.exception.NotFoundException;
import za.co.learnings.todolist.api.service.EmployeeService;
import za.co.learnings.todolist.api.testmodel.TaskBuilder;

import static java.util.List.of;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static za.co.learnings.todolist.api.testmodel.EmployeeBuilder.anEmployee;
import static za.co.learnings.todolist.api.testmodel.EmployeeCreateRequestBuilder.anEmployeeCreateRequest;

@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmployeeService employeeService;


    @Test
    public void getListOfEmployeesShouldReturnEmployeesList() throws Exception {
        var expected = anEmployee().build();

        given(employeeService.getAllEmployees())
                .willReturn(of(new EmployeeModel(expected)));

        this.mockMvc.perform(get("/api/employees"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void getEmployeeByIdShouldReturnEmployeeModel() throws Exception {
        var expected = anEmployee()
                .withEmployeeId(99)
                .build();

        given(employeeService.getEmployeeById(99))
                .willReturn(new EmployeeModel(expected));

        var actual = this.mockMvc.perform(get("/api/employees/99"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        var actualResponseBody = actual.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(expected));
    }

    @Test
    public void getEmployeeByIdWHenIdDoesNotExistShouldReturnNotFound() throws Exception {
        given(employeeService.getEmployeeById(999))
                .willThrow(new NotFoundException("Provided id does not exist"));

        this.mockMvc.perform(get("/api/employees/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].message").value("Provided id does not exist"));
    }

    @Test
    public void createEmployeeWhenFirstnameIsNullShouldReturnBadRequest() throws Exception {
        var request = anEmployeeCreateRequest()
                .withFirstname(null)
                .build();

        given(employeeService.createEmployee(any(EmployeeCreateRequest.class)))
                .willThrow(new InvalidFieldException("Firstname cannot be null or empty"));

        this.mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message").value("Firstname cannot be null or empty"));
    }

    @Test
    public void getEmployeeTasksShouldReturnSuccess() throws Exception {
        var expected = TaskBuilder.aTask().build();

        given(employeeService.getEmployeeTasks(1))
                .willReturn(of(new TaskModel(expected)));

        this.mockMvc.perform(get("/api/employees/1/tasks"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void createEmployeeShouldReturnSuccess() throws Exception {
        var request = anEmployeeCreateRequest().build();
        var expected = anEmployee().build();

        given(employeeService.createEmployee(any(EmployeeCreateRequest.class)))
                .willReturn(new EmployeeModel(expected));

        var response = this.mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        assertThat(response.getContentAsString()).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(expected));
    }

}
