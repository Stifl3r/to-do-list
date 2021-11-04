package za.co.learnings.todolist.api.jsreport;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class JsReportClient {

    private final RestTemplate restTemplate;

    @Value("${jsReportHostUrl}")
    private String jsReportUrl;

    public JsReportClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public JsReportRequest<JsReportBaseData> createCSVRequest(JsReportBaseData data){
        var templateOptions = new TemplateOptions();
        final String csvBaseTemplate = "{{for dataHeadings}}{{if #index !== 0}},{{/if}}\"{{:#data}}\"{{/for}}\n" +
                "{{for rows}}{{for #data}}{{if #index !== 0}},{{/if}}\"{{:#data}}\"{{/for}}\n{{/for}}";
        templateOptions.setContent(csvBaseTemplate);
        templateOptions.setRecipe("text");
        templateOptions.setEngine("jsrender");

        var jsReportRequest = new JsReportRequest<JsReportBaseData>();
        jsReportRequest.setTemplate(templateOptions);

        jsReportRequest.setData(data);
        Map<String, Object> options = new HashMap<>();
        Map<String, Object> save = new HashMap<>();
        save.put("save", true);
        options.put("reports", save);
        jsReportRequest.setOptions(options);
        return jsReportRequest;
    }

    public OutputStream sendAndWriteToBuffer(JsReportRequest<?> jsReportRequest) {
        var converter = new ByteArrayHttpMessageConverter();
        var outputStream = new ByteArrayOutputStream();
        restTemplate.getMessageConverters().add(converter);

        var headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
        var entity = new HttpEntity<>(jsReportRequest, headers);


        var res =  executeCallToJsReport(jsReportRequest, outputStream, entity);
        //Not sure if keeping it there will break other stuff. So let's just remove it
        restTemplate.getMessageConverters().remove(converter);
        return res;
    }

    private OutputStream executeCallToJsReport(JsReportRequest<?> jsReportRequest, OutputStream outputStream, HttpEntity<? extends JsReportRequest<?>> entity) {
        var response = restTemplate.exchange(jsReportUrl, HttpMethod.POST, entity, byte[].class, "1");

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                outputStream.write(Objects.requireNonNull(response.getBody()));
            } catch (IOException e) {
                log.warn("Writing data to stream failed " + e.getMessage());
            }
        } else {
            log.error(String.format("JS REPORT failed with %s", jsReportRequest));
        }
        return outputStream;
    }
}
