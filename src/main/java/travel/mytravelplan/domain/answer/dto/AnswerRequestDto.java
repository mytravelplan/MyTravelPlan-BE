package travel.mytravelplan.domain.answer.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.answer.enums.ANSWER_TYPE;

@Getter
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "answerType",
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SelfReviewAnswerRequestDto.class, name = "SELF_REVIEW"),
        @JsonSubTypes.Type(value = MultipleChoiceAnswerRequestDto.class, name = "MULTIPLE_CHOICE"),
        @JsonSubTypes.Type(value = DictationAnswerRequestDto.class, name = "DICTATION")
})
public abstract class AnswerRequestDto {
    private ANSWER_TYPE answerType;

    protected AnswerRequestDto(ANSWER_TYPE answerType) {
        this.answerType = answerType;
    }
}
