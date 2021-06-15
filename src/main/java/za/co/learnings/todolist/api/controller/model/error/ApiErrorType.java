package za.co.learnings.todolist.api.controller.model.error;

import lombok.Getter;

@Getter
public enum ApiErrorType {
    API_ERROR("apiError"),
    NOT_FOUND_ERROR("notFoundError"),
    AUTHORISATION_ERROR("authorisationError"),
    BACKEND_ERROR("backendError"),
    VALIDATION_ERROR("validationError"),
    UNIT_PRICING_ERROR("unitPricingError"),
    NOT_IMPLEMENTED_ERROR("notImplementedError");

    private final String code;

    ApiErrorType(String code) {
        this.code = code;
    }
}
