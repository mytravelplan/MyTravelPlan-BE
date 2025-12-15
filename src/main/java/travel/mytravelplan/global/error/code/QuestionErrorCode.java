package travel.mytravelplan.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum QuestionErrorCode implements ErrorCode {
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "QUESTION-01", "질문을 찾을 수 없습니다."),
    CORRECT_CHOICE_NOT_FOUND(HttpStatus.NOT_FOUND, "QUESTION-02", "정답 선택지를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
