package travel.mytravelplan.domain.checklist.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PersonalCheckListDto extends CheckListDto {
    @Builder
    private PersonalCheckListDto(Long id, String name) {
        super(id, name);
    }
}
