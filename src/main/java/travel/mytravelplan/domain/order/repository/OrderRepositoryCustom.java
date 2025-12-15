package travel.mytravelplan.domain.order.repository;

import travel.mytravelplan.domain.order.entity.Order;
import travel.mytravelplan.domain.order.enums.OrderStatus;

import java.util.List;

public interface OrderRepositoryCustom {
    List<Order> findAllByCursor(String username, OrderStatus orderStatus, String orderBy, String direction, String cursor, Long after, int limit);
}
