package travel.mytravelplan.domain.review.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.product.entity.Product;
import travel.mytravelplan.domain.product.repository.ProductRepository;
import travel.mytravelplan.domain.review.entity.ProductReview;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.support.RepositoryTestSupport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("상품 리뷰 레포지토리 테스트")
class ProductReviewRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("상품 리뷰를 저장하고 조회할 수 있다")
    void save_and_findById() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", 10000, 100, user);

        ProductReview review = createProductReview(user, product, new BigDecimal("4.5"), "좋은 상품입니다");
        ProductReview savedReview = productReviewRepository.save(review);

        em.flush();
        em.clear();

        // when
        Optional<ProductReview> foundReview = productReviewRepository.findById(savedReview.getId());

        // then
        assertThat(foundReview).isPresent();
        assertThat(foundReview.get().getContent()).isEqualTo("좋은 상품입니다");
        assertThat(foundReview.get().getRating()).isEqualByComparingTo(new BigDecimal("4.5"));
        assertThat(foundReview.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(foundReview.get().getProduct().getId()).isEqualTo(product.getId());
    }

    @Test
    @DisplayName("상품 리뷰를 수정할 수 있다")
    void update() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", 10000, 100, user);

        ProductReview review = createAndSaveProductReview(user, product, new BigDecimal("4.5"), "좋은 상품입니다");
        Long reviewId = review.getId();

        em.flush();
        em.clear();

        // when
        ProductReview foundReview = productReviewRepository.findById(reviewId).orElseThrow();
        foundReview.update("정말 좋은 상품입니다", new BigDecimal("5.0"));

        em.flush();
        em.clear();

        // then
        ProductReview updatedReview = productReviewRepository.findById(reviewId).orElseThrow();
        assertThat(updatedReview.getContent()).isEqualTo("정말 좋은 상품입니다");
        assertThat(updatedReview.getRating()).isEqualByComparingTo(new BigDecimal("5.0"));
    }

    @Test
    @DisplayName("상품 리뷰를 삭제할 수 있다")
    void delete() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", 10000, 100, user);

        ProductReview review = createAndSaveProductReview(user, product, new BigDecimal("4.5"), "좋은 상품입니다");
        Long reviewId = review.getId();

        em.flush();
        em.clear();

        // when
        ProductReview foundReview = productReviewRepository.findById(reviewId).orElseThrow();
        productReviewRepository.delete(foundReview);

        em.flush();
        em.clear();

        // then
        Optional<ProductReview> deletedReview = productReviewRepository.findById(reviewId);
        assertThat(deletedReview).isEmpty();
    }

    @Test
    @DisplayName("특정 상품의 리뷰를 조회할 수 있다")
    void findAllByCursor_byProductId() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");
        Product product1 = createProduct("상품1", 10000, 100, user1);
        Product product2 = createProduct("상품2", 20000, 50, user1);

        createAndSaveProductReview(user1, product1, new BigDecimal("4.5"), "좋은 상품");
        createAndSaveProductReview(user2, product1, new BigDecimal("5.0"), "최고의 상품");
        createAndSaveProductReview(user1, product2, new BigDecimal("3.5"), "그냥 그래요");

        em.flush();
        em.clear();

        // when
        List<ProductReview> reviews = productReviewRepository.findAllByCursor(
                product1.getId(),
                null,
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(reviews).hasSize(2);
        assertThat(reviews)
                .extracting(ProductReview::getContent)
                .containsExactlyInAnyOrder("좋은 상품", "최고의 상품");
    }

    @Test
    @DisplayName("키워드로 리뷰를 검색할 수 있다")
    void findAllByCursor_withKeyword() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", 10000, 100, user);

        createAndSaveProductReview(user, product, new BigDecimal("4.5"), "배송이 빨라요");
        createAndSaveProductReview(user, product, new BigDecimal("5.0"), "품질이 좋아요");
        createAndSaveProductReview(user, product, new BigDecimal("3.5"), "배송은 별로");

        em.flush();
        em.clear();

        // when
        List<ProductReview> reviews = productReviewRepository.findAllByCursor(
                product.getId(),
                "배송",
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(reviews).hasSize(2);
        assertThat(reviews)
                .extracting(ProductReview::getContent)
                .containsExactlyInAnyOrder("배송이 빨라요", "배송은 별로");
    }

    @Test
    @DisplayName("이미지가 있는 리뷰만 조회할 수 있다")
    void findAllByCursor_withImgOnly() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", 10000, 100, user);

        ProductReview reviewWithImage = createAndSaveProductReview(user, product, new BigDecimal("4.5"), "이미지 있음");
        reviewWithImage.getImageUrls().add("https://example.com/image1.jpg");

        createAndSaveProductReview(user, product, new BigDecimal("5.0"), "이미지 없음");

        em.flush();
        em.clear();

        // when
        List<ProductReview> reviews = productReviewRepository.findAllByCursor(
                product.getId(),
                null,
                true,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(reviews).hasSize(1);
        assertThat(reviews.getFirst().getContent()).isEqualTo("이미지 있음");
        assertThat(reviews.getFirst().getImageUrls()).isNotEmpty();
    }

    @Test
    @DisplayName("평점으로 리뷰를 필터링할 수 있다")
    void findAllByCursor_withRating() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", 10000, 100, user);

        createAndSaveProductReview(user, product, new BigDecimal("4.5"), "좋아요");
        createAndSaveProductReview(user, product, new BigDecimal("5.0"), "최고예요");
        createAndSaveProductReview(user, product, new BigDecimal("5.0"), "완벽해요");

        em.flush();
        em.clear();

        // when
        List<ProductReview> reviews = productReviewRepository.findAllByCursor(
                product.getId(),
                null,
                null,
                new BigDecimal("5.0"),
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(reviews).hasSize(2);
        assertThat(reviews)
                .extracting(ProductReview::getContent)
                .containsExactlyInAnyOrder("최고예요", "완벽해요");
        assertThat(reviews)
                .allMatch(review -> review.getRating().compareTo(new BigDecimal("5.0")) == 0);
    }

    @Test
    @DisplayName("생성일 기준 내림차순으로 리뷰를 조회한다")
    void findAllByCursor_orderByCreatedAtDesc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", 10000, 100, user);

        createAndSaveProductReview(user, product, new BigDecimal("4.5"), "첫 번째 리뷰");
        createAndSaveProductReview(user, product, new BigDecimal("5.0"), "두 번째 리뷰");
        createAndSaveProductReview(user, product, new BigDecimal("3.5"), "세 번째 리뷰");

        em.flush();
        em.clear();

        // when
        List<ProductReview> reviews = productReviewRepository.findAllByCursor(
                product.getId(),
                null,
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(reviews).hasSize(3);
        assertThat(reviews.get(0).getContent()).isEqualTo("세 번째 리뷰");
        assertThat(reviews.get(1).getContent()).isEqualTo("두 번째 리뷰");
        assertThat(reviews.get(2).getContent()).isEqualTo("첫 번째 리뷰");
    }

    @Test
    @DisplayName("생성일 기준 오름차순으로 리뷰를 조회한다")
    void findAllByCursor_orderByCreatedAtAsc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", 10000, 100, user);

        createAndSaveProductReview(user, product, new BigDecimal("4.5"), "첫 번째 리뷰");
        createAndSaveProductReview(user, product, new BigDecimal("5.0"), "두 번째 리뷰");
        createAndSaveProductReview(user, product, new BigDecimal("3.5"), "세 번째 리뷰");

        em.flush();
        em.clear();

        // when
        List<ProductReview> reviews = productReviewRepository.findAllByCursor(
                product.getId(),
                null,
                null,
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(reviews).hasSize(3);
        assertThat(reviews.get(0).getContent()).isEqualTo("첫 번째 리뷰");
        assertThat(reviews.get(1).getContent()).isEqualTo("두 번째 리뷰");
        assertThat(reviews.get(2).getContent()).isEqualTo("세 번째 리뷰");
    }

    @Test
    @DisplayName("평점 기준 내림차순으로 리뷰를 조회한다")
    void findAllByCursor_orderByRatingDesc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", 10000, 100, user);

        createAndSaveProductReview(user, product, new BigDecimal("4.5"), "4.5점 리뷰");
        createAndSaveProductReview(user, product, new BigDecimal("5.0"), "5.0점 리뷰");
        createAndSaveProductReview(user, product, new BigDecimal("3.5"), "3.5점 리뷰");

        em.flush();
        em.clear();

        // when
        List<ProductReview> reviews = productReviewRepository.findAllByCursor(
                product.getId(),
                null,
                null,
                null,
                "rating",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(reviews).hasSize(3);
        assertThat(reviews.get(0).getRating()).isEqualByComparingTo(new BigDecimal("5.0"));
        assertThat(reviews.get(1).getRating()).isEqualByComparingTo(new BigDecimal("4.5"));
        assertThat(reviews.get(2).getRating()).isEqualByComparingTo(new BigDecimal("3.5"));
    }

    @Test
    @DisplayName("평점 기준 오름차순으로 리뷰를 조회한다")
    void findAllByCursor_orderByRatingAsc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", 10000, 100, user);

        createAndSaveProductReview(user, product, new BigDecimal("4.5"), "4.5점 리뷰");
        createAndSaveProductReview(user, product, new BigDecimal("5.0"), "5.0점 리뷰");
        createAndSaveProductReview(user, product, new BigDecimal("3.5"), "3.5점 리뷰");

        em.flush();
        em.clear();

        // when
        List<ProductReview> reviews = productReviewRepository.findAllByCursor(
                product.getId(),
                null,
                null,
                null,
                "rating",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(reviews).hasSize(3);
        assertThat(reviews.get(0).getRating()).isEqualByComparingTo(new BigDecimal("3.5"));
        assertThat(reviews.get(1).getRating()).isEqualByComparingTo(new BigDecimal("4.5"));
        assertThat(reviews.get(2).getRating()).isEqualByComparingTo(new BigDecimal("5.0"));
    }

    @Test
    @DisplayName("limit 개수만큼 리뷰를 조회한다")
    void findAllByCursor_withLimit() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", 10000, 100, user);

        createAndSaveProductReview(user, product, new BigDecimal("4.5"), "리뷰 1");
        createAndSaveProductReview(user, product, new BigDecimal("5.0"), "리뷰 2");
        createAndSaveProductReview(user, product, new BigDecimal("3.5"), "리뷰 3");
        createAndSaveProductReview(user, product, new BigDecimal("4.0"), "리뷰 4");
        createAndSaveProductReview(user, product, new BigDecimal("4.5"), "리뷰 5");

        em.flush();
        em.clear();

        // when
        List<ProductReview> reviews = productReviewRepository.findAllByCursor(
                product.getId(),
                null,
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                3
        );

        // then
        assertThat(reviews).hasSize(3);
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 리뷰를 조회한다 - 생성일 내림차순")
    void findAllByCursor_withCursor_createdAtDesc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", 10000, 100, user);

        createAndSaveProductReview(user, product, new BigDecimal("4.5"), "리뷰 1");
        createAndSaveProductReview(user, product, new BigDecimal("5.0"), "리뷰 2");
        createAndSaveProductReview(user, product, new BigDecimal("3.5"), "리뷰 3");

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<ProductReview> firstPage = productReviewRepository.findAllByCursor(
                product.getId(),
                null,
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getContent()).isEqualTo("리뷰 3");
        assertThat(firstPage.get(1).getContent()).isEqualTo("리뷰 2");

        // when - 두 번째 페이지 조회
        ProductReview lastReview = firstPage.getLast();
        List<ProductReview> secondPage = productReviewRepository.findAllByCursor(
                product.getId(),
                null,
                null,
                null,
                "createdAt",
                "desc",
                lastReview.getCreatedAt().toString(),
                lastReview.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.getFirst().getContent()).isEqualTo("리뷰 1");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 리뷰를 조회한다 - 생성일 오름차순")
    void findAllByCursor_withCursor_createdAtAsc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", 10000, 100, user);

        createAndSaveProductReview(user, product, new BigDecimal("4.5"), "리뷰 1");
        createAndSaveProductReview(user, product, new BigDecimal("5.0"), "리뷰 2");
        createAndSaveProductReview(user, product, new BigDecimal("3.5"), "리뷰 3");

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<ProductReview> firstPage = productReviewRepository.findAllByCursor(
                product.getId(),
                null,
                null,
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getContent()).isEqualTo("리뷰 1");
        assertThat(firstPage.get(1).getContent()).isEqualTo("리뷰 2");

        // when - 두 번째 페이지 조회
        ProductReview lastReview = firstPage.getLast();
        List<ProductReview> secondPage = productReviewRepository.findAllByCursor(
                product.getId(),
                null,
                null,
                null,
                "createdAt",
                "asc",
                lastReview.getCreatedAt().toString(),
                lastReview.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.getFirst().getContent()).isEqualTo("리뷰 3");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 리뷰를 조회한다 - 평점 내림차순")
    void findAllByCursor_withCursor_ratingDesc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", 10000, 100, user);

        createAndSaveProductReview(user, product, new BigDecimal("3.5"), "3.5점 리뷰");
        createAndSaveProductReview(user, product, new BigDecimal("4.5"), "4.5점 리뷰");
        createAndSaveProductReview(user, product, new BigDecimal("5.0"), "5.0점 리뷰");

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<ProductReview> firstPage = productReviewRepository.findAllByCursor(
                product.getId(),
                null,
                null,
                null,
                "rating",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getRating()).isEqualByComparingTo(new BigDecimal("5.0"));
        assertThat(firstPage.get(1).getRating()).isEqualByComparingTo(new BigDecimal("4.5"));

        // when - 두 번째 페이지 조회
        ProductReview lastReview = firstPage.getLast();
        List<ProductReview> secondPage = productReviewRepository.findAllByCursor(
                product.getId(),
                null,
                null,
                null,
                "rating",
                "desc",
                lastReview.getRating().toString(),
                lastReview.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.getFirst().getRating()).isEqualByComparingTo(new BigDecimal("3.5"));
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 리뷰를 조회한다 - 평점 오름차순")
    void findAllByCursor_withCursor_ratingAsc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", 10000, 100, user);

        createAndSaveProductReview(user, product, new BigDecimal("3.5"), "3.5점 리뷰");
        createAndSaveProductReview(user, product, new BigDecimal("4.5"), "4.5점 리뷰");
        createAndSaveProductReview(user, product, new BigDecimal("5.0"), "5.0점 리뷰");

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<ProductReview> firstPage = productReviewRepository.findAllByCursor(
                product.getId(),
                null,
                null,
                null,
                "rating",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getRating()).isEqualByComparingTo(new BigDecimal("3.5"));
        assertThat(firstPage.get(1).getRating()).isEqualByComparingTo(new BigDecimal("4.5"));

        // when - 두 번째 페이지 조회
        ProductReview lastReview = firstPage.getLast();
        List<ProductReview> secondPage = productReviewRepository.findAllByCursor(
                product.getId(),
                null,
                null,
                null,
                "rating",
                "asc",
                lastReview.getRating().toString(),
                lastReview.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.getFirst().getRating()).isEqualByComparingTo(new BigDecimal("5.0"));
    }

    @Test
    @DisplayName("여러 조건을 조합하여 리뷰를 조회할 수 있다")
    void findAllByCursor_withMultipleConditions() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", 10000, 100, user);

        ProductReview review1 = createAndSaveProductReview(user, product, new BigDecimal("5.0"), "배송이 정말 빨라요");
        review1.getImageUrls().add("https://example.com/image1.jpg");

        createAndSaveProductReview(user, product, new BigDecimal("5.0"), "품질이 좋아요");

        ProductReview review3 = createAndSaveProductReview(user, product, new BigDecimal("5.0"), "배송 서비스가 좋네요");
        review3.getImageUrls().add("https://example.com/image3.jpg");

        createAndSaveProductReview(user, product, new BigDecimal("4.0"), "배송이 괜찮아요");

        em.flush();
        em.clear();

        // when - 5.0점, 이미지 있는, 키워드 '배송' 포함 리뷰 조회
        List<ProductReview> reviews = productReviewRepository.findAllByCursor(
                product.getId(),
                "배송",
                true,
                new BigDecimal("5.0"),
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(reviews).hasSize(2);
        assertThat(reviews)
                .allMatch(review -> review.getRating().compareTo(new BigDecimal("5.0")) == 0)
                .allMatch(review -> review.getContent().contains("배송"))
                .allMatch(review -> !review.getImageUrls().isEmpty());
    }

    @Test
    @DisplayName("productId가 null일 때 모든 상품의 리뷰를 조회한다")
    void findAllByCursor_withNullProductId() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product1 = createProduct("상품1", 10000, 100, user);
        Product product2 = createProduct("상품2", 20000, 50, user);

        createAndSaveProductReview(user, product1, new BigDecimal("4.5"), "상품1 리뷰");
        createAndSaveProductReview(user, product2, new BigDecimal("5.0"), "상품2 리뷰");

        em.flush();
        em.clear();

        // when - productId를 null로 조회
        List<ProductReview> reviews = productReviewRepository.findAllByCursor(
                null,
                null,
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(reviews).hasSize(2);
        assertThat(reviews)
                .extracting(ProductReview::getContent)
                .containsExactlyInAnyOrder("상품1 리뷰", "상품2 리뷰");
    }

    @Test
    @DisplayName("imgOnly가 null일 때 이미지 유무와 관계없이 모든 리뷰를 조회한다")
    void findAllByCursor_withNullImgOnly() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", 10000, 100, user);

        ProductReview reviewWithImage = createAndSaveProductReview(user, product, new BigDecimal("4.5"), "이미지 있는 리뷰");
        reviewWithImage.getImageUrls().add("https://example.com/image1.jpg");

        createAndSaveProductReview(user, product, new BigDecimal("5.0"), "이미지 없는 리뷰");

        em.flush();
        em.clear();

        // when - imgOnly를 null로 조회
        List<ProductReview> reviews = productReviewRepository.findAllByCursor(
                product.getId(),
                null,
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(reviews).hasSize(2);
        assertThat(reviews)
                .extracting(ProductReview::getContent)
                .containsExactlyInAnyOrder("이미지 있는 리뷰", "이미지 없는 리뷰");
    }

    @Test
    @DisplayName("imgOnly가 false일 때 이미지 유무와 관계없이 모든 리뷰를 조회한다")
    void findAllByCursor_withImgOnlyFalse() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", 10000, 100, user);

        ProductReview reviewWithImage = createAndSaveProductReview(user, product, new BigDecimal("4.5"), "이미지 있는 리뷰");
        reviewWithImage.getImageUrls().add("https://example.com/image1.jpg");

        createAndSaveProductReview(user, product, new BigDecimal("5.0"), "이미지 없는 리뷰");

        em.flush();
        em.clear();

        // when - imgOnly를 false로 조회
        List<ProductReview> reviews = productReviewRepository.findAllByCursor(
                product.getId(),
                null,
                false,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(reviews).hasSize(2);
        assertThat(reviews)
                .extracting(ProductReview::getContent)
                .containsExactlyInAnyOrder("이미지 있는 리뷰", "이미지 없는 리뷰");
    }

    @Test
    @DisplayName("cursor와 after가 null일 때 첫 페이지부터 조회한다")
    void findAllByCursor_withNullCursorAndAfter() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", 10000, 100, user);

        createAndSaveProductReview(user, product, new BigDecimal("4.5"), "리뷰 1");
        createAndSaveProductReview(user, product, new BigDecimal("5.0"), "리뷰 2");
        createAndSaveProductReview(user, product, new BigDecimal("3.5"), "리뷰 3");

        em.flush();
        em.clear();

        // when - cursor와 after를 null로 조회
        List<ProductReview> reviews = productReviewRepository.findAllByCursor(
                product.getId(),
                null,
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(reviews).hasSize(3);
        assertThat(reviews.get(0).getContent()).isEqualTo("리뷰 3");
        assertThat(reviews.get(1).getContent()).isEqualTo("리뷰 2");
        assertThat(reviews.get(2).getContent()).isEqualTo("리뷰 1");
    }

    @Test
    @DisplayName("cursor만 있고 after가 null일 때는 커서 조건이 적용되지 않는다")
    void findAllByCursor_withCursorOnlyAndNullAfter() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", 10000, 100, user);

        createAndSaveProductReview(user, product, new BigDecimal("4.5"), "리뷰 1");
        createAndSaveProductReview(user, product, new BigDecimal("5.0"), "리뷰 2");
        createAndSaveProductReview(user, product, new BigDecimal("3.5"), "리뷰 3");

        em.flush();
        em.clear();

        // when - cursor만 있고 after는 null로 조회
        List<ProductReview> reviews = productReviewRepository.findAllByCursor(
                product.getId(),
                null,
                null,
                null,
                "createdAt",
                "desc",
                LocalDateTime.now().toString(),
                null,
                10
        );

        // then - cursor 조건이 적용되지 않으므로 모든 리뷰가 조회됨
        assertThat(reviews).hasSize(3);
    }

    @Test
    @DisplayName("after만 있고 cursor가 null일 때는 커서 조건이 적용되지 않는다")
    void findAllByCursor_withAfterOnlyAndNullCursor() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", 10000, 100, user);

        createAndSaveProductReview(user, product, new BigDecimal("4.5"), "리뷰 1");
        createAndSaveProductReview(user, product, new BigDecimal("5.0"), "리뷰 2");
        createAndSaveProductReview(user, product, new BigDecimal("3.5"), "리뷰 3");

        em.flush();
        em.clear();

        // when - after만 있고 cursor는 null로 조회
        List<ProductReview> reviews = productReviewRepository.findAllByCursor(
                product.getId(),
                null,
                null,
                null,
                "createdAt",
                "desc",
                null,
                1L,
                10
        );

        // then - cursor 조건이 적용되지 않으므로 모든 리뷰가 조회됨
        assertThat(reviews).hasSize(3);
    }

    @Test
    @DisplayName("productId, imgOnly, cursor, after가 모두 null일 때 모든 리뷰를 조회한다")
    void findAllByCursor_withAllNullConditions() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product1 = createProduct("상품1", 10000, 100, user);
        Product product2 = createProduct("상품2", 20000, 50, user);

        ProductReview review1 = createAndSaveProductReview(user, product1, new BigDecimal("4.5"), "리뷰 1");
        review1.getImageUrls().add("https://example.com/image1.jpg");

        createAndSaveProductReview(user, product2, new BigDecimal("5.0"), "리뷰 2");

        em.flush();
        em.clear();

        // when - 모든 필터 조건을 null로 조회
        List<ProductReview> reviews = productReviewRepository.findAllByCursor(
                null,
                null,
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(reviews).hasSize(2);
        assertThat(reviews)
                .extracting(ProductReview::getContent)
                .containsExactlyInAnyOrder("리뷰 1", "리뷰 2");
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

    private Product createProduct(String name, int price, int stockQuantity, User seller) {
        Product product = Product.createProduct(
                name,
                "https://example.com/product.jpg",
                price,
                stockQuantity,
                List.of(),
                seller
        );
        return productRepository.save(product);
    }

    private ProductReview createProductReview(User user, Product product, BigDecimal rating, String content) {
        return ProductReview.createProductReview(user, product, rating, content);
    }

    private ProductReview createAndSaveProductReview(User user, Product product, BigDecimal rating, String content) {
        ProductReview review = ProductReview.createProductReview(user, product, rating, content);
        return productReviewRepository.save(review);
    }
}