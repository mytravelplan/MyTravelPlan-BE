package travel.mytravelplan.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import travel.mytravelplan.domain.order.entity.Order;

import java.util.List;
import java.util.Optional;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {
    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.delivery " +
           "WHERE o.id = :orderId")
    Optional<Order> findWithDeliveryById(Long orderId);

}
