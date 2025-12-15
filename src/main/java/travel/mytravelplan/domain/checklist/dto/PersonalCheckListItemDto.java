package travel.mytravelplan.domain.checklist.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
public class PersonalCheckListItemDto extends CheckListItemDto {
    @Builder
    private PersonalCheckListItemDto(Long id, String text, boolean checked) {
        super(id, text, checked);
    }
}
