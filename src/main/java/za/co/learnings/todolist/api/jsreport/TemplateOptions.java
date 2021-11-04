package za.co.learnings.todolist.api.jsreport;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemplateOptions {

    private String content;
    private String recipe;
    private String engine;
    private ChromePdfOptions chrome;
}
