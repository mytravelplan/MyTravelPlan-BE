package travel.mytravelplan.domain.quiz.dto;

import lombok.Builder;
import lombok.Getter;
import travel.mytravelplan.domain.quiz.enums.QuizType;

@Getter
public class QuizDto {
    private Long id;
    private QuizType quizType;

    @Builder
    private QuizDto(Long id, QuizType quizType) {
        this.id = id;
        this.quizType = quizType;
    }
}
