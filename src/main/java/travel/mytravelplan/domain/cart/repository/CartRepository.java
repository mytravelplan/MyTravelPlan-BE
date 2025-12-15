package travel.mytravelplan.domain.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.cart.entity.Cart;
import travel.mytravelplan.domain.product.entity.Product;
import travel.mytravelplan.domain.user.entity.User;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByProductAndUser(Product product, User user);
}
