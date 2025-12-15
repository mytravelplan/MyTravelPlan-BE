package travel.mytravelplan.domain.currency.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.currency.enums.CurrencyType;

@Getter
@NoArgsConstructor
public class TripCurrencyCreateRequestDto {
    private CurrencyType currencyType;

    @Builder
    private TripCurrencyCreateRequestDto(CurrencyType currencyType) {
        this.currencyType = currencyType;
    }
}
