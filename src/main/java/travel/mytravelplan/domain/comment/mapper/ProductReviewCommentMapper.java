package travel.mytravelplan.domain.comment.mapper;

import org.mapstruct.Mapper;
import travel.mytravelplan.domain.comment.dto.ProductReviewCommentDto;
import travel.mytravelplan.domain.comment.entity.ProductReviewComment;

@Mapper(componentModel = "spring")
public interface ProductReviewCommentMapper {
    ProductReviewCommentDto toDto(ProductReviewComment reviewComment);
}
