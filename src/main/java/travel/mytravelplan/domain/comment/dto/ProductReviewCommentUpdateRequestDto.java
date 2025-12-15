package travel.mytravelplan.domain.comment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductReviewCommentUpdateRequestDto {
    private String content;

    @Builder
    private ProductReviewCommentUpdateRequestDto(String content) {
        this.content = content;
    }
}
