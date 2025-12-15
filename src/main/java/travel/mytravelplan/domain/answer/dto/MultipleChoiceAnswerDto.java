package travel.mytravelplan.domain.answer.dto;

import lombok.Builder;
import lombok.Getter;
import travel.mytravelplan.domain.question.dto.ChoiceDto;

@Getter
public class MultipleChoiceAnswerDto extends AnswerDto {
    private ChoiceDto choice;

    @Builder
    private MultipleChoiceAnswerDto(ChoiceDto choice) {
        this.choice = choice;
    }
}
