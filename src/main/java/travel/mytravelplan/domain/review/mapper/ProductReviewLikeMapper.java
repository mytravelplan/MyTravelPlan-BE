package travel.mytravelplan.domain.review.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import travel.mytravelplan.domain.review.dto.ProductReviewLikeDto;
import travel.mytravelplan.domain.review.entity.ProductReviewLike;

@Mapper(componentModel = "spring")
public interface ProductReviewLikeMapper {
    @Mapping(target = "reviewId", source = "productReviewLike.productReview.id")
    @Mapping(target = "userId", source = "productReviewLike.user.id")
    @Mapping(target = "liked", source = "liked")
    ProductReviewLikeDto toDto(ProductReviewLike productReviewLike, boolean liked);
}
