package travel.mytravelplan.domain.answer.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.question.entity.Choice;
import travel.mytravelplan.domain.question.entity.Question;

@Getter
@Entity
@DiscriminatorValue("MULTIPLE_CHOICE_ANSWER")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MultipleChoiceAnswer extends Answer {
    @OneToOne(fetch = FetchType.LAZY)
    private Choice choice;

    @Builder(access = AccessLevel.PRIVATE)
    private MultipleChoiceAnswer(Choice choice) {
        this.choice = choice;
    }

    public static MultipleChoiceAnswer createMultipleChoice(Choice choice) {
        return MultipleChoiceAnswer.builder()
                .choice(choice)
                .build();
    }
}