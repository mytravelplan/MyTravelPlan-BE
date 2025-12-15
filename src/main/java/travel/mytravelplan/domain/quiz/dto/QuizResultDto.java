package travel.mytravelplan.domain.quiz.dto;

import lombok.Builder;
import lombok.Getter;
import travel.mytravelplan.domain.question.dto.QuestionResultDto;

import travel.mytravelplan.domain.quiz.enums.QuizType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class QuizResultDto {
    private Long quizId;
    private QuizType quizType;
    private LocalDateTime finishedAt;
    private List<QuestionResultDto> questions;
    private List<QuizResultStatisticsDto> statistics;

    @Builder
    private QuizResultDto(Long quizId, QuizType quizType, LocalDateTime finishedAt, List<QuestionResultDto> questions, List<QuizResultStatisticsDto> statistics) {
        this.quizId = quizId;
        this.quizType = quizType;
        this.finishedAt = finishedAt;
        this.questions = questions;
        this.statistics = statistics;
    }
}
