package travel.mytravelplan.domain.product.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.category.entity.Category;
import travel.mytravelplan.domain.category.repository.CategoryRepository;
import travel.mytravelplan.domain.product.dto.*;
import travel.mytravelplan.domain.product.entity.Product;
import travel.mytravelplan.domain.product.entity.ProductBookMark;
import travel.mytravelplan.domain.product.entity.ProductCategory;
import travel.mytravelplan.domain.product.exception.ProductException;
import travel.mytravelplan.domain.product.mapper.ProductBookMarkMapper;
import travel.mytravelplan.domain.product.mapper.ProductMapper;
import travel.mytravelplan.domain.product.repository.ProductBookMarkRepository;
import travel.mytravelplan.domain.product.repository.ProductRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.support.ServiceTestSupport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("상품 서비스 테스트")
class ProductServiceTest extends ServiceTestSupport {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductBookMarkRepository productBookMarkRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductBookMarkMapper productBookMarkMapper;

    @InjectMocks
    private ProductService productService;

    private User seller;
    private User buyer;
    private Category category;
    private Product product;
    private ProductCategory productCategory;

    @BeforeEach
    void setUp() {
        seller = User.createUser(
                "seller",
                "password123",
                "seller@example.com",
                SocialType.GOOGLE,
                "social123",
                Set.of(Role.SELLER)
        );
        ReflectionTestUtils.setField(seller, "id", 1L);

        buyer = User.createUser(
                "buyer",
                "password123",
                "buyer@example.com",
                SocialType.GOOGLE,
                "social456",
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(buyer, "id", 2L);

        category = Category.createCategory("여행용품", 1, null);
        ReflectionTestUtils.setField(category, "id", 1L);

        productCategory = ProductCategory.createProductCategory(category);

        product = Product.createProduct(
                "여행 가방",
                "bag.jpg",
                50000,
                10,
                List.of(productCategory),
                seller
        );
        ReflectionTestUtils.setField(product, "id", 1L);
        ReflectionTestUtils.setField(product, "createdAt", LocalDateTime.now());
    }

    @Test
    @DisplayName("상품을 생성한다")
    void createProduct() {
        // given
        ProductCreateRequestDto requestDto = ProductCreateRequestDto.builder()
                .name("여행 가방")
                .imageUrl("bag.jpg")
                .price(50000)
                .stockQuantity(10)
                .categoryIds(List.of(1L))
                .build();

        given(categoryRepository.findAllByIds(eq(List.of(1L)))).willReturn(List.of(category));
        given(productRepository.save(any(Product.class))).willReturn(product);

        ProductDto expectedDto = ProductDto.builder()
                .id(1L)
                .name("여행 가방")
                .imageUrl("bag.jpg")
                .price(50000)
                .stockQuantity(10)
                .build();
        given(productMapper.toDto(any(Product.class), eq(seller))).willReturn(expectedDto);

        // when
        ProductDto result = productService.createProduct(seller, requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("여행 가방");
        assertThat(result.getImageUrl()).isEqualTo("bag.jpg");
        assertThat(result.getPrice()).isEqualTo(50000);
        assertThat(result.getStockQuantity()).isEqualTo(10);

        then(categoryRepository).should().findAllByIds(eq(List.of(1L)));
        then(productRepository).should().save(any(Product.class));
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 상품 목록을 조회한다")
    void getProducts() {
        // given
        String keyword = "가방";
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        List<Product> products = List.of(product);
        given(productRepository.findAllCursor(eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(products);

        ProductDto productDto = ProductDto.builder()
                .id(1L)
                .name("여행 가방")
                .imageUrl("bag.jpg")
                .price(50000)
                .stockQuantity(10)
                .build();
        given(productMapper.toDto(any(Product.class), eq(buyer))).willReturn(productDto);

        // when
        CursorPageResponseDto<ProductDto> result = productService.getProducts(buyer, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getSize()).isEqualTo(1);

        then(productRepository).should().findAllCursor(eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("다음 페이지가 있는 경우 hasNext가 true이고 nextCursor와 nextAfter가 설정된다")
    void getProductsWithNextPage() {
        // given
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 1;

        Product product2 = Product.createProduct(
                "여행 텀블러",
                "tumbler.jpg",
                20000,
                5,
                List.of(productCategory),
                seller
        );
        ReflectionTestUtils.setField(product2, "id", 2L);
        ReflectionTestUtils.setField(product2, "createdAt", LocalDateTime.now().minusDays(1));

        List<Product> products = List.of(product, product2);
        given(productRepository.findAllCursor(eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(products);

        ProductDto productDto = ProductDto.builder()
                .id(1L)
                .name("여행 가방")
                .build();
        given(productMapper.toDto(any(Product.class), eq(buyer))).willReturn(productDto);

        // when
        CursorPageResponseDto<ProductDto> result = productService.getProducts(buyer, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isNotNull();
        assertThat(result.getNextAfter()).isEqualTo(1L);

        then(productRepository).should().findAllCursor(eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("상품 목록이 비어있는 경우 빈 리스트를 반환한다")
    void getProductsEmptyList() {
        // given
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        given(productRepository.findAllCursor(eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(List.of());

        // when
        CursorPageResponseDto<ProductDto> result = productService.getProducts(buyer, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();

        then(productRepository).should().findAllCursor(eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("상품 ID로 상품을 조회한다")
    void getProduct() {
        // given
        Long productId = 1L;

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));

        ProductDto expectedDto = ProductDto.builder()
                .id(1L)
                .name("여행 가방")
                .imageUrl("bag.jpg")
                .price(50000)
                .stockQuantity(10)
                .build();
        given(productMapper.toDto(any(Product.class), eq(buyer))).willReturn(expectedDto);

        // when
        ProductDto result = productService.getProduct(buyer, productId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("여행 가방");
        assertThat(result.getPrice()).isEqualTo(50000);

        then(productRepository).should().findById(eq(productId));
    }

    @Test
    @DisplayName("존재하지 않는 상품을 조회하면 예외가 발생한다")
    void getProductNotFound() {
        // given
        Long productId = 999L;

        given(productRepository.findById(eq(productId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.getProduct(buyer, productId))
                .isInstanceOf(ProductException.class);

        then(productRepository).should().findById(eq(productId));
    }

    @Test
    @DisplayName("상품을 수정한다")
    void updateProduct() {
        // given
        Long productId = 1L;
        ProductUpdateRequestDto requestDto = ProductUpdateRequestDto.builder()
                .name("수정된 여행 가방")
                .imageUrl("updated_bag.jpg")
                .price(60000)
                .stockQuantity(20)
                .categoryIds(List.of(1L))
                .build();

        given(categoryRepository.findAllByIds(eq(List.of(1L)))).willReturn(List.of(category));
        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));

        ProductDto expectedDto = ProductDto.builder()
                .id(1L)
                .name("수정된 여행 가방")
                .imageUrl("updated_bag.jpg")
                .price(60000)
                .stockQuantity(20)
                .build();
        given(productMapper.toDto(any(Product.class), eq(seller))).willReturn(expectedDto);

        // when
        ProductDto result = productService.updateProduct(seller, productId, requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("수정된 여행 가방");
        assertThat(result.getImageUrl()).isEqualTo("updated_bag.jpg");
        assertThat(result.getPrice()).isEqualTo(60000);
        assertThat(result.getStockQuantity()).isEqualTo(20);

        then(categoryRepository).should().findAllByIds(eq(List.of(1L)));
        then(productRepository).should().findById(eq(productId));
    }

    @Test
    @DisplayName("존재하지 않는 상품을 수정하면 예외가 발생한다")
    void updateProductNotFound() {
        // given
        Long productId = 999L;
        ProductUpdateRequestDto requestDto = ProductUpdateRequestDto.builder()
                .name("수정된 여행 가방")
                .imageUrl("updated_bag.jpg")
                .price(60000)
                .stockQuantity(20)
                .categoryIds(List.of(1L))
                .build();

        given(categoryRepository.findAllByIds(eq(List.of(1L)))).willReturn(List.of(category));
        given(productRepository.findById(eq(productId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.updateProduct(seller, productId, requestDto))
                .isInstanceOf(ProductException.class);

        then(categoryRepository).should().findAllByIds(eq(List.of(1L)));
        then(productRepository).should().findById(eq(productId));
    }

    @Test
    @DisplayName("상품을 삭제한다")
    void deleteProduct() {
        // given
        Long productId = 1L;

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));

        // when
        productService.deleteProduct(productId);

        // then
        then(productRepository).should().findById(eq(productId));
        then(productRepository).should().delete(any(Product.class));
    }

    @Test
    @DisplayName("존재하지 않는 상품을 삭제하면 예외가 발생한다")
    void deleteProductNotFound() {
        // given
        Long productId = 999L;

        given(productRepository.findById(eq(productId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.deleteProduct(productId))
                .isInstanceOf(ProductException.class);

        then(productRepository).should().findById(eq(productId));
    }

    @Test
    @DisplayName("상품을 북마크한다")
    void bookmarkProduct() {
        // given
        Long productId = 1L;

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(productBookMarkRepository.findByProductAndUser(any(Product.class), eq(buyer))).willReturn(Optional.empty());

        ProductBookMark productBookMark = ProductBookMark.createProductBookMark(product, buyer);
        ReflectionTestUtils.setField(productBookMark, "id", 1L);

        given(productBookMarkRepository.save(any(ProductBookMark.class))).willReturn(productBookMark);

        ProductBookMarkDto expectedDto = ProductBookMarkDto.builder()
                .productId(1L)
                .userId(2L)
                .bookmarked(true)
                .build();
        given(productBookMarkMapper.toDto(any(ProductBookMark.class), eq(true))).willReturn(expectedDto);

        // when
        ProductBookMarkDto result = productService.bookmarkProduct(buyer, productId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(2L);
        assertThat(result.isBookmarked()).isTrue();

        then(productRepository).should().findById(eq(productId));
        then(productBookMarkRepository).should().findByProductAndUser(any(Product.class), eq(buyer));
        then(productBookMarkRepository).should().save(any(ProductBookMark.class));
    }

    @Test
    @DisplayName("북마크된 상품을 다시 북마크하면 북마크가 취소된다")
    void unbookmarkProduct() {
        // given
        Long productId = 1L;

        ProductBookMark productBookMark = ProductBookMark.createProductBookMark(product, buyer);
        ReflectionTestUtils.setField(productBookMark, "id", 1L);

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(productBookMarkRepository.findByProductAndUser(any(Product.class), eq(buyer))).willReturn(Optional.of(productBookMark));

        ProductBookMarkDto expectedDto = ProductBookMarkDto.builder()
                .productId(1L)
                .userId(2L)
                .bookmarked(false)
                .build();
        given(productBookMarkMapper.toDto(any(ProductBookMark.class), eq(false))).willReturn(expectedDto);

        // when
        ProductBookMarkDto result = productService.bookmarkProduct(buyer, productId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(2L);
        assertThat(result.isBookmarked()).isFalse();

        then(productRepository).should().findById(eq(productId));
        then(productBookMarkRepository).should().findByProductAndUser(any(Product.class), eq(buyer));
        then(productBookMarkRepository).should().delete(any(ProductBookMark.class));
    }

    @Test
    @DisplayName("존재하지 않는 상품을 북마크하면 예외가 발생한다")
    void bookmarkProductNotFound() {
        // given
        Long productId = 999L;

        given(productRepository.findById(eq(productId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.bookmarkProduct(buyer, productId))
                .isInstanceOf(ProductException.class);

        then(productRepository).should().findById(eq(productId));
    }
}