package za.co.learnings.todolist.api.controller.model.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private Integer errorCode;

    private String code;

    private String message;

    private String docUrl;

    private String source;



    public ApiError(ApiErrorType apiErrorType, String message, String docUrl) {
        this.code = apiErrorType.getCode();
        this.docUrl = docUrl;
        this.message = message;
    }

    public ApiError(ApiErrorType apiErrorType, Integer errorCode, String message, String docUrl, String source) {
        this(apiErrorType, errorCode, message, docUrl);
        this.source = source;
    }


    public ApiError(ApiErrorType apiErrorType, Integer errorCode, String message, String docUrl) {
        this.errorCode = errorCode;
        this.code = apiErrorType.getCode();
        this.docUrl = docUrl;
        this.message = message;
    }
}
