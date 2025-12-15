package travel.mytravelplan.domain.quiz.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.question.entity.Question;
import travel.mytravelplan.domain.quiz.enums.QuizType;
import travel.mytravelplan.domain.quiz.exception.QuizException;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.entity.BaseEntity;
import travel.mytravelplan.global.error.code.QuizErrorCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private QuizType quizType;

    @OneToMany(mappedBy = "quiz")
    private List<Question> questions = new ArrayList<>();

    private LocalDateTime finishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder(access = AccessLevel.PRIVATE)
    private Quiz(QuizType quizType, User user) {
        this.quizType = quizType;
        this.user = user;
    }

    public static Quiz createQuiz(QuizType quizType, User user) {
        return Quiz.builder()
                .quizType(quizType)
                .user(user)
                .build();
    }

    public void addQuestion(Question question) {
        this.questions.add(question);
        question.setQuiz(this);
    }

    public void finish(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }
}
