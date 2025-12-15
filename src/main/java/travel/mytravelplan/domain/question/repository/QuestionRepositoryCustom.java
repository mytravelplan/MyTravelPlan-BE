package travel.mytravelplan.domain.question.repository;

import travel.mytravelplan.domain.question.entity.Question;

import java.util.List;

public interface QuestionRepositoryCustom {
    List<Question> findAllByCursor(Long quizId, String orderBy, String direction, String cursor, Long after, int limit);
}
