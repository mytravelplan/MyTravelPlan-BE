package travel.mytravelplan.domain.trip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.entity.TripJoin;
import travel.mytravelplan.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface TripJoinRepository extends JpaRepository<TripJoin, Long> {
    Optional<TripJoin> findByUserAndTrip(User User, Trip trip);
    Optional<TripJoin> findByUserIdAndTripId(Long userId, Long tripId);
    List<TripJoin> findByTripIdAndUserIdIn(Long tripId, List<Long> ids);
    boolean existsByUserIdAndTripId(Long userId, Long tripId);
}
