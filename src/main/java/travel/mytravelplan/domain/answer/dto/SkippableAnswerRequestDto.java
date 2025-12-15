package travel.mytravelplan.domain.answer.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public abstract class SkippableAnswerRequestDto extends AnswerRequestDto {
    private boolean skipped;

    protected SkippableAnswerRequestDto(boolean skipped) {
        this.skipped = skipped;
    }
}
