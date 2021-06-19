package za.co.learnings.todolist.api.exception;

import lombok.Getter;

@Getter
public class FailedDependencyException extends GenericException {

    public FailedDependencyException(String message) {
        super(message);
    }

    public FailedDependencyException(String message, int errorCode) {
        super(message, errorCode);
    }
}
