package travel.mytravelplan.domain.review.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class ProductReviewCreateRequestDto {
    private String content;
    private BigDecimal rating;

    @Builder
    private ProductReviewCreateRequestDto(String content, BigDecimal rating) {
        this.content = content;
        this.rating = rating;
    }
}
