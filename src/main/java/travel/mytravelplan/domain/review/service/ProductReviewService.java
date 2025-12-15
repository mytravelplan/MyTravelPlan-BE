package travel.mytravelplan.domain.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.product.dto.PopularProductReviewDto;
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
import travel.mytravelplan.global.common.enums.Period;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.error.code.ProductErrorCode;
import travel.mytravelplan.global.error.code.ProductReviewErrorCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductReviewService {
    private final ProductRepository productRepository;
    private final ProductReviewRepository productReviewRepository;
    private final ProductReviewLikeRepository productReviewLikeRepository;
    private final ProductReviewMapper productReviewMapper;
    private final ProductReviewLikeMapper productReviewLikeMapper;

    @Transactional
    public ProductReviewDto createProductReview(User currentUser, Long productId, ProductReviewCreateRequestDto productReviewCreateRequestDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        ProductReview productReview = ProductReview.createProductReview(currentUser, product, productReviewCreateRequestDto.getRating(), productReviewCreateRequestDto.getContent());

        productReviewRepository.save(productReview);

        return productReviewMapper.toDto(productReview, currentUser);
    }

    public ProductReviewDto getProductReview(User currentUser, Long productId, Long productReviewId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        ProductReview productReview = productReviewRepository.findById(productReviewId)
                .orElseThrow(() -> new ProductReviewException(ProductReviewErrorCode.PRODUCT_REVIEW_NOT_FOUND));

        validateProductReviewBelongsToProduct(productReview, product);

        return productReviewMapper.toDto(productReview, currentUser);
    }

    public CursorPageResponseDto<ProductReviewDto> getProductReviews(User currentUser, Long productId, String keyword, boolean imgOnly, BigDecimal rating, String orderBy, String direction, String cursor, Long after, int limit) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        List<ProductReview> productReviews = productReviewRepository.findAllByCursor(product.getId(), keyword, imgOnly, rating, orderBy, direction, cursor, after, limit + 1);

        boolean hasNext = productReviews.size() > limit;

        List<ProductReview> pagedProductReviews = hasNext ? productReviews.subList(0, limit) : productReviews;

        List<ProductReviewDto> productReviewDtos = pagedProductReviews.stream()
                .map(productReview -> productReviewMapper.toDto(productReview, currentUser))
                .toList();

        String nextCursor = null;
        Long nextAfter = null;

        if (hasNext) {
            ProductReview lastReview = pagedProductReviews.getLast();

            if (orderBy.equals("createdAt")) {
                nextCursor = lastReview.getCreatedAt().toString();
            } else if (orderBy.equals("rating")) {
                nextCursor = String.valueOf(lastReview.getRating());
            }

            nextAfter = lastReview.getId();
        }

        return CursorPageResponseDto.<ProductReviewDto>builder()
                .content(productReviewDtos)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(productReviewDtos.size())
                .hasNext(hasNext)
                .build();
    }

    @Transactional
    public ProductReviewDto updateProductReview(User currentUser, Long productId, Long productReviewId, ProductReviewUpdateRequestDto productReviewUpdateRequestDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        ProductReview productReview = productReviewRepository.findById(productReviewId)
                .orElseThrow(() -> new ProductReviewException(ProductReviewErrorCode.PRODUCT_REVIEW_NOT_FOUND));

        validateProductReviewBelongsToProduct(productReview, product);

        productReview.update(
                productReviewUpdateRequestDto.getContent(),
                productReviewUpdateRequestDto.getRating()
        );

        return productReviewMapper.toDto(productReview, currentUser);
    }

    @Transactional
    public void deleteProductReview(Long productId, Long productReviewId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        ProductReview productReview = productReviewRepository.findById(productReviewId)
                .orElseThrow(() -> new ProductReviewException(ProductReviewErrorCode.PRODUCT_REVIEW_NOT_FOUND));

        validateProductReviewBelongsToProduct(productReview, product);

        productReviewRepository.delete(productReview);
    }

    @Transactional
    public ProductReviewLikeDto likeProductReview(User currentUser, Long productId, Long productReviewId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        ProductReview productReview = productReviewRepository.findById(productReviewId)
                .orElseThrow(() -> new ProductReviewException(ProductReviewErrorCode.PRODUCT_REVIEW_NOT_FOUND));

        validateProductReviewBelongsToProduct(productReview, product);

        Optional<ProductReviewLike> productReviewLikeOptional = productReviewLikeRepository.findByProductReviewAndUser(productReview, currentUser);

        ProductReviewLike productReviewLike;
        boolean isLiked;

        if (productReviewLikeOptional.isPresent()) {
            productReviewLike = productReviewLikeOptional.get();
            productReviewLikeRepository.delete(productReviewLike);
            isLiked = false;
        } else {
            productReviewLike = ProductReviewLike.createProductReviewLike(productReview, currentUser);
            productReviewLikeRepository.save(productReviewLike);
            isLiked = true;
        }

        return productReviewLikeMapper.toDto(productReviewLike, isLiked);
    }

/*
    public CursorPageResponseDto<PopularProductReviewDto> getPopularProductReviews(Long productId, Period period, String direction, String cursor, Long after, int limit) {
        return null;
    }
*/

    private void validateProductReviewBelongsToProduct(ProductReview productReview, Product product) {
        if (!productReview.getProduct().equals(product)) {
            throw new ProductReviewException(ProductReviewErrorCode.PRODUCT_REVIEW_NOT_BELONG_TO_PRODUCT);
        }
    }

}
