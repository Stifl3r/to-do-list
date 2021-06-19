package za.co.learnings.todolist.api.controller.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class QuartzInformationResponse {
    private String version;
    private String schedulerName;
    private String instanceId;
    private Class threadPoolClass;
    private int numberOfThreads;
    private Class schedulerClass;
    private boolean isClustered;
    private Class jobStoreClass;
    private long numberOfJobsExecuted;
    private Date startTime;
    private boolean inStandbyMode;
    private List<String> simpleJobDetail = new ArrayList<>();
    public String getSchedulerProductName() {
        return "Quartz Scheduler (spring-boot-starter-quartz)";
    }

    public void addSimpleJobDetail(@NonNull List<String> add) {
        this.simpleJobDetail.addAll(add);
    }
}
