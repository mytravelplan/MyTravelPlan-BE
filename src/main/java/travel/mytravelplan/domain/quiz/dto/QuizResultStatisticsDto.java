package travel.mytravelplan.domain.quiz.dto;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
public abstract class QuizResultStatisticsDto {
    private String percentage;
    private Long count;

    protected QuizResultStatisticsDto(String percentage, Long count) {
        this.percentage = percentage;
        this.count = count;
    }
}