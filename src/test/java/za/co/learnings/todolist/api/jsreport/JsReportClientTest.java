package za.co.learnings.todolist.api.jsreport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class JsReportClientTest {

    private static final String JS_REPORT_URL = "http://jsreport.test/api/report";

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private JsReportClient jsReportClient;

    @BeforeEach
    public void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        jsReportClient = new JsReportClient(restTemplate);
        ReflectionTestUtils.setField(jsReportClient, "jsReportUrl", JS_REPORT_URL);
    }

    @Test
    public void createCSVRequestShouldUseTextRecipeAndSaveReport() {
        //Given
        var data = reportData();

        //When
        var actual = jsReportClient.createCSVRequest(data);

        //Then
        assertEquals("text", actual.getTemplate().getRecipe());
        assertEquals("jsrender", actual.getTemplate().getEngine());
        assertThat(actual.getTemplate().getContent()).contains("{{for dataHeadings}}", "{{for rows}}");
        assertThat(actual.getTemplate().getChrome()).isNull();
        assertThat(actual.getData()).isSameAs(data);
        assertEquals(Map.of("reports", Map.of("save", true)), actual.getOptions());
    }

    @Test
    public void createPDFRequestShouldUseChromePdfRecipeWithLogoHeader() {
        //Given
        var data = reportData();

        //When
        var actual = jsReportClient.createPDFRequest(data);

        //Then
        assertEquals("chrome-pdf", actual.getTemplate().getRecipe());
        assertEquals("jsrender", actual.getTemplate().getEngine());
        assertThat(actual.getTemplate().getContent()).contains("<table>", "{{for dataHeadings}}");
        var chrome = actual.getTemplate().getChrome();
        assertThat(chrome.isDisplayHeaderFooter()).isTrue();
        assertThat(chrome.getHeaderTemplate()).contains("data:image/png;base64,", "Overdue Tasks Report");
        assertThat(chrome.getFooterTemplate()).contains("pageNumber", "totalPages");
        assertEquals(70, chrome.getMarginTop());
        assertEquals(40, chrome.getMarginBottom());
        assertThat(actual.getData()).isSameAs(data);
        assertEquals(Map.of("reports", Map.of("save", true)), actual.getOptions());
    }

    @Test
    public void sendAndWriteToBufferWhenJsReportReturnsOkShouldWriteBody() {
        //Given
        var request = jsReportClient.createCSVRequest(reportData());
        server.expect(requestTo(JS_REPORT_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Accept", MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(jsonPath("$.template.recipe").value("text"))
                .andExpect(jsonPath("$.data.dataHeadings[0]").value("Name"))
                .andRespond(withSuccess("\"Name\"\n\"task\"\n", MediaType.APPLICATION_OCTET_STREAM));
        var convertersBefore = restTemplate.getMessageConverters().size();

        //When
        var actual = (ByteArrayOutputStream) jsReportClient.sendAndWriteToBuffer(request);

        //Then
        server.verify();
        assertEquals("\"Name\"\n\"task\"\n", actual.toString());
        assertEquals(convertersBefore, restTemplate.getMessageConverters().size());
    }

    @Test
    public void sendAndWriteToBufferWhenJsReportReturnsNonOkSuccessShouldReturnEmptyBuffer() {
        //Given
        var request = jsReportClient.createCSVRequest(reportData());
        server.expect(requestTo(JS_REPORT_URL))
                .andRespond(withStatus(HttpStatus.ACCEPTED));

        //When
        var actual = (ByteArrayOutputStream) jsReportClient.sendAndWriteToBuffer(request);

        //Then
        server.verify();
        assertEquals(0, actual.size());
    }

    @Test
    public void sendAndWriteToBufferWhenJsReportFailsShouldPropagateError() {
        //Given
        var request = jsReportClient.createPDFRequest(reportData());
        server.expect(requestTo(JS_REPORT_URL))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        //When
        var thrown = catchThrowable(() -> jsReportClient.sendAndWriteToBuffer(request));

        //Then
        assertThat(thrown).isInstanceOf(HttpServerErrorException.class);
    }

    private static JsReportBaseData reportData() {
        var data = new JsReportBaseData();
        data.setDataHeadings(List.of("Name"));
        data.setRows(List.of(List.of("task")));
        return data;
    }
}
