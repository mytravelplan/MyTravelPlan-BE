package travel.mytravelplan.domain.question.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Choice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    private boolean correctAnswer;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    private MultipleChoice multipleChoice;

    @Builder(access = AccessLevel.PRIVATE)
    private Choice(String text, boolean correctAnswer) {
        this.text = text;
        this.correctAnswer = correctAnswer;
    }

    public static Choice createChoice(String text, boolean correctAnswer) {
        return Choice.builder()
                .text(text)
                .correctAnswer(correctAnswer)
                .build();
    }
}
