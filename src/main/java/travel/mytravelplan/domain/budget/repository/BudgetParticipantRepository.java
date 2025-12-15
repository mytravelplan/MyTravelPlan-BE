package travel.mytravelplan.domain.budget.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.budget.entity.BudgetParticipant;

public interface BudgetParticipantRepository extends JpaRepository<BudgetParticipant, Long> {

}
