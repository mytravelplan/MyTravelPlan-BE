package travel.mytravelplan.domain.product.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductBookMarkDto {
    private Long productId;
    private Long userId;
    private boolean bookmarked;

    @Builder
    private ProductBookMarkDto(Long productId, Long userId, boolean bookmarked) {
        this.productId = productId;
        this.userId = userId;
        this.bookmarked = bookmarked;
    }
}
