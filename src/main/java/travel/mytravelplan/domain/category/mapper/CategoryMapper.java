package travel.mytravelplan.domain.category.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import travel.mytravelplan.domain.category.dto.CategoryDto;
import travel.mytravelplan.domain.category.entity.Category;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class CategoryMapper {

    @Mapping(target = "children", expression = "java(resolveChildren(productCategory))")
    abstract public CategoryDto toDto(Category productCategory);

    protected List<CategoryDto> resolveChildren(Category productCategory) {
        return productCategory.getChildren().stream()
                .map(this::toDto)
                .toList();
    }

}
