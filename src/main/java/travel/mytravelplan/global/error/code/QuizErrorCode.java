package travel.mytravelplan.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum QuizErrorCode implements ErrorCode {
    QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "QUIZ-01", "퀴즈를 찾을 수 없습니다."),
    QUIZ_NOT_FINISHED(HttpStatus.BAD_REQUEST, "QUIZ-02", "퀴즈가 완료되지 않았습니다."),
    QUIZ_ALREADY_FINISHED(HttpStatus.BAD_REQUEST, "QUIZ-03", "퀴즈가 이미 완료되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
