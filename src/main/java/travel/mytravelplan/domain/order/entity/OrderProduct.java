package travel.mytravelplan.domain.order.entity;

import jakarta.persistence.*;
import lombok.*;
import travel.mytravelplan.domain.product.entity.Product;
import travel.mytravelplan.global.common.entity.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderProduct extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice; // 주문 가격
    private int quantity; // 주문 수량

    @Builder(access = AccessLevel.PRIVATE)
    private OrderProduct(Product product, int orderPrice, int quantity) {
        this.product = product;
        this.orderPrice = orderPrice;
        this.quantity = quantity;
    }

    public static OrderProduct createOrderProduct(Product product, int orderPrice, int quantity) {
        return OrderProduct.builder()
                .product(product)
                .orderPrice(orderPrice)
                .quantity(quantity)
                .build();
    }
}
