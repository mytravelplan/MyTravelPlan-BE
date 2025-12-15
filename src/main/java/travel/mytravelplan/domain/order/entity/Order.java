package travel.mytravelplan.domain.order.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.delivery.entity.Delivery;
import travel.mytravelplan.domain.delivery.enums.DeliveryStatus;
import travel.mytravelplan.domain.order.enums.Orderer;
import travel.mytravelplan.domain.order.exception.OrderException;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.entity.BaseEntity;
import travel.mytravelplan.domain.order.enums.OrderStatus;
import travel.mytravelplan.global.error.code.OrderErrorCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    @Embedded
    private Orderer orderer; // 주문자 정보

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderProduct> orderProducts = new ArrayList<>();

    private LocalDateTime orderDate; // 주문 시간

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus; // 주문 상태 [ORDER, CANCEL]

    @Builder(access = AccessLevel.PRIVATE)
    private Order(User user, Delivery delivery, Orderer orderer, LocalDateTime orderDate, OrderStatus orderStatus) {
        this.user = user;
        this.delivery = delivery;
        this.orderer = orderer;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
    }

    public static Order createOrder(User user, Delivery delivery, Orderer orderer, List<OrderProduct> orderProducts) {
        Order order = Order.builder()
                .orderStatus(OrderStatus.ORDER)
                .user(user)
                .orderer(orderer)
                .delivery(delivery)
                .orderDate(LocalDateTime.now())
                .build();

        delivery.setOrder(order);

        for (OrderProduct orderProduct : orderProducts) {
            order.addOrderProduct(orderProduct);
        }

        return order;
    }

    public void addOrderProduct(OrderProduct orderProduct) {
        orderProducts.add(orderProduct);
        orderProduct.setOrder(this);
    }

    public void cancel() {
        if(delivery.getDeliveryStatus() == DeliveryStatus.DELIVERING || delivery.getDeliveryStatus() == DeliveryStatus.COMP) {
            throw new OrderException(OrderErrorCode.CANNOT_CANCEL_ORDER);
        }
    }
}
