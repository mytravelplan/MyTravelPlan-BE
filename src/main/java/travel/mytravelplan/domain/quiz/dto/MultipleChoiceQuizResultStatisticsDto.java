package travel.mytravelplan.domain.quiz.dto;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import travel.mytravelplan.domain.answer.enums.GRADE;

@Getter
public class MultipleChoiceQuizResultStatisticsDto extends QuizResultStatisticsDto {
    private GRADE grade;

    @Builder
    private MultipleChoiceQuizResultStatisticsDto(String percentage, Long count, GRADE grade) {
        super(percentage, count);
        this.grade = grade;
    }
}