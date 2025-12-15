package travel.mytravelplan.domain.budget.repository;

import travel.mytravelplan.domain.budget.entity.Budget;
import java.util.List;

public interface BudgetRepositoryCustom {
    List<Budget> findAllByCursor(Long tripId, String keyword, String orderBy, String direction, String cursor, Long after, int limit);
}
