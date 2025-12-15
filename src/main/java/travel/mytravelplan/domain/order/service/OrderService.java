package travel.mytravelplan.domain.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.delivery.dto.DeliveryAddressDto;
import travel.mytravelplan.domain.delivery.entity.Delivery;
import travel.mytravelplan.domain.delivery.entity.DeliveryAddress;
import travel.mytravelplan.domain.delivery.enums.Address;
import travel.mytravelplan.domain.delivery.exception.DeliveryAddressException;
import travel.mytravelplan.domain.delivery.repository.DeliveryAddressRepository;
import travel.mytravelplan.domain.order.dto.OrderCreateRequestDto;
import travel.mytravelplan.domain.order.dto.OrderDto;
import travel.mytravelplan.domain.order.dto.OrdererDto;
import travel.mytravelplan.domain.order.entity.Order;
import travel.mytravelplan.domain.order.entity.OrderProduct;
import travel.mytravelplan.domain.order.enums.OrderStatus;
import travel.mytravelplan.domain.order.exception.OrderException;
import travel.mytravelplan.domain.order.mapper.OrderMapper;
import travel.mytravelplan.domain.order.repository.OrderRepository;
import travel.mytravelplan.domain.product.entity.Product;
import travel.mytravelplan.domain.product.repository.ProductRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.error.code.DeliveryAddressErrorCode;
import travel.mytravelplan.global.error.code.OrderErrorCode;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderDto createOrder(User currentUser, OrderCreateRequestDto orderCreateRequestDto) {
        List<Long> ids = orderCreateRequestDto.getProducts().keySet().stream().toList();
        Map<Long, Product> products = productRepository.findAllByIds(ids)
                .stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        for (Map.Entry<Long, Integer> entry : orderCreateRequestDto.getProducts().entrySet()) {
            Long productId = entry.getKey();
            int quantity = entry.getValue();
            Product product = products.get(productId);

            product.decreaseStock(quantity);
        }

        DeliveryAddress deliveryAddress = deliveryAddressRepository.findById(orderCreateRequestDto.getDeliveryAddressId())
                .orElseThrow(() -> new DeliveryAddressException(DeliveryAddressErrorCode.DELIVERY_ADDRESS_NOT_FOUND));

        Delivery delivery = Delivery.createDelivery(deliveryAddress.getAddress(), orderCreateRequestDto.getRequirement());

        List<OrderProduct> orderProducts = products.entrySet().stream()
                .map(entry -> {
                    Long productId = entry.getKey();
                    Product product = entry.getValue();
                    int quantity = orderCreateRequestDto.getProducts().get(productId);
                    return OrderProduct.createOrderProduct(product, product.getPrice(), quantity);
                })
                .toList();

        Order order = Order.createOrder(currentUser, delivery, orderCreateRequestDto.getOrderer(), orderProducts);

        orderRepository.save(order);

        return orderMapper.toDto(order);
    }

    public OrderDto getOrder(Long orderId) {
        Order order = orderRepository.findWithDeliveryById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        return orderMapper.toDto(order);
/*
        return OrderDto.builder()
                .orderId(order.getId())
                .orderDate(order.getCreatedAt())
                .totalPrice(order.getOrderProducts().stream()
                        .mapToInt(orderProduct -> orderProduct.getOrderPrice() * orderProduct.getQuantity())
                        .sum()
                )
                .orderer(OrdererDto.builder()
                        .name(order.getOrderer().getName())
                        .phoneNumber(order.getOrderer().getPhoneNumber())
                        .email(order.getOrderer().getEmail())
                        .build())
                .deliveryAddress(
                        DeliveryAddressDto.builder()
                                .address(Address.builder()
                                        .recipient("홍길동")
                                        .phone("010-1234-5678")
                                        .zipcode("12345")
                                        .address("서울특별시 강남구 테헤란로 123")
                                        .detailAddress("101동 202호")
                                        .build())
                                .build()
                )
                .build();
*/
    }

    public CursorPageResponseDto<OrderDto> getOrders(User currentUser, OrderStatus orderStatus, String orderBy, String direction, String cursor, Long after, int limit) {
        List<Order> orders = orderRepository.findAllByCursor(currentUser.getUsername(), orderStatus, orderBy, direction, cursor, after, limit + 1);

        boolean hasNext = orders.size() > limit;

        List<Order> pagedOrders = hasNext ? orders.subList(0, limit) : orders;

        List<OrderDto> orderDtos = pagedOrders.stream()
                .map(orderMapper::toDto)
                .toList();

        String nextCursor = null;
        Long nextAfter = null;

        if (hasNext) {
            Order lastOrder = pagedOrders.getLast();

            if (orderBy.equals("createdAt")) {
                nextCursor = lastOrder.getCreatedAt().toString();
            }

            nextAfter = lastOrder.getId();
        }

        return CursorPageResponseDto.<OrderDto>builder()
                .content(orderDtos)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(orderDtos.size())
                .hasNext(hasNext)
                .build();
    }

    @Transactional
    public OrderDto cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        order.cancel();

        return orderMapper.toDto(order);
    }
}
