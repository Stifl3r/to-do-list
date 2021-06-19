package za.co.learnings.todolist.api.job;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import za.co.learnings.todolist.api.exception.FailedDependencyException;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@Component
@Slf4j
public class OverdueTasksJob extends QuartzJobBean {

    private RestTemplate restTemplate;

    public OverdueTasksJob(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SneakyThrows
    @Override
    protected void executeInternal(JobExecutionContext context) {
        log.info("Starting our OverdueTasksJob");
        try {
            var headers = new HttpHeaders();
            var baseUrl = "http://localhost:8081/batch";

            var builder = fromHttpUrl(baseUrl + "/overdueTasksTrigger");
            var request = new HttpEntity<>(headers);
            var res = restTemplate.exchange(
                    builder.build().toUri(), POST, request, Object.class);

            if (res.getStatusCode() != OK) {
                throw new FailedDependencyException("Down stream services failed");
            }
        } catch (Exception ex) {
            log.error("Server failed unexpectedly");
            throw new FailedDependencyException("Down stream services failed");
        }
    }
}
