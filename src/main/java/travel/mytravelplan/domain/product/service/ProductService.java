package travel.mytravelplan.domain.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import travel.mytravelplan.global.common.enums.Period;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.error.code.ProductErrorCode;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductBookMarkRepository productBookMarkRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ProductBookMarkMapper productBookMarkMapper;

    @Transactional
    public ProductDto createProduct(User currentUser, ProductCreateRequestDto productCreateRequestDto) {
        List<Category> categories = categoryRepository.findAllByIds(productCreateRequestDto.getCategoryIds());

        List<ProductCategory> productCategories = categories.stream()
                .map(ProductCategory::createProductCategory)
                .toList();

        Product product = Product.createProduct(
                productCreateRequestDto.getName(),
                productCreateRequestDto.getImageUrl(),
                productCreateRequestDto.getPrice(),
                productCreateRequestDto.getStockQuantity(),
                productCategories,
                currentUser);

        productRepository.save(product);

        return productMapper.toDto(product, currentUser);
    }

    public CursorPageResponseDto<ProductDto> getProducts(User currentUser, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        List<Product> products = productRepository.findAllCursor(keyword, orderBy, direction, cursor, after, limit + 1);

        boolean hasNext = products.size() > limit;

        List<Product> pagedProducts = hasNext ? products.subList(0, limit) : products;

        List<ProductDto> productDtos = pagedProducts.stream()
                .map(product -> productMapper.toDto(product, currentUser))
                .toList();

        String nextCursor = null;
        Long nextAfter = null;

        if (hasNext) {
            Product lastProduct = pagedProducts.getLast();

            if (orderBy.equals("createdAt")) {
                nextCursor = lastProduct.getCreatedAt().toString();
            }

            nextAfter = lastProduct.getId();
        }

        return CursorPageResponseDto.<ProductDto>builder()
                .content(productDtos)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(productDtos.size())
                .hasNext(hasNext)
                .build();
    }

    public ProductDto getProduct(User currentUser, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        return productMapper.toDto(product, currentUser);
    }

    @Transactional
    public ProductDto updateProduct(User currentUser, Long productId, ProductUpdateRequestDto productUpdateRequestDto) {
        List<Category> categories = categoryRepository.findAllByIds(productUpdateRequestDto.getCategoryIds());

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        List<ProductCategory> productCategories = categories.stream()
                .map(ProductCategory::createProductCategory)
                .toList();

        product.update(
                productUpdateRequestDto.getName(),
                productUpdateRequestDto.getImageUrl(),
                productUpdateRequestDto.getPrice(),
                productUpdateRequestDto.getStockQuantity(),
                productCategories
        );

        return productMapper.toDto(product, currentUser);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        productRepository.delete(product);
    }

    @Transactional
    public ProductBookMarkDto bookmarkProduct(User currentUser, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        Optional<ProductBookMark> productBookMarkOptional = productBookMarkRepository.findByProductAndUser(product, currentUser);

        ProductBookMark productBookMark;
        boolean isBookmarked;

        if (productBookMarkOptional.isPresent()) {
            productBookMark = productBookMarkOptional.get();
            productBookMarkRepository.delete(productBookMark);
            isBookmarked = false;
        } else {
            productBookMark = ProductBookMark.createProductBookMark(product, currentUser);
            productBookMarkRepository.save(productBookMark);
            isBookmarked = true;
        }

        return productBookMarkMapper.toDto(productBookMark, isBookmarked);
    }

/*
    public CursorPageResponseDto<PopularProductDto> getPopularProducts(Period period, String direction, String cursor, Long after, int limit) {
        return null;
    }
*/
}
