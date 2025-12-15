package travel.mytravelplan.domain.review.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class ProductReviewUpdateRequestDto {
    private String content;
    private BigDecimal rating;

    @Builder
    private ProductReviewUpdateRequestDto(String content, BigDecimal rating) {
        this.content = content;
        this.rating = rating;
    }
}
