package travel.mytravelplan.domain.trip.repository;

import travel.mytravelplan.domain.trip.entity.Trip;

import java.util.List;

public interface TripRepositoryCustom {
    List<Trip> findAllByUserCursor(String username, String orderBy, String direction, String cursor, Long after, int limit);
}
