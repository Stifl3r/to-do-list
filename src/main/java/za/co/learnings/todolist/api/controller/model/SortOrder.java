package za.co.learnings.todolist.api.controller.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import java.io.Serializable;

@Getter
@Setter
public class SortOrder implements Serializable {

    private static final long serialVersionUID = -7934594151683517016L;

    private Sort.Direction direction;
    private String property;
}
