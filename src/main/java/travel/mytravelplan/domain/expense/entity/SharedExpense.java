package travel.mytravelplan.domain.expense.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.expense.enums.CalculateType;
import travel.mytravelplan.domain.expense.enums.ExpenseCategory;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;
import travel.mytravelplan.domain.schedule.entity.Schedule;
import travel.mytravelplan.domain.trip.entity.TripJoin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@DiscriminatorValue("SHARED_EXPENSE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SharedExpense extends Expense {
    @Enumerated(EnumType.STRING)
    private CalculateType calculateType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paryer_id")
    private TripJoin payer;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseParticipant> expenseParticipants = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private SharedExpense(LocalDateTime dateTime, String memo, PaymentMethod paymentMethod, ExpenseCategory expenseCategory, CurrencyType currencyType, BigDecimal exchangeRate, CalculateType calculateType, TripJoin payer, Schedule schedule) {
        super(dateTime, memo, paymentMethod, expenseCategory, currencyType, exchangeRate, schedule);
        this.payer = payer;
        this.calculateType = calculateType;
    }

    public static SharedExpense createSharedExpense(LocalDateTime dateTime, String memo, PaymentMethod paymentMethod, ExpenseCategory expenseCategory, CurrencyType currencyType, BigDecimal exchangeRate, CalculateType calculateType, TripJoin payer, List<ExpenseParticipant> expenseParticipants, Schedule schedule) {
        SharedExpense sharedExpense = SharedExpense.builder()
                .dateTime(dateTime)
                .memo(memo)
                .paymentMethod(paymentMethod)
                .expenseCategory(expenseCategory)
                .currencyType(currencyType)
                .exchangeRate(exchangeRate)
                .calculateType(calculateType)
                .payer(payer)
                .schedule(schedule)
                .build();

        for(ExpenseParticipant expenseParticipant : expenseParticipants) {
            sharedExpense.addExpenseParticipant(expenseParticipant);
        }

        return sharedExpense;
    }

    public void addExpenseParticipant(ExpenseParticipant participant) {
        participant.setExpense(this);
        this.expenseParticipants.add(participant);
    }

    public void update(LocalDateTime datetime, String memo, PaymentMethod paymentMethod, ExpenseCategory expenseCategory, CurrencyType currencyType, BigDecimal exchangeRate, CalculateType calculateType, TripJoin payer, List<ExpenseParticipant> expenseParticipants) {
        super.update(datetime, memo, paymentMethod, expenseCategory, currencyType, exchangeRate);

        this.calculateType = calculateType;
        this.payer = payer;

        this.expenseParticipants.clear();
        for(ExpenseParticipant participant : expenseParticipants) {
            this.addExpenseParticipant(participant);
        }
    }
}
