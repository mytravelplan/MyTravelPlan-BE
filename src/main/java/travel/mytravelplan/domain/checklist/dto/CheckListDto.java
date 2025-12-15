package travel.mytravelplan.domain.checklist.dto;

import lombok.Getter;

@Getter
public abstract class CheckListDto {
    private Long id;
    private String name;

    protected CheckListDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
