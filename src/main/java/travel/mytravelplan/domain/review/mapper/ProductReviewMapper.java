package travel.mytravelplan.domain.review.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import travel.mytravelplan.domain.review.dto.ProductReviewDto;
import travel.mytravelplan.domain.review.entity.ProductReview;
import travel.mytravelplan.domain.user.entity.User;

@Mapper(componentModel = "spring")
public interface ProductReviewMapper {
    @Mapping(target = "id", source = "productReview.id")
    @Mapping(target = "userId", source = "productReview.user.id")
    ProductReviewDto toDto(ProductReview productReview, User user);
}
