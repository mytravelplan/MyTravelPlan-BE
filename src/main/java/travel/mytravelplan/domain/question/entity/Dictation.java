package travel.mytravelplan.domain.question.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.card.entity.Card;
import travel.mytravelplan.domain.quiz.entity.Quiz;

@Getter
@Entity
@DiscriminatorValue("DICTATION")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dictation extends GradeQuestion {

    @Builder(access = AccessLevel.PRIVATE)
    private Dictation(Quiz quiz, Card card) {
        super(quiz, card);
    }

    public static Dictation createDictation(Quiz quiz, Card card) {
        return Dictation.builder()
                .quiz(quiz)
                .card(card)
                .build();
    }
}
