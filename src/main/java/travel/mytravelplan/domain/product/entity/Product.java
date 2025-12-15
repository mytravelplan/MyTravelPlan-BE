package travel.mytravelplan.domain.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.product.exception.ProductException;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.entity.BaseEntity;
import travel.mytravelplan.global.error.code.ProductErrorCode;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String imageUrl;

    private int price;

    private int stockQuantity;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductCategory> productCategories = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private User seller;

    @Builder(access = AccessLevel.PRIVATE)
    private Product(String name, String imageUrl, Integer price, int stockQuantity, User seller) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.seller = seller;
    }

    public static Product createProduct(String name, String imageUrl, Integer price, int stockQuantity, List<ProductCategory> productCategories, User seller) {
        Product product = Product.builder()
                .name(name)
                .imageUrl(imageUrl)
                .price(price)
                .stockQuantity(stockQuantity)
                .seller(seller)
                .build();

        for(ProductCategory productCategory : productCategories) {
            product.addProductCategory(productCategory);
        }

        return product;
    }

    private void addProductCategory(ProductCategory productCategory) {
        this.productCategories.add(productCategory);
        productCategory.setProduct(this);
    }

    public void update(String name, String imageUrl, Integer price, int stockQuantity, List<ProductCategory> productCategories) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.stockQuantity = stockQuantity;

        this.productCategories.clear();

        for(ProductCategory productCategory : productCategories) {
            this.addProductCategory(productCategory);
        }
    }

    public void decreaseStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new ProductException(ProductErrorCode.INSUFFICIENT_STOCK);
        }
        this.stockQuantity -= quantity;
    }
}
