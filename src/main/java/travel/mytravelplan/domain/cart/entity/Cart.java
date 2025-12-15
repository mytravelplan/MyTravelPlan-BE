package travel.mytravelplan.domain.cart.entity;

import jakarta.persistence.*;
import lombok.*;
import travel.mytravelplan.domain.product.entity.Product;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.entity.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder(access = AccessLevel.PRIVATE)
    private Cart(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public static Cart createCart(Product product, int quantity) {
        return Cart.builder()
                .product(product)
                .quantity(quantity)
                .build();
    }

    public void update(int quantity) {
        this.quantity = quantity;
    }
}
