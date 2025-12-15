package travel.mytravelplan.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import travel.mytravelplan.global.error.code.ProductErrorCode;
import travel.mytravelplan.global.error.code.ProductReviewCommentErrorCode;
import travel.mytravelplan.global.error.code.ProductReviewErrorCode;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductReviewCommentService {
    private final ProductRepository productRepository;
    private final ProductReviewRepository productReviewRepository;
    private final ProductReviewCommentRepository productReviewCommentRepository;
    private final ProductReviewCommentMapper productReviewCommentMapper;

    @Transactional
    public ProductReviewCommentDto createProductReviewComment(User currentUser, Long productId, Long productReviewId, ProductReviewCommentCreateRequestDto productReviewCommentCreateRequestDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        ProductReview productReview = productReviewRepository.findById(productReviewId)
                .orElseThrow(() -> new ProductReviewException(ProductReviewErrorCode.PRODUCT_REVIEW_NOT_FOUND));

        validateProductReviewBelongsToProduct(product, productReview);

        travel.mytravelplan.domain.comment.entity.ProductReviewComment productReviewComment = travel.mytravelplan.domain.comment.entity.ProductReviewComment.createProductReviewComment(
                productReviewCommentCreateRequestDto.getContent(),
                productReview,
                currentUser
        );

        productReviewCommentRepository.save(productReviewComment);

        return productReviewCommentMapper.toDto(productReviewComment);
    }

    public CursorPageResponseDto<ProductReviewCommentDto> getProductReviewComments(Long productId, Long productReviewId, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        ProductReview productReview = productReviewRepository.findById(productReviewId)
                .orElseThrow(() -> new ProductReviewException(ProductReviewErrorCode.PRODUCT_REVIEW_NOT_FOUND));

        validateProductReviewBelongsToProduct(product, productReview);

        List<ProductReviewComment> productReviewComments = productReviewCommentRepository.findAllByCursor(productReviewId, keyword, orderBy, direction, cursor, after, limit + 1);

        boolean hasNext = productReviewComments.size() > limit;

        List<ProductReviewComment> pagedProductReviewComments = hasNext ? productReviewComments.subList(0, limit) : productReviewComments;

        List<ProductReviewCommentDto> productReviewCommentDtos = pagedProductReviewComments.stream()
                .map(productReviewCommentMapper::toDto)
                .toList();

        String nextCursor = null;
        Long nextAfter = null;

        if (hasNext) {
            ProductReviewComment lastProductReviewComment = pagedProductReviewComments.getLast();

            if (orderBy.equals("createdAt")) {
                nextCursor = lastProductReviewComment.getCreatedAt().toString();
            }

            nextAfter = lastProductReviewComment.getId();
        }

        return CursorPageResponseDto.<ProductReviewCommentDto>builder()
                .content(productReviewCommentDtos)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(productReviewCommentDtos.size())
                .hasNext(hasNext)
                .build();
    }

    public ProductReviewCommentDto getProductReviewComment(Long productId, Long productReviewId, Long productCommentId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        ProductReview productReview = productReviewRepository.findById(productReviewId)
                .orElseThrow(() -> new ProductReviewException(ProductReviewErrorCode.PRODUCT_REVIEW_NOT_FOUND));

        validateProductReviewBelongsToProduct(product, productReview);

        ProductReviewComment productReviewComment = productReviewCommentRepository.findById(productCommentId)
                .orElseThrow(() -> new ProductReviewCommentException(ProductReviewCommentErrorCode.PRODUCT_REVIEW_COMMENT_NOT_FOUND));

        return productReviewCommentMapper.toDto(productReviewComment);
    }

    @Transactional
    public ProductReviewCommentDto updateProductReviewComment(Long productId, Long productReviewId, Long productCommentId, ProductReviewCommentUpdateRequestDto productReviewCommentUpdateRequestDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        ProductReview productReview = productReviewRepository.findById(productReviewId)
                .orElseThrow(() -> new ProductReviewException(ProductReviewErrorCode.PRODUCT_REVIEW_NOT_FOUND));

        validateProductReviewBelongsToProduct(product, productReview);

        ProductReviewComment productReviewComment = productReviewCommentRepository.findById(productCommentId)
                .orElseThrow(() -> new ProductReviewCommentException(ProductReviewCommentErrorCode.PRODUCT_REVIEW_COMMENT_NOT_FOUND));

        productReviewComment.update(productReviewCommentUpdateRequestDto.getContent());

        return productReviewCommentMapper.toDto(productReviewComment);
    }

    @Transactional
    public void deleteProductReviewComment(Long productId, Long productReviewId, Long productCommentId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        ProductReview productReview = productReviewRepository.findById(productReviewId)
                .orElseThrow(() -> new ProductReviewException(ProductReviewErrorCode.PRODUCT_REVIEW_NOT_FOUND));

        validateProductReviewBelongsToProduct(product, productReview);

        ProductReviewComment reviewComment = productReviewCommentRepository.findById(productCommentId)
                .orElseThrow(() -> new ProductReviewCommentException(ProductReviewCommentErrorCode.PRODUCT_REVIEW_COMMENT_NOT_FOUND));

        productReviewCommentRepository.delete(reviewComment);
    }

    private void validateProductReviewBelongsToProduct(Product product, ProductReview productReview) {
        if (!productReview.getProduct().equals(product)) {
            throw new ProductReviewException(ProductReviewErrorCode.PRODUCT_REVIEW_NOT_BELONG_TO_PRODUCT);
        }
    }
}
