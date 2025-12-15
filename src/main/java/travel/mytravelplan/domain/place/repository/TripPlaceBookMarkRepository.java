package travel.mytravelplan.domain.place.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.place.entity.TripPlace;
import travel.mytravelplan.domain.place.entity.TripPlaceBookMark;
import travel.mytravelplan.domain.user.entity.User;

import java.util.Optional;

public interface TripPlaceBookMarkRepository extends JpaRepository<TripPlaceBookMark, Long> {
    Optional<TripPlaceBookMark> findByTripPlaceAndUser(TripPlace tripPlace, User user);
}
