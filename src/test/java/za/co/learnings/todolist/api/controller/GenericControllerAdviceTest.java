package za.co.learnings.todolist.api.controller;

import org.junit.jupiter.api.Test;
import za.co.learnings.todolist.api.exception.FailedDependencyException;
import za.co.learnings.todolist.api.exception.GenericException;
import za.co.learnings.todolist.api.exception.InvalidFieldException;
import za.co.learnings.todolist.api.exception.NotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GenericControllerAdviceTest {

    private final GenericControllerAdvice advice = new GenericControllerAdvice();

    @Test
    public void validationErrorsShouldReturnExceptionMessageAndErrorCode() {
        //When
        var actual = advice.validationErrors(new InvalidFieldException("Name cannot be null", 7)).getErrors().get(0);

        //Then
        assertEquals("validationError", actual.getCode());
        assertEquals(7, actual.getErrorCode());
        assertEquals("Name cannot be null", actual.getMessage());
    }

    @Test
    public void validationErrorsWhenCauseHasUnderlyingCauseShouldReturnRootMessage() {
        //When
        var actual = advice.validationErrors(withNestedCause(new InvalidFieldException("wrapper"))).getErrors().get(0);

        //Then
        assertEquals("root cause", actual.getMessage());
    }

    @Test
    public void handleNotFoundErrorShouldReturnExceptionMessage() {
        //When
        var actual = advice.handleNotFoundError(new NotFoundException("Provided id does not exist")).getErrors().get(0);

        //Then
        assertEquals("validationError", actual.getCode());
        assertEquals("Provided id does not exist", actual.getMessage());
    }

    @Test
    public void handleNotFoundErrorWhenCauseHasUnderlyingCauseShouldReturnRootMessage() {
        //When
        var actual = advice.handleNotFoundError(withNestedCause(new NotFoundException("wrapper"))).getErrors().get(0);

        //Then
        assertEquals("root cause", actual.getMessage());
    }

    @Test
    public void failedDependencyErrorShouldReturnBackendError() {
        //When
        var actual = advice.failedDependencyError(new FailedDependencyException("Down stream services failed", 3)).getErrors().get(0);

        //Then
        assertEquals("backendError", actual.getCode());
        assertEquals(3, actual.getErrorCode());
        assertEquals("Down stream services failed", actual.getMessage());
    }

    @Test
    public void failedDependencyErrorWhenCauseHasUnderlyingCauseShouldReturnRootMessage() {
        //When
        var actual = advice.failedDependencyError(withNestedCause(new FailedDependencyException("wrapper"))).getErrors().get(0);

        //Then
        assertEquals("root cause", actual.getMessage());
    }

    private static <T extends GenericException> T withNestedCause(T exception) {
        exception.initCause(new RuntimeException("middle", new RuntimeException("root cause")));
        return exception;
    }
}
