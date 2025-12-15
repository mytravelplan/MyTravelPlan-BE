package travel.mytravelplan.domain.answer.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class DictationAnswerDto extends AnswerDto {
    private String text;

    @Builder
    private DictationAnswerDto(String text) {
        this.text = text;
    }
}
