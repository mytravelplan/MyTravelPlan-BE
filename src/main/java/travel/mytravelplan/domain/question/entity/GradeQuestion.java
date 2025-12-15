package travel.mytravelplan.domain.question.entity;

import jakarta.persistence.*;
import lombok.*;
import travel.mytravelplan.domain.answer.enums.GRADE;
import travel.mytravelplan.domain.card.entity.Card;
import travel.mytravelplan.domain.quiz.entity.Quiz;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class GradeQuestion extends Question {
    @Setter
    @Enumerated(EnumType.STRING)
    private GRADE grade = GRADE.SKIPPED;

    protected GradeQuestion(Quiz quiz, Card card) {
        super(quiz, card);
    }
}