package travel.mytravelplan.domain.expense.entity;

import jakarta.persistence.*;
import lombok.*;
import travel.mytravelplan.domain.trip.entity.TripJoin;
import travel.mytravelplan.global.common.entity.BaseEntity;

import java.math.BigDecimal;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpenseParticipant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id")
    private Expense expense;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_join_id")
    private TripJoin tripJoin;

    private BigDecimal amount; // 개별 부담 금액

    @Builder(access = AccessLevel.PRIVATE)
    private ExpenseParticipant(Expense expense, TripJoin tripJoin, BigDecimal amount) {
        this.expense = expense;
        this.tripJoin = tripJoin;
        this.amount = amount;
    }

    public static ExpenseParticipant createExpenseParticipant(TripJoin tripJoin, BigDecimal amount) {
        return ExpenseParticipant.builder()
                .tripJoin(tripJoin)
                .amount(amount)
                .build();
    }
}
