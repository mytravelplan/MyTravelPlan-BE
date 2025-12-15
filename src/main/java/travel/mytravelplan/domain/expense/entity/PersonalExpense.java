package travel.mytravelplan.domain.expense.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.expense.enums.CalculateType;
import travel.mytravelplan.domain.expense.enums.ExpenseCategory;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;
import travel.mytravelplan.domain.schedule.entity.Schedule;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@DiscriminatorValue("PERSONAL_EXPENSE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalExpense extends Expense {
    private BigDecimal totalAmount;

    @Builder(access = AccessLevel.PRIVATE)
    private PersonalExpense(LocalDateTime dateTime, String memo, PaymentMethod paymentMethod, ExpenseCategory expenseCategory, CurrencyType currencyType, BigDecimal exchangeRate, BigDecimal totalAmount, Schedule schedule) {
        super(dateTime, memo, paymentMethod, expenseCategory, currencyType, exchangeRate, schedule);
        this.totalAmount = totalAmount;
    }

    public static PersonalExpense createPersonalExpense(LocalDateTime dateTime, String memo, PaymentMethod paymentMethod, ExpenseCategory expenseCategory, CurrencyType currencyType, BigDecimal exchangeRate, BigDecimal totalAmount, Schedule schedule) {
        return PersonalExpense.builder()
                .dateTime(dateTime)
                .memo(memo)
                .paymentMethod(paymentMethod)
                .expenseCategory(expenseCategory)
                .currencyType(currencyType)
                .exchangeRate(exchangeRate)
                .totalAmount(totalAmount)
                .schedule(schedule)
                .build();
    }

    public void update(LocalDateTime dateTime, String memo, PaymentMethod paymentMethod, ExpenseCategory expenseCategory, CurrencyType currencyType, BigDecimal exchangeRate, BigDecimal totalAmount) {
        super.update(dateTime, memo, paymentMethod, expenseCategory, currencyType, exchangeRate);
        this.totalAmount = totalAmount;
    }
}
