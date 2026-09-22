package za.co.learnings.todolist.api.jsreport;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class JsReportClient {

    private final RestTemplate restTemplate;
    private final String logoDataUri;

    @Value("${jsReportHostUrl}")
    private String jsReportUrl;

    public JsReportClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.logoDataUri = loadLogoAsDataUri();
    }

    private static String loadLogoAsDataUri() {
        try (var in = new ClassPathResource("images/logo.png").getInputStream()) {
            var base64 = Base64.getEncoder().encodeToString(in.readAllBytes());
            return "data:image/png;base64," + base64;
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to load report logo from classpath:images/logo.png", e);
        }
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

    public JsReportRequest<JsReportBaseData> createPDFRequest(JsReportBaseData data) {
        var chromeOptions = new ChromePdfOptions();
        chromeOptions.setDisplayHeaderFooter(true);
        chromeOptions.setHeaderTemplate(
                "<div style=\"width:100%;font-size:9px;padding:0 24px;display:flex;" +
                "align-items:center;justify-content:space-between;color:#333;" +
                "font-family:Arial,Helvetica,sans-serif;\">" +
                "<img src=\"" + logoDataUri + "\" style=\"height:24px;\" />" +
                "<span>Overdue Tasks Report</span>" +
                "</div>");
        chromeOptions.setFooterTemplate(
                "<div style=\"width:100%;font-size:8px;padding:0 24px;text-align:center;color:#888;" +
                "font-family:Arial,Helvetica,sans-serif;\">" +
                "<span class=\"pageNumber\"></span> / <span class=\"totalPages\"></span>" +
                "</div>");
        chromeOptions.setMarginTop(70);
        chromeOptions.setMarginBottom(40);
        chromeOptions.setMarginLeft(24);
        chromeOptions.setMarginRight(24);

        var templateOptions = new TemplateOptions();
        final String pdfBaseTemplate =
                "<style>" +
                "body{font-family:Arial,Helvetica,sans-serif;color:#1e1e2e;margin:0;}" +
                "table{width:100%;border-collapse:collapse;}" +
                "th,td{border:1px solid #ddd;padding:8px 10px;font-size:11px;text-align:left;}" +
                "th{background:#6d28d9;color:#fff;text-transform:uppercase;font-size:9px;letter-spacing:0.04em;}" +
                "tr:nth-child(even){background:#f5f3ff;}" +
                "</style>" +
                "<table><thead><tr>{{for dataHeadings}}<th>{{:#data}}</th>{{/for}}</tr></thead>" +
                "<tbody>{{for rows}}<tr>{{for #data}}<td>{{:#data}}</td>{{/for}}</tr>{{/for}}</tbody></table>";
        templateOptions.setContent(pdfBaseTemplate);
        templateOptions.setRecipe("chrome-pdf");
        templateOptions.setEngine("jsrender");
        templateOptions.setChrome(chromeOptions);

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
