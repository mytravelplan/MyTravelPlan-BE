package travel.mytravelplan.domain.product.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import travel.mytravelplan.domain.product.entity.ProductBookMark;
import travel.mytravelplan.domain.product.dto.ProductBookMarkDto;

@Mapper(componentModel = "spring")
public interface ProductBookMarkMapper {
    @Mapping(target = "productId", source = "productBookMark.product.id")
    @Mapping(target = "userId", source = "productBookMark.user.id")
    @Mapping(target = "bookmarked", source = "isBookmarked")
    ProductBookMarkDto toDto(ProductBookMark productBookMark, boolean isBookmarked);
}
