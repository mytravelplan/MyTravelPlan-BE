package travel.mytravelplan.domain.answer.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.answer.enums.ANSWER_TYPE;
import travel.mytravelplan.domain.answer.enums.SelfReviewStatus;

@Getter
@NoArgsConstructor
public class SelfReviewAnswerRequestDto extends AnswerRequestDto {
    private SelfReviewStatus selfReviewStatus;

    @Builder
    private SelfReviewAnswerRequestDto(ANSWER_TYPE answerType, SelfReviewStatus selfReviewStatus) {
        super(answerType);
        this.selfReviewStatus = selfReviewStatus;
    }
}
