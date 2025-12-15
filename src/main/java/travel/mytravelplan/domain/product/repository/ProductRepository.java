package travel.mytravelplan.domain.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import travel.mytravelplan.domain.product.entity.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {
    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    List<Product> findAllByIds(List<Long> ids);

}
