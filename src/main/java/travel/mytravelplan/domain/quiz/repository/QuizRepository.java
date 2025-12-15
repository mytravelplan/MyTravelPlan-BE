package travel.mytravelplan.domain.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.quiz.entity.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long>, QuizRepositoryCustom {
}
