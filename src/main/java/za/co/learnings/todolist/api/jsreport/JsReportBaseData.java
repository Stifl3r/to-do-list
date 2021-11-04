package za.co.learnings.todolist.api.jsreport;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JsReportBaseData {
    private List<String> dataHeadings;
    private List<List<String>> rows;
}
