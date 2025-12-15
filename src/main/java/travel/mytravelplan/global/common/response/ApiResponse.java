package travel.mytravelplan.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.validation.BindingResult;
import travel.mytravelplan.global.error.ErrorResponse;
import travel.mytravelplan.global.error.code.CommonErrorCode;
import travel.mytravelplan.global.error.code.ErrorCode;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드를 JSON에 포함하지 않음
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ErrorResponse error;

    private ApiResponse(boolean success, T data, ErrorResponse error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, ErrorResponse.of(errorCode));
    }

    public static <T> ApiResponse<T> validationError(BindingResult bindingResult) {
        return new ApiResponse<>(false, null, ErrorResponse.of(CommonErrorCode.INVALID_REQUEST, bindingResult));
    }
}
