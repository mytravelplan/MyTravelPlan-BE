package travel.mytravelplan.domain.checklist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.checklist.entity.SharedCheckListItem;

import java.time.LocalDateTime;
import java.util.List;

public interface SharedCheckListItemRepository extends JpaRepository<SharedCheckListItem, Long>, SharedCheckListItemRepositoryCustom {
}
