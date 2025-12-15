package travel.mytravelplan.domain.question.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.question.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Long>, QuestionRepositoryCustom {
}
