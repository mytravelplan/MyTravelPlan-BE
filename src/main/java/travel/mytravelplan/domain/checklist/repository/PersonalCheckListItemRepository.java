package travel.mytravelplan.domain.checklist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.checklist.entity.PersonalCheckListItem;

import java.time.LocalDateTime;
import java.util.List;

public interface PersonalCheckListItemRepository extends JpaRepository<PersonalCheckListItem, Long>, PersonalCheckListItemRepositoryCustom {
}
