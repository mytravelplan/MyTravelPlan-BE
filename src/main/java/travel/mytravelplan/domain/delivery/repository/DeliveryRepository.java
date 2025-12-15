package travel.mytravelplan.domain.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.delivery.entity.Delivery;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
}
