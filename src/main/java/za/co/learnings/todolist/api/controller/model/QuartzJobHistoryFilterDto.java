package za.co.learnings.todolist.api.controller.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Schema(description = "QuartzJobHistoryFilter")
public class QuartzJobHistoryFilterDto extends Paging  implements Serializable {
    private static final long serialVersionUID = 6624465381368121567L;

    private String triggerType;
}
