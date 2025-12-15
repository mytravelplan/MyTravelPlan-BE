package travel.mytravelplan.domain.checklist.mapper;

import org.mapstruct.Mapper;
import travel.mytravelplan.domain.checklist.dto.CheckListItemDto;
import travel.mytravelplan.domain.checklist.dto.PersonalCheckListItemDto;
import travel.mytravelplan.domain.checklist.dto.SharedCheckListItemDto;
import travel.mytravelplan.domain.checklist.entity.CheckListItem;
import travel.mytravelplan.domain.checklist.entity.PersonalCheckListItem;
import travel.mytravelplan.domain.checklist.entity.SharedCheckListItem;

@Mapper(componentModel = "spring")
public interface CheckListItemMapper {
    PersonalCheckListItemDto toDto(PersonalCheckListItem personalCheckListItem);
    SharedCheckListItemDto toDto(SharedCheckListItem sharedCheckListItem);
}
