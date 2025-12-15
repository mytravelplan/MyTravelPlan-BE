package travel.mytravelplan.domain.question.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ChoiceDto {
    private Long id;
    private String text;
    private boolean correct;

    @Builder
    private ChoiceDto(Long id, String text, boolean correct) {
        this.id = id;
        this.text = text;
        this.correct = correct;
    }
}
