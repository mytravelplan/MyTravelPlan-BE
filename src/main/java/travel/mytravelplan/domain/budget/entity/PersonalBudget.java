package travel.mytravelplan.domain.budget.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;
import travel.mytravelplan.domain.trip.entity.Trip;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@DiscriminatorValue("PERSONAL_BUDGET")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalBudget extends Budget {
    private BigDecimal totalAmount;

    @Builder(access = AccessLevel.PRIVATE)
    private PersonalBudget(LocalDateTime dateTime, String memo, PaymentMethod paymentMethod, CurrencyType currencyType, BigDecimal exchangeRate, BigDecimal totalAmount, Trip trip) {
        super(dateTime, memo, paymentMethod, currencyType, exchangeRate, trip);
        this.totalAmount = totalAmount;
    }

    public static PersonalBudget createPersonalBudget(LocalDateTime dateTime, String memo, PaymentMethod paymentMethod, CurrencyType currencyType, BigDecimal exchangeRate, BigDecimal totalAmount, Trip trip) {
        return PersonalBudget.builder()
                .dateTime(dateTime)
                .memo(memo)
                .paymentMethod(paymentMethod)
                .currencyType(currencyType)
                .exchangeRate(exchangeRate)
                .totalAmount(totalAmount)
                .trip(trip)
                .build();
    }

    public void update(LocalDateTime dateTime, String memo, PaymentMethod paymentMethod, CurrencyType currencyType, BigDecimal exchangeRate, BigDecimal totalAmount) {
        super.update(dateTime, memo, paymentMethod, currencyType, exchangeRate);
        this.totalAmount = totalAmount;
    }
}
