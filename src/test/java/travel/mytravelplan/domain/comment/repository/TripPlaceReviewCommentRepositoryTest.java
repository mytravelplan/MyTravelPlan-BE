package travel.mytravelplan.domain.comment.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.comment.entity.TripPlaceReviewComment;
import travel.mytravelplan.domain.place.entity.TripPlace;
import travel.mytravelplan.domain.place.enums.PlaceCategory;
import travel.mytravelplan.domain.place.repository.PlaceRepository;
import travel.mytravelplan.domain.review.entity.TripPlaceReview;
import travel.mytravelplan.domain.review.repository.TripPlaceReviewRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.support.RepositoryTestSupport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("여행 장소 리뷰 댓글 레포지토리 테스트")
class TripPlaceReviewCommentRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private TripPlaceReviewCommentRepository tripPlaceReviewCommentRepository;

    @Autowired
    private TripPlaceReviewRepository tripPlaceReviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Test
    @DisplayName("여행 장소 리뷰 댓글을 저장하고 조회할 수 있다")
    void save_and_findById() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");
        TripPlaceReview review = createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "좋은 카페였습니다");

        TripPlaceReviewComment comment = createTripPlaceReviewComment("좋은 리뷰 감사합니다", review, user);
        TripPlaceReviewComment savedComment = tripPlaceReviewCommentRepository.save(comment);

        em.flush();
        em.clear();

        // when
        Optional<TripPlaceReviewComment> foundComment = tripPlaceReviewCommentRepository.findById(savedComment.getId());

        // then
        assertThat(foundComment).isPresent();
        assertThat(foundComment.get().getContent()).isEqualTo("좋은 리뷰 감사합니다");
        assertThat(foundComment.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(foundComment.get().getTripPlaceReview().getId()).isEqualTo(review.getId());
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글을 수정할 수 있다")
    void update() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");
        TripPlaceReview review = createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "좋은 카페였습니다");

        TripPlaceReviewComment comment = createAndSaveTripPlaceReviewComment("좋은 리뷰 감사합니다", review, user);
        Long commentId = comment.getId();

        em.flush();
        em.clear();

        // when
        TripPlaceReviewComment foundComment = tripPlaceReviewCommentRepository.findById(commentId).orElseThrow();
        foundComment.update("정말 좋은 리뷰네요");

        em.flush();
        em.clear();

        // then
        TripPlaceReviewComment updatedComment = tripPlaceReviewCommentRepository.findById(commentId).orElseThrow();
        assertThat(updatedComment.getContent()).isEqualTo("정말 좋은 리뷰네요");
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글을 삭제할 수 있다")
    void delete() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");
        TripPlaceReview review = createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "좋은 카페였습니다");

        TripPlaceReviewComment comment = createAndSaveTripPlaceReviewComment("좋은 리뷰 감사합니다", review, user);
        Long commentId = comment.getId();

        em.flush();
        em.clear();

        // when
        TripPlaceReviewComment foundComment = tripPlaceReviewCommentRepository.findById(commentId).orElseThrow();
        tripPlaceReviewCommentRepository.delete(foundComment);

        em.flush();
        em.clear();

        // then
        Optional<TripPlaceReviewComment> deletedComment = tripPlaceReviewCommentRepository.findById(commentId);
        assertThat(deletedComment).isEmpty();
    }

    @Test
    @DisplayName("특정 리뷰의 댓글을 조회할 수 있다")
    void findAllByCursor_byTripPlaceReviewId() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");
        TripPlaceReview review1 = createAndSaveTripPlaceReview(user1, tripPlace, new BigDecimal("4.5"), "좋은 카페");
        TripPlaceReview review2 = createAndSaveTripPlaceReview(user2, tripPlace, new BigDecimal("5.0"), "최고의 카페");

        createAndSaveTripPlaceReviewComment("좋은 리뷰 감사합니다", review1, user2);
        createAndSaveTripPlaceReviewComment("공감합니다", review1, user1);
        createAndSaveTripPlaceReviewComment("다른 리뷰 댓글", review2, user1);

        em.flush();
        em.clear();

        // when
        List<TripPlaceReviewComment> comments = tripPlaceReviewCommentRepository.findAllByCursor(
                review1.getId(),
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
                .extracting(TripPlaceReviewComment::getContent)
                .containsExactlyInAnyOrder("좋은 리뷰 감사합니다", "공감합니다");
    }

    @Test
    @DisplayName("키워드로 댓글을 검색할 수 있다")
    void findAllByCursor_withKeyword() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");
        TripPlaceReview review = createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "좋은 카페");

        createAndSaveTripPlaceReviewComment("정말 좋은 리뷰네요", review, user);
        createAndSaveTripPlaceReviewComment("공감합니다", review, user);
        createAndSaveTripPlaceReviewComment("좋은 정보 감사합니다", review, user);

        em.flush();
        em.clear();

        // when
        List<TripPlaceReviewComment> comments = tripPlaceReviewCommentRepository.findAllByCursor(
                review.getId(),
                "좋은",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(comments).hasSize(2);
        assertThat(comments)
                .extracting(TripPlaceReviewComment::getContent)
                .containsExactlyInAnyOrder("정말 좋은 리뷰네요", "좋은 정보 감사합니다");
    }

    @Test
    @DisplayName("생성일 기준 내림차순으로 댓글을 조회한다")
    void findAllByCursor_orderByCreatedAtDesc() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");
        TripPlaceReview review = createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "좋은 카페");

        createAndSaveTripPlaceReviewComment("첫 번째 댓글", review, user);
        createAndSaveTripPlaceReviewComment("두 번째 댓글", review, user);
        createAndSaveTripPlaceReviewComment("세 번째 댓글", review, user);

        em.flush();
        em.clear();

        // when
        List<TripPlaceReviewComment> comments = tripPlaceReviewCommentRepository.findAllByCursor(
                review.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(comments).hasSize(3);
        assertThat(comments.get(0).getContent()).isEqualTo("세 번째 댓글");
        assertThat(comments.get(1).getContent()).isEqualTo("두 번째 댓글");
        assertThat(comments.get(2).getContent()).isEqualTo("첫 번째 댓글");
    }

    @Test
    @DisplayName("생성일 기준 오름차순으로 댓글을 조회한다")
    void findAllByCursor_orderByCreatedAtAsc() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");
        TripPlaceReview review = createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "좋은 카페");

        createAndSaveTripPlaceReviewComment("첫 번째 댓글", review, user);
        createAndSaveTripPlaceReviewComment("두 번째 댓글", review, user);
        createAndSaveTripPlaceReviewComment("세 번째 댓글", review, user);

        em.flush();
        em.clear();

        // when
        List<TripPlaceReviewComment> comments = tripPlaceReviewCommentRepository.findAllByCursor(
                review.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(comments).hasSize(3);
        assertThat(comments.get(0).getContent()).isEqualTo("첫 번째 댓글");
        assertThat(comments.get(1).getContent()).isEqualTo("두 번째 댓글");
        assertThat(comments.get(2).getContent()).isEqualTo("세 번째 댓글");
    }

    @Test
    @DisplayName("limit 개수만큼 댓글을 조회한다")
    void findAllByCursor_withLimit() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");
        TripPlaceReview review = createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "좋은 카페");

        createAndSaveTripPlaceReviewComment("댓글 1", review, user);
        createAndSaveTripPlaceReviewComment("댓글 2", review, user);
        createAndSaveTripPlaceReviewComment("댓글 3", review, user);
        createAndSaveTripPlaceReviewComment("댓글 4", review, user);
        createAndSaveTripPlaceReviewComment("댓글 5", review, user);

        em.flush();
        em.clear();

        // when
        List<TripPlaceReviewComment> comments = tripPlaceReviewCommentRepository.findAllByCursor(
                review.getId(),
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
    @DisplayName("커서 기반 페이지네이션으로 댓글을 조회한다 - 내림차순")
    void findAllByCursor_withCursor_desc() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");
        TripPlaceReview review = createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "좋은 카페");

        createAndSaveTripPlaceReviewComment("댓글 1", review, user);
        createAndSaveTripPlaceReviewComment("댓글 2", review, user);
        createAndSaveTripPlaceReviewComment("댓글 3", review, user);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<TripPlaceReviewComment> firstPage = tripPlaceReviewCommentRepository.findAllByCursor(
                review.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getContent()).isEqualTo("댓글 3");
        assertThat(firstPage.get(1).getContent()).isEqualTo("댓글 2");

        // when - 두 번째 페이지 조회
        TripPlaceReviewComment lastComment = firstPage.get(firstPage.size() - 1);
        List<TripPlaceReviewComment> secondPage = tripPlaceReviewCommentRepository.findAllByCursor(
                review.getId(),
                null,
                "createdAt",
                "desc",
                lastComment.getCreatedAt().toString(),
                lastComment.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getContent()).isEqualTo("댓글 1");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 댓글을 조회한다 - 오름차순")
    void findAllByCursor_withCursor_asc() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");
        TripPlaceReview review = createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "좋은 카페");

        createAndSaveTripPlaceReviewComment("댓글 1", review, user);
        createAndSaveTripPlaceReviewComment("댓글 2", review, user);
        createAndSaveTripPlaceReviewComment("댓글 3", review, user);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<TripPlaceReviewComment> firstPage = tripPlaceReviewCommentRepository.findAllByCursor(
                review.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getContent()).isEqualTo("댓글 1");
        assertThat(firstPage.get(1).getContent()).isEqualTo("댓글 2");

        // when - 두 번째 페이지 조회
        TripPlaceReviewComment lastComment = firstPage.get(firstPage.size() - 1);
        List<TripPlaceReviewComment> secondPage = tripPlaceReviewCommentRepository.findAllByCursor(
                review.getId(),
                null,
                "createdAt",
                "asc",
                lastComment.getCreatedAt().toString(),
                lastComment.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getContent()).isEqualTo("댓글 3");
    }

    @Test
    @DisplayName("여러 조건을 조합하여 댓글을 조회할 수 있다")
    void findAllByCursor_withMultipleConditions() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");
        TripPlaceReview review = createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "좋은 카페");

        createAndSaveTripPlaceReviewComment("정말 좋은 리뷰네요", review, user);
        createAndSaveTripPlaceReviewComment("공감합니다", review, user);
        createAndSaveTripPlaceReviewComment("좋은 정보 감사합니다", review, user);
        createAndSaveTripPlaceReviewComment("별로였어요", review, user);

        em.flush();
        em.clear();

        // when - 키워드 '좋은' 포함 댓글 조회
        List<TripPlaceReviewComment> comments = tripPlaceReviewCommentRepository.findAllByCursor(
                review.getId(),
                "좋은",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(comments).hasSize(2);
        assertThat(comments)
                .allMatch(comment -> comment.getContent().contains("좋은"));
    }

    @Test
    @DisplayName("cursor가 null이지만 after가 있는 경우 - after는 무시된다")
    void findAllByCursor_withNullCursorButAfterExists() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");
        TripPlaceReview review = createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "좋은 카페");

        createAndSaveTripPlaceReviewComment("댓글 1", review, user);
        createAndSaveTripPlaceReviewComment("댓글 2", review, user);
        createAndSaveTripPlaceReviewComment("댓글 3", review, user);

        em.flush();
        em.clear();

        // when
        List<TripPlaceReviewComment> comments = tripPlaceReviewCommentRepository.findAllByCursor(
                review.getId(),
                null,
                "createdAt",
                "asc",
                null,
                999L,
                10
        );

        // then - cursor가 null이므로 모든 데이터 조회
        assertThat(comments).hasSize(3);
    }

    @Test
    @DisplayName("cursor만 있고 after가 null인 경우 - 조건이 적용되지 않는다")
    void findAllByCursor_withCursorButNullAfter() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");
        TripPlaceReview review = createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "좋은 카페");

        TripPlaceReviewComment comment1 = createAndSaveTripPlaceReviewComment("댓글 1", review, user);
        createAndSaveTripPlaceReviewComment("댓글 2", review, user);
        createAndSaveTripPlaceReviewComment("댓글 3", review, user);

        em.flush();
        em.clear();

        // when
        List<TripPlaceReviewComment> comments = tripPlaceReviewCommentRepository.findAllByCursor(
                review.getId(),
                null,
                "createdAt",
                "asc",
                comment1.getCreatedAt().toString(),
                null,
                10
        );

        // then - after가 null이므로 모든 데이터 조회
        assertThat(comments).hasSize(3);
    }

    @Test
    @DisplayName("tripPlaceReviewId가 null인 경우 - 모든 리뷰의 댓글을 조회한다")
    void findAllByCursor_withNullTripPlaceReviewId() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");
        TripPlaceReview review1 = createAndSaveTripPlaceReview(user1, tripPlace, new BigDecimal("4.5"), "좋은 카페");
        TripPlaceReview review2 = createAndSaveTripPlaceReview(user2, tripPlace, new BigDecimal("5.0"), "최고의 카페");

        createAndSaveTripPlaceReviewComment("리뷰1의 댓글1", review1, user1);
        createAndSaveTripPlaceReviewComment("리뷰1의 댓글2", review1, user2);
        createAndSaveTripPlaceReviewComment("리뷰2의 댓글1", review2, user1);
        createAndSaveTripPlaceReviewComment("리뷰2의 댓글2", review2, user2);

        em.flush();
        em.clear();

        // when - tripPlaceReviewId가 null이므로 모든 댓글 조회
        List<TripPlaceReviewComment> comments = tripPlaceReviewCommentRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then - tripPlaceReviewId 필터링 없이 모든 댓글 조회
        assertThat(comments).hasSize(4);
        assertThat(comments)
                .extracting(TripPlaceReviewComment::getContent)
                .containsExactlyInAnyOrder("리뷰1의 댓글1", "리뷰1의 댓글2", "리뷰2의 댓글1", "리뷰2의 댓글2");
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

    private TripPlace createTripPlace(String name, String address) {
        TripPlace tripPlace = TripPlace.createTripPlace(
                name,
                address,
                "테스트 장소입니다",
                new BigDecimal("33.123456"),
                new BigDecimal("126.123456"),
                PlaceCategory.CAFE,
                "https://example.com"
        );
        return placeRepository.save(tripPlace);
    }

    private TripPlaceReview createAndSaveTripPlaceReview(User user, TripPlace tripPlace, BigDecimal rating, String content) {
        TripPlaceReview review = TripPlaceReview.createTripPlaceReview(user, tripPlace, rating, content);
        return tripPlaceReviewRepository.save(review);
    }

    private TripPlaceReviewComment createTripPlaceReviewComment(String content, TripPlaceReview review, User user) {
        return TripPlaceReviewComment.createTripPlaceReviewComment(content, review, user);
    }

    private TripPlaceReviewComment createAndSaveTripPlaceReviewComment(String content, TripPlaceReview review, User user) {
        TripPlaceReviewComment comment = TripPlaceReviewComment.createTripPlaceReviewComment(content, review, user);
        return tripPlaceReviewCommentRepository.save(comment);
    }
}