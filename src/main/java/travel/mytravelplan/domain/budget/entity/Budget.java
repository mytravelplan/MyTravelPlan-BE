package travel.mytravelplan.domain.budget.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.global.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Budget extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateTime;

    private String memo;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private CurrencyType currencyType;

    private BigDecimal exchangeRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    protected Budget(LocalDateTime dateTime, String memo, PaymentMethod paymentMethod, CurrencyType currencyType, BigDecimal exchangeRate, Trip trip) {
        this.dateTime = dateTime;
        this.memo = memo;
        this.currencyType = currencyType;
        this.paymentMethod = paymentMethod;
        this.exchangeRate = exchangeRate;
        this.trip = trip;
    }

    protected void update(LocalDateTime dateTime, String memo, PaymentMethod paymentMethod, CurrencyType currencyType, BigDecimal exchangeRate) {
        this.dateTime = dateTime;
        this.memo = memo;
        this.paymentMethod = paymentMethod;
        this.currencyType = currencyType;
        this.exchangeRate = exchangeRate;
    }
}
