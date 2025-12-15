package travel.mytravelplan;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.currency.entity.TripCurrency;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.currency.exception.TripCurrencyException;
import travel.mytravelplan.domain.currency.repository.CurrencyRepository;
import travel.mytravelplan.domain.currency.repository.TripCurrencyRepository;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.entity.TripJoin;
import travel.mytravelplan.domain.trip.enums.Country;
import travel.mytravelplan.domain.trip.repository.TripJoinRepository;
import travel.mytravelplan.domain.trip.repository.TripRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.exception.UserException;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.error.code.TripCurrencyErrorCode;
import travel.mytravelplan.global.error.code.UserErrorCode;
import java.time.LocalDate;
import java.util.Set;

@Profile("local")
@Component
@Order(2)
@RequiredArgsConstructor
public class TripInitializer implements ApplicationRunner {
    private final TripRepository tripRepository;
    private final TripJoinRepository tripJoinRepository;
    private final CurrencyRepository currencyRepository;
    private final UserRepository userRepository;
    private final TripCurrencyRepository tripCurrencyRepository;

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {
        User user1 = userRepository.findByUsername("cksgud0403")
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        User user2 = userRepository.findByUsername("cksguddl0403")
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        User user3 = userRepository.findByUsername("chanhyeong0403")
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Trip trip = Trip.createTrip(
                "즐거운 일본 여행",
                LocalDate.of(2025, 10, 1),
                LocalDate.of(2025, 10, 3),
                "https://example.com/trip-image.jpg",
                Set.of(Country.JP)
        );

        TripJoin tripJoin1 = TripJoin.createTripJoin(trip, user1);

        TripJoin tripJoin2 = TripJoin.createTripJoin(trip, user2);

        TripJoin tripJoin3 = TripJoin.createTripJoin(trip, user3);

        TripCurrency tripCurrency1 = TripCurrency.createTripCurrency(
                trip,
                CurrencyType.KRW,
                currencyRepository.findByCurrencyType(CurrencyType.KRW).orElseThrow(() -> new TripCurrencyException(TripCurrencyErrorCode.TRIP_CURRENCY_NOT_FOUND)).getExchangeRate()
        );

        TripCurrency.createTripCurrency(
                trip,
                CurrencyType.KRW,
                currencyRepository.findByCurrencyType(CurrencyType.KRW).orElseThrow(() -> new TripCurrencyException(TripCurrencyErrorCode.TRIP_CURRENCY_NOT_FOUND)).getExchangeRate()
        );

        TripCurrency tripCurrency2 = TripCurrency.createTripCurrency(
                trip,
                CurrencyType.JPY,
                currencyRepository.findByCurrencyType(CurrencyType.JPY).orElseThrow(() -> new TripCurrencyException(TripCurrencyErrorCode.TRIP_CURRENCY_NOT_FOUND)).getExchangeRate()
        );

        TripCurrency tripCurrency3 = TripCurrency.createTripCurrency(
                trip,
                CurrencyType.USD,
                currencyRepository.findByCurrencyType(CurrencyType.USD).orElseThrow(() -> new TripCurrencyException(TripCurrencyErrorCode.TRIP_CURRENCY_NOT_FOUND)).getExchangeRate()
        );

        tripRepository.save(trip);

        tripJoinRepository.save(tripJoin1);
        tripJoinRepository.save(tripJoin2);
        tripJoinRepository.save(tripJoin3);

        tripCurrencyRepository.save(tripCurrency1);
        tripCurrencyRepository.save(tripCurrency2);
        tripCurrencyRepository.save(tripCurrency3);
    }
}
