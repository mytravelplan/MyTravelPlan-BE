package travel.mytravelplan.domain.category.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CategoryCreateRequestDto {
    private String name;
    private int depth;
    private Long parentId;

    @Builder
    private CategoryCreateRequestDto(String name, int depth, Long parentId) {
        this.name = name;
        this.depth = depth;
        this.parentId = parentId;
    }
}
