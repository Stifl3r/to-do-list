package za.co.learnings.todolist.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import za.co.learnings.todolist.api.controller.model.EmployeeModel;
import za.co.learnings.todolist.api.exception.InvalidFieldException;
import za.co.learnings.todolist.api.exception.NotFoundException;
import za.co.learnings.todolist.api.service.EmployeeService;

import static java.util.List.of;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

    @MockBean
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
        this.mockMvc = MockMvcBuilders
                .standaloneSetup()
                .setControllerAdvice(new GenericControllerAdvice())
                .build();

        given(employeeService.getEmployeeById(999))
                .willThrow(new NotFoundException("Provided id does not exist"));

       var response = this.mockMvc.perform(get("/api/employees/1")
                       .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isNotFound())
               .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString()).isEmpty();
    }

    @Test
    public void createEmployeeWhenFirstnameIsNullShouldReturnBadRequest() throws Exception {
        var expected = anEmployeeCreateRequest()
                .withFirstname(null)
                .build();

        given(employeeService.createEmployee(expected))
                .willThrow(new InvalidFieldException("Firstname cannot be null or empty"));

       var response = this.mockMvc.perform(post("/api/employees", expected).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isEmpty();
    }
}
