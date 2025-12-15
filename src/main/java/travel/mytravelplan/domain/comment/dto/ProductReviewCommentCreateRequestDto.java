package travel.mytravelplan.domain.comment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductReviewCommentCreateRequestDto {
    private String content;

    @Builder
    private ProductReviewCommentCreateRequestDto(String content) {
        this.content = content;
    }
}
