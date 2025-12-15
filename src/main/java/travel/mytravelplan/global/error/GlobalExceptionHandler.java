package travel.mytravelplan.global.error;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import travel.mytravelplan.global.error.code.CommonErrorCode;
import travel.mytravelplan.global.common.response.ApiResponse;
import travel.mytravelplan.global.error.exception.BusinessException;
import travel.mytravelplan.global.error.exception.ExternalServiceException;


@RestControllerAdvice
public class GlobalExceptionHandler {
    // 유효성 검사 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(CommonErrorCode.INVALID_REQUEST.getHttpStatus()).body(ApiResponse.validationError(ex.getBindingResult()));
    }

    // HTTP 메서드 오류 처리
     @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(CommonErrorCode.METHOD_NOT_ALLOWED.getHttpStatus()).body(ApiResponse.error(CommonErrorCode.METHOD_NOT_ALLOWED));
    }

     // 비즈니스 로직 예외 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleBusinessException(BusinessException ex) {
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(ApiResponse.error(ex.getErrorCode()));
    }

    // 외부 서비스 예외 처리
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleExternalServiceException(ExternalServiceException ex) {
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(ApiResponse.error(ex.getErrorCode()));
    }

    // 스프링 시큐리티 예외 처리
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleAccessDeniedException(AccessDeniedException ex) throws AccessDeniedException {
        throw ex;
    }

    // 그 외 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleException(Exception ex) {
        return ResponseEntity.status(CommonErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus()).body(ApiResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR));
    }
}
