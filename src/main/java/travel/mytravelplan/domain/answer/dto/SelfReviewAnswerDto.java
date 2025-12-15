package travel.mytravelplan.domain.answer.dto;

import lombok.Builder;
import lombok.Getter;
import travel.mytravelplan.domain.answer.enums.SelfReviewStatus;

@Getter
public class SelfReviewAnswerDto extends AnswerDto {
    private SelfReviewStatus selfReviewStatus;

    @Builder
    private SelfReviewAnswerDto(SelfReviewStatus selfReviewStatus) {
        this.selfReviewStatus = selfReviewStatus;
    }
}
