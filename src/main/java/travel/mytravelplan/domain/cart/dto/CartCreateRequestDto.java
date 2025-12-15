package travel.mytravelplan.domain.cart.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CartCreateRequestDto {
    private Long productId;
    private int quantity;

    @Builder
    private CartCreateRequestDto(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}
