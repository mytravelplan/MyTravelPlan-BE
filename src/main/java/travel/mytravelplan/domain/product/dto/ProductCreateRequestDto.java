package travel.mytravelplan.domain.product.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ProductCreateRequestDto {
    private String name;
    private String imageUrl;
    private int price;
    private int stockQuantity;
    private List<Long> categoryIds;

    @Builder
    private ProductCreateRequestDto(String name, String imageUrl, int price, int stockQuantity, List<Long> categoryIds) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.categoryIds = categoryIds;
    }
}
