package travel.mytravelplan.domain.checklist.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.checklist.enums.CheckListType;

@Getter
@NoArgsConstructor
public class PersonalCheckListCreateRequestDto extends CheckListCreateRequestDto {
    @Builder
    private PersonalCheckListCreateRequestDto(CheckListType checkListType, String name) {
        super(checkListType, name);
    }
}
