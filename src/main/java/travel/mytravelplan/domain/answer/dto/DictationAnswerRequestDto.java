package travel.mytravelplan.domain.answer.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DictationAnswerRequestDto extends SkippableAnswerRequestDto {
    private String text;

    @Builder
    private DictationAnswerRequestDto(boolean skipped, String text) {
        super(skipped);
        this.text = text;
    }
}
