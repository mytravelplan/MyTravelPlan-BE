package travel.mytravelplan.domain.diary.mapper;

import org.mapstruct.Mapper;
import travel.mytravelplan.domain.diary.dto.DiaryDto;
import travel.mytravelplan.domain.diary.entity.Diary;

@Mapper(componentModel = "spring")
public interface DiaryMapper {
    DiaryDto toDto(Diary diary);
}
