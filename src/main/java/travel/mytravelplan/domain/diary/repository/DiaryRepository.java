package travel.mytravelplan.domain.diary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.diary.entity.Diary;

public interface DiaryRepository extends JpaRepository<Diary, Long>, DiaryRepositoryCustom {

}
