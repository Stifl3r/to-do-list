package za.co.learnings.todolist.api.controller.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Trigger")
public class TriggerRequest {
    @Schema(description = "Unique job identifier", example = "TestFibonacciJob")
    private String jobId;
    @Schema(description = "Trigger repeat interval", allowableValues = "cron, fireNow, onceOff")
    private String interval;
    @Schema(description = "Date time for simple trigger")
    private LocalDateTime dateTime;
    @Schema(description = "Cron Expression", example = "0 0 * ? * MON,TUE,WED,THU,FRI *")
    private String cronExpression;
}
