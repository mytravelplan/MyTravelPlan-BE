package travel.mytravelplan.domain.budget.entity;

import jakarta.persistence.*;
import lombok.*;
import travel.mytravelplan.domain.trip.entity.TripJoin;
import travel.mytravelplan.global.common.entity.BaseEntity;

import java.math.BigDecimal;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BudgetParticipant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id")
    private Budget budget;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_join_id")
    private TripJoin tripJoin;

    private BigDecimal amount;

    @Builder(access = AccessLevel.PRIVATE)
    private BudgetParticipant(Budget budget, TripJoin tripJoin, BigDecimal amount) {
        this.budget = budget;
        this.tripJoin = tripJoin;
        this.amount = amount;
    }

    public static BudgetParticipant createBudgetParticipant(TripJoin tripJoin, BigDecimal amount) {
        return BudgetParticipant.builder()
                .tripJoin(tripJoin)
                .amount(amount)
                .build();
    }
}
