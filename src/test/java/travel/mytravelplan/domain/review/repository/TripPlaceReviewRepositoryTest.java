package travel.mytravelplan.domain.review.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.place.entity.TripPlace;
import travel.mytravelplan.domain.place.enums.PlaceCategory;
import travel.mytravelplan.domain.place.repository.PlaceRepository;
import travel.mytravelplan.domain.review.entity.TripPlaceReview;
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

@DisplayName("여행 장소 리뷰 레포지토리 테스트")
class TripPlaceReviewRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private TripPlaceReviewRepository tripPlaceReviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Test
    @DisplayName("여행 장소 리뷰를 저장하고 조회할 수 있다")
    void save_and_findById() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");

        TripPlaceReview review = createTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "좋은 카페였습니다");
        TripPlaceReview savedReview = tripPlaceReviewRepository.save(review);

        em.flush();
        em.clear();

        // when
        Optional<TripPlaceReview> foundReview = tripPlaceReviewRepository.findById(savedReview.getId());

        // then
        assertThat(foundReview).isPresent();
        assertThat(foundReview.get().getContent()).isEqualTo("좋은 카페였습니다");
        assertThat(foundReview.get().getRating()).isEqualByComparingTo(new BigDecimal("4.5"));
        assertThat(foundReview.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(foundReview.get().getTripPlace().getId()).isEqualTo(tripPlace.getId());
    }

    @Test
    @DisplayName("여행 장소 리뷰를 수정할 수 있다")
    void update() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");

        TripPlaceReview review = createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "좋은 카페였습니다");
        Long reviewId = review.getId();

        em.flush();
        em.clear();

        // when
        TripPlaceReview foundReview = tripPlaceReviewRepository.findById(reviewId).orElseThrow();
        foundReview.update("정말 좋은 카페였습니다", new BigDecimal("5.0"));

        em.flush();
        em.clear();

        // then
        TripPlaceReview updatedReview = tripPlaceReviewRepository.findById(reviewId).orElseThrow();
        assertThat(updatedReview.getContent()).isEqualTo("정말 좋은 카페였습니다");
        assertThat(updatedReview.getRating()).isEqualByComparingTo(new BigDecimal("5.0"));
    }

    @Test
    @DisplayName("여행 장소 리뷰를 삭제할 수 있다")
    void delete() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");

        TripPlaceReview review = createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "좋은 카페였습니다");
        Long reviewId = review.getId();

        em.flush();
        em.clear();

        // when
        TripPlaceReview foundReview = tripPlaceReviewRepository.findById(reviewId).orElseThrow();
        tripPlaceReviewRepository.delete(foundReview);

        em.flush();
        em.clear();

        // then
        Optional<TripPlaceReview> deletedReview = tripPlaceReviewRepository.findById(reviewId);
        assertThat(deletedReview).isEmpty();
    }

    @Test
    @DisplayName("특정 여행 장소의 리뷰를 조회할 수 있다")
    void findAllByCursor_byTripPlaceId() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");
        TripPlace tripPlace1 = createTripPlace("제주 카페", "제주시 어딘가");
        TripPlace tripPlace2 = createTripPlace("서울 카페", "서울시 어딘가");

        createAndSaveTripPlaceReview(user1, tripPlace1, new BigDecimal("4.5"), "좋은 카페");
        createAndSaveTripPlaceReview(user2, tripPlace1, new BigDecimal("5.0"), "최고의 카페");
        createAndSaveTripPlaceReview(user1, tripPlace2, new BigDecimal("3.5"), "그냥 그래요");

        em.flush();
        em.clear();

        // when
        List<TripPlaceReview> reviews = tripPlaceReviewRepository.findAllByCursor(
                tripPlace1.getId(),
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
                .extracting(TripPlaceReview::getContent)
                .containsExactlyInAnyOrder("좋은 카페", "최고의 카페");
    }

    @Test
    @DisplayName("키워드로 리뷰를 검색할 수 있다")
    void findAllByCursor_withKeyword() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");

        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "커피가 맛있어요");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("5.0"), "디저트가 좋아요");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("3.5"), "커피는 별로");

        em.flush();
        em.clear();

        // when
        List<TripPlaceReview> reviews = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
                "커피",
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
                .extracting(TripPlaceReview::getContent)
                .containsExactlyInAnyOrder("커피가 맛있어요", "커피는 별로");
    }

    @Test
    @DisplayName("이미지가 있는 리뷰만 조회할 수 있다")
    void findAllByCursor_withImgOnly() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");

        TripPlaceReview reviewWithImage = createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "이미지 있음");
        reviewWithImage.getImageUrls().add("https://example.com/image1.jpg");

        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("5.0"), "이미지 없음");

        em.flush();
        em.clear();

        // when
        List<TripPlaceReview> reviews = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
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
        assertThat(reviews.get(0).getContent()).isEqualTo("이미지 있음");
        assertThat(reviews.get(0).getImageUrls()).isNotEmpty();
    }

    @Test
    @DisplayName("평점으로 리뷰를 필터링할 수 있다")
    void findAllByCursor_withRating() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");

        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "좋아요");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("5.0"), "최고예요");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("5.0"), "완벽해요");

        em.flush();
        em.clear();

        // when
        List<TripPlaceReview> reviews = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
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
                .extracting(TripPlaceReview::getContent)
                .containsExactlyInAnyOrder("최고예요", "완벽해요");
        assertThat(reviews)
                .allMatch(review -> review.getRating().compareTo(new BigDecimal("5.0")) == 0);
    }

    @Test
    @DisplayName("생성일 기준 내림차순으로 리뷰를 조회한다")
    void findAllByCursor_orderByCreatedAtDesc() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");

        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "첫 번째 리뷰");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("5.0"), "두 번째 리뷰");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("3.5"), "세 번째 리뷰");

        em.flush();
        em.clear();

        // when
        List<TripPlaceReview> reviews = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
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
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");

        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "첫 번째 리뷰");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("5.0"), "두 번째 리뷰");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("3.5"), "세 번째 리뷰");

        em.flush();
        em.clear();

        // when
        List<TripPlaceReview> reviews = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
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
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");

        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "4.5점 리뷰");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("5.0"), "5.0점 리뷰");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("3.5"), "3.5점 리뷰");

        em.flush();
        em.clear();

        // when
        List<TripPlaceReview> reviews = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
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
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");

        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "4.5점 리뷰");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("5.0"), "5.0점 리뷰");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("3.5"), "3.5점 리뷰");

        em.flush();
        em.clear();

        // when
        List<TripPlaceReview> reviews = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
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
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");

        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "리뷰 1");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("5.0"), "리뷰 2");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("3.5"), "리뷰 3");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.0"), "리뷰 4");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "리뷰 5");

        em.flush();
        em.clear();

        // when
        List<TripPlaceReview> reviews = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
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
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");

        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "리뷰 1");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("5.0"), "리뷰 2");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("3.5"), "리뷰 3");

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<TripPlaceReview> firstPage = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
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
        TripPlaceReview lastReview = firstPage.get(firstPage.size() - 1);
        List<TripPlaceReview> secondPage = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
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
        assertThat(secondPage.get(0).getContent()).isEqualTo("리뷰 1");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 리뷰를 조회한다 - 생성일 오름차순")
    void findAllByCursor_withCursor_createdAtAsc() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");

        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "리뷰 1");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("5.0"), "리뷰 2");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("3.5"), "리뷰 3");

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<TripPlaceReview> firstPage = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
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
        TripPlaceReview lastReview = firstPage.get(firstPage.size() - 1);
        List<TripPlaceReview> secondPage = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
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
        assertThat(secondPage.get(0).getContent()).isEqualTo("리뷰 3");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 리뷰를 조회한다 - 평점 내림차순")
    void findAllByCursor_withCursor_ratingDesc() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");

        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("3.5"), "3.5점 리뷰");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "4.5점 리뷰");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("5.0"), "5.0점 리뷰");

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<TripPlaceReview> firstPage = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
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
        TripPlaceReview lastReview = firstPage.get(firstPage.size() - 1);
        List<TripPlaceReview> secondPage = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
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
        assertThat(secondPage.get(0).getRating()).isEqualByComparingTo(new BigDecimal("3.5"));
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 리뷰를 조회한다 - 평점 오름차순")
    void findAllByCursor_withCursor_ratingAsc() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");

        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("3.5"), "3.5점 리뷰");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "4.5점 리뷰");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("5.0"), "5.0점 리뷰");

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<TripPlaceReview> firstPage = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
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
        TripPlaceReview lastReview = firstPage.get(firstPage.size() - 1);
        List<TripPlaceReview> secondPage = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
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
        assertThat(secondPage.get(0).getRating()).isEqualByComparingTo(new BigDecimal("5.0"));
    }

    @Test
    @DisplayName("여러 조건을 조합하여 리뷰를 조회할 수 있다")
    void findAllByCursor_withMultipleConditions() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");

        TripPlaceReview review1 = createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("5.0"), "커피가 정말 맛있어요");
        review1.getImageUrls().add("https://example.com/image1.jpg");

        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("5.0"), "디저트가 좋아요");

        TripPlaceReview review3 = createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("5.0"), "커피 향이 좋네요");
        review3.getImageUrls().add("https://example.com/image3.jpg");

        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.0"), "커피가 괜찮아요");

        em.flush();
        em.clear();

        // when - 5.0점, 이미지 있는, 키워드 '커피' 포함 리뷰 조회
        List<TripPlaceReview> reviews = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
                "커피",
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
                .allMatch(review -> review.getContent().contains("커피"))
                .allMatch(review -> !review.getImageUrls().isEmpty());
    }

    @Test
    @DisplayName("Branch Coverage - cursor와 after가 모두 null인 경우")
    void findAllByCursor_cursorNull_afterNull() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");

        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "리뷰 1");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("5.0"), "리뷰 2");

        em.flush();
        em.clear();

        // when - cursor와 after 모두 null
        List<TripPlaceReview> reviews = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
                null,
                null,
                null,
                "createdAt",
                "desc",
                null,  // cursor = null
                null,  // after = null
                10
        );

        // then - 커서 조건 없이 전체 조회
        assertThat(reviews).hasSize(2);
    }

    @Test
    @DisplayName("Branch Coverage - cursor는 not null, after는 null인 경우")
    void findAllByCursor_cursorNotNull_afterNull() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");

        TripPlaceReview review1 = createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "리뷰 1");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("5.0"), "리뷰 2");

        em.flush();
        em.clear();

        // when - cursor는 not null, after는 null (조건 불만족)
        List<TripPlaceReview> reviews = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
                null,
                null,
                null,
                "createdAt",
                "desc",
                review1.getCreatedAt().toString(),  // cursor = not null
                null,  // after = null
                10
        );

        // then - if 조건을 만족하지 못하므로 커서 조건 없이 전체 조회
        assertThat(reviews).hasSize(2);
    }

    @Test
    @DisplayName("Branch Coverage - cursor는 null, after는 not null인 경우")
    void findAllByCursor_cursorNull_afterNotNull() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");

        TripPlaceReview review1 = createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "리뷰 1");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("5.0"), "리뷰 2");

        em.flush();
        em.clear();

        // when - cursor는 null, after는 not null (조건 불만족)
        List<TripPlaceReview> reviews = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
                null,
                null,
                null,
                "createdAt",
                "desc",
                null,  // cursor = null
                review1.getId(),  // after = not null
                10
        );

        // then - if 조건을 만족하지 못하므로 커서 조건 없이 전체 조회
        assertThat(reviews).hasSize(2);
    }

    @Test
    @DisplayName("Branch Coverage - cursor와 after가 모두 not null인 경우 (생성일 기준)")
    void findAllByCursor_cursorNotNull_afterNotNull_createdAt() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");

        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "리뷰 1");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("5.0"), "리뷰 2");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("3.5"), "리뷰 3");

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<TripPlaceReview> firstPage = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
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

        // when - cursor와 after 모두 not null로 두 번째 페이지 조회
        TripPlaceReview lastReview = firstPage.get(firstPage.size() - 1);
        List<TripPlaceReview> secondPage = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
                null,
                null,
                null,
                "createdAt",
                "desc",
                lastReview.getCreatedAt().toString(),  // cursor = not null
                lastReview.getId(),  // after = not null
                2
        );

        // then - if 조건을 만족하여 커서 이후 데이터 조회
        assertThat(secondPage).hasSize(1);
    }

    @Test
    @DisplayName("Branch Coverage - cursor와 after가 모두 not null인 경우 (평점 기준)")
    void findAllByCursor_cursorNotNull_afterNotNull_rating() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");

        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("3.5"), "3.5점 리뷰");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "4.5점 리뷰");
        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("5.0"), "5.0점 리뷰");

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<TripPlaceReview> firstPage = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
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

        // when - cursor와 after 모두 not null로 두 번째 페이지 조회
        TripPlaceReview lastReview = firstPage.get(firstPage.size() - 1);
        List<TripPlaceReview> secondPage = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
                null,
                null,
                null,
                "rating",
                "desc",
                lastReview.getRating().toString(),  // cursor = not null
                lastReview.getId(),  // after = not null
                2
        );

        // then - if 조건을 만족하여 커서 이후 데이터 조회
        assertThat(secondPage).hasSize(1);
    }

    @Test
    @DisplayName("Branch Coverage - tripPlaceId가 null인 경우 (모든 여행지의 리뷰 조회)")
    void findAllByCursor_tripPlaceIdNull() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace1 = createTripPlace("제주 카페", "제주시 어딘가");
        TripPlace tripPlace2 = createTripPlace("서울 카페", "서울시 어딘가");

        createAndSaveTripPlaceReview(user, tripPlace1, new BigDecimal("4.5"), "제주 카페 리뷰");
        createAndSaveTripPlaceReview(user, tripPlace2, new BigDecimal("5.0"), "서울 카페 리뷰");

        em.flush();
        em.clear();

        // when - tripPlaceId = null (조건 불만족, 모든 장소의 리뷰 조회)
        List<TripPlaceReview> reviews = tripPlaceReviewRepository.findAllByCursor(
                null,  // tripPlaceId = null
                null,
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then - if 조건을 만족하지 못하므로 모든 장소의 리뷰 조회
        assertThat(reviews).hasSize(2);
    }

    @Test
    @DisplayName("Branch Coverage - tripPlaceId가 not null인 경우 (특정 여행지의 리뷰만 조회)")
    void findAllByCursor_tripPlaceIdNotNull() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace1 = createTripPlace("제주 카페", "제주시 어딘가");
        TripPlace tripPlace2 = createTripPlace("서울 카페", "서울시 어딘가");

        createAndSaveTripPlaceReview(user, tripPlace1, new BigDecimal("4.5"), "제주 카페 리뷰");
        createAndSaveTripPlaceReview(user, tripPlace2, new BigDecimal("5.0"), "서울 카페 리뷰");

        em.flush();
        em.clear();

        // when - tripPlaceId = not null (조건 만족, 특정 장소의 리뷰만 조회)
        List<TripPlaceReview> reviews = tripPlaceReviewRepository.findAllByCursor(
                tripPlace1.getId(),  // tripPlaceId = not null
                null,
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then - if 조건을 만족하여 tripPlace1의 리뷰만 조회
        assertThat(reviews).hasSize(1);
        assertThat(reviews.get(0).getContent()).isEqualTo("제주 카페 리뷰");
        assertThat(reviews.get(0).getTripPlace().getId()).isEqualTo(tripPlace1.getId());
    }

    @Test
    @DisplayName("Branch Coverage - imgOnly가 null인 경우 (이미지 유무 무관하게 조회)")
    void findAllByCursor_imgOnlyNull() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");

        TripPlaceReview reviewWithImage = createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "이미지 있음");
        reviewWithImage.getImageUrls().add("https://example.com/image1.jpg");

        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("5.0"), "이미지 없음");

        em.flush();
        em.clear();

        // when - imgOnly = null (조건 불만족, 이미지 유무 무관하게 조회)
        List<TripPlaceReview> reviews = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
                null,
                null,  // imgOnly = null
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then - if 조건을 만족하지 못하므로 이미지 유무와 상관없이 모든 리뷰 조회
        assertThat(reviews).hasSize(2);
    }

    @Test
    @DisplayName("Branch Coverage - imgOnly가 false인 경우 (이미지 유무 무관하게 조회)")
    void findAllByCursor_imgOnlyFalse() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");

        TripPlaceReview reviewWithImage = createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "이미지 있음");
        reviewWithImage.getImageUrls().add("https://example.com/image1.jpg");

        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("5.0"), "이미지 없음");

        em.flush();
        em.clear();

        // when - imgOnly = false (조건 불만족, 이미지 유무 무관하게 조회)
        List<TripPlaceReview> reviews = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
                null,
                false,  // imgOnly = false
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then - if 조건을 만족하지 못하므로 이미지 유무와 상관없이 모든 리뷰 조회
        assertThat(reviews).hasSize(2);
    }

    @Test
    @DisplayName("Branch Coverage - imgOnly가 true인 경우 (이미지 있는 리뷰만 조회)")
    void findAllByCursor_imgOnlyTrue() {
        // given
        User user = createUser("testUser", "test@email.com");
        TripPlace tripPlace = createTripPlace("제주 카페", "제주시 어딘가");

        TripPlaceReview reviewWithImage = createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("4.5"), "이미지 있음");
        reviewWithImage.getImageUrls().add("https://example.com/image1.jpg");

        createAndSaveTripPlaceReview(user, tripPlace, new BigDecimal("5.0"), "이미지 없음");

        em.flush();
        em.clear();

        // when - imgOnly = true (조건 만족, 이미지 있는 리뷰만 조회)
        List<TripPlaceReview> reviews = tripPlaceReviewRepository.findAllByCursor(
                tripPlace.getId(),
                null,
                true,  // imgOnly = true
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then - if 조건을 만족하여 이미지가 있는 리뷰만 조회
        assertThat(reviews).hasSize(1);
        assertThat(reviews.get(0).getContent()).isEqualTo("이미지 있음");
        assertThat(reviews.get(0).getImageUrls()).isNotEmpty();
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

    private TripPlaceReview createTripPlaceReview(User user, TripPlace tripPlace, BigDecimal rating, String content) {
        return TripPlaceReview.createTripPlaceReview(user, tripPlace, rating, content);
    }

    private TripPlaceReview createAndSaveTripPlaceReview(User user, TripPlace tripPlace, BigDecimal rating, String content) {
        TripPlaceReview review = TripPlaceReview.createTripPlaceReview(user, tripPlace, rating, content);
        return tripPlaceReviewRepository.save(review);
    }
}