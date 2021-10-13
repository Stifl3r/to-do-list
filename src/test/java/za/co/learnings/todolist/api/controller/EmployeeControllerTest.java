package za.co.learnings.todolist.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import za.co.learnings.todolist.api.controller.model.EmployeeModel;
import za.co.learnings.todolist.api.service.EmployeeService;

import static java.util.List.of;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static za.co.learnings.todolist.api.testmodel.EmployeeBuilder.anEmployee;

@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
}
