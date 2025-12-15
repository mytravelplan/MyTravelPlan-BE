package travel.mytravelplan.domain.question.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class QuestionDto {
    private Long id;
    private String front;
    private String back;

    @Builder
    private QuestionDto(Long id, String front, String back) {
        this.id = id;
        this.front = front;
        this.back = back;
    }
}
