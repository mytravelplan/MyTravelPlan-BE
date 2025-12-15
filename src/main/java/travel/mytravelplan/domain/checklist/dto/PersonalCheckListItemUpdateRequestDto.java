package travel.mytravelplan.domain.checklist.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.checklist.enums.CheckListType;

@Getter
@NoArgsConstructor
public class PersonalCheckListItemUpdateRequestDto extends CheckListItemUpdateRequestDto {
    @Builder
    private PersonalCheckListItemUpdateRequestDto(CheckListType checkListType, String text, boolean checked) {
        super(checkListType, text, checked);
    }
}
