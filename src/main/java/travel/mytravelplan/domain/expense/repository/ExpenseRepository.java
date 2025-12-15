package travel.mytravelplan.domain.expense.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import travel.mytravelplan.domain.expense.entity.Expense;
import travel.mytravelplan.domain.expense.entity.SharedExpense;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, ExpenseRepositoryCustom {
/*
    @Query("""
        SELECT DISTINCT se FROM SharedExpense se
            LEFT JOIN FETCH se.expenseParticipants ep
            LEFT JOIN FETCH ep.tripJoin tj
            LEFT JOIN FETCH tj.user u
            LEFT JOIN FETCH u.userProfile p
            WHERE se.schedule.trip.id = :tripId
        """)
*/
    @Query("""
        SELECT se FROM SharedExpense se
        WHERE se.schedule.trip.id = :tripId
        """)
    List<SharedExpense> findSharedExpenseAllByTripId(@Param("tripId") Long tripId);

/*
    @Query("""
        SELECT DISTINCT e FROM Expense e
            LEFT JOIN FETCH TREAT(e as SharedExpense) .expenseParticipants ep
            LEFT JOIN FETCH ep.tripJoin tj
            LEFT JOIN FETCH tj.user u
            LEFT JOIN FETCH u.userProfile p
            WHERE TREAT(e as SharedExpense).schedule.trip.id = :tripId
        """)
*/
    @Query("""
        SELECT e FROM Expense e
        WHERE e.schedule.trip.id =:tripId
        """)
    List<Expense> findAllByTripId(@Param("tripId") Long tripId);
}