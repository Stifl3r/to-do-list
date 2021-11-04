package za.co.learnings.todolist.api.jsreport;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class JsReportRequest<T> {

    private TemplateOptions template;
    private T data;
    private Map<String, Object> options;
}
