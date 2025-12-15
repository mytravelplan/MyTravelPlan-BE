package travel.mytravelplan.domain.schedule.repository;

import travel.mytravelplan.domain.schedule.entity.Schedule;
import java.util.List;

public interface ScheduleRepositoryCustom {
    List<Schedule> findAllByCursor(Long tripId, String keyword, String orderBy, String direction, String cursor, Long after, int limit);
    Long findMaxDisplayOrderByTripId(Long tripId);
}
