package travel.mytravelplan.domain.answer.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.question.entity.Question;

@Getter
@Entity
@DiscriminatorValue("DICTATION_ANSWER")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DictationAnswer extends Answer {
    private String text;

    @Builder(access = AccessLevel.PRIVATE)
    private DictationAnswer(String text) {
        this.text = text;
    }

    public static  DictationAnswer createDictation(String text) {
        return DictationAnswer.builder()
                .text(text)
                .build();
    }
}
