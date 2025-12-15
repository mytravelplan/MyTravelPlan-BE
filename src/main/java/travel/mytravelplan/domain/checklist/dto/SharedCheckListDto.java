package travel.mytravelplan.domain.checklist.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SharedCheckListDto extends CheckListDto {
    @Builder
    private SharedCheckListDto(Long id, String name) {
        super(id, name);
    }
}
