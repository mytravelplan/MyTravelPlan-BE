package travel.mytravelplan.domain.question.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.answer.enums.SelfReviewStatus;
import travel.mytravelplan.domain.card.entity.Card;
import travel.mytravelplan.domain.quiz.entity.Quiz;

@Getter
@Entity
@DiscriminatorValue("SELF_REVIEW")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SelfReview extends Question {
    private SelfReviewStatus selfReviewStatus;

    @Builder(access = AccessLevel.PRIVATE)
    private SelfReview(Quiz quiz, Card card) {
        super(quiz, card);
    }

    public static SelfReview createSelfReview(Quiz quiz, Card card) {
        return SelfReview.builder()
                .quiz(quiz)
                .card(card)
                .build();
    }
}
