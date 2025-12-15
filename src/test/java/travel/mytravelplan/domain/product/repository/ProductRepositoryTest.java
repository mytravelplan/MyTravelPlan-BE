package travel.mytravelplan.domain.product.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.category.entity.Category;
import travel.mytravelplan.domain.category.repository.CategoryRepository;
import travel.mytravelplan.domain.product.entity.Product;
import travel.mytravelplan.domain.product.entity.ProductCategory;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.support.RepositoryTestSupport;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("상품 레포지토리 테스트")
class ProductRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("상품을 저장한다")
    void saveProduct() {
        // given
        User seller = createUser("seller", "seller@email.com");
        Category category = createAndSaveCategory("전자기기", 1, null);
        ProductCategory productCategory = createProductCategory(category);
        Product product = createProduct("노트북", "image.jpg", 1500000, 10, List.of(productCategory), seller);

        // when
        Product savedProduct = productRepository.save(product);
        em.flush();
        em.clear();

        // then
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("노트북");
        assertThat(savedProduct.getPrice()).isEqualTo(1500000);
        assertThat(savedProduct.getStockQuantity()).isEqualTo(10);
        assertThat(savedProduct.getSeller().getId()).isEqualTo(seller.getId());
        assertThat(savedProduct.getProductCategories()).hasSize(1);
    }

    @Test
    @DisplayName("상품을 ID로 조회한다")
    void findProductById() {
        // given
        User seller = createUser("seller", "seller@email.com");
        Category category = createAndSaveCategory("의류", 1, null);
        ProductCategory productCategory = createProductCategory(category);
        Product product = createAndSaveProduct("티셔츠", "tshirt.jpg", 30000, 50, List.of(productCategory), seller);
        em.flush();
        em.clear();

        // when
        Product foundProduct = productRepository.findById(product.getId()).orElse(null);

        // then
        assertThat(foundProduct).isNotNull();
        assertThat(foundProduct.getId()).isEqualTo(product.getId());
        assertThat(foundProduct.getName()).isEqualTo("티셔츠");
        assertThat(foundProduct.getPrice()).isEqualTo(30000);
    }

    @Test
    @DisplayName("상품을 수정한다")
    void updateProduct() {
        // given
        User seller = createUser("seller", "seller@email.com");
        Category category1 = createAndSaveCategory("전자기기", 1, null);
        Category category2 = createAndSaveCategory("컴퓨터", 1, null);
        ProductCategory productCategory1 = createProductCategory(category1);
        Product product = createAndSaveProduct("노트북", "laptop.jpg", 1500000, 10, List.of(productCategory1), seller);
        em.flush();
        em.clear();

        // when
        Product foundProduct = productRepository.findById(product.getId()).orElseThrow();
        ProductCategory productCategory2 = createProductCategory(category2);
        foundProduct.update("게이밍 노트북", "gaming-laptop.jpg", 2000000, 5, List.of(productCategory2));
        em.flush();
        em.clear();

        // then
        Product updatedProduct = productRepository.findById(product.getId()).orElse(null);
        assertThat(updatedProduct).isNotNull();
        assertThat(updatedProduct.getName()).isEqualTo("게이밍 노트북");
        assertThat(updatedProduct.getImageUrl()).isEqualTo("gaming-laptop.jpg");
        assertThat(updatedProduct.getPrice()).isEqualTo(2000000);
        assertThat(updatedProduct.getStockQuantity()).isEqualTo(5);
        assertThat(updatedProduct.getProductCategories()).hasSize(1);
    }

    @Test
    @DisplayName("상품을 삭제한다")
    void deleteProduct() {
        // given
        User seller = createUser("seller", "seller@email.com");
        Category category = createAndSaveCategory("식품", 1, null);
        ProductCategory productCategory = createProductCategory(category);
        Product product = createAndSaveProduct("사과", "apple.jpg", 5000, 100, List.of(productCategory), seller);
        em.flush();
        em.clear();

        // when
        productRepository.deleteById(product.getId());
        em.flush();
        em.clear();

        // then
        Product deletedProduct = productRepository.findById(product.getId()).orElse(null);
        assertThat(deletedProduct).isNull();
    }

    @Test
    @DisplayName("여러 ID로 상품을 조회한다")
    void findAllByIds() {
        // given
        User seller = createUser("seller", "seller@email.com");
        Category category = createAndSaveCategory("전자기기", 1, null);
        ProductCategory pc1 = createProductCategory(category);
        ProductCategory pc2 = createProductCategory(category);
        ProductCategory pc3 = createProductCategory(category);

        Product product1 = createAndSaveProduct("노트북", "laptop.jpg", 1500000, 10, List.of(pc1), seller);
        Product product2 = createAndSaveProduct("마우스", "mouse.jpg", 30000, 50, List.of(pc2), seller);
        Product product3 = createAndSaveProduct("키보드", "keyboard.jpg", 80000, 30, List.of(pc3), seller);
        em.flush();
        em.clear();

        // when
        List<Product> products = productRepository.findAllByIds(List.of(product1.getId(), product2.getId()));

        // then
        assertThat(products).hasSize(2);
        assertThat(products)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("노트북", "마우스");
    }

    @Test
    @DisplayName("키워드로 상품을 검색한다")
    void findAllByCursor_byKeyword() {
        // given
        User seller = createUser("seller", "seller@email.com");
        Category category = createAndSaveCategory("전자기기", 1, null);

        ProductCategory pc1 = createProductCategory(category);
        ProductCategory pc2 = createProductCategory(category);
        ProductCategory pc3 = createProductCategory(category);

        Product product1 = createAndSaveProduct("Apple 노트북", "laptop.jpg", 2000000, 10, List.of(pc1), seller);
        Product product2 = createAndSaveProduct("Samsung 노트북", "samsung.jpg", 1500000, 15, List.of(pc2), seller);
        Product product3 = createAndSaveProduct("Apple 마우스", "mouse.jpg", 80000, 50, List.of(pc3), seller);
        em.flush();
        em.clear();

        // when
        List<Product> products = productRepository.findAllCursor(
                "Apple",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(products).hasSize(2);
        assertThat(products)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Apple 노트북", "Apple 마우스");
    }

    @Test
    @DisplayName("생성일 기준 내림차순으로 상품을 조회한다")
    void findAllByCursor_orderByCreatedAtDesc() {
        // given
        User seller = createUser("seller", "seller@email.com");
        Category category = createAndSaveCategory("전자기기", 1, null);

        ProductCategory pc1 = createProductCategory(category);
        ProductCategory pc2 = createProductCategory(category);
        ProductCategory pc3 = createProductCategory(category);

        Product product1 = createAndSaveProduct("상품1", "img1.jpg", 10000, 10, List.of(pc1), seller);
        Product product2 = createAndSaveProduct("상품2", "img2.jpg", 20000, 20, List.of(pc2), seller);
        Product product3 = createAndSaveProduct("상품3", "img3.jpg", 30000, 30, List.of(pc3), seller);
        em.flush();
        em.clear();

        // when
        List<Product> products = productRepository.findAllCursor(
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(products).hasSize(3);
        assertThat(products.get(0).getName()).isEqualTo("상품3");
        assertThat(products.get(1).getName()).isEqualTo("상품2");
        assertThat(products.get(2).getName()).isEqualTo("상품1");
    }

    @Test
    @DisplayName("생성일 기준 오름차순으로 상품을 조회한다")
    void findAllByCursor_orderByCreatedAtAsc() {
        // given
        User seller = createUser("seller", "seller@email.com");
        Category category = createAndSaveCategory("전자기기", 1, null);

        ProductCategory pc1 = createProductCategory(category);
        ProductCategory pc2 = createProductCategory(category);
        ProductCategory pc3 = createProductCategory(category);

        Product product1 = createAndSaveProduct("상품1", "img1.jpg", 10000, 10, List.of(pc1), seller);
        Product product2 = createAndSaveProduct("상품2", "img2.jpg", 20000, 20, List.of(pc2), seller);
        Product product3 = createAndSaveProduct("상품3", "img3.jpg", 30000, 30, List.of(pc3), seller);
        em.flush();
        em.clear();

        // when
        List<Product> products = productRepository.findAllCursor(
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(products).hasSize(3);
        assertThat(products.get(0).getName()).isEqualTo("상품1");
        assertThat(products.get(1).getName()).isEqualTo("상품2");
        assertThat(products.get(2).getName()).isEqualTo("상품3");
    }

    @Test
    @DisplayName("limit 개수만큼 상품을 조회한다")
    void findAllByCursor_withLimit() {
        // given
        User seller = createUser("seller", "seller@email.com");
        Category category = createAndSaveCategory("전자기기", 1, null);

        ProductCategory pc1 = createProductCategory(category);
        ProductCategory pc2 = createProductCategory(category);
        ProductCategory pc3 = createProductCategory(category);
        ProductCategory pc4 = createProductCategory(category);
        ProductCategory pc5 = createProductCategory(category);

        Product product1 = createAndSaveProduct("상품1", "img1.jpg", 10000, 10, List.of(pc1), seller);
        Product product2 = createAndSaveProduct("상품2", "img2.jpg", 20000, 20, List.of(pc2), seller);
        Product product3 = createAndSaveProduct("상품3", "img3.jpg", 30000, 30, List.of(pc3), seller);
        Product product4 = createAndSaveProduct("상품4", "img4.jpg", 40000, 40, List.of(pc4), seller);
        Product product5 = createAndSaveProduct("상품5", "img5.jpg", 50000, 50, List.of(pc5), seller);
        em.flush();
        em.clear();

        // when
        List<Product> products = productRepository.findAllCursor(
                null,
                "createdAt",
                "desc",
                null,
                null,
                3
        );

        // then
        assertThat(products).hasSize(3);
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 상품을 조회한다 - 내림차순")
    void findAllByCursor_withCursor_desc() {
        // given
        User seller = createUser("seller", "seller@email.com");
        Category category = createAndSaveCategory("전자기기", 1, null);

        ProductCategory pc1 = createProductCategory(category);
        ProductCategory pc2 = createProductCategory(category);
        ProductCategory pc3 = createProductCategory(category);

        Product product1 = createAndSaveProduct("상품1", "img1.jpg", 10000, 10, List.of(pc1), seller);
        Product product2 = createAndSaveProduct("상품2", "img2.jpg", 20000, 20, List.of(pc2), seller);
        Product product3 = createAndSaveProduct("상품3", "img3.jpg", 30000, 30, List.of(pc3), seller);
        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Product> firstPage = productRepository.findAllCursor(
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getName()).isEqualTo("상품3");
        assertThat(firstPage.get(1).getName()).isEqualTo("상품2");

        // when - 두 번째 페이지 조회
        Product lastProduct = firstPage.get(firstPage.size() - 1);
        List<Product> secondPage = productRepository.findAllCursor(
                null,
                "createdAt",
                "desc",
                lastProduct.getCreatedAt().toString(),
                lastProduct.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getName()).isEqualTo("상품1");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 상품을 조회한다 - 오름차순")
    void findAllByCursor_withCursor_asc() {
        // given
        User seller = createUser("seller", "seller@email.com");
        Category category = createAndSaveCategory("전자기기", 1, null);

        ProductCategory pc1 = createProductCategory(category);
        ProductCategory pc2 = createProductCategory(category);
        ProductCategory pc3 = createProductCategory(category);

        Product product1 = createAndSaveProduct("상품1", "img1.jpg", 10000, 10, List.of(pc1), seller);
        Product product2 = createAndSaveProduct("상품2", "img2.jpg", 20000, 20, List.of(pc2), seller);
        Product product3 = createAndSaveProduct("상품3", "img3.jpg", 30000, 30, List.of(pc3), seller);
        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Product> firstPage = productRepository.findAllCursor(
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getName()).isEqualTo("상품1");
        assertThat(firstPage.get(1).getName()).isEqualTo("상품2");

        // when - 두 번째 페이지 조회
        Product lastProduct = firstPage.get(firstPage.size() - 1);
        List<Product> secondPage = productRepository.findAllCursor(
                null,
                "createdAt",
                "asc",
                lastProduct.getCreatedAt().toString(),
                lastProduct.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getName()).isEqualTo("상품3");
    }

    @Test
    @DisplayName("상품의 재고를 감소시킨다")
    void decreaseStock() {
        // given
        User seller = createUser("seller", "seller@email.com");
        Category category = createAndSaveCategory("전자기기", 1, null);
        ProductCategory productCategory = createProductCategory(category);
        Product product = createAndSaveProduct("노트북", "laptop.jpg", 1500000, 10, List.of(productCategory), seller);
        em.flush();
        em.clear();

        // when
        Product foundProduct = productRepository.findById(product.getId()).orElseThrow();
        foundProduct.decreaseStock(3);
        em.flush();
        em.clear();

        // then
        Product updatedProduct = productRepository.findById(product.getId()).orElse(null);
        assertThat(updatedProduct).isNotNull();
        assertThat(updatedProduct.getStockQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("여러 카테고리를 가진 상품을 저장한다")
    void saveProductWithMultipleCategories() {
        // given
        User seller = createUser("seller", "seller@email.com");
        Category category1 = createAndSaveCategory("전자기기", 1, null);
        Category category2 = createAndSaveCategory("컴퓨터", 1, null);
        Category category3 = createAndSaveCategory("노트북", 2, category2);

        ProductCategory pc1 = createProductCategory(category1);
        ProductCategory pc2 = createProductCategory(category2);
        ProductCategory pc3 = createProductCategory(category3);

        Product product = createAndSaveProduct("게이밍 노트북", "gaming.jpg", 2500000, 5,
                List.of(pc1, pc2, pc3), seller);
        em.flush();
        em.clear();

        // when
        Product foundProduct = productRepository.findById(product.getId()).orElse(null);

        // then
        assertThat(foundProduct).isNotNull();
        assertThat(foundProduct.getProductCategories()).hasSize(3);
        assertThat(foundProduct.getProductCategories())
                .extracting(pc -> pc.getCategory().getName())
                .containsExactlyInAnyOrder("전자기기", "컴퓨터", "노트북");
    }

    @Test
    @DisplayName("커서와 after가 null일 때 상품을 조회한다")
    void findAllByCursor_whenCursorAndAfterAreNull() {
        // given
        User seller = createUser("seller", "seller@email.com");
        Category category = createAndSaveCategory("전자기기", 1, null);

        ProductCategory pc1 = createProductCategory(category);
        ProductCategory pc2 = createProductCategory(category);

        Product product1 = createAndSaveProduct("상품1", "img1.jpg", 10000, 10, List.of(pc1), seller);
        Product product2 = createAndSaveProduct("상품2", "img2.jpg", 20000, 20, List.of(pc2), seller);
        em.flush();
        em.clear();

        // when
        List<Product> products = productRepository.findAllCursor(
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(products).hasSize(2);
        assertThat(products.get(0).getName()).isEqualTo("상품2");
        assertThat(products.get(1).getName()).isEqualTo("상품1");
    }

    @Test
    @DisplayName("커서만 있고 after가 null일 때 상품을 조회한다")
    void findAllByCursor_whenCursorIsProvidedAndAfterIsNull() {
        // given
        User seller = createUser("seller", "seller@email.com");
        Category category = createAndSaveCategory("전자기기", 1, null);

        ProductCategory pc1 = createProductCategory(category);
        ProductCategory pc2 = createProductCategory(category);
        ProductCategory pc3 = createProductCategory(category);

        Product product1 = createAndSaveProduct("상품1", "img1.jpg", 10000, 10, List.of(pc1), seller);
        Product product2 = createAndSaveProduct("상품2", "img2.jpg", 20000, 20, List.of(pc2), seller);
        Product product3 = createAndSaveProduct("상품3", "img3.jpg", 30000, 30, List.of(pc3), seller);
        em.flush();
        em.clear();

        // when - cursor만 제공
        List<Product> products = productRepository.findAllCursor(
                null,
                "createdAt",
                "desc",
                product2.getCreatedAt().toString(),
                null,
                10
        );

        // then
        assertThat(products).isNotEmpty();
    }

    @Test
    @DisplayName("after만 있고 cursor가 null일 때 상품을 조회한다")
    void findAllByCursor_whenAfterIsProvidedAndCursorIsNull() {
        // given
        User seller = createUser("seller", "seller@email.com");
        Category category = createAndSaveCategory("전자기기", 1, null);

        ProductCategory pc1 = createProductCategory(category);
        ProductCategory pc2 = createProductCategory(category);
        ProductCategory pc3 = createProductCategory(category);

        Product product1 = createAndSaveProduct("상품1", "img1.jpg", 10000, 10, List.of(pc1), seller);
        Product product2 = createAndSaveProduct("상품2", "img2.jpg", 20000, 20, List.of(pc2), seller);
        Product product3 = createAndSaveProduct("상품3", "img3.jpg", 30000, 30, List.of(pc3), seller);
        em.flush();
        em.clear();

        // when - after만 제공
        List<Product> products = productRepository.findAllCursor(
                null,
                "createdAt",
                "desc",
                null,
                product2.getId(),
                10
        );

        // then
        assertThat(products).isNotEmpty();
    }

    @Test
    @DisplayName("커서와 after 모두 있을 때 상품을 조회한다 - 내림차순")
    void findAllByCursor_whenBothCursorAndAfterAreProvided_desc() {
        // given
        User seller = createUser("seller", "seller@email.com");
        Category category = createAndSaveCategory("전자기기", 1, null);

        ProductCategory pc1 = createProductCategory(category);
        ProductCategory pc2 = createProductCategory(category);
        ProductCategory pc3 = createProductCategory(category);
        ProductCategory pc4 = createProductCategory(category);

        Product product1 = createAndSaveProduct("상품1", "img1.jpg", 10000, 10, List.of(pc1), seller);
        Product product2 = createAndSaveProduct("상품2", "img2.jpg", 20000, 20, List.of(pc2), seller);
        Product product3 = createAndSaveProduct("상품3", "img3.jpg", 30000, 30, List.of(pc3), seller);
        Product product4 = createAndSaveProduct("상품4", "img4.jpg", 40000, 40, List.of(pc4), seller);
        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Product> firstPage = productRepository.findAllCursor(
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        Product lastProduct = firstPage.get(firstPage.size() - 1);

        // when - cursor와 after 모두 제공하여 두 번째 페이지 조회
        List<Product> secondPage = productRepository.findAllCursor(
                null,
                "createdAt",
                "desc",
                lastProduct.getCreatedAt().toString(),
                lastProduct.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage.get(0).getId()).isNotEqualTo(lastProduct.getId());
    }

    @Test
    @DisplayName("커서와 after 모두 있을 때 상품을 조회한다 - 오름차순")
    void findAllByCursor_whenBothCursorAndAfterAreProvided_asc() {
        // given
        User seller = createUser("seller", "seller@email.com");
        Category category = createAndSaveCategory("전자기기", 1, null);

        ProductCategory pc1 = createProductCategory(category);
        ProductCategory pc2 = createProductCategory(category);
        ProductCategory pc3 = createProductCategory(category);
        ProductCategory pc4 = createProductCategory(category);

        Product product1 = createAndSaveProduct("상품1", "img1.jpg", 10000, 10, List.of(pc1), seller);
        Product product2 = createAndSaveProduct("상품2", "img2.jpg", 20000, 20, List.of(pc2), seller);
        Product product3 = createAndSaveProduct("상품3", "img3.jpg", 30000, 30, List.of(pc3), seller);
        Product product4 = createAndSaveProduct("상품4", "img4.jpg", 40000, 40, List.of(pc4), seller);
        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Product> firstPage = productRepository.findAllCursor(
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        Product lastProduct = firstPage.get(firstPage.size() - 1);

        // when - cursor와 after 모두 제공하여 두 번째 페이지 조회
        List<Product> secondPage = productRepository.findAllCursor(
                null,
                "createdAt",
                "asc",
                lastProduct.getCreatedAt().toString(),
                lastProduct.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage.get(0).getId()).isNotEqualTo(lastProduct.getId());
    }

    @Test
    @DisplayName("키워드 검색과 커서 페이지네이션을 함께 사용한다")
    void findAllByCursor_withKeywordAndCursor() {
        // given
        User seller = createUser("seller", "seller@email.com");
        Category category = createAndSaveCategory("전자기기", 1, null);

        ProductCategory pc1 = createProductCategory(category);
        ProductCategory pc2 = createProductCategory(category);
        ProductCategory pc3 = createProductCategory(category);
        ProductCategory pc4 = createProductCategory(category);

        Product product1 = createAndSaveProduct("Apple 노트북 Pro", "img1.jpg", 2000000, 10, List.of(pc1), seller);
        Product product2 = createAndSaveProduct("Apple 노트북 Air", "img2.jpg", 1500000, 20, List.of(pc2), seller);
        Product product3 = createAndSaveProduct("Apple 마우스", "img3.jpg", 80000, 30, List.of(pc3), seller);
        Product product4 = createAndSaveProduct("Samsung 노트북", "img4.jpg", 1200000, 15, List.of(pc4), seller);
        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Product> firstPage = productRepository.findAllCursor(
                "Apple",
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage)
                .allMatch(p -> p.getName().contains("Apple"));

        // when - cursor와 after를 제공하여 두 번째 페이지 조회
        Product lastProduct = firstPage.get(firstPage.size() - 1);
        List<Product> secondPage = productRepository.findAllCursor(
                "Apple",
                "createdAt",
                "desc",
                lastProduct.getCreatedAt().toString(),
                lastProduct.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getName()).contains("Apple");
        assertThat(secondPage.get(0).getId()).isNotEqualTo(lastProduct.getId());
    }

    @Test
    @DisplayName("커서 페이지네이션으로 빈 결과를 조회한다")
    void findAllByCursor_emptyResult() {
        // given
        User seller = createUser("seller", "seller@email.com");
        Category category = createAndSaveCategory("전자기기", 1, null);

        ProductCategory pc1 = createProductCategory(category);

        Product product1 = createAndSaveProduct("상품1", "img1.jpg", 10000, 10, List.of(pc1), seller);
        em.flush();
        em.clear();

        Product savedProduct = productRepository.findById(product1.getId())
                .orElseThrow();

        System.out.println("product1 createdAt: " + product1.getCreatedAt());
        System.out.println("savedProduct createdAt: " + savedProduct.getCreatedAt());

        // when - 마지막 상품 이후 조회
        List<Product> products = productRepository.findAllCursor(
                null,
                "createdAt",
                "desc",
                product1.getCreatedAt().toString(),
                product1.getId(),
                10
        );

        // then
        assertThat(products).isEmpty();
    }

    @Test
    @DisplayName("커서와 after로 정확한 페이지 경계를 테스트한다")
    void findAllByCursor_exactPageBoundary() {
        // given
        User seller = createUser("seller", "seller@email.com");
        Category category = createAndSaveCategory("전자기기", 1, null);

        ProductCategory pc1 = createProductCategory(category);
        ProductCategory pc2 = createProductCategory(category);
        ProductCategory pc3 = createProductCategory(category);
        ProductCategory pc4 = createProductCategory(category);
        ProductCategory pc5 = createProductCategory(category);

        Product product1 = createAndSaveProduct("상품1", "img1.jpg", 10000, 10, List.of(pc1), seller);
        Product product2 = createAndSaveProduct("상품2", "img2.jpg", 20000, 20, List.of(pc2), seller);
        Product product3 = createAndSaveProduct("상품3", "img3.jpg", 30000, 30, List.of(pc3), seller);
        Product product4 = createAndSaveProduct("상품4", "img4.jpg", 40000, 40, List.of(pc4), seller);
        Product product5 = createAndSaveProduct("상품5", "img5.jpg", 50000, 50, List.of(pc5), seller);
        em.flush();
        em.clear();

        // when - 첫 페이지 (2개)
        List<Product> page1 = productRepository.findAllCursor(
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(page1).hasSize(2);
        assertThat(page1.get(0).getName()).isEqualTo("상품5");
        assertThat(page1.get(1).getName()).isEqualTo("상품4");

        // when - 두 번째 페이지 (2개)
        Product lastOfPage1 = page1.get(page1.size() - 1);
        List<Product> page2 = productRepository.findAllCursor(
                null,
                "createdAt",
                "desc",
                lastOfPage1.getCreatedAt().toString(),
                lastOfPage1.getId(),
                2
        );

        // then
        assertThat(page2).hasSize(2);
        assertThat(page2.get(0).getName()).isEqualTo("상품3");
        assertThat(page2.get(1).getName()).isEqualTo("상품2");

        // when - 세 번째 페이지 (1개)
        Product lastOfPage2 = page2.get(page2.size() - 1);
        List<Product> page3 = productRepository.findAllCursor(
                null,
                "createdAt",
                "desc",
                lastOfPage2.getCreatedAt().toString(),
                lastOfPage2.getId(),
                2
        );

        // then
        assertThat(page3).hasSize(1);
        assertThat(page3.get(0).getName()).isEqualTo("상품1");
    }

    // TestFixture 메서드들
    private User createUser(String username, String email) {
        User user = User.createUser(
                username,
                "password123",
                email,
                SocialType.LOCAL,
                null,
                LocalDate.of(1990, 1, 1),
                "010-1234-5678",
                Gender.MALE,
                Set.of(Role.USER)
        );
        return userRepository.save(user);
    }

    private Category createAndSaveCategory(String name, int depth, Category parent) {
        Category category = Category.createCategory(name, depth, parent);
        return categoryRepository.save(category);
    }

    private ProductCategory createProductCategory(Category category) {
        return ProductCategory.createProductCategory(category);
    }

    private Product createProduct(String name, String imageUrl, int price, int stockQuantity,
                                   List<ProductCategory> productCategories, User seller) {
        return Product.createProduct(name, imageUrl, price, stockQuantity, productCategories, seller);
    }

    private Product createAndSaveProduct(String name, String imageUrl, int price, int stockQuantity,
                                          List<ProductCategory> productCategories, User seller) {
        Product product = Product.createProduct(name, imageUrl, price, stockQuantity, productCategories, seller);
        return productRepository.save(product);
    }
}