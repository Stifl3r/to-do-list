package za.co.learnings.todolist.api.controller.model.error;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class ApiErrorResponse {

    private final List<ApiError> errors;

    public ApiErrorResponse(ApiError error) {
        this.errors = Collections.singletonList(error);
    }
}
