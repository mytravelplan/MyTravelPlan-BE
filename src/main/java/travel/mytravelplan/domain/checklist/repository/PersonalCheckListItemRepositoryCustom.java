package travel.mytravelplan.domain.checklist.repository;

import travel.mytravelplan.domain.checklist.entity.PersonalCheckListItem;
import java.util.List;

public interface PersonalCheckListItemRepositoryCustom {
    List<PersonalCheckListItem> findAllByCursor(Long checkListId, String keyword, String orderBy, String direction, String cursor, Long after, int limit);
}
