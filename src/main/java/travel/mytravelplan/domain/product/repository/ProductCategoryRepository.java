package travel.mytravelplan.domain.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.product.entity.ProductCategory;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
}
