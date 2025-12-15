package travel.mytravelplan.domain.place.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.place.entity.CustomPlace;

public interface CustomPlaceRepository extends JpaRepository<CustomPlace, Long>, CustomPlaceRepositoryCustom {
}
