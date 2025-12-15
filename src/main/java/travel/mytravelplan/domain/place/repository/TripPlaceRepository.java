package travel.mytravelplan.domain.place.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.place.entity.Place;
import travel.mytravelplan.domain.place.entity.TripPlace;

public interface TripPlaceRepository extends JpaRepository<TripPlace, Long>, TripPlaceRepositoryCustom {
}
