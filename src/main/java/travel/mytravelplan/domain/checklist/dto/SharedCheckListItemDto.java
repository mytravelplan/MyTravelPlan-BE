package travel.mytravelplan.domain.checklist.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SharedCheckListItemDto extends CheckListItemDto {
    @Builder
    private SharedCheckListItemDto(Long id, String text, boolean checked) {
        super(id, text, checked);
    }
}
