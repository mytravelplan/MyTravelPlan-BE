package travel.mytravelplan.domain.expense.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.expense.entity.ExpenseParticipant;

public interface ExpenseParticipantRepository extends JpaRepository<ExpenseParticipant, Long> {
}
