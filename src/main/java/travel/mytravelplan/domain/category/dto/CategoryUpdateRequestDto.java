package travel.mytravelplan.domain.category.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CategoryUpdateRequestDto {
    private String name;

    @Builder
    private CategoryUpdateRequestDto(String name) {
        this.name = name;
    }
}
