package travel.mytravelplan.domain.order.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import travel.mytravelplan.domain.order.entity.Order;
import travel.mytravelplan.domain.order.entity.QOrder;
import travel.mytravelplan.domain.order.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QOrder order = QOrder.order;

    @Override
    public List<Order> findAllByCursor(String username, OrderStatus orderStatus, String orderBy, String direction, String cursor, Long after, int limit) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if(username != null) {
            booleanBuilder.and(order.user.username.eq(username));
        }

        if (orderStatus != null) {
            booleanBuilder.and(order.orderStatus.eq(orderStatus));
        }

        if (cursor != null && after != null) {
            if (orderBy.equals("createdAt")) {
                LocalDateTime createdAt = LocalDateTime.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(order.createdAt.gt(createdAt))
                            .or(order.createdAt.eq(createdAt).and(order.id.gt(after)));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(order.createdAt.lt(createdAt))
                            .or(order.createdAt.eq(createdAt).and(order.id.lt(after)));
                }
            }
        }

        OrderSpecifier<?> firstOrder = null;

        if (orderBy.equals("createdAt")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? order.createdAt.asc() : order.createdAt.desc();
        }

        OrderSpecifier<?> secondOrder = direction.equalsIgnoreCase("asc") ? order.id.asc() : order.id.desc();

        return queryFactory
                .selectFrom(order)
                .where(booleanBuilder)
                .orderBy(firstOrder, secondOrder)
                .limit(limit)
                .fetch();
    }
}
