package travel.mytravelplan.domain.currency.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.currency.dto.CurrencyDto;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.currency.exception.CurrencyException;
import travel.mytravelplan.domain.currency.mapper.CurrencyMapper;
import travel.mytravelplan.domain.currency.repository.CurrencyRepository;
import travel.mytravelplan.global.error.code.CurrencyErrorCode;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CurrencyService {
    private final CurrencyRepository currencyRepository;
    private final CurrencyMapper currencyMapper;

    public CurrencyDto getCurrency(CurrencyType currencyType) {
        return currencyRepository.findByCurrencyType(currencyType)
                .map(currencyMapper::toDto)
                .orElseThrow(() -> new CurrencyException(CurrencyErrorCode.CURRENCY_NOT_FOUND));
    }

    public List<CurrencyDto> getAllCurrencies() {
        return currencyRepository.findAll().stream()
                .map(currencyMapper::toDto)
                .toList();
    }
}
