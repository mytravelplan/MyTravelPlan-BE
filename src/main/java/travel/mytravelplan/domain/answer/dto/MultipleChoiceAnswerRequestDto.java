package travel.mytravelplan.domain.answer.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MultipleChoiceAnswerRequestDto extends SkippableAnswerRequestDto {
    private Long choiceId;

    @Builder
    private MultipleChoiceAnswerRequestDto(boolean skipped, Long choiceId) {
        super(skipped);
        this.choiceId = choiceId;
    }
}
