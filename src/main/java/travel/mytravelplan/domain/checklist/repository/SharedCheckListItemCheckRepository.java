package travel.mytravelplan.domain.checklist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.checklist.entity.PersonalCheckListItem;
import travel.mytravelplan.domain.checklist.entity.SharedCheckListItem;
import travel.mytravelplan.domain.checklist.entity.SharedCheckListItemCheck;

import java.time.LocalDateTime;
import java.util.List;

public interface SharedCheckListItemCheckRepository extends JpaRepository<SharedCheckListItemCheck, Long> {
}
