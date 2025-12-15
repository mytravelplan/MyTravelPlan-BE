package travel.mytravelplan.domain.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.delivery.entity.DeliveryAddress;
import travel.mytravelplan.domain.user.entity.User;

import java.util.List;

public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {
    List<DeliveryAddress> findAllByUser(User user);
}
