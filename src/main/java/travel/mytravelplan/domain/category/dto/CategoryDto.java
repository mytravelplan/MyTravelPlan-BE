package travel.mytravelplan.domain.category.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
public class CategoryDto {
    private Long id;
    private String name;
    private int depth;
    private List<CategoryDto> children;

    @Builder
    private CategoryDto(Long id, String name, int depth, List<CategoryDto> children) {
        this.id = id;
        this.name = name;
        this.depth = depth;
        this.children = children;
    }
}
