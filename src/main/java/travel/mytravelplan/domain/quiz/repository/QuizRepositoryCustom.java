package travel.mytravelplan.domain.quiz.repository;

import travel.mytravelplan.domain.quiz.entity.Quiz;
import travel.mytravelplan.domain.quiz.enums.QuizType;

import java.util.List;

public interface QuizRepositoryCustom {
    List<Quiz> findAllByCursor(String username, QuizType quizType, String orderBy, String direction, String cursor, Long after, int limit);
}
