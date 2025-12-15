package travel.mytravelplan.domain.comment.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.comment.entity.ProductReviewComment;
import travel.mytravelplan.domain.product.entity.Product;
import travel.mytravelplan.domain.product.repository.ProductRepository;
import travel.mytravelplan.domain.review.entity.ProductReview;
import travel.mytravelplan.domain.review.repository.ProductReviewRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.support.RepositoryTestSupport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("상품 리뷰 댓글 레포지토리 테스트")
class ProductReviewCommentRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private ProductReviewCommentRepository productReviewCommentRepository;

    @Autowired
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("상품 리뷰 댓글을 저장한다")
    void saveProductReviewComment() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", user);
        ProductReview productReview = createProductReview(user, product, BigDecimal.valueOf(4.5), "좋은 상품입니다");
        ProductReviewComment comment = createProductReviewComment("댓글 내용", productReview, user);

        // when
        ProductReviewComment savedComment = productReviewCommentRepository.save(comment);
        em.flush();
        em.clear();

        // then
        assertThat(savedComment.getId()).isNotNull();
        assertThat(savedComment.getContent()).isEqualTo("댓글 내용");
        assertThat(savedComment.getProductReview().getId()).isEqualTo(productReview.getId());
        assertThat(savedComment.getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("상품 리뷰 댓글을 ID로 조회한다")
    void findProductReviewCommentById() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", user);
        ProductReview productReview = createProductReview(user, product, BigDecimal.valueOf(4.5), "좋은 상품입니다");
        ProductReviewComment comment = createAndSaveProductReviewComment("댓글 내용", productReview, user);
        em.flush();
        em.clear();

        // when
        ProductReviewComment foundComment = productReviewCommentRepository.findById(comment.getId()).orElse(null);

        // then
        assertThat(foundComment).isNotNull();
        assertThat(foundComment.getId()).isEqualTo(comment.getId());
        assertThat(foundComment.getContent()).isEqualTo("댓글 내용");
    }

    @Test
    @DisplayName("상품 리뷰 댓글을 수정한다")
    void updateProductReviewComment() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", user);
        ProductReview productReview = createProductReview(user, product, BigDecimal.valueOf(4.5), "좋은 상품입니다");
        ProductReviewComment comment = createAndSaveProductReviewComment("댓글 내용", productReview, user);
        em.flush();
        em.clear();

        // when
        ProductReviewComment foundComment = productReviewCommentRepository.findById(comment.getId()).orElseThrow();
        foundComment.update("수정된 댓글 내용");
        em.flush();
        em.clear();

        // then
        ProductReviewComment updatedComment = productReviewCommentRepository.findById(comment.getId()).orElse(null);
        assertThat(updatedComment).isNotNull();
        assertThat(updatedComment.getContent()).isEqualTo("수정된 댓글 내용");
    }

    @Test
    @DisplayName("상품 리뷰 댓글을 삭제한다")
    void deleteProductReviewComment() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", user);
        ProductReview productReview = createProductReview(user, product, BigDecimal.valueOf(4.5), "좋은 상품입니다");
        ProductReviewComment comment = createAndSaveProductReviewComment("댓글 내용", productReview, user);
        em.flush();
        em.clear();

        // when
        productReviewCommentRepository.deleteById(comment.getId());
        em.flush();
        em.clear();

        // then
        ProductReviewComment deletedComment = productReviewCommentRepository.findById(comment.getId()).orElse(null);
        assertThat(deletedComment).isNull();
    }

    @Test
    @DisplayName("키워드로 상품 리뷰 댓글을 검색한다")
    void findAllByCursor_byKeyword() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", user);
        ProductReview productReview = createProductReview(user, product, BigDecimal.valueOf(4.5), "좋은 상품입니다");

        ProductReviewComment comment1 = createAndSaveProductReviewComment("정말 좋아요", productReview, user);
        ProductReviewComment comment2 = createAndSaveProductReviewComment("배송이 빨라요", productReview, user);
        ProductReviewComment comment3 = createAndSaveProductReviewComment("품질이 좋아요", productReview, user);

        em.flush();
        em.clear();

        // when
        List<ProductReviewComment> comments = productReviewCommentRepository.findAllByCursor(
                productReview.getId(),
                "좋아요",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(comments).hasSize(2);
        assertThat(comments)
                .extracting(ProductReviewComment::getContent)
                .containsExactlyInAnyOrder("정말 좋아요", "품질이 좋아요");
    }

    @Test
    @DisplayName("생성일 기준 내림차순으로 상품 리뷰 댓글을 조회한다")
    void findAllByCursor_orderByCreatedAtDesc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", user);
        ProductReview productReview = createProductReview(user, product, BigDecimal.valueOf(4.5), "좋은 상품입니다");

        ProductReviewComment comment1 = createAndSaveProductReviewComment("댓글1", productReview, user);
        ProductReviewComment comment2 = createAndSaveProductReviewComment("댓글2", productReview, user);
        ProductReviewComment comment3 = createAndSaveProductReviewComment("댓글3", productReview, user);

        em.flush();
        em.clear();

        // when
        List<ProductReviewComment> comments = productReviewCommentRepository.findAllByCursor(
                productReview.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(comments).hasSize(3);
        assertThat(comments.get(0).getContent()).isEqualTo("댓글3");
        assertThat(comments.get(1).getContent()).isEqualTo("댓글2");
        assertThat(comments.get(2).getContent()).isEqualTo("댓글1");
    }

    @Test
    @DisplayName("생성일 기준 오름차순으로 상품 리뷰 댓글을 조회한다")
    void findAllByCursor_orderByCreatedAtAsc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", user);
        ProductReview productReview = createProductReview(user, product, BigDecimal.valueOf(4.5), "좋은 상품입니다");

        ProductReviewComment comment1 = createAndSaveProductReviewComment("댓글1", productReview, user);
        ProductReviewComment comment2 = createAndSaveProductReviewComment("댓글2", productReview, user);
        ProductReviewComment comment3 = createAndSaveProductReviewComment("댓글3", productReview, user);

        em.flush();
        em.clear();

        // when
        List<ProductReviewComment> comments = productReviewCommentRepository.findAllByCursor(
                productReview.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(comments).hasSize(3);
        assertThat(comments.get(0).getContent()).isEqualTo("댓글1");
        assertThat(comments.get(1).getContent()).isEqualTo("댓글2");
        assertThat(comments.get(2).getContent()).isEqualTo("댓글3");
    }

    @Test
    @DisplayName("limit 개수만큼 상품 리뷰 댓글을 조회한다")
    void findAllByCursor_withLimit() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", user);
        ProductReview productReview = createProductReview(user, product, BigDecimal.valueOf(4.5), "좋은 상품입니다");

        ProductReviewComment comment1 = createAndSaveProductReviewComment("댓글1", productReview, user);
        ProductReviewComment comment2 = createAndSaveProductReviewComment("댓글2", productReview, user);
        ProductReviewComment comment3 = createAndSaveProductReviewComment("댓글3", productReview, user);
        ProductReviewComment comment4 = createAndSaveProductReviewComment("댓글4", productReview, user);
        ProductReviewComment comment5 = createAndSaveProductReviewComment("댓글5", productReview, user);

        em.flush();
        em.clear();

        // when
        List<ProductReviewComment> comments = productReviewCommentRepository.findAllByCursor(
                productReview.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                3
        );

        // then
        assertThat(comments).hasSize(3);
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 상품 리뷰 댓글을 조회한다 - 내림차순")
    void findAllByCursor_withCursor_desc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", user);
        ProductReview productReview = createProductReview(user, product, BigDecimal.valueOf(4.5), "좋은 상품입니다");

        ProductReviewComment comment1 = createAndSaveProductReviewComment("댓글1", productReview, user);
        ProductReviewComment comment2 = createAndSaveProductReviewComment("댓글2", productReview, user);
        ProductReviewComment comment3 = createAndSaveProductReviewComment("댓글3", productReview, user);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<ProductReviewComment> firstPage = productReviewCommentRepository.findAllByCursor(
                productReview.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getContent()).isEqualTo("댓글3");
        assertThat(firstPage.get(1).getContent()).isEqualTo("댓글2");

        // when - 두 번째 페이지 조회
        ProductReviewComment lastComment = firstPage.get(firstPage.size() - 1);
        List<ProductReviewComment> secondPage = productReviewCommentRepository.findAllByCursor(
                productReview.getId(),
                null,
                "createdAt",
                "desc",
                lastComment.getCreatedAt().toString(),
                lastComment.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getContent()).isEqualTo("댓글1");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 상품 리뷰 댓글을 조회한다 - 오름차순")
    void findAllByCursor_withCursor_asc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", user);
        ProductReview productReview = createProductReview(user, product, BigDecimal.valueOf(4.5), "좋은 상품입니다");

        ProductReviewComment comment1 = createAndSaveProductReviewComment("댓글1", productReview, user);
        ProductReviewComment comment2 = createAndSaveProductReviewComment("댓글2", productReview, user);
        ProductReviewComment comment3 = createAndSaveProductReviewComment("댓글3", productReview, user);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<ProductReviewComment> firstPage = productReviewCommentRepository.findAllByCursor(
                productReview.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getContent()).isEqualTo("댓글1");
        assertThat(firstPage.get(1).getContent()).isEqualTo("댓글2");

        // when - 두 번째 페이지 조회
        ProductReviewComment lastComment = firstPage.get(firstPage.size() - 1);
        List<ProductReviewComment> secondPage = productReviewCommentRepository.findAllByCursor(
                productReview.getId(),
                null,
                "createdAt",
                "asc",
                lastComment.getCreatedAt().toString(),
                lastComment.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getContent()).isEqualTo("댓글3");
    }

    @Test
    @DisplayName("특정 리뷰의 댓글만 조회한다")
    void findAllByCursor_byProductReviewId() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", user);
        ProductReview productReview1 = createProductReview(user, product, BigDecimal.valueOf(4.5), "좋은 상품입니다");
        ProductReview productReview2 = createProductReview(user, product, BigDecimal.valueOf(3.0), "그냥 그래요");

        ProductReviewComment comment1 = createAndSaveProductReviewComment("리뷰1의 댓글1", productReview1, user);
        ProductReviewComment comment2 = createAndSaveProductReviewComment("리뷰1의 댓글2", productReview1, user);
        ProductReviewComment comment3 = createAndSaveProductReviewComment("리뷰2의 댓글1", productReview2, user);

        em.flush();
        em.clear();

        // when
        List<ProductReviewComment> comments = productReviewCommentRepository.findAllByCursor(
                productReview1.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(comments).hasSize(2);
        assertThat(comments)
                .extracting(ProductReviewComment::getContent)
                .containsExactlyInAnyOrder("리뷰1의 댓글1", "리뷰1의 댓글2");
    }

    @Test
    @DisplayName("키워드와 정렬을 함께 사용하여 상품 리뷰 댓글을 조회한다")
    void findAllByCursor_byKeywordAndOrder() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", user);
        ProductReview productReview = createProductReview(user, product, BigDecimal.valueOf(4.5), "좋은 상품입니다");

        createAndSaveProductReviewComment("배송이 빠릅니다", productReview, user);
        createAndSaveProductReviewComment("상품이 빠릅니다", productReview, user);
        createAndSaveProductReviewComment("품질이 좋습니다", productReview, user);
        createAndSaveProductReviewComment("배송이 좋습니다", productReview, user);

        em.flush();
        em.clear();

        // when
        List<ProductReviewComment> comments = productReviewCommentRepository.findAllByCursor(
                productReview.getId(),
                "빠릅니다",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getContent()).isEqualTo("상품이 빠릅니다");
        assertThat(comments.get(1).getContent()).isEqualTo("배송이 빠릅니다");
    }

    @Test
    @DisplayName("커서와 after가 null인 경우 첫 페이지를 조회한다")
    void findAllByCursor_withNullCursorAndAfter() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", user);
        ProductReview productReview = createProductReview(user, product, BigDecimal.valueOf(4.5), "좋은 상품입니다");

        ProductReviewComment comment1 = createAndSaveProductReviewComment("댓글1", productReview, user);
        ProductReviewComment comment2 = createAndSaveProductReviewComment("댓글2", productReview, user);

        em.flush();
        em.clear();

        // when
        List<ProductReviewComment> comments = productReviewCommentRepository.findAllByCursor(
                productReview.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getContent()).isEqualTo("댓글1");
        assertThat(comments.get(1).getContent()).isEqualTo("댓글2");
    }

    @Test
    @DisplayName("productReviewId가 null인 경우 모든 리뷰의 댓글을 조회한다")
    void findAllByCursor_withNullProductReviewId() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", user);
        ProductReview productReview1 = createProductReview(user, product, BigDecimal.valueOf(4.5), "좋은 상품입니다");
        ProductReview productReview2 = createProductReview(user, product, BigDecimal.valueOf(3.0), "그냥 그래요");

        ProductReviewComment comment1 = createAndSaveProductReviewComment("리뷰1 댓글", productReview1, user);
        ProductReviewComment comment2 = createAndSaveProductReviewComment("리뷰2 댓글", productReview2, user);

        em.flush();
        em.clear();

        // when
        List<ProductReviewComment> comments = productReviewCommentRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(comments).hasSize(2);
    }

    @Test
    @DisplayName("키워드가 빈 문자열인 경우 모든 댓글을 조회한다")
    void findAllByCursor_withEmptyKeyword() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", user);
        ProductReview productReview = createProductReview(user, product, BigDecimal.valueOf(4.5), "좋은 상품입니다");

        ProductReviewComment comment1 = createAndSaveProductReviewComment("호텔", productReview, user);
        ProductReviewComment comment2 = createAndSaveProductReviewComment("식당", productReview, user);

        em.flush();
        em.clear();

        // when
        List<ProductReviewComment> comments = productReviewCommentRepository.findAllByCursor(
                productReview.getId(),
                "",
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(comments).hasSize(2);
    }

    @Test
    @DisplayName("키워드 검색 시 대소문자를 구분하지 않는다")
    void findAllByCursor_withKeywordCaseInsensitive() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", user);
        ProductReview productReview = createProductReview(user, product, BigDecimal.valueOf(4.5), "좋은 상품입니다");

        createAndSaveProductReviewComment("GOOD Product", productReview, user);
        createAndSaveProductReviewComment("good product", productReview, user);
        createAndSaveProductReviewComment("Good Product", productReview, user);

        em.flush();
        em.clear();

        // when
        List<ProductReviewComment> comments = productReviewCommentRepository.findAllByCursor(
                productReview.getId(),
                "good",
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(comments).hasSize(3);
    }

    @Test
    @DisplayName("cursor만 존재하고 after가 null인 경우에도 첫 페이지를 조회한다")
    void findAllByCursor_withCursorOnlyAndNullAfter() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", user);
        ProductReview productReview = createProductReview(user, product, BigDecimal.valueOf(4.5), "좋은 상품입니다");

        ProductReviewComment comment1 = createAndSaveProductReviewComment("댓글1", productReview, user);
        ProductReviewComment comment2 = createAndSaveProductReviewComment("댓글2", productReview, user);
        ProductReviewComment comment3 = createAndSaveProductReviewComment("댓글3", productReview, user);

        em.flush();
        em.clear();

        // when - cursor만 있고 after가 null
        List<ProductReviewComment> comments = productReviewCommentRepository.findAllByCursor(
                productReview.getId(),
                null,
                "createdAt",
                "asc",
                comment1.getCreatedAt().toString(),
                null,
                10
        );

        // then - cursor와 after가 모두 null이 아니어야 조건이 적용되므로 모든 댓글이 조회됨
        assertThat(comments).hasSize(3);
    }

    @Test
    @DisplayName("after만 존재하고 cursor가 null인 경우에도 첫 페이지를 조회한다")
    void findAllByCursor_withAfterOnlyAndNullCursor() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", user);
        ProductReview productReview = createProductReview(user, product, BigDecimal.valueOf(4.5), "좋은 상품입니다");

        ProductReviewComment comment1 = createAndSaveProductReviewComment("댓글1", productReview, user);
        ProductReviewComment comment2 = createAndSaveProductReviewComment("댓글2", productReview, user);
        ProductReviewComment comment3 = createAndSaveProductReviewComment("댓글3", productReview, user);

        em.flush();
        em.clear();

        // when - after만 있고 cursor가 null
        List<ProductReviewComment> comments = productReviewCommentRepository.findAllByCursor(
                productReview.getId(),
                null,
                "createdAt",
                "asc",
                null,
                comment1.getId(),
                10
        );

        // then - cursor와 after가 모두 null이 아니어야 조건이 적용되므로 모든 댓글이 조회됨
        assertThat(comments).hasSize(3);
    }

    @Test
    @DisplayName("cursor와 after가 모두 존재하는 경우 커서 기반 페이지네이션이 적용된다 - 오름차순")
    void findAllByCursor_withBothCursorAndAfter_asc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", user);
        ProductReview productReview = createProductReview(user, product, BigDecimal.valueOf(4.5), "좋은 상품입니다");

        ProductReviewComment comment1 = createAndSaveProductReviewComment("댓글1", productReview, user);
        ProductReviewComment comment2 = createAndSaveProductReviewComment("댓글2", productReview, user);
        ProductReviewComment comment3 = createAndSaveProductReviewComment("댓글3", productReview, user);
        ProductReviewComment comment4 = createAndSaveProductReviewComment("댓글4", productReview, user);

        em.flush();
        em.clear();

        // when - 첫 페이지
        List<ProductReviewComment> firstPage = productReviewCommentRepository.findAllByCursor(
                productReview.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getContent()).isEqualTo("댓글1");
        assertThat(firstPage.get(1).getContent()).isEqualTo("댓글2");

        // when - cursor와 after가 모두 존재하는 경우 (두 번째 페이지)
        ProductReviewComment lastComment = firstPage.get(firstPage.size() - 1);
        List<ProductReviewComment> secondPage = productReviewCommentRepository.findAllByCursor(
                productReview.getId(),
                null,
                "createdAt",
                "asc",
                lastComment.getCreatedAt().toString(),
                lastComment.getId(),
                2
        );

        // then - cursor와 after 조건이 적용되어 다음 페이지가 조회됨
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage.get(0).getContent()).isEqualTo("댓글3");
        assertThat(secondPage.get(1).getContent()).isEqualTo("댓글4");
    }

    @Test
    @DisplayName("cursor와 after가 모두 존재하는 경우 커서 기반 페이지네이션이 적용된다 - 내림차순")
    void findAllByCursor_withBothCursorAndAfter_desc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Product product = createProduct("테스트 상품", user);
        ProductReview productReview = createProductReview(user, product, BigDecimal.valueOf(4.5), "좋은 상품입니다");

        ProductReviewComment comment1 = createAndSaveProductReviewComment("댓글1", productReview, user);
        ProductReviewComment comment2 = createAndSaveProductReviewComment("댓글2", productReview, user);
        ProductReviewComment comment3 = createAndSaveProductReviewComment("댓글3", productReview, user);
        ProductReviewComment comment4 = createAndSaveProductReviewComment("댓글4", productReview, user);

        em.flush();
        em.clear();

        // when - 첫 페이지
        List<ProductReviewComment> firstPage = productReviewCommentRepository.findAllByCursor(
                productReview.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getContent()).isEqualTo("댓글4");
        assertThat(firstPage.get(1).getContent()).isEqualTo("댓글3");

        // when - cursor와 after가 모두 존재하는 경우 (두 번째 페이지)
        ProductReviewComment lastComment = firstPage.get(firstPage.size() - 1);
        List<ProductReviewComment> secondPage = productReviewCommentRepository.findAllByCursor(
                productReview.getId(),
                null,
                "createdAt",
                "desc",
                lastComment.getCreatedAt().toString(),
                lastComment.getId(),
                2
        );

        // then - cursor와 after 조건이 적용되어 다음 페이지가 조회됨
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage.get(0).getContent()).isEqualTo("댓글2");
        assertThat(secondPage.get(1).getContent()).isEqualTo("댓글1");
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

    private Product createProduct(String name, User seller) {
        Product product = Product.createProduct(
                name,
                "https://example.com/image.jpg",
                10000,
                100,
                new ArrayList<>(),
                seller
        );
        return productRepository.save(product);
    }

    private ProductReview createProductReview(User user, Product product, BigDecimal rating, String content) {
        ProductReview productReview = ProductReview.createProductReview(user, product, rating, content);
        return productReviewRepository.save(productReview);
    }

    private ProductReviewComment createProductReviewComment(String content, ProductReview productReview, User user) {
        return ProductReviewComment.createProductReviewComment(content, productReview, user);
    }

    private ProductReviewComment createAndSaveProductReviewComment(String content, ProductReview productReview, User user) {
        ProductReviewComment comment = ProductReviewComment.createProductReviewComment(content, productReview, user);
        return productReviewCommentRepository.save(comment);
    }
}