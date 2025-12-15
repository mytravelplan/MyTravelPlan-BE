package travel.mytravelplan.domain.question.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.answer.enums.GRADE;
import travel.mytravelplan.domain.card.entity.Card;
import travel.mytravelplan.domain.quiz.entity.Quiz;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@DiscriminatorValue("MULTI_CHOICE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MultipleChoice extends GradeQuestion {

    @OneToMany(mappedBy = "multipleChoice")
    private List<Choice> choices = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private MultipleChoice(Quiz quiz, Card card) {
        super(quiz, card);
    }

    public static MultipleChoice createMultipleChoice(Quiz quiz, Card card) {
        return MultipleChoice.builder()
                .quiz(quiz)
                .card(card)
                .build();
    }

    public void addChoice(Choice choice) {
        choices.add(choice);
        choice.setMultipleChoice(this);
    }
}
