package za.co.learnings.todolist.api.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import za.co.learnings.todolist.api.controller.model.error.ApiError;
import za.co.learnings.todolist.api.controller.model.error.ApiErrorResponse;
import za.co.learnings.todolist.api.exception.GenericException;
import za.co.learnings.todolist.api.exception.InvalidFieldException;
import za.co.learnings.todolist.api.exception.NotFoundException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static za.co.learnings.todolist.api.controller.model.error.ApiErrorType.VALIDATION_ERROR;

@ControllerAdvice(assignableTypes = {
        TaskController.class
})
public class GenericControllerAdvice {

    @ExceptionHandler({
            InvalidFieldException.class
    })
    @ResponseBody
    @ResponseStatus(BAD_REQUEST)
    private ApiErrorResponse validationErrors(Exception e) {
        Throwable cause = e.getCause();

        var ipe = (GenericException) e;
        if (hasUnderlyingCause(cause)) {
            return new ApiErrorResponse(new ApiError(VALIDATION_ERROR, ipe.getErrorCode(), cause.getCause().getMessage(), null));
        }
        return new ApiErrorResponse(new ApiError(VALIDATION_ERROR, ipe.getErrorCode(),  e.getMessage(), null));
    }

    @ExceptionHandler({
            NotFoundException.class
    })
    @ResponseBody
    @ResponseStatus(NOT_FOUND)
    private ApiErrorResponse handleNotFoundError(Exception e) {
        Throwable cause = e.getCause();

        var ipe = (GenericException) e;
        if (hasUnderlyingCause(cause)) {
            return new ApiErrorResponse(new ApiError(VALIDATION_ERROR, ipe.getErrorCode(), cause.getCause().getMessage(), null));
        }
        return new ApiErrorResponse(new ApiError(VALIDATION_ERROR, ipe.getErrorCode(),  e.getMessage(), null));
    }

    private boolean hasUnderlyingCause(Throwable cause) {
        return cause != null && cause.getCause() != null;
    }
}
