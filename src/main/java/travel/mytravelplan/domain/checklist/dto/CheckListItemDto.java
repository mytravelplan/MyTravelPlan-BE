package travel.mytravelplan.domain.checklist.dto;

import lombok.Getter;

@Getter
public abstract class CheckListItemDto {
    private Long id;
    private String text;
    private boolean checked;

    protected CheckListItemDto(Long id, String text, boolean checked) {
        this.id = id;
        this.text = text;
        this.checked = checked;
    }
}
