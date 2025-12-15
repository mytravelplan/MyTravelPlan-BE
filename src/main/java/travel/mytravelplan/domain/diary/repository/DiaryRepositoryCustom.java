package travel.mytravelplan.domain.diary.repository;

import travel.mytravelplan.domain.diary.entity.Diary;

import java.util.List;

public interface DiaryRepositoryCustom {
    List<Diary> findAllByCursor(Long tripId, String keyword, String orderBy, String direction, String cursor, Long after, int limit);
}
