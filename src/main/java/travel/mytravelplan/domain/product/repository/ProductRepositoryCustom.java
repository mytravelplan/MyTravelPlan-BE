package travel.mytravelplan.domain.product.repository;

import travel.mytravelplan.domain.product.entity.Product;

import java.util.List;

public interface ProductRepositoryCustom {
    List<Product> findAllCursor(String keyword, String orderBy, String direction, String cursor, Long after, int limit);
}
