package travel.mytravelplan.domain.budget.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.expense.enums.CalculateType;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;
import travel.mytravelplan.domain.trip.entity.Trip;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@DiscriminatorValue("SHARED_BUDGET")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SharedBudget extends Budget {
    @Enumerated(EnumType.STRING)
    private CalculateType calculateType;

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BudgetParticipant> budgetParticipants = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private SharedBudget(LocalDateTime dateTime, String memo, PaymentMethod paymentMethod, CurrencyType currencyType, BigDecimal exchangeRate, CalculateType calculateType, Trip trip) {
        super(dateTime, memo, paymentMethod, currencyType, exchangeRate, trip);
        this.calculateType = calculateType;
    }

    public static SharedBudget createSharedBudget(LocalDateTime dateTime, String memo, PaymentMethod paymentMethod, CurrencyType currencyType, BigDecimal exchangeRate, CalculateType calculateType, List<BudgetParticipant> budgetParticipants, Trip trip) {
        SharedBudget sharedBudget = SharedBudget.builder()
                .dateTime(dateTime)
                .memo(memo)
                .paymentMethod(paymentMethod)
                .currencyType(currencyType)
                .exchangeRate(exchangeRate)
                .calculateType(calculateType)
                .trip(trip)
                .build();

        for (BudgetParticipant budgetParticipant : budgetParticipants) {
            sharedBudget.addBudgetParticipant(budgetParticipant);
        }

        return sharedBudget;
    }

    public void addBudgetParticipant(BudgetParticipant budgetParticipant) {
        budgetParticipant.setBudget(this);
        this.budgetParticipants.add(budgetParticipant);
    }

    public void update(LocalDateTime dateTime, String memo, PaymentMethod paymentMethod, CurrencyType currencyType, BigDecimal exchangeRate, CalculateType calculateType, List<BudgetParticipant> budgetParticipants) {
        super.update(dateTime, memo, paymentMethod, currencyType, exchangeRate);
        this.calculateType = calculateType;

        this.budgetParticipants.clear();
        for (BudgetParticipant budgetParticipant : budgetParticipants) {
            this.addBudgetParticipant(budgetParticipant);
        }
    }
}
