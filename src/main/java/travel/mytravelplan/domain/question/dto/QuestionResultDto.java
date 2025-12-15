package travel.mytravelplan.domain.question.dto;

import lombok.Builder;
import lombok.Getter;
import travel.mytravelplan.domain.answer.dto.AnswerDto;
import travel.mytravelplan.domain.answer.enums.GRADE;

@Getter
public class QuestionResultDto {
    private Long questionId;
    private String front;
    private String back;
    private Long choiceId;
    private GRADE grade;
    private AnswerDto answer;

    @Builder
    private QuestionResultDto(Long questionId, String front, String back, Long choiceId, GRADE grade, AnswerDto answer) {
        this.questionId = questionId;
        this.front = front;
        this.back = back;
        this.choiceId = choiceId;
        this.grade = grade;
        this.answer = answer;
    }
}
