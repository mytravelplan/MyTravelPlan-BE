package travel.mytravelplan.domain.budget.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import travel.mytravelplan.domain.budget.entity.Budget;
import travel.mytravelplan.domain.budget.entity.SharedBudget;

import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long>, BudgetRepositoryCustom {
/*
    @Query("""
        SELECT DISTINCT sb FROM SharedBudget sb
            LEFT JOIN FETCH sb.budgetParticipants bp
            LEFT JOIN FETCH bp.tripJoin tj
            LEFT JOIN FETCH tj.user u
            LEFT JOIN FETCH u.userProfile p
        WHERE sb.trip.id = :tripId
        """
    )
*/
    @Query("""
        SELECT sb FROM SharedBudget sb
        WHERE sb.trip.id = :tripId
        """
    )
    List<SharedBudget> findSharedBudgetExpenseAllByTripId(Long tripId);
}
