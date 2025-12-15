package travel.mytravelplan.domain.checklist.mapper;

import org.mapstruct.Mapper;
import travel.mytravelplan.domain.checklist.dto.CheckListDto;
import travel.mytravelplan.domain.checklist.dto.PersonalCheckListDto;
import travel.mytravelplan.domain.checklist.dto.SharedCheckListDto;
import travel.mytravelplan.domain.checklist.entity.CheckList;
import travel.mytravelplan.domain.checklist.entity.PersonalCheckList;
import travel.mytravelplan.domain.checklist.entity.SharedCheckList;

@Mapper(componentModel = "spring")
public interface CheckListMapper {
   default CheckListDto toDto(CheckList checkList) {
       if (checkList instanceof SharedCheckList) {
           return toDto((SharedCheckList) checkList);
       } else if (checkList instanceof PersonalCheckList) {
           return toDto((PersonalCheckList) checkList);
       }
       return null;
   }

   SharedCheckListDto toDto(SharedCheckList sharedCheckList);

   PersonalCheckListDto toDto(PersonalCheckList personalCheckList);
}
