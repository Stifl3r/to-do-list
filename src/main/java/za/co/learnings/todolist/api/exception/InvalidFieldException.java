package za.co.learnings.todolist.api.exception;

import lombok.Getter;

@Getter
public class InvalidFieldException extends GenericException {

    public InvalidFieldException(String message) {
        super(message);
    }

    public InvalidFieldException(String message, int errorCode) {
        super(message, errorCode);
    }
}
