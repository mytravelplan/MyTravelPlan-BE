package travel.mytravelplan.domain.place.repository;

import travel.mytravelplan.domain.place.entity.TripPlace;

import java.util.List;

public interface TripPlaceRepositoryCustom {
    List<TripPlace> findAllByCursor(String keyword, String orderBy, String direction, String cursor, Long after, int limit);
}
