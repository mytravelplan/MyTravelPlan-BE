package travel.mytravelplan.domain.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.product.entity.ProductBookMark;
import travel.mytravelplan.domain.product.entity.Product;
import travel.mytravelplan.domain.user.entity.User;

import java.util.Optional;

public interface ProductBookMarkRepository extends JpaRepository<ProductBookMark, Long> {
    Optional<ProductBookMark> findByProductAndUser(Product product, User user);
}
