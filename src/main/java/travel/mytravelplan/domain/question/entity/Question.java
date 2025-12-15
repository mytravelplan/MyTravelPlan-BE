package travel.mytravelplan.domain.question.entity;

import jakarta.persistence.*;
import lombok.*;
import travel.mytravelplan.domain.answer.entity.Answer;
import travel.mytravelplan.domain.card.entity.Card;
import travel.mytravelplan.domain.quiz.entity.Quiz;
import travel.mytravelplan.global.common.entity.BaseEntity;

@Getter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Question extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    private Card card;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id")
    private Answer answer;

    protected Question(Quiz quiz, Card card) {
        this.quiz = quiz;
        this.card = card;
    }

    public void addAnswer(Answer answer) {
        this.answer = answer;
    }
}
