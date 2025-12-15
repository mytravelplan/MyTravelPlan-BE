package travel.mytravelplan.domain.place.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.place.entity.Place;

public interface PlaceRepository extends JpaRepository<Place, Long> {
}
