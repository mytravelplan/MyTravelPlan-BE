package travel.mytravelplan.domain.currency.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.currency.dto.TripCurrencyUpdateRequestDto;
import travel.mytravelplan.domain.currency.entity.TripCurrency;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.currency.exception.CurrencyException;
import travel.mytravelplan.domain.currency.exception.TripCurrencyException;
import travel.mytravelplan.domain.currency.repository.CurrencyRepository;
import travel.mytravelplan.domain.currency.repository.TripCurrencyRepository;
import travel.mytravelplan.domain.currency.dto.TripCurrencyCreateRequestDto;
import travel.mytravelplan.domain.currency.dto.TripCurrencyDto;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.exception.TripException;
import travel.mytravelplan.domain.trip.mapper.TripCurrencyMapper;
import travel.mytravelplan.domain.trip.repository.TripRepository;
import travel.mytravelplan.global.error.code.CurrencyErrorCode;
import travel.mytravelplan.global.error.code.TripCurrencyErrorCode;
import travel.mytravelplan.global.error.code.TripErrorCode;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TripCurrencyService {
    private final TripCurrencyRepository tripCurrencyRepository;
    private final TripCurrencyMapper tripCurrencyMapper;
    private final TripRepository tripRepository;
    private final CurrencyRepository currencyRepository;

    @Transactional
    public TripCurrencyDto createTripCurrency(Long tripId, TripCurrencyCreateRequestDto tripCurrencyCreateRequestDto) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        TripCurrency tripCurrency = TripCurrency.createTripCurrency(
                trip,
                tripCurrencyCreateRequestDto.getCurrencyType(),
                currencyRepository.findByCurrencyType(tripCurrencyCreateRequestDto.getCurrencyType())
                        .orElseThrow(() -> new CurrencyException(CurrencyErrorCode.CURRENCY_NOT_FOUND))
                        .getExchangeRate()
        );

        tripCurrencyRepository.save(tripCurrency);

        return tripCurrencyMapper.toDto(tripCurrency);
    }

    public List<TripCurrencyDto> getTripCurrencies(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        List<TripCurrency> tripCurrencies = tripCurrencyRepository.findByTripId(trip.getId());

        return tripCurrencies.stream()
                .map(tripCurrencyMapper::toDto)
                .toList();
    }

    @Transactional
    public TripCurrencyDto updateTripCurrency(Long tripId, CurrencyType currencyType, TripCurrencyUpdateRequestDto tripCurrencyUpdateRequestDto) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        TripCurrency tripCurrency = tripCurrencyRepository.findByTripAndCurrencyType(trip, currencyType)
                .orElseThrow(() -> new TripCurrencyException(TripCurrencyErrorCode.TRIP_CURRENCY_NOT_FOUND));

        tripCurrency.update(tripCurrencyUpdateRequestDto.getExchangeRate());

        return tripCurrencyMapper.toDto(tripCurrency);
    }
}
