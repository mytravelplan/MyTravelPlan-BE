package travel.mytravelplan.domain.currency.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.global.common.entity.BaseEntity;

import java.math.BigDecimal;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TripCurrency extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @Enumerated(EnumType.STRING)
    private CurrencyType currencyType;

    private BigDecimal exchangeRate;

    @Builder(access = AccessLevel.PRIVATE)
    private TripCurrency(Trip trip, CurrencyType currencyType, BigDecimal exchangeRate) {
        this.trip = trip;
        this.currencyType = currencyType;
        this.exchangeRate = exchangeRate;
    }

    public static TripCurrency createTripCurrency(Trip trip, CurrencyType currencyType, BigDecimal exchangeRate) {
        return TripCurrency.builder()
                .trip(trip)
                .currencyType(currencyType)
                .exchangeRate(exchangeRate)
                .build();
    }

    public void update(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }
}
