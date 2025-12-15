package travel.mytravelplan.domain.currency.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import travel.mytravelplan.domain.currency.dto.CurrencyDto;
import travel.mytravelplan.domain.currency.entity.Currency;

@Mapper(componentModel = "spring")
public interface CurrencyMapper {
    @Mapping(target = "name", expression = "java(currency.getCurrencyType().getDescription())")
    CurrencyDto toDto(Currency currency);
}
