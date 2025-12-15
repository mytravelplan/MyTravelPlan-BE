package travel.mytravelplan.domain.currency.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.currency.entity.TripCurrency;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.trip.entity.Trip;

import java.util.List;
import java.util.Optional;

public interface TripCurrencyRepository extends JpaRepository<TripCurrency, Long> {
    Optional<TripCurrency> findByTripAndCurrencyType(Trip trip, CurrencyType currencyType);
    List<TripCurrency> findByTripId(Long tripId);
}
