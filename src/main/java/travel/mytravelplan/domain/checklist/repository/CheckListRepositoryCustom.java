package travel.mytravelplan.domain.checklist.repository;

import travel.mytravelplan.domain.checklist.entity.CheckList;
import java.util.List;

public interface CheckListRepositoryCustom {
    List<CheckList> findAllByCursor(Long tripId, String keyword, String orderBy, String direction, String cursor, Long after, int limit);
}
