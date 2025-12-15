package travel.mytravelplan.domain.product.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductDto {
    private Long id;
    private String name;
    private String imageUrl;
    private int price;
    private int stockQuantity;

    @Builder
    private ProductDto(Long id, String name, String imageUrl, int price, int stockQuantity) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }
}
