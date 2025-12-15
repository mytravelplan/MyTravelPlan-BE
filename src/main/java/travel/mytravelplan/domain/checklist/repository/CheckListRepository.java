package travel.mytravelplan.domain.checklist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.checklist.entity.CheckList;

public interface CheckListRepository extends JpaRepository<CheckList, Long>, CheckListRepositoryCustom {
}
