package travel.mytravelplan.domain.cart.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CartUpdateRequestDto {
    private int quantity;

    @Builder
    private CartUpdateRequestDto(int quantity) {
        this.quantity = quantity;
    }
}
