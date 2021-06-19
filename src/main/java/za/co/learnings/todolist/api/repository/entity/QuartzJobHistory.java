package za.co.learnings.todolist.api.repository.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "Qrtz_Job_History")
@Getter
@Setter
public class QuartzJobHistory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String uuid;
    private String jobName;
    private String jobGroup;
    private String jobDescription;
    private Long jobRunTime;
    private String triggerName;
    private String triggerGroup;
    private String triggerDescription;
    private String triggerType;
    @Lob
    private String result;
    private Date fireTime;
    private Date scheduledFireTime;
    private Date previousFireTime;
    private Date nextFireTime;
}
