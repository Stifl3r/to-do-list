package za.co.learnings.todolist.api.controller.model;

import lombok.Getter;

@Getter
public enum StatusType {
    BACK_LOG("BACK_LOG"),
    IN_PROGRESS("IN_PROGRESS"),
    TESTING("TESTING"),
    COMPLETED("COMPLETED"),
    DEPLOYED_TO_PRODUCTION("DEPLOYED_TO_PRODUCTION");

    private final String value;

    StatusType(String value) {
        this.value = value;
    }
}
