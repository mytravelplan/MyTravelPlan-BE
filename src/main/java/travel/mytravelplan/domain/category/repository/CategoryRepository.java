package travel.mytravelplan.domain.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import travel.mytravelplan.domain.category.entity.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByParentIsNull();

    @Query("SELECT c FROM Category c WHERE c.id IN :ids")
    List<Category> findAllByIds(List<Long> ids);
}
