package travel.mytravelplan.domain.product.entity;

import jakarta.persistence.*;
import lombok.*;
import travel.mytravelplan.domain.category.entity.Category;
import travel.mytravelplan.global.common.entity.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductCategory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    @Builder(access = AccessLevel.PRIVATE)
    private ProductCategory(Category category) {
        this.category = category;
    }

    public static ProductCategory createProductCategory(Category category) {
        return ProductCategory.builder()
                .category(category)
                .build();
    }
}
