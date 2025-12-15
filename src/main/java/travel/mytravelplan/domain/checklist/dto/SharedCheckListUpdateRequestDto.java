package travel.mytravelplan.domain.checklist.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.checklist.enums.CheckListType;

@Getter
@NoArgsConstructor
public class SharedCheckListUpdateRequestDto extends CheckListUpdateRequestDto {
    @Builder
    private SharedCheckListUpdateRequestDto(CheckListType checkListType, String name) {
        super(checkListType, name);
    }
}
