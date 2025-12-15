package travel.mytravelplan.domain.currency.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class TripCurrencyUpdateRequestDto {
    private BigDecimal exchangeRate;

    @Builder
    private TripCurrencyUpdateRequestDto(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }
}
