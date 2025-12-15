package travel.mytravelplan.domain.answer.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.question.entity.Question;

@Getter
@Entity
@DiscriminatorValue("SELF_REVIEW_ANSWER")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SelfReviewAnswer extends Answer {

    @Builder(access = AccessLevel.PRIVATE)
    private SelfReviewAnswer(Question question) {
    }

    public static SelfReviewAnswer createSelfReview() {
        return SelfReviewAnswer.builder()
                .build();
    }
}