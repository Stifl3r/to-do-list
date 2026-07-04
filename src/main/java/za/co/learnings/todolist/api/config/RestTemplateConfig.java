package za.co.learnings.todolist.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        // RestTemplateBuilder has moved/changed across Spring Boot versions.
        // For a simple RestTemplate bean, instantiate directly to avoid compatibility issues.
        return new RestTemplate();
    }
}
