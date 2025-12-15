package travel.mytravelplan.domain.trip.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import travel.mytravelplan.domain.currency.entity.TripCurrency;
import travel.mytravelplan.domain.currency.dto.TripCurrencyDto;

@Mapper(componentModel = "spring")
public interface TripCurrencyMapper {
    @Mapping(target = "name", expression = "java(tripCurrency.getCurrencyType().getDescription())")
    TripCurrencyDto toDto(TripCurrency tripCurrency);
}