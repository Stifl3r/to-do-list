package za.co.learnings.todolist.api.controller.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Quartz Job Context")
public class QuartzJobContextDto {

    private String uuid;
    private String result;

}
