package travel.mytravelplan.domain.review.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.product.entity.Product;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductReview extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ElementCollection
    @CollectionTable(
            name = "product_review_image_url",
            joinColumns = @JoinColumn(name = "product_review_id")
    )
    private List<String> imageUrls = new ArrayList<>();

    private String content;

    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal rating;


    @Builder(access = AccessLevel.PRIVATE)
    private ProductReview(User user, Product product, BigDecimal rating, String content) {
        this.user = user;
        this.product = product;
        this.rating = rating;
        this.content = content;
    }

    public static ProductReview createProductReview(User user, Product product, BigDecimal rating, String content) {
        return ProductReview.builder()
                .user(user)
                .product(product)
                .rating(rating)
                .content(content)
                .build();
    }

    public void update(String content, BigDecimal rating) {
        this.content = content;
        this.rating = rating;
    }
}