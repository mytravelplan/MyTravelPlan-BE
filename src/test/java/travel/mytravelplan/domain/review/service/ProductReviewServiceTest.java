package travel.mytravelplan.domain.review.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.product.entity.Product;
import travel.mytravelplan.domain.product.exception.ProductException;
import travel.mytravelplan.domain.product.repository.ProductRepository;
import travel.mytravelplan.domain.review.dto.*;
import travel.mytravelplan.domain.review.entity.ProductReview;
import travel.mytravelplan.domain.review.entity.ProductReviewLike;
import travel.mytravelplan.domain.review.exception.ProductReviewException;
import travel.mytravelplan.domain.review.mapper.ProductReviewLikeMapper;
import travel.mytravelplan.domain.review.mapper.ProductReviewMapper;
import travel.mytravelplan.domain.review.repository.ProductReviewLikeRepository;
import travel.mytravelplan.domain.review.repository.ProductReviewRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.global.support.ServiceTestSupport;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

@DisplayName("상품 리뷰 서비스 테스트")
class ProductReviewServiceTest extends ServiceTestSupport {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductReviewRepository productReviewRepository;

    @Mock
    private ProductReviewLikeRepository productReviewLikeRepository;

    @Mock
    private ProductReviewMapper productReviewMapper;

    @Mock
    private ProductReviewLikeMapper productReviewLikeMapper;

    @InjectMocks
    private ProductReviewService productReviewService;

    private User user;
    private Product product;
    private ProductReview productReview;
    private ProductReviewDto productReviewDto;
    private ProductReviewCreateRequestDto createRequestDto;
    private ProductReviewUpdateRequestDto updateRequestDto;

    @BeforeEach
    void setUp() {
        user = User.createUser(
                "testUser",
                "password123",
                "test@example.com",
                SocialType.GOOGLE,
                "social123",
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(user, "id", 1L);

        product = Product.createProduct(
                "Test Product",
                "test.jpg",
                10000,
                100,
                List.of(),
                user
        );
        ReflectionTestUtils.setField(product, "id", 1L);

        productReview = ProductReview.createProductReview(
                user,
                product,
                new BigDecimal("4.5"),
                "좋은 상품입니다."
        );

        productReviewDto = ProductReviewDto.builder()
                .id(1L)
                .productId(1L)
                .userId(1L)
                .username("testUser")
                .content("좋은 상품입니다.")
                .rating(new BigDecimal("4.5"))
                .liked(false)
                .numberOfLikes(0)
                .numberOfComments(0)
                .build();

        createRequestDto = ProductReviewCreateRequestDto.builder()
                .content("좋은 상품입니다.")
                .rating(new BigDecimal("4.5"))
                .build();

        updateRequestDto = ProductReviewUpdateRequestDto.builder()
                .content("수정된 리뷰입니다.")
                .rating(new BigDecimal("5.0"))
                .build();
    }

    @Test
    @DisplayName("상품 리뷰 생성 성공")
    void createProductReview_Success() {
        // given
        given(productRepository.findById(eq(1L))).willReturn(Optional.of(product));
        given(productReviewRepository.save(any(ProductReview.class))).willReturn(productReview);
        given(productReviewMapper.toDto(any(ProductReview.class), eq(user))).willReturn(productReviewDto);

        // when
        ProductReviewDto result = productReviewService.createProductReview(user, 1L, createRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("좋은 상품입니다.");
        assertThat(result.getRating()).isEqualTo(new BigDecimal("4.5"));
        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(1L);

        then(productRepository).should().findById(eq(1L));
        then(productReviewRepository).should().save(any(ProductReview.class));
        then(productReviewMapper).should().toDto(any(ProductReview.class), eq(user));
    }

    @Test
    @DisplayName("상품 리뷰 생성 실패 - 상품을 찾을 수 없음")
    void createProductReview_ProductNotFound() {
        // given
        given(productRepository.findById(eq(1L))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productReviewService.createProductReview(user, 1L, createRequestDto))
                .isInstanceOf(ProductException.class);

        then(productRepository).should().findById(eq(1L));
    }

    @Test
    @DisplayName("상품 리뷰 조회 성공")
    void getProductReview_Success() {
        // given
        given(productRepository.findById(eq(1L))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(1L))).willReturn(Optional.of(productReview));
        given(productReviewMapper.toDto(any(ProductReview.class), eq(user))).willReturn(productReviewDto);

        // when
        ProductReviewDto result = productReviewService.getProductReview(user, 1L, 1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getContent()).isEqualTo("좋은 상품입니다.");
        assertThat(result.getRating()).isEqualTo(new BigDecimal("4.5"));

        then(productRepository).should().findById(eq(1L));
        then(productReviewRepository).should().findById(eq(1L));
        then(productReviewMapper).should().toDto(any(ProductReview.class), eq(user));
    }

    @Test
    @DisplayName("상품 리뷰 조회 실패 - 상품을 찾을 수 없음")
    void getProductReview_ProductNotFound() {
        // given
        given(productRepository.findById(eq(1L))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productReviewService.getProductReview(user, 1L, 1L))
                .isInstanceOf(ProductException.class);

        then(productRepository).should().findById(eq(1L));
    }

    @Test
    @DisplayName("상품 리뷰 조회 실패 - 리뷰를 찾을 수 없음")
    void getProductReview_ReviewNotFound() {
        // given
        given(productRepository.findById(eq(1L))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(1L))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productReviewService.getProductReview(user, 1L, 1L))
                .isInstanceOf(ProductReviewException.class);

        then(productRepository).should().findById(eq(1L));
        then(productReviewRepository).should().findById(eq(1L));
    }

    @Test
    @DisplayName("상품 리뷰 조회 실패 - 리뷰가 상품에 속하지 않음")
    void getProductReview_ReviewNotBelongToProduct() {
        // given
        Product anotherProduct = Product.createProduct(
                "Another Product",
                "another.jpg",
                20000,
                50,
                List.of(),
                user
        );
        ReflectionTestUtils.setField(anotherProduct, "id", 2L);

        ProductReview anotherProductReview = ProductReview.createProductReview(
                user,
                anotherProduct,
                new BigDecimal("4.0"),
                "다른 상품 리뷰"
        );

        given(productRepository.findById(eq(1L))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(1L))).willReturn(Optional.of(anotherProductReview));

        // when & then
        assertThatThrownBy(() -> productReviewService.getProductReview(user, 1L, 1L))
                .isInstanceOf(ProductReviewException.class);

        then(productRepository).should().findById(eq(1L));
        then(productReviewRepository).should().findById(eq(1L));
    }

    @Test
    @DisplayName("상품 리뷰 목록 조회 성공 - 다음 페이지 있음")
    void getProductReviews_WithNextPage() {
        // given
        List<ProductReview> reviews = List.of(
                createProductReview(1L, user, product, new BigDecimal("5.0"), "리뷰1"),
                createProductReview(2L, user, product, new BigDecimal("4.0"), "리뷰2"),
                createProductReview(3L, user, product, new BigDecimal("3.0"), "리뷰3")
        );

        given(productRepository.findById(eq(1L))).willReturn(Optional.of(product));
        given(productReviewRepository.findAllByCursor(eq(1L), eq(null), eq(false), eq(null), eq("createdAt"), eq("desc"), eq(null), eq(null), eq(3)))
                .willReturn(reviews);
        given(productReviewMapper.toDto(any(ProductReview.class), eq(user))).willReturn(productReviewDto);

        // when
        var result = productReviewService.getProductReviews(user, 1L, null, false, null, "createdAt", "desc", null, null, 2);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isNotNull();
        assertThat(result.getNextAfter()).isEqualTo(2L);

        then(productRepository).should().findById(eq(1L));
        then(productReviewRepository).should().findAllByCursor(eq(1L), eq(null), eq(false), eq(null), eq("createdAt"), eq("desc"), eq(null), eq(null), eq(3));
    }

    @Test
    @DisplayName("상품 리뷰 목록 조회 성공 - 다음 페이지 없음")
    void getProductReviews_WithoutNextPage() {
        // given
        List<ProductReview> reviews = List.of(
                createProductReview(1L, user, product, new BigDecimal("5.0"), "리뷰1"),
                createProductReview(2L, user, product, new BigDecimal("4.0"), "리뷰2")
        );

        given(productRepository.findById(eq(1L))).willReturn(Optional.of(product));
        given(productReviewRepository.findAllByCursor(eq(1L), eq(null), eq(false), eq(null), eq("createdAt"), eq("desc"), eq(null), eq(null), eq(3)))
                .willReturn(reviews);
        given(productReviewMapper.toDto(any(ProductReview.class), eq(user))).willReturn(productReviewDto);

        // when
        var result = productReviewService.getProductReviews(user, 1L, null, false, null, "createdAt", "desc", null, null, 2);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();

        then(productRepository).should().findById(eq(1L));
        then(productReviewRepository).should().findAllByCursor(eq(1L), eq(null), eq(false), eq(null), eq("createdAt"), eq("desc"), eq(null), eq(null), eq(3));
    }

    @Test
    @DisplayName("상품 리뷰 목록 조회 성공 - 키워드 검색")
    void getProductReviews_WithKeyword() {
        // given
        List<ProductReview> reviews = List.of(
                createProductReview(1L, user, product, new BigDecimal("5.0"), "좋은 상품입니다")
        );

        given(productRepository.findById(eq(1L))).willReturn(Optional.of(product));
        given(productReviewRepository.findAllByCursor(eq(1L), eq("좋은"), eq(false), eq(null), eq("createdAt"), eq("desc"), eq(null), eq(null), eq(11)))
                .willReturn(reviews);
        given(productReviewMapper.toDto(any(ProductReview.class), eq(user))).willReturn(productReviewDto);

        // when
        var result = productReviewService.getProductReviews(user, 1L, "좋은", false, null, "createdAt", "desc", null, null, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();

        then(productRepository).should().findById(eq(1L));
        then(productReviewRepository).should().findAllByCursor(eq(1L), eq("좋은"), eq(false), eq(null), eq("createdAt"), eq("desc"), eq(null), eq(null), eq(11));
    }

    @Test
    @DisplayName("상품 리뷰 목록 조회 성공 - 이미지만 조회")
    void getProductReviews_WithImgOnly() {
        // given
        List<ProductReview> reviews = List.of(
                createProductReview(1L, user, product, new BigDecimal("5.0"), "리뷰1")
        );

        given(productRepository.findById(eq(1L))).willReturn(Optional.of(product));
        given(productReviewRepository.findAllByCursor(eq(1L), eq(null), eq(true), eq(null), eq("createdAt"), eq("desc"), eq(null), eq(null), eq(11)))
                .willReturn(reviews);
        given(productReviewMapper.toDto(any(ProductReview.class), eq(user))).willReturn(productReviewDto);

        // when
        var result = productReviewService.getProductReviews(user, 1L, null, true, null, "createdAt", "desc", null, null, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        then(productRepository).should().findById(eq(1L));
        then(productReviewRepository).should().findAllByCursor(eq(1L), eq(null), eq(true), eq(null), eq("createdAt"), eq("desc"), eq(null), eq(null), eq(11));
    }

    @Test
    @DisplayName("상품 리뷰 목록 조회 성공 - 평점 필터링")
    void getProductReviews_WithRating() {
        // given
        BigDecimal rating = new BigDecimal("4.0");
        List<ProductReview> reviews = List.of(
                createProductReview(1L, user, product, new BigDecimal("4.0"), "리뷰1")
        );

        given(productRepository.findById(eq(1L))).willReturn(Optional.of(product));
        given(productReviewRepository.findAllByCursor(eq(1L), eq(null), eq(false), eq(rating), eq("createdAt"), eq("desc"), eq(null), eq(null), eq(11)))
                .willReturn(reviews);
        given(productReviewMapper.toDto(any(ProductReview.class), eq(user))).willReturn(productReviewDto);

        // when
        var result = productReviewService.getProductReviews(user, 1L, null, false, rating, "createdAt", "desc", null, null, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        then(productRepository).should().findById(eq(1L));
        then(productReviewRepository).should().findAllByCursor(eq(1L), eq(null), eq(false), eq(rating), eq("createdAt"), eq("desc"), eq(null), eq(null), eq(11));
    }

    @Test
    @DisplayName("상품 리뷰 목록 조회 성공 - 평점 순 정렬")
    void getProductReviews_OrderByRating() {
        // given
        List<ProductReview> reviews = List.of(
                createProductReview(1L, user, product, new BigDecimal("5.0"), "리뷰1"),
                createProductReview(2L, user, product, new BigDecimal("4.0"), "리뷰2")
        );

        given(productRepository.findById(eq(1L))).willReturn(Optional.of(product));
        given(productReviewRepository.findAllByCursor(eq(1L), eq(null), eq(false), eq(null), eq("rating"), eq("desc"), eq(null), eq(null), eq(11)))
                .willReturn(reviews);
        given(productReviewMapper.toDto(any(ProductReview.class), eq(user))).willReturn(productReviewDto);

        // when
        var result = productReviewService.getProductReviews(user, 1L, null, false, null, "rating", "desc", null, null, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);

        then(productRepository).should().findById(eq(1L));
        then(productReviewRepository).should().findAllByCursor(eq(1L), eq(null), eq(false), eq(null), eq("rating"), eq("desc"), eq(null), eq(null), eq(11));
    }

    @Test
    @DisplayName("상품 리뷰 목록 조회 성공 - 평점 순 정렬, 다음 페이지 있음")
    void getProductReviews_OrderByRating_WithNextPage() {
        // given
        List<ProductReview> reviews = List.of(
                createProductReview(1L, user, product, new BigDecimal("5.0"), "리뷰1"),
                createProductReview(2L, user, product, new BigDecimal("4.0"), "리뷰2"),
                createProductReview(3L, user, product, new BigDecimal("3.0"), "리뷰3")
        );

        given(productRepository.findById(eq(1L))).willReturn(Optional.of(product));
        given(productReviewRepository.findAllByCursor(eq(1L), eq(null), eq(false), eq(null), eq("rating"), eq("desc"), eq(null), eq(null), eq(3)))
                .willReturn(reviews);
        given(productReviewMapper.toDto(any(ProductReview.class), eq(user))).willReturn(productReviewDto);

        // when
        var result = productReviewService.getProductReviews(user, 1L, null, false, null, "rating", "desc", null, null, 2);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isEqualTo("4.0"); // rating 값으로 설정
        assertThat(result.getNextAfter()).isEqualTo(2L);

        then(productRepository).should().findById(eq(1L));
        then(productReviewRepository).should().findAllByCursor(eq(1L), eq(null), eq(false), eq(null), eq("rating"), eq("desc"), eq(null), eq(null), eq(3));
    }

    @Test
    @DisplayName("상품 리뷰 목록 조회 실패 - 상품을 찾을 수 없음")
    void getProductReviews_ProductNotFound() {
        // given
        given(productRepository.findById(eq(1L))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productReviewService.getProductReviews(user, 1L, null, false, null, "createdAt", "desc", null, null, 10))
                .isInstanceOf(ProductException.class);

        then(productRepository).should().findById(eq(1L));
    }

    private ProductReview createProductReview(Long id, User user, Product product, BigDecimal rating, String content) {
        ProductReview review = ProductReview.createProductReview(user, product, rating, content);
        ReflectionTestUtils.setField(review, "id", id);

        // BaseEntity의 createdAt 필드 설정 (Java Reflection 사용)
        try {
            java.time.LocalDateTime createdAt = java.time.LocalDateTime.now().minusHours(id);
            java.lang.reflect.Field createdAtField = review.getClass().getSuperclass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(review, createdAt);

            java.lang.reflect.Field updatedAtField = review.getClass().getSuperclass().getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(review, createdAt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set createdAt field", e);
        }

        // ProductReview의 rating 필드 설정
        ReflectionTestUtils.setField(review, "rating", rating);
        return review;
    }

    @Test
    @DisplayName("상품 리뷰 수정 성공")
    void updateProductReview_Success() {
        // given
        ProductReviewDto updatedDto = ProductReviewDto.builder()
                .id(1L)
                .productId(1L)
                .userId(1L)
                .username("testUser")
                .content("수정된 리뷰입니다.")
                .rating(new BigDecimal("5.0"))
                .liked(false)
                .numberOfLikes(0)
                .numberOfComments(0)
                .build();

        given(productRepository.findById(eq(1L))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(1L))).willReturn(Optional.of(productReview));
        given(productReviewMapper.toDto(any(ProductReview.class), eq(user))).willReturn(updatedDto);

        // when
        ProductReviewDto result = productReviewService.updateProductReview(user, 1L, 1L, updateRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("수정된 리뷰입니다.");
        assertThat(result.getRating()).isEqualTo(new BigDecimal("5.0"));

        then(productRepository).should().findById(eq(1L));
        then(productReviewRepository).should().findById(eq(1L));
        then(productReviewMapper).should().toDto(any(ProductReview.class), eq(user));
    }

    @Test
    @DisplayName("상품 리뷰 수정 실패 - 상품을 찾을 수 없음")
    void updateProductReview_ProductNotFound() {
        // given
        given(productRepository.findById(eq(1L))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productReviewService.updateProductReview(user, 1L, 1L, updateRequestDto))
                .isInstanceOf(ProductException.class);

        then(productRepository).should().findById(eq(1L));
    }

    @Test
    @DisplayName("상품 리뷰 수정 실패 - 리뷰를 찾을 수 없음")
    void updateProductReview_ReviewNotFound() {
        // given
        given(productRepository.findById(eq(1L))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(1L))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productReviewService.updateProductReview(user, 1L, 1L, updateRequestDto))
                .isInstanceOf(ProductReviewException.class);

        then(productRepository).should().findById(eq(1L));
        then(productReviewRepository).should().findById(eq(1L));
    }

    @Test
    @DisplayName("상품 리뷰 수정 실패 - 리뷰가 상품에 속하지 않음")
    void updateProductReview_ReviewNotBelongToProduct() {
        // given
        Product anotherProduct = Product.createProduct(
                "Another Product",
                "another.jpg",
                20000,
                50,
                List.of(),
                user
        );
        ReflectionTestUtils.setField(anotherProduct, "id", 2L);

        ProductReview anotherProductReview = ProductReview.createProductReview(
                user,
                anotherProduct,
                new BigDecimal("4.0"),
                "다른 상품 리뷰"
        );

        given(productRepository.findById(eq(1L))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(1L))).willReturn(Optional.of(anotherProductReview));

        // when & then
        assertThatThrownBy(() -> productReviewService.updateProductReview(user, 1L, 1L, updateRequestDto))
                .isInstanceOf(ProductReviewException.class);

        then(productRepository).should().findById(eq(1L));
        then(productReviewRepository).should().findById(eq(1L));
    }

    @Test
    @DisplayName("상품 리뷰 삭제 성공")
    void deleteProductReview_Success() {
        // given
        given(productRepository.findById(eq(1L))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(1L))).willReturn(Optional.of(productReview));
        willDoNothing().given(productReviewRepository).delete(any(ProductReview.class));

        // when
        productReviewService.deleteProductReview(1L, 1L);

        // then
        then(productRepository).should().findById(eq(1L));
        then(productReviewRepository).should().findById(eq(1L));
        then(productReviewRepository).should().delete(any(ProductReview.class));
    }

    @Test
    @DisplayName("상품 리뷰 삭제 실패 - 상품을 찾을 수 없음")
    void deleteProductReview_ProductNotFound() {
        // given
        given(productRepository.findById(eq(1L))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productReviewService.deleteProductReview(1L, 1L))
                .isInstanceOf(ProductException.class);

        then(productRepository).should().findById(eq(1L));
    }

    @Test
    @DisplayName("상품 리뷰 삭제 실패 - 리뷰를 찾을 수 없음")
    void deleteProductReview_ReviewNotFound() {
        // given
        given(productRepository.findById(eq(1L))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(1L))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productReviewService.deleteProductReview(1L, 1L))
                .isInstanceOf(ProductReviewException.class);

        then(productRepository).should().findById(eq(1L));
        then(productReviewRepository).should().findById(eq(1L));
    }

    @Test
    @DisplayName("상품 리뷰 삭제 실패 - 리뷰가 상품에 속하지 않음")
    void deleteProductReview_ReviewNotBelongToProduct() {
        // given
        Product anotherProduct = Product.createProduct(
                "Another Product",
                "another.jpg",
                20000,
                50,
                List.of(),
                user
        );
        ReflectionTestUtils.setField(anotherProduct, "id", 2L);

        ProductReview anotherProductReview = ProductReview.createProductReview(
                user,
                anotherProduct,
                new BigDecimal("4.0"),
                "다른 상품 리뷰"
        );

        given(productRepository.findById(eq(1L))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(1L))).willReturn(Optional.of(anotherProductReview));

        // when & then
        assertThatThrownBy(() -> productReviewService.deleteProductReview(1L, 1L))
                .isInstanceOf(ProductReviewException.class);

        then(productRepository).should().findById(eq(1L));
        then(productReviewRepository).should().findById(eq(1L));
    }

    @Test
    @DisplayName("상품 리뷰 좋아요 성공 - 좋아요 추가")
    void likeProductReview_AddLike() {
        // given
        ProductReviewLike productReviewLike = ProductReviewLike.createProductReviewLike(productReview, user);
        ProductReviewLikeDto likeDto = ProductReviewLikeDto.builder()
                .reviewId(1L)
                .userId(1L)
                .liked(true)
                .build();

        given(productRepository.findById(eq(1L))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(1L))).willReturn(Optional.of(productReview));
        given(productReviewLikeRepository.findByProductReviewAndUser(any(ProductReview.class), eq(user)))
                .willReturn(Optional.empty());
        given(productReviewLikeRepository.save(any(ProductReviewLike.class))).willReturn(productReviewLike);
        given(productReviewLikeMapper.toDto(any(ProductReviewLike.class), eq(true))).willReturn(likeDto);

        // when
        ProductReviewLikeDto result = productReviewService.likeProductReview(user, 1L, 1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isLiked()).isTrue();
        assertThat(result.getReviewId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(1L);

        then(productRepository).should().findById(eq(1L));
        then(productReviewRepository).should().findById(eq(1L));
        then(productReviewLikeRepository).should().findByProductReviewAndUser(any(ProductReview.class), eq(user));
        then(productReviewLikeRepository).should().save(any(ProductReviewLike.class));
        then(productReviewLikeMapper).should().toDto(any(ProductReviewLike.class), eq(true));
    }

    @Test
    @DisplayName("상품 리뷰 좋아요 성공 - 좋아요 취소")
    void likeProductReview_RemoveLike() {
        // given
        ProductReviewLike productReviewLike = ProductReviewLike.createProductReviewLike(productReview, user);
        ProductReviewLikeDto likeDto = ProductReviewLikeDto.builder()
                .reviewId(1L)
                .userId(1L)
                .liked(false)
                .build();

        given(productRepository.findById(eq(1L))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(1L))).willReturn(Optional.of(productReview));
        given(productReviewLikeRepository.findByProductReviewAndUser(any(ProductReview.class), eq(user)))
                .willReturn(Optional.of(productReviewLike));
        willDoNothing().given(productReviewLikeRepository).delete(any(ProductReviewLike.class));
        given(productReviewLikeMapper.toDto(any(ProductReviewLike.class), eq(false))).willReturn(likeDto);

        // when
        ProductReviewLikeDto result = productReviewService.likeProductReview(user, 1L, 1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isLiked()).isFalse();
        assertThat(result.getReviewId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(1L);

        then(productRepository).should().findById(eq(1L));
        then(productReviewRepository).should().findById(eq(1L));
        then(productReviewLikeRepository).should().findByProductReviewAndUser(any(ProductReview.class), eq(user));
        then(productReviewLikeRepository).should().delete(any(ProductReviewLike.class));
        then(productReviewLikeMapper).should().toDto(any(ProductReviewLike.class), eq(false));
    }

    @Test
    @DisplayName("상품 리뷰 좋아요 실패 - 상품을 찾을 수 없음")
    void likeProductReview_ProductNotFound() {
        // given
        given(productRepository.findById(eq(1L))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productReviewService.likeProductReview(user, 1L, 1L))
                .isInstanceOf(ProductException.class);

        then(productRepository).should().findById(eq(1L));
    }

    @Test
    @DisplayName("상품 리뷰 좋아요 실패 - 리뷰를 찾을 수 없음")
    void likeProductReview_ReviewNotFound() {
        // given
        given(productRepository.findById(eq(1L))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(1L))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productReviewService.likeProductReview(user, 1L, 1L))
                .isInstanceOf(ProductReviewException.class);

        then(productRepository).should().findById(eq(1L));
        then(productReviewRepository).should().findById(eq(1L));
    }

    @Test
    @DisplayName("상품 리뷰 좋아요 실패 - 리뷰가 상품에 속하지 않음")
    void likeProductReview_ReviewNotBelongToProduct() {
        // given
        Product anotherProduct = Product.createProduct(
                "Another Product",
                "another.jpg",
                20000,
                50,
                List.of(),
                user
        );
        ReflectionTestUtils.setField(anotherProduct, "id", 2L);

        ProductReview anotherProductReview = ProductReview.createProductReview(
                user,
                anotherProduct,
                new BigDecimal("4.0"),
                "다른 상품 리뷰"
        );

        given(productRepository.findById(eq(1L))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(1L))).willReturn(Optional.of(anotherProductReview));

        // when & then
        assertThatThrownBy(() -> productReviewService.likeProductReview(user, 1L, 1L))
                .isInstanceOf(ProductReviewException.class);

        then(productRepository).should().findById(eq(1L));
        then(productReviewRepository).should().findById(eq(1L));
    }

}
