package travel.mytravelplan.domain.trip.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.currency.entity.Currency;
import travel.mytravelplan.domain.currency.entity.TripCurrency;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.currency.repository.CurrencyRepository;
import travel.mytravelplan.domain.currency.repository.TripCurrencyRepository;
import travel.mytravelplan.domain.trip.dto.*;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.entity.TripJoin;
import travel.mytravelplan.domain.trip.exception.TripException;
import travel.mytravelplan.domain.trip.mapper.TripMapper;
import travel.mytravelplan.domain.trip.repository.TripJoinRepository;
import travel.mytravelplan.domain.trip.repository.TripRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.error.code.TripErrorCode;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TripService {
    private final TripRepository tripRepository;
    private final TripCurrencyRepository tripCurrencyRepository;
    private final TripJoinRepository tripJoinRepository;
    private final TripMapper tripMapper;
    private final CurrencyRepository currencyRepository;

    @Transactional
    public TripDto createTrip(User user, TripCreateRequestDto tripCreateRequestDto) {
        Trip trip = Trip.createTrip(
                tripCreateRequestDto.getTitle(),
                tripCreateRequestDto.getStartDate(),
                tripCreateRequestDto.getEndDate(),
                tripCreateRequestDto.getImageUrl(),
                tripCreateRequestDto.getCountries()
        );

        TripJoin tripJoin = TripJoin.createTripJoin(trip, user);

        tripRepository.save(trip);

        tripJoinRepository.save(tripJoin);

        Set<CurrencyType> currencyTypes = new HashSet<>();

        currencyTypes.add(CurrencyType.KRW);
        currencyTypes.add(CurrencyType.USD);

        tripCreateRequestDto.getCountries().forEach(c -> currencyTypes.add(c.getCurrencyType()));

        List<Currency> currencies = currencyRepository.findByCurrencyTypeIn(currencyTypes);
        Map<CurrencyType, BigDecimal> currencyTypeRateMap = currencies.stream()
                .collect(Collectors.toMap(Currency::getCurrencyType, Currency::getExchangeRate));

        List<TripCurrency> tripCurrencies = currencyTypes.stream()
                .map(type ->
                        TripCurrency.createTripCurrency(
                            trip,
                            type,
                            currencyTypeRateMap.get(type)
                        )
                ).toList();

        tripCurrencyRepository.saveAll(tripCurrencies);

        return tripMapper.toDto(trip);
    }

    public CursorPageResponseDto<TripDto> getUserTrips(User currentUser, String orderBy, String direction, String cursor, Long after, int limit) {
        List<Trip> trips = tripRepository.findAllByUserCursor(currentUser.getUsername(), orderBy, direction, cursor, after, limit + 1);

        boolean hasNext = trips.size() > limit;

        List<Trip> pagedTrips = hasNext ? trips.subList(0, limit) : trips;

        List<TripDto> tripDtos = pagedTrips.stream()
                .map(tripMapper::toDto)
                .toList();


        String nextCursor = null;
        Long nextAfter = null;

        if (hasNext) {
            Trip lastTrip = pagedTrips.getLast();

            if (orderBy.equals("createdAt")) {
                nextCursor = lastTrip.getCreatedAt().toString();
            }

            nextAfter = lastTrip.getId();
        }

        return CursorPageResponseDto.<TripDto>builder()
                .content(tripDtos)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(tripDtos.size())
                .hasNext(hasNext)
                .build();
    }

    public TripDto getTrip(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        return tripMapper.toDto(trip);
    }

    @Transactional
    public TripDto updateTrip(Long tripId, TripUpdateRequestDto tripUpdateRequestDto) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        trip.update(
                tripUpdateRequestDto.getTitle(),
                tripUpdateRequestDto.getStartDate(),
                tripUpdateRequestDto.getEndDate(),
                tripUpdateRequestDto.getImageUrl(),
                tripUpdateRequestDto.getCountries()
        );

        return tripMapper.toDto(trip);
    }

    @Transactional
    public void deleteTrip(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        tripRepository.delete(trip);
    }

/*
    @Transactional
    public TripInviteLinkDto generateTripInviteLink(Long tripId) {
        return null;
    }
*/

/*
    @Transactional
    public TripDto join(User user, TripJoinRequestDto tripJoinRequestDto) {
        // 해당 여행에 사용자가 이미 참여했는지 확인
        // 레디스에 초대 코드가 있는지 확인
        // 참여하지 않았으면 TripJoin 생성 후 저장
        return null;
    }
*/
}
