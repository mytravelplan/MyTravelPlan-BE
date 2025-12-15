package travel.mytravelplan.domain.place.repository;

import travel.mytravelplan.domain.place.entity.CustomPlace;

import java.util.List;

public interface CustomPlaceRepositoryCustom {
    List<CustomPlace> findAllByCursor(String username, String keyword, String orderBy, String direction, String cursor, Long after, int limit);
}
