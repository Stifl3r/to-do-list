package za.co.learnings.todolist.api.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import za.co.learnings.todolist.api.controller.model.QuartzJobContextDto;

import javax.transaction.Transactional;
import java.util.UUID;

@Component
@Transactional
@Slf4j
public class TestJob extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        log.info("Starting our fibonacci test job");

        int maxNumber = 10, previousNumber = 0, nextNumber = 1;
        System.out.print("Fibonacci Series of "+maxNumber+" numbers:");
        var result = "Fibonacci Series of "+maxNumber+" numbers:\n";

        int i=1;
        while(i <= maxNumber)
        {
            result = result + previousNumber+" ";
            System.out.print(previousNumber+" ");
            int sum = previousNumber + nextNumber;
            previousNumber = nextNumber;
            nextNumber = sum;
            i++;
        }

        var uuid = UUID.randomUUID().toString();
        var resultDto = new QuartzJobContextDto();
        resultDto.setResult(result);
        resultDto.setUuid(uuid);
        context.setResult(resultDto);

        log.info("Finish our fibonacci test job");
    }
}
