package travel.mytravelplan.domain.question.mapper;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.question.entity.Choice;

public interface ChoiceRepository extends JpaRepository<Choice, Long> {
}
