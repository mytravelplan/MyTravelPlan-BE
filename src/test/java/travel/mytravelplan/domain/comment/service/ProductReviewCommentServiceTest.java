package travel.mytravelplan.domain.comment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.comment.dto.ProductReviewCommentCreateRequestDto;
import travel.mytravelplan.domain.comment.dto.ProductReviewCommentDto;
import travel.mytravelplan.domain.comment.dto.ProductReviewCommentUpdateRequestDto;
import travel.mytravelplan.domain.comment.entity.ProductReviewComment;
import travel.mytravelplan.domain.comment.exception.ProductReviewCommentException;
import travel.mytravelplan.domain.comment.mapper.ProductReviewCommentMapper;
import travel.mytravelplan.domain.comment.repository.ProductReviewCommentRepository;
import travel.mytravelplan.domain.product.entity.Product;
import travel.mytravelplan.domain.product.exception.ProductException;
import travel.mytravelplan.domain.product.repository.ProductRepository;
import travel.mytravelplan.domain.review.entity.ProductReview;
import travel.mytravelplan.domain.review.exception.ProductReviewException;
import travel.mytravelplan.domain.review.repository.ProductReviewRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.support.ServiceTestSupport;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("상품 리뷰 댓글 서비스 테스트")
class ProductReviewCommentServiceTest extends ServiceTestSupport {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductReviewRepository productReviewRepository;

    @Mock
    private ProductReviewCommentRepository productReviewCommentRepository;

    @Mock
    private ProductReviewCommentMapper productReviewCommentMapper;

    @InjectMocks
    private ProductReviewCommentService productReviewCommentService;

    private User user;
    private Product product;
    private ProductReview productReview;
    private ProductReviewComment productReviewComment;
    private ProductReviewCommentDto productReviewCommentDto;
    private ProductReviewCommentCreateRequestDto createRequestDto;
    private ProductReviewCommentUpdateRequestDto updateRequestDto;

    private Long productId;
    private Long productReviewId;
    private Long productCommentId;

    @BeforeEach
    void setUp() {
        // 공통 ID 설정
        productId = 1L;
        productReviewId = 1L;
        productCommentId = 1L;

        // User 설정
        user = User.createUser("testuser", "password", "test@test.com", null, null, null);
        ReflectionTestUtils.setField(user, "id", 1L);

        // Product 설정 및 리플렉션으로 ID 주입
        product = Product.createProduct("테스트 상품", "image.jpg", 10000, 100, List.of(), user);
        ReflectionTestUtils.setField(product, "id", productId);

        // ProductReview 설정 및 리플렉션으로 ID 주입
        productReview = ProductReview.createProductReview(user, product, BigDecimal.valueOf(5.0), "테스트 리뷰");
        ReflectionTestUtils.setField(productReview, "id", productReviewId);
        ReflectionTestUtils.setField(productReview, "product", product);

        // ProductReviewComment 설정 및 리플렉션으로 ID 주입
        productReviewComment = ProductReviewComment.createProductReviewComment("테스트 댓글", productReview, user);
        ReflectionTestUtils.setField(productReviewComment, "id", productCommentId);
        ReflectionTestUtils.setField(productReviewComment, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(productReviewComment, "updatedAt", LocalDateTime.now());

        // ProductReviewCommentDto 설정
        productReviewCommentDto = ProductReviewCommentDto.builder()
                .id(productCommentId)
                .productReviewId(productReviewId)
                .userId(1L)
                .username("testuser")
                .content("테스트 댓글")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 생성 요청 DTO 설정
        createRequestDto = ProductReviewCommentCreateRequestDto.builder()
                .content("새로운 댓글")
                .build();

        // 수정 요청 DTO 설정
        updateRequestDto = ProductReviewCommentUpdateRequestDto.builder()
                .content("수정된 댓글")
                .build();
    }

    @Test
    @DisplayName("상품 리뷰 댓글 생성 성공")
    void createProductReviewComment_Success() {
        // given
        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(productReviewId))).willReturn(Optional.of(productReview));
        given(productReviewCommentRepository.save(any(ProductReviewComment.class))).willReturn(productReviewComment);
        given(productReviewCommentMapper.toDto(any(ProductReviewComment.class))).willReturn(productReviewCommentDto);

        // when
        ProductReviewCommentDto result = productReviewCommentService.createProductReviewComment(user, productId, productReviewId, createRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(productReviewCommentDto);

        then(productRepository).should().findById(eq(productId));
        then(productReviewRepository).should().findById(eq(productReviewId));
        then(productReviewCommentRepository).should().save(any(ProductReviewComment.class));
        then(productReviewCommentMapper).should().toDto(any(ProductReviewComment.class));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 생성 실패 - 상품을 찾을 수 없음")
    void createProductReviewComment_ProductNotFound() {
        // given
        Long invalidProductId = 999L;
        given(productRepository.findById(eq(invalidProductId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productReviewCommentService.createProductReviewComment(user, invalidProductId, productReviewId, createRequestDto))
                .isInstanceOf(ProductException.class);

        then(productRepository).should().findById(eq(invalidProductId));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 생성 실패 - 리뷰를 찾을 수 없음")
    void createProductReviewComment_ReviewNotFound() {
        // given
        Long invalidProductReviewId = 999L;
        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(invalidProductReviewId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productReviewCommentService.createProductReviewComment(user, productId, invalidProductReviewId, createRequestDto))
                .isInstanceOf(ProductReviewException.class);

        then(productRepository).should().findById(eq(productId));
        then(productReviewRepository).should().findById(eq(invalidProductReviewId));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 생성 실패 - 리뷰가 상품에 속하지 않음")
    void createProductReviewComment_ReviewNotBelongToProduct() {
        // given
        Product anotherProduct = Product.createProduct("다른 상품", "other.jpg", 20000, 50, List.of(), user);
        ReflectionTestUtils.setField(anotherProduct, "id", 2L);
        ProductReview anotherProductReview = ProductReview.createProductReview(user, anotherProduct, BigDecimal.valueOf(4.0), "다른 리뷰");
        ReflectionTestUtils.setField(anotherProductReview, "product", anotherProduct);

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(productReviewId))).willReturn(Optional.of(anotherProductReview));

        // when & then
        assertThatThrownBy(() -> productReviewCommentService.createProductReviewComment(user, productId, productReviewId, createRequestDto))
                .isInstanceOf(ProductReviewException.class);

        then(productRepository).should().findById(eq(productId));
        then(productReviewRepository).should().findById(eq(productReviewId));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 목록 조회 성공")
    void getProductReviewComments_Success() {
        // given
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "DESC";
        String cursor = null;
        Long after = null;
        int limit = 10;

        ProductReviewComment productReviewComment2 = ProductReviewComment.createProductReviewComment("테스트 댓글2", productReview, user);
        ReflectionTestUtils.setField(productReviewComment2, "id", 2L);
        ReflectionTestUtils.setField(productReviewComment2, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(productReviewComment2, "updatedAt", LocalDateTime.now());
        List<ProductReviewComment> productReviewComments = Arrays.asList(productReviewComment, productReviewComment2);

        ProductReviewCommentDto productReviewCommentDto2 = ProductReviewCommentDto.builder()
                .id(2L)
                .productReviewId(productReviewId)
                .userId(1L)
                .username("testuser")
                .content("테스트 댓글2")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(productReviewId))).willReturn(Optional.of(productReview));
        given(productReviewCommentRepository.findAllByCursor(eq(productReviewId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(productReviewComments);
        given(productReviewCommentMapper.toDto(eq(productReviewComment))).willReturn(productReviewCommentDto);
        given(productReviewCommentMapper.toDto(eq(productReviewComment2))).willReturn(productReviewCommentDto2);

        // when
        CursorPageResponseDto<ProductReviewCommentDto> result = productReviewCommentService.getProductReviewComments(
                productId, productReviewId, keyword, orderBy, direction, cursor, after, limit
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactly(productReviewCommentDto, productReviewCommentDto2);
        assertThat(result.getHasNext()).isFalse();

        then(productRepository).should().findById(eq(productId));
        then(productReviewRepository).should().findById(eq(productReviewId));
        then(productReviewCommentRepository).should().findAllByCursor(eq(productReviewId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(productReviewCommentMapper).should().toDto(eq(productReviewComment));
        then(productReviewCommentMapper).should().toDto(eq(productReviewComment2));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 목록 조회 성공 - 다음 페이지 있음")
    void getProductReviewComments_WithNextPage_Success() {
        // given
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "DESC";
        String cursor = null;
        Long after = null;
        int limit = 1;

        ProductReviewComment productReviewComment2 = ProductReviewComment.createProductReviewComment("테스트 댓글2", productReview, user);
        ReflectionTestUtils.setField(productReviewComment2, "id", 2L);
        ReflectionTestUtils.setField(productReviewComment2, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(productReviewComment2, "updatedAt", LocalDateTime.now());
        List<ProductReviewComment> productReviewComments = Arrays.asList(productReviewComment, productReviewComment2);

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(productReviewId))).willReturn(Optional.of(productReview));
        given(productReviewCommentRepository.findAllByCursor(eq(productReviewId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(productReviewComments);
        given(productReviewCommentMapper.toDto(eq(productReviewComment))).willReturn(productReviewCommentDto);

        // when
        CursorPageResponseDto<ProductReviewCommentDto> result = productReviewCommentService.getProductReviewComments(
                productId, productReviewId, keyword, orderBy, direction, cursor, after, limit
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isNotNull();
        assertThat(result.getNextAfter()).isNotNull();

        then(productRepository).should().findById(eq(productId));
        then(productReviewRepository).should().findById(eq(productReviewId));
        then(productReviewCommentRepository).should().findAllByCursor(eq(productReviewId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(productReviewCommentMapper).should().toDto(eq(productReviewComment));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 목록 조회 실패 - 상품을 찾을 수 없음")
    void getProductReviewComments_ProductNotFound() {
        // given
        Long invalidProductId = 999L;
        given(productRepository.findById(eq(invalidProductId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productReviewCommentService.getProductReviewComments(
                invalidProductId, productReviewId, null, "createdAt", "DESC", null, null, 10
        )).isInstanceOf(ProductException.class);

        then(productRepository).should().findById(eq(invalidProductId));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 목록 조회 실패 - 리뷰를 찾을 수 없음")
    void getProductReviewComments_ReviewNotFound() {
        // given
        Long invalidProductReviewId = 999L;
        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(invalidProductReviewId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productReviewCommentService.getProductReviewComments(
                productId, invalidProductReviewId, null, "createdAt", "DESC", null, null, 10
        )).isInstanceOf(ProductReviewException.class);

        then(productRepository).should().findById(eq(productId));
        then(productReviewRepository).should().findById(eq(invalidProductReviewId));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 단건 조회 성공")
    void getProductReviewComment_Success() {
        // given
        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(productReviewId))).willReturn(Optional.of(productReview));
        given(productReviewCommentRepository.findById(eq(productCommentId))).willReturn(Optional.of(productReviewComment));
        given(productReviewCommentMapper.toDto(eq(productReviewComment))).willReturn(productReviewCommentDto);

        // when
        ProductReviewCommentDto result = productReviewCommentService.getProductReviewComment(productId, productReviewId, productCommentId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(productReviewCommentDto);

        then(productRepository).should().findById(eq(productId));
        then(productReviewRepository).should().findById(eq(productReviewId));
        then(productReviewCommentRepository).should().findById(eq(productCommentId));
        then(productReviewCommentMapper).should().toDto(eq(productReviewComment));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 단건 조회 실패 - 상품을 찾을 수 없음")
    void getProductReviewComment_ProductNotFound() {
        // given
        Long invalidProductId = 999L;
        given(productRepository.findById(eq(invalidProductId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productReviewCommentService.getProductReviewComment(invalidProductId, productReviewId, productCommentId))
                .isInstanceOf(ProductException.class);

        then(productRepository).should().findById(eq(invalidProductId));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 단건 조회 실패 - 리뷰를 찾을 수 없음")
    void getProductReviewComment_ReviewNotFound() {
        // given
        Long invalidProductReviewId = 999L;
        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(invalidProductReviewId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productReviewCommentService.getProductReviewComment(productId, invalidProductReviewId, productCommentId))
                .isInstanceOf(ProductReviewException.class);

        then(productRepository).should().findById(eq(productId));
        then(productReviewRepository).should().findById(eq(invalidProductReviewId));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 단건 조회 실패 - 댓글을 찾을 수 없음")
    void getProductReviewComment_CommentNotFound() {
        // given
        Long invalidProductCommentId = 999L;
        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(productReviewId))).willReturn(Optional.of(productReview));
        given(productReviewCommentRepository.findById(eq(invalidProductCommentId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productReviewCommentService.getProductReviewComment(productId, productReviewId, invalidProductCommentId))
                .isInstanceOf(ProductReviewCommentException.class);

        then(productRepository).should().findById(eq(productId));
        then(productReviewRepository).should().findById(eq(productReviewId));
        then(productReviewCommentRepository).should().findById(eq(invalidProductCommentId));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 단건 조회 실패 - 리뷰가 상품에 속하지 않음")
    void getProductReviewComment_ReviewNotBelongToProduct() {
        // given
        Product anotherProduct = Product.createProduct("다른 상품", "other.jpg", 20000, 50, List.of(), user);
        ReflectionTestUtils.setField(anotherProduct, "id", 2L);
        ProductReview anotherProductReview = ProductReview.createProductReview(user, anotherProduct, BigDecimal.valueOf(4.0), "다른 리뷰");
        ReflectionTestUtils.setField(anotherProductReview, "product", anotherProduct);

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(productReviewId))).willReturn(Optional.of(anotherProductReview));

        // when & then
        assertThatThrownBy(() -> productReviewCommentService.getProductReviewComment(productId, productReviewId, productCommentId))
                .isInstanceOf(ProductReviewException.class);

        then(productRepository).should().findById(eq(productId));
        then(productReviewRepository).should().findById(eq(productReviewId));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 수정 성공")
    void updateProductReviewComment_Success() {
        // given
        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(productReviewId))).willReturn(Optional.of(productReview));
        given(productReviewCommentRepository.findById(eq(productCommentId))).willReturn(Optional.of(productReviewComment));
        given(productReviewCommentMapper.toDto(eq(productReviewComment))).willReturn(productReviewCommentDto);

        // when
        ProductReviewCommentDto result = productReviewCommentService.updateProductReviewComment(productId, productReviewId, productCommentId, updateRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(productReviewCommentDto);

        then(productRepository).should().findById(eq(productId));
        then(productReviewRepository).should().findById(eq(productReviewId));
        then(productReviewCommentRepository).should().findById(eq(productCommentId));
        then(productReviewCommentMapper).should().toDto(eq(productReviewComment));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 수정 실패 - 상품을 찾을 수 없음")
    void updateProductReviewComment_ProductNotFound() {
        // given
        Long invalidProductId = 999L;
        given(productRepository.findById(eq(invalidProductId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productReviewCommentService.updateProductReviewComment(invalidProductId, productReviewId, productCommentId, updateRequestDto))
                .isInstanceOf(ProductException.class);

        then(productRepository).should().findById(eq(invalidProductId));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 수정 실패 - 리뷰를 찾을 수 없음")
    void updateProductReviewComment_ReviewNotFound() {
        // given
        Long invalidProductReviewId = 999L;
        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(invalidProductReviewId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productReviewCommentService.updateProductReviewComment(productId, invalidProductReviewId, productCommentId, updateRequestDto))
                .isInstanceOf(ProductReviewException.class);

        then(productRepository).should().findById(eq(productId));
        then(productReviewRepository).should().findById(eq(invalidProductReviewId));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 수정 실패 - 댓글을 찾을 수 없음")
    void updateProductReviewComment_CommentNotFound() {
        // given
        Long invalidProductCommentId = 999L;
        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(productReviewId))).willReturn(Optional.of(productReview));
        given(productReviewCommentRepository.findById(eq(invalidProductCommentId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productReviewCommentService.updateProductReviewComment(productId, productReviewId, invalidProductCommentId, updateRequestDto))
                .isInstanceOf(ProductReviewCommentException.class);

        then(productRepository).should().findById(eq(productId));
        then(productReviewRepository).should().findById(eq(productReviewId));
        then(productReviewCommentRepository).should().findById(eq(invalidProductCommentId));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 수정 실패 - 리뷰가 상품에 속하지 않음")
    void updateProductReviewComment_ReviewNotBelongToProduct() {
        // given
        Product anotherProduct = Product.createProduct("다른 상품", "other.jpg", 20000, 50, List.of(), user);
        ReflectionTestUtils.setField(anotherProduct, "id", 2L);
        ProductReview anotherProductReview = ProductReview.createProductReview(user, anotherProduct, BigDecimal.valueOf(4.0), "다른 리뷰");
        ReflectionTestUtils.setField(anotherProductReview, "product", anotherProduct);

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(productReviewId))).willReturn(Optional.of(anotherProductReview));

        // when & then
        assertThatThrownBy(() -> productReviewCommentService.updateProductReviewComment(productId, productReviewId, productCommentId, updateRequestDto))
                .isInstanceOf(ProductReviewException.class);

        then(productRepository).should().findById(eq(productId));
        then(productReviewRepository).should().findById(eq(productReviewId));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 삭제 성공")
    void deleteProductReviewComment_Success() {
        // given
        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(productReviewId))).willReturn(Optional.of(productReview));
        given(productReviewCommentRepository.findById(eq(productCommentId))).willReturn(Optional.of(productReviewComment));

        // when
        productReviewCommentService.deleteProductReviewComment(productId, productReviewId, productCommentId);

        // then
        then(productRepository).should().findById(eq(productId));
        then(productReviewRepository).should().findById(eq(productReviewId));
        then(productReviewCommentRepository).should().findById(eq(productCommentId));
        then(productReviewCommentRepository).should().delete(eq(productReviewComment));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 삭제 실패 - 상품을 찾을 수 없음")
    void deleteProductReviewComment_ProductNotFound() {
        // given
        Long invalidProductId = 999L;
        given(productRepository.findById(eq(invalidProductId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productReviewCommentService.deleteProductReviewComment(invalidProductId, productReviewId, productCommentId))
                .isInstanceOf(ProductException.class);

        then(productRepository).should().findById(eq(invalidProductId));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 삭제 실패 - 리뷰를 찾을 수 없음")
    void deleteProductReviewComment_ReviewNotFound() {
        // given
        Long invalidProductReviewId = 999L;
        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(invalidProductReviewId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productReviewCommentService.deleteProductReviewComment(productId, invalidProductReviewId, productCommentId))
                .isInstanceOf(ProductReviewException.class);

        then(productRepository).should().findById(eq(productId));
        then(productReviewRepository).should().findById(eq(invalidProductReviewId));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 삭제 실패 - 댓글을 찾을 수 없음")
    void deleteProductReviewComment_CommentNotFound() {
        // given
        Long invalidProductCommentId = 999L;
        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(productReviewId))).willReturn(Optional.of(productReview));
        given(productReviewCommentRepository.findById(eq(invalidProductCommentId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productReviewCommentService.deleteProductReviewComment(productId, productReviewId, invalidProductCommentId))
                .isInstanceOf(ProductReviewCommentException.class);

        then(productRepository).should().findById(eq(productId));
        then(productReviewRepository).should().findById(eq(productReviewId));
        then(productReviewCommentRepository).should().findById(eq(invalidProductCommentId));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 삭제 실패 - 리뷰가 상품에 속하지 않음")
    void deleteProductReviewComment_ReviewNotBelongToProduct() {
        // given
        Product anotherProduct = Product.createProduct("다른 상품", "other.jpg", 20000, 50, List.of(), user);
        ReflectionTestUtils.setField(anotherProduct, "id", 2L);
        ProductReview anotherProductReview = ProductReview.createProductReview(user, anotherProduct, BigDecimal.valueOf(4.0), "다른 리뷰");
        ReflectionTestUtils.setField(anotherProductReview, "product", anotherProduct);

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(productReviewRepository.findById(eq(productReviewId))).willReturn(Optional.of(anotherProductReview));

        // when & then
        assertThatThrownBy(() -> productReviewCommentService.deleteProductReviewComment(productId, productReviewId, productCommentId))
                .isInstanceOf(ProductReviewException.class);

        then(productRepository).should().findById(eq(productId));
        then(productReviewRepository).should().findById(eq(productReviewId));
    }
}