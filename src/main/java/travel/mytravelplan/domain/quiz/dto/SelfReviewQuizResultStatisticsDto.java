package travel.mytravelplan.domain.quiz.dto;
import lombok.Builder;
import lombok.Getter;
import travel.mytravelplan.domain.answer.enums.SelfReviewStatus;

@Getter
public class SelfReviewQuizResultStatisticsDto extends QuizResultStatisticsDto {
    private SelfReviewStatus status;

    @Builder
    private SelfReviewQuizResultStatisticsDto(String percentage, Long count, SelfReviewStatus status) {
        super(percentage, count);
        this.status = status;
    }
}