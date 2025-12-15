package travel.mytravelplan.domain.quiz.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.quiz.enums.QuizType;

import java.util.List;

@Getter
@NoArgsConstructor
public class QuizCreateRequestDto {
    private QuizType quizType;
    private List<Long> deckIds;

    @Builder
    private QuizCreateRequestDto(QuizType quizType, List<Long> deckIds) {
        this.quizType = quizType;
        this.deckIds = deckIds;
    }
}
