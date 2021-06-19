package za.co.learnings.todolist.api.controller.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class QuartzResponse {
    private ResponseType type;
    private String name;
    private String group;
    private boolean result;
    private String status;

    public enum ResponseType {
        DELETE
    }
}
