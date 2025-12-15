package travel.mytravelplan.domain.checklist.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.checklist.enums.CheckListType;

@Getter
@NoArgsConstructor
public class SharedCheckListCreateRequestDto extends CheckListCreateRequestDto {
    @Builder
    private SharedCheckListCreateRequestDto(CheckListType checkListType, String name) {
        super(checkListType, name);
    }
}
