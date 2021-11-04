package za.co.learnings.todolist.api.jsreport;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChromePdfOptions {

    private boolean landscape = false;
    private boolean displayHeaderFooter = true;
    private String headerTemplate = "<span/>";
    private String footerTemplate = "";
    private int marginTop = 0;
    private int marginBottom = 0;
    private int marginLeft = 0;
    private int marginRight = 0;
}
