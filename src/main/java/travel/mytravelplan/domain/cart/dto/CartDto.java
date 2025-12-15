package travel.mytravelplan.domain.cart.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CartDto {
    private Long id;
    private Long productId;
    private int quantity;

    @Builder
    private CartDto(Long id, Long productId, int quantity) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
    }
}
