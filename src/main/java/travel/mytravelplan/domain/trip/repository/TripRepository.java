package travel.mytravelplan.domain.trip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.trip.entity.Trip;

public interface TripRepository extends JpaRepository<Trip, Long>, TripRepositoryCustom {
}
