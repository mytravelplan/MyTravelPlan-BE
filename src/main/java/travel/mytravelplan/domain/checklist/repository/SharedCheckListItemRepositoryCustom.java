package travel.mytravelplan.domain.checklist.repository;

import travel.mytravelplan.domain.checklist.entity.SharedCheckListItem;
import java.util.List;

public interface SharedCheckListItemRepositoryCustom {
    List<SharedCheckListItem> findAllByCursor(Long checkListId, String keyword, String orderBy, String direction, String cursor, Long after, int limit);
}
