package travel.mytravelplan.domain.diary.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.diary.entity.Diary;
import travel.mytravelplan.domain.diary.enums.Emotion;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.entity.TripJoin;
import travel.mytravelplan.domain.trip.enums.Country;
import travel.mytravelplan.domain.trip.repository.TripJoinRepository;
import travel.mytravelplan.domain.trip.repository.TripRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.support.RepositoryTestSupport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("일기 레포지토리 테스트")
class DiaryRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripJoinRepository tripJoinRepository;

    @Test
    @DisplayName("일기를 저장하고 조회할 수 있다")
    void save_and_findById() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("제주도 여행");
        TripJoin tripJoin = createTripJoin(trip, user);

        Diary diary = createDiary("여행 첫째 날", "제주도에 도착했다", trip, tripJoin);
        Diary savedDiary = diaryRepository.save(diary);

        em.flush();
        em.clear();

        // when
        Optional<Diary> foundDiary = diaryRepository.findById(savedDiary.getId());

        // then
        assertThat(foundDiary).isPresent();
        assertThat(foundDiary.get().getTitle()).isEqualTo("여행 첫째 날");
        assertThat(foundDiary.get().getContent()).isEqualTo("제주도에 도착했다");
        assertThat(foundDiary.get().getEmotion()).isEqualTo(Emotion.HAPPY);
    }

    @Test
    @DisplayName("특정 여행의 모든 일기를 조회할 수 있다")
    void findAll_byTrip() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip1 = createTrip("제주도 여행");
        Trip trip2 = createTrip("부산 여행");
        TripJoin tripJoin1 = createTripJoin(trip1, user);
        TripJoin tripJoin2 = createTripJoin(trip2, user);

        createAndSaveDiary("제주도 첫째 날", "제주도 도착", trip1, tripJoin1);
        createAndSaveDiary("제주도 둘째 날", "한라산 등반", trip1, tripJoin1);
        createAndSaveDiary("부산 첫째 날", "해운대 방문", trip2, tripJoin2);

        em.flush();
        em.clear();

        // when
        List<Diary> diaries = diaryRepository.findAll();
        List<Diary> trip1Diaries = diaries.stream()
                .filter(d -> d.getTrip().getId().equals(trip1.getId()))
                .toList();

        // then
        assertThat(trip1Diaries).hasSize(2);
        assertThat(trip1Diaries)
                .extracting(Diary::getTitle)
                .containsExactlyInAnyOrder("제주도 첫째 날", "제주도 둘째 날");
    }

    @Test
    @DisplayName("일기를 수정할 수 있다")
    void update() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("제주도 여행");
        TripJoin tripJoin = createTripJoin(trip, user);

        Diary diary = createAndSaveDiary("여행 첫째 날", "제주도에 도착했다", trip, tripJoin);
        Long diaryId = diary.getId();

        em.flush();
        em.clear();

        // when
        Diary foundDiary = diaryRepository.findById(diaryId).orElseThrow();
        foundDiary.update(
                "여행 첫째 날 수정",
                "제주도에 도착했다 - 날씨가 좋았다",
                List.of("https://example.com/image2.jpg"),
                LocalDate.of(2024, 1, 2),
                Emotion.EXCITED
        );

        em.flush();
        em.clear();

        // then
        Diary updatedDiary = diaryRepository.findById(diaryId).orElseThrow();
        assertThat(updatedDiary.getTitle()).isEqualTo("여행 첫째 날 수정");
        assertThat(updatedDiary.getContent()).isEqualTo("제주도에 도착했다 - 날씨가 좋았다");
        assertThat(updatedDiary.getEmotion()).isEqualTo(Emotion.EXCITED);
        assertThat(updatedDiary.getImageUrls()).containsExactly("https://example.com/image2.jpg");
    }

    @Test
    @DisplayName("일기를 삭제할 수 있다")
    void delete() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("제주도 여행");
        TripJoin tripJoin = createTripJoin(trip, user);

        Diary diary = createAndSaveDiary("여행 첫째 날", "제주도에 도착했다", trip, tripJoin);
        Long diaryId = diary.getId();

        em.flush();
        em.clear();

        // when
        Diary foundDiary = diaryRepository.findById(diaryId).orElseThrow();
        diaryRepository.delete(foundDiary);

        em.flush();
        em.clear();

        // then
        Optional<Diary> deletedDiary = diaryRepository.findById(diaryId);
        assertThat(deletedDiary).isEmpty();
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 일기 목록을 조회할 수 있다")
    void findAllByCursor_pagination() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("제주도 여행");
        TripJoin tripJoin = createTripJoin(trip, user);

        createAndSaveDiary("일기 1", "내용 1", trip, tripJoin);
        createAndSaveDiary("일기 2", "내용 2", trip, tripJoin);
        createAndSaveDiary("일기 3", "내용 3", trip, tripJoin);

        em.flush();
        em.clear();

        // when
        List<Diary> diaries = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(diaries).hasSize(3);
        assertThat(diaries)
                .extracting(Diary::getTitle)
                .containsExactly("일기 3", "일기 2", "일기 1");
    }

    @Test
    @DisplayName("키워드로 일기를 검색할 수 있다")
    void findAllByCursor_withKeyword() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("제주도 여행");
        TripJoin tripJoin = createTripJoin(trip, user);

        createAndSaveDiary("제주도 첫째 날", "한라산 등반", trip, tripJoin);
        createAndSaveDiary("제주도 둘째 날", "해변 산책", trip, tripJoin);
        createAndSaveDiary("제주도 셋째 날", "한라산 하산", trip, tripJoin);

        em.flush();
        em.clear();

        // when
        List<Diary> diaries = diaryRepository.findAllByCursor(
                trip.getId(),
                "한라산",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(diaries).hasSize(2);
        assertThat(diaries)
                .extracting(Diary::getContent)
                .containsExactlyInAnyOrder("한라산 등반", "한라산 하산");
    }

    @Test
    @DisplayName("여행 참여자별로 일기를 조회할 수 있다")
    void findAll_byTripJoin() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");
        Trip trip = createTrip("제주도 여행");
        TripJoin tripJoin1 = createTripJoin(trip, user1);
        TripJoin tripJoin2 = createTripJoin(trip, user2);

        createAndSaveDiary("user1의 일기 1", "내용 1", trip, tripJoin1);
        createAndSaveDiary("user1의 일기 2", "내용 2", trip, tripJoin1);
        createAndSaveDiary("user2의 일기 1", "내용 3", trip, tripJoin2);

        em.flush();
        em.clear();

        // when
        List<Diary> allDiaries = diaryRepository.findAll();
        List<Diary> user1Diaries = allDiaries.stream()
                .filter(d -> d.getTripJoin().getId().equals(tripJoin1.getId()))
                .toList();

        // then
        assertThat(user1Diaries).hasSize(2);
        assertThat(user1Diaries)
                .extracting(Diary::getTitle)
                .containsExactlyInAnyOrder("user1의 일기 1", "user1의 일기 2");
    }

    @Test
    @DisplayName("감정별로 일기를 필터링할 수 있다")
    void findAll_byEmotion() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("제주도 여행");
        TripJoin tripJoin = createTripJoin(trip, user);

        createAndSaveDiary("행복한 날", "즐거웠다", Emotion.HAPPY, trip, tripJoin);
        createAndSaveDiary("피곤한 날", "많이 걸었다", Emotion.TIRED, trip, tripJoin);
        createAndSaveDiary("신나는 날", "액티비티 했다", Emotion.EXCITED, trip, tripJoin);

        em.flush();
        em.clear();

        // when
        List<Diary> allDiaries = diaryRepository.findAll();
        List<Diary> happyDiaries = allDiaries.stream()
                .filter(d -> d.getEmotion() == Emotion.HAPPY)
                .toList();

        // then
        assertThat(happyDiaries).hasSize(1);
        assertThat(happyDiaries.getFirst().getTitle()).isEqualTo("행복한 날");
    }

    @Test
    @DisplayName("날짜 범위로 일기를 조회할 수 있다")
    void findAll_byDateRange() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("제주도 여행");
        TripJoin tripJoin = createTripJoin(trip, user);

        createAndSaveDiaryWithDate("1월 1일", LocalDate.of(2024, 1, 1), trip, tripJoin);
        createAndSaveDiaryWithDate("1월 5일", LocalDate.of(2024, 1, 5), trip, tripJoin);
        createAndSaveDiaryWithDate("1월 10일", LocalDate.of(2024, 1, 10), trip, tripJoin);

        em.flush();
        em.clear();

        // when
        List<Diary> allDiaries = diaryRepository.findAll();
        List<Diary> filteredDiaries = allDiaries.stream()
                .filter(d -> !d.getDate().isBefore(LocalDate.of(2024, 1, 3)))
                .filter(d -> !d.getDate().isAfter(LocalDate.of(2024, 1, 8)))
                .toList();

        // then
        assertThat(filteredDiaries).hasSize(1);
        assertThat(filteredDiaries.getFirst().getTitle()).isEqualTo("1월 5일");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션 - 오름차순으로 일기를 조회할 수 있다")
    void findAllByCursor_orderByAsc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("제주도 여행");
        TripJoin tripJoin = createTripJoin(trip, user);

        createAndSaveDiary("일기 1", "내용 1", trip, tripJoin);
        createAndSaveDiary("일기 2", "내용 2", trip, tripJoin);
        createAndSaveDiary("일기 3", "내용 3", trip, tripJoin);

        em.flush();
        em.clear();

        // when
        List<Diary> diaries = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(diaries).hasSize(3);
        assertThat(diaries)
                .extracting(Diary::getTitle)
                .containsExactly("일기 1", "일기 2", "일기 3");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션 - limit을 적용하여 일기를 조회할 수 있다")
    void findAllByCursor_withLimit() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("제주도 여행");
        TripJoin tripJoin = createTripJoin(trip, user);

        createAndSaveDiary("일기 1", "내용 1", trip, tripJoin);
        createAndSaveDiary("일기 2", "내용 2", trip, tripJoin);
        createAndSaveDiary("일기 3", "내용 3", trip, tripJoin);
        createAndSaveDiary("일기 4", "내용 4", trip, tripJoin);
        createAndSaveDiary("일기 5", "내용 5", trip, tripJoin);

        em.flush();
        em.clear();

        // when
        List<Diary> diaries = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                3
        );

        // then
        assertThat(diaries).hasSize(3);
        assertThat(diaries)
                .extracting(Diary::getTitle)
                .containsExactly("일기 5", "일기 4", "일기 3");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션 - 커서와 after를 사용하여 다음 페이지를 조회할 수 있다 (desc)")
    void findAllByCursor_withCursorAndAfter_desc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("제주도 여행");
        TripJoin tripJoin = createTripJoin(trip, user);

        Diary diary1 = createAndSaveDiary("일기 1", "내용 1", trip, tripJoin);
        Diary diary2 = createAndSaveDiary("일기 2", "내용 2", trip, tripJoin);
        Diary diary3 = createAndSaveDiary("일기 3", "내용 3", trip, tripJoin);
        Diary diary4 = createAndSaveDiary("일기 4", "내용 4", trip, tripJoin);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Diary> firstPage = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then - 첫 페이지 검증
        assertThat(firstPage).hasSize(2);

        // when - 두 번째 페이지 조회
        Diary lastDiary = firstPage.get(firstPage.size() - 1);
        List<Diary> secondPage = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                lastDiary.getCreatedAt().toString(),
                lastDiary.getId(),
                2
        );

        // then - 두 번째 페이지 검증
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage)
                .extracting(Diary::getTitle)
                .containsExactly("일기 2", "일기 1");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션 - 커서와 after를 사용하여 다음 페이지를 조회할 수 있다 (asc)")
    void findAllByCursor_withCursorAndAfter_asc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("제주도 여행");
        TripJoin tripJoin = createTripJoin(trip, user);

        createAndSaveDiary("일기 1", "내용 1", trip, tripJoin);
        createAndSaveDiary("일기 2", "내용 2", trip, tripJoin);
        createAndSaveDiary("일기 3", "내용 3", trip, tripJoin);
        createAndSaveDiary("일기 4", "내용 4", trip, tripJoin);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Diary> firstPage = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then - 첫 페이지 검증
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage)
                .extracting(Diary::getTitle)
                .containsExactly("일기 1", "일기 2");

        // when - 두 번째 페이지 조회
        Diary lastDiary = firstPage.get(firstPage.size() - 1);
        List<Diary> secondPage = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                lastDiary.getCreatedAt().toString(),
                lastDiary.getId(),
                2
        );

        // then - 두 번째 페이지 검증
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage)
                .extracting(Diary::getTitle)
                .containsExactly("일기 3", "일기 4");
    }

    @Test
    @DisplayName("제목에서 키워드로 일기를 검색할 수 있다")
    void findAllByCursor_searchByTitleKeyword() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("제주도 여행");
        TripJoin tripJoin = createTripJoin(trip, user);

        createAndSaveDiary("제주도 맛집 탐방", "오늘은 흑돼지를 먹었다", trip, tripJoin);
        createAndSaveDiary("한라산 등반 일기", "한라산에 올랐다", trip, tripJoin);
        createAndSaveDiary("제주도 카페 투어", "예쁜 카페를 방문했다", trip, tripJoin);

        em.flush();
        em.clear();

        // when
        List<Diary> diaries = diaryRepository.findAllByCursor(
                trip.getId(),
                "제주도",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(diaries).hasSize(2);
        assertThat(diaries)
                .extracting(Diary::getTitle)
                .containsExactlyInAnyOrder("제주도 맛집 탐방", "제주도 카페 투어");
    }

    @Test
    @DisplayName("내용에서 키워드로 일기를 검색할 수 있다")
    void findAllByCursor_searchByContentKeyword() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("제주도 여행");
        TripJoin tripJoin = createTripJoin(trip, user);

        createAndSaveDiary("첫째 날", "제주도에 도착했다", trip, tripJoin);
        createAndSaveDiary("둘째 날", "바다를 구경했다", trip, tripJoin);
        createAndSaveDiary("셋째 날", "제주도 특산물을 샀다", trip, tripJoin);

        em.flush();
        em.clear();

        // when
        List<Diary> diaries = diaryRepository.findAllByCursor(
                trip.getId(),
                "제주도",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(diaries).hasSize(2);
        assertThat(diaries)
                .extracting(Diary::getTitle)
                .containsExactlyInAnyOrder("첫째 날", "셋째 날");
    }

    @Test
    @DisplayName("대소문자 구분 없이 키워드로 일기를 검색할 수 있다")
    void findAllByCursor_searchKeywordIgnoreCase() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("제주도 여행");
        TripJoin tripJoin = createTripJoin(trip, user);

        createAndSaveDiary("JEJU Travel", "Beautiful island", trip, tripJoin);
        createAndSaveDiary("Jeju Food", "Delicious food", trip, tripJoin);
        createAndSaveDiary("Tokyo Trip", "Nice city", trip, tripJoin);

        em.flush();
        em.clear();

        // when
        List<Diary> diaries = diaryRepository.findAllByCursor(
                trip.getId(),
                "jeju",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(diaries).hasSize(2);
        assertThat(diaries)
                .extracting(Diary::getTitle)
                .containsExactlyInAnyOrder("JEJU Travel", "Jeju Food");
    }

    @Test
    @DisplayName("tripId가 null이면 모든 여행의 일기를 조회할 수 있다")
    void findAllByCursor_withoutTripId() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip1 = createTrip("제주도 여행");
        Trip trip2 = createTrip("부산 여행");
        TripJoin tripJoin1 = createTripJoin(trip1, user);
        TripJoin tripJoin2 = createTripJoin(trip2, user);

        createAndSaveDiary("제주도 일기", "제주도 내용", trip1, tripJoin1);
        createAndSaveDiary("부산 일기", "부산 내용", trip2, tripJoin2);

        em.flush();
        em.clear();

        // when
        List<Diary> diaries = diaryRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(diaries).hasSize(2);
        assertThat(diaries)
                .extracting(Diary::getTitle)
                .containsExactlyInAnyOrder("제주도 일기", "부산 일기");
    }

    @Test
    @DisplayName("키워드가 일치하는 일기가 없으면 빈 리스트를 반환한다")
    void findAllByCursor_noMatchingKeyword() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("제주도 여행");
        TripJoin tripJoin = createTripJoin(trip, user);

        createAndSaveDiary("제주도 일기", "제주도 내용", trip, tripJoin);
        createAndSaveDiary("한라산 일기", "한라산 내용", trip, tripJoin);

        em.flush();
        em.clear();

        // when
        List<Diary> diaries = diaryRepository.findAllByCursor(
                trip.getId(),
                "부산",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(diaries).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 tripId로 조회하면 빈 리스트를 반환한다")
    void findAllByCursor_nonExistentTripId() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("제주도 여행");
        TripJoin tripJoin = createTripJoin(trip, user);

        createAndSaveDiary("제주도 일기", "제주도 내용", trip, tripJoin);

        em.flush();
        em.clear();

        // when
        List<Diary> diaries = diaryRepository.findAllByCursor(
                999L,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(diaries).isEmpty();
    }

    @Test
    @DisplayName("복합 검색 - tripId와 키워드를 함께 사용하여 일기를 검색할 수 있다")
    void findAllByCursor_withTripIdAndKeyword() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip1 = createTrip("제주도 여행");
        Trip trip2 = createTrip("부산 여행");
        TripJoin tripJoin1 = createTripJoin(trip1, user);
        TripJoin tripJoin2 = createTripJoin(trip2, user);

        createAndSaveDiary("제주도 맛집", "제주도 흑돼지", trip1, tripJoin1);
        createAndSaveDiary("제주도 관광", "한라산 등반", trip1, tripJoin1);
        createAndSaveDiary("부산 맛집", "부산 해산물", trip2, tripJoin2);

        em.flush();
        em.clear();

        // when
        List<Diary> diaries = diaryRepository.findAllByCursor(
                trip1.getId(),
                "맛집",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(diaries).hasSize(1);
        assertThat(diaries.getFirst().getTitle()).isEqualTo("제주도 맛집");
    }

    @Test
    @DisplayName("여러 이미지 URL을 가진 일기를 저장하고 조회할 수 있다")
    void save_and_findById_withMultipleImages() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("제주도 여행");
        TripJoin tripJoin = createTripJoin(trip, user);

        Diary diary = Diary.createDiary(
                "여행 첫째 날",
                "제주도에 도착했다",
                List.of(
                        "https://example.com/image1.jpg",
                        "https://example.com/image2.jpg",
                        "https://example.com/image3.jpg"
                ),
                LocalDate.of(2024, 1, 1),
                Emotion.HAPPY,
                trip,
                tripJoin
        );
        Diary savedDiary = diaryRepository.save(diary);

        em.flush();
        em.clear();

        // when
        Optional<Diary> foundDiary = diaryRepository.findById(savedDiary.getId());

        // then
        assertThat(foundDiary).isPresent();
        assertThat(foundDiary.get().getImageUrls()).hasSize(3);
        assertThat(foundDiary.get().getImageUrls()).containsExactly(
                "https://example.com/image1.jpg",
                "https://example.com/image2.jpg",
                "https://example.com/image3.jpg"
        );
    }

    @Test
    @DisplayName("이미지가 없는 일기를 저장하고 조회할 수 있다")
    void save_and_findById_withoutImages() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("제주도 여행");
        TripJoin tripJoin = createTripJoin(trip, user);

        Diary diary = Diary.createDiary(
                "여행 첫째 날",
                "제주도에 도착했다",
                List.of(),
                LocalDate.of(2024, 1, 1),
                Emotion.HAPPY,
                trip,
                tripJoin
        );
        Diary savedDiary = diaryRepository.save(diary);

        em.flush();
        em.clear();

        // when
        Optional<Diary> foundDiary = diaryRepository.findById(savedDiary.getId());

        // then
        assertThat(foundDiary).isPresent();
        assertThat(foundDiary.get().getImageUrls()).isEmpty();
    }

    @Test
    @DisplayName("모든 감정 타입의 일기를 저장하고 조회할 수 있다")
    void save_and_findById_allEmotionTypes() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("제주도 여행");
        TripJoin tripJoin = createTripJoin(trip, user);

        createAndSaveDiary("행복한 날", "즐거웠다", Emotion.HAPPY, trip, tripJoin);
        createAndSaveDiary("슬픈 날", "아쉬웠다", Emotion.SAD, trip, tripJoin);
        createAndSaveDiary("신나는 날", "흥미진진했다", Emotion.EXCITED, trip, tripJoin);
        createAndSaveDiary("피곤한 날", "많이 걸었다", Emotion.TIRED, trip, tripJoin);
        createAndSaveDiary("평화로운 날", "조용했다", Emotion.PEACEFUL, trip, tripJoin);

        em.flush();
        em.clear();

        // when
        List<Diary> diaries = diaryRepository.findAll();

        // then
        assertThat(diaries).hasSize(5);
        assertThat(diaries)
                .extracting(Diary::getEmotion)
                .containsExactlyInAnyOrder(
                        Emotion.HAPPY,
                        Emotion.SAD,
                        Emotion.EXCITED,
                        Emotion.TIRED,
                        Emotion.PEACEFUL
                );
    }

    @Test
    @DisplayName("cursor가 null이지만 after가 있는 경우 - after는 무시된다")
    void findAllByCursor_withNullCursorButAfterExists() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("커서 null 테스트");
        TripJoin tripJoin = createTripJoin(trip, user);

        createAndSaveDiary("일기 1", "내용 1", trip, tripJoin);
        createAndSaveDiary("일기 2", "내용 2", trip, tripJoin);
        createAndSaveDiary("일기 3", "내용 3", trip, tripJoin);

        em.flush();
        em.clear();

        // when - cursor가 null이고 after만 있는 경우
        List<Diary> diaries = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                null,
                999L,
                10
        );

        // then - cursor가 null이므로 모든 데이터 조회
        assertThat(diaries).hasSize(3);
        assertThat(diaries)
                .extracting(Diary::getTitle)
                .containsExactly("일기 3", "일기 2", "일기 1");
    }

    @Test
    @DisplayName("cursor만 있고 after가 null인 경우 - 조건이 적용되지 않는다")
    void findAllByCursor_withCursorButNullAfter() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("after null 테스트");
        TripJoin tripJoin = createTripJoin(trip, user);

        createAndSaveDiary("일기 1", "내용 1", trip, tripJoin);
        createAndSaveDiary("일기 2", "내용 2", trip, tripJoin);
        createAndSaveDiary("일기 3", "내용 3", trip, tripJoin);

        em.flush();
        em.clear();

        // when - cursor는 있지만 after가 null인 경우
        List<Diary> diaries = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                LocalDate.now().toString(),
                null,
                10
        );

        // then - after가 null이므로 모든 데이터 조회
        assertThat(diaries).hasSize(3);
    }

    @Test
    @DisplayName("cursor와 after가 모두 null인 경우 - 처음부터 조회한다")
    void findAllByCursor_withBothCursorAndAfterNull() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("둘 다 null 테스트");
        TripJoin tripJoin = createTripJoin(trip, user);

        createAndSaveDiary("일기 1", "내용 1", trip, tripJoin);
        createAndSaveDiary("일기 2", "내용 2", trip, tripJoin);
        createAndSaveDiary("일기 3", "내용 3", trip, tripJoin);

        em.flush();
        em.clear();

        // when - cursor와 after가 모두 null인 경우
        List<Diary> diaries = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then - 처음부터 모든 데이터 조회
        assertThat(diaries).hasSize(3);
        assertThat(diaries)
                .extracting(Diary::getTitle)
                .containsExactly("일기 3", "일기 2", "일기 1");
    }

    @Test
    @DisplayName("cursor와 after가 모두 있는 경우 - desc 정렬 페이지네이션")
    void findAllByCursor_withBothCursorAndAfter_descPagination() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("둘 다 있는 경우 desc");
        TripJoin tripJoin = createTripJoin(trip, user);

        createAndSaveDiary("일기 1", "내용 1", trip, tripJoin);
        createAndSaveDiary("일기 2", "내용 2", trip, tripJoin);
        createAndSaveDiary("일기 3", "내용 3", trip, tripJoin);
        createAndSaveDiary("일기 4", "내용 4", trip, tripJoin);
        createAndSaveDiary("일기 5", "내용 5", trip, tripJoin);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Diary> firstPage = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then - 첫 페이지 검증
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage)
                .extracting(Diary::getTitle)
                .containsExactly("일기 5", "일기 4");

        // when - cursor와 after가 모두 있는 경우 두 번째 페이지 조회
        Diary lastDiary = firstPage.getLast();
        List<Diary> secondPage = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                lastDiary.getCreatedAt().toString(),
                lastDiary.getId(),
                2
        );

        // then - 두 번째 페이지 검증 (cursor와 after가 모두 적용됨)
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage)
                .extracting(Diary::getTitle)
                .containsExactly("일기 3", "일기 2");
    }

    @Test
    @DisplayName("cursor와 after가 모두 있는 경우 - asc 정렬 페이지네이션")
    void findAllByCursor_withBothCursorAndAfter_ascPagination() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("둘 다 있는 경우 asc");
        TripJoin tripJoin = createTripJoin(trip, user);

        createAndSaveDiary("일기 1", "내용 1", trip, tripJoin);
        createAndSaveDiary("일기 2", "내용 2", trip, tripJoin);
        createAndSaveDiary("일기 3", "내용 3", trip, tripJoin);
        createAndSaveDiary("일기 4", "내용 4", trip, tripJoin);
        createAndSaveDiary("일기 5", "내용 5", trip, tripJoin);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Diary> firstPage = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then - 첫 페이지 검증
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage)
                .extracting(Diary::getTitle)
                .containsExactly("일기 1", "일기 2");

        // when - cursor와 after가 모두 있는 경우 두 번째 페이지 조회
        Diary lastDiary = firstPage.getLast();
        List<Diary> secondPage = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                lastDiary.getCreatedAt().toString(),
                lastDiary.getId(),
                2
        );

        // then - 두 번째 페이지 검증 (cursor와 after가 모두 적용됨)
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage)
                .extracting(Diary::getTitle)
                .containsExactly("일기 3", "일기 4");
    }

    @Test
    @DisplayName("cursor와 after가 모두 있고 키워드 검색을 함께 사용하는 경우")
    void findAllByCursor_withBothCursorAndAfterAndKeyword() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("복합 조건 테스트");
        TripJoin tripJoin = createTripJoin(trip, user);

        createAndSaveDiary("제주도 일기 1", "제주도 내용 1", trip, tripJoin);
        createAndSaveDiary("제주도 일기 2", "제주도 내용 2", trip, tripJoin);
        createAndSaveDiary("부산 일기", "부산 내용", trip, tripJoin);
        createAndSaveDiary("제주도 일기 3", "제주도 내용 3", trip, tripJoin);
        createAndSaveDiary("제주도 일기 4", "제주도 내용 4", trip, tripJoin);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회 (키워드 포함)
        List<Diary> firstPage = diaryRepository.findAllByCursor(
                trip.getId(),
                "제주도",
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then - 첫 페이지 검증
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage)
                .extracting(Diary::getTitle)
                .containsExactly("제주도 일기 4", "제주도 일기 3");

        // when - cursor, after, 키워드가 모두 있는 경우
        Diary lastDiary = firstPage.getLast();
        List<Diary> secondPage = diaryRepository.findAllByCursor(
                trip.getId(),
                "제주도",
                "createdAt",
                "desc",
                lastDiary.getCreatedAt().toString(),
                lastDiary.getId(),
                2
        );

        // then - 두 번째 페이지 검증
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage)
                .extracting(Diary::getTitle)
                .containsExactly("제주도 일기 2", "제주도 일기 1");
    }

    @Test
    @DisplayName("cursor와 after가 모두 있지만 조회 결과가 없는 경우")
    void findAllByCursor_withBothCursorAndAfterButNoResults() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("결과 없음 테스트");
        TripJoin tripJoin = createTripJoin(trip, user);

        createAndSaveDiary("일기 1", "내용 1", trip, tripJoin);
        createAndSaveDiary("일기 2", "내용 2", trip, tripJoin);

        em.flush();
        em.clear();

        // when - 먼저 모든 데이터 조회
        List<Diary> allDiaries = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then - 2개의 데이터가 조회됨
        assertThat(allDiaries).hasSize(2);

        // when - 마지막 일기의 cursor와 after를 사용하여 다음 페이지 조회
        Diary lastDiary = allDiaries.getLast();
        List<Diary> nextPage = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                lastDiary.getCreatedAt().toString(),
                lastDiary.getId(),
                10
        );

        // then - 더 이상 조회할 데이터가 없음
        assertThat(nextPage).isEmpty();
    }

    @Test
    @DisplayName("cursor와 after가 모두 있고 limit이 1인 경우")
    void findAllByCursor_withBothCursorAndAfterAndLimitOne() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("limit 1 테스트");
        TripJoin tripJoin = createTripJoin(trip, user);

        createAndSaveDiary("일기 1", "내용 1", trip, tripJoin);
        createAndSaveDiary("일기 2", "내용 2", trip, tripJoin);
        createAndSaveDiary("일기 3", "내용 3", trip, tripJoin);

        em.flush();
        em.clear();

        // when - 첫 페이지
        List<Diary> firstPage = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                1
        );

        // then
        assertThat(firstPage).hasSize(1);
        assertThat(firstPage.getFirst().getTitle()).isEqualTo("일기 3");

        // when - cursor와 after가 모두 있고 limit이 1
        Diary lastDiary = firstPage.getFirst();
        List<Diary> secondPage = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                lastDiary.getCreatedAt().toString(),
                lastDiary.getId(),
                1
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.getFirst().getTitle()).isEqualTo("일기 2");
    }

    @Test
    @DisplayName("cursor와 after가 모두 있고 여러 데이터 페이지네이션 - desc")
    void findAllByCursor_withBothCursorAndAfterMultipleData_desc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여러 데이터 정렬 테스트");
        TripJoin tripJoin = createTripJoin(trip, user);

        // createdAt이 다르도록 순차적으로 생성
        createAndSaveDiary("일기1", "내용1", trip, tripJoin);
        createAndSaveDiary("일기2", "내용2", trip, tripJoin);
        createAndSaveDiary("일기3", "내용3", trip, tripJoin);
        createAndSaveDiary("일기4", "내용4", trip, tripJoin);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회 (createdAt 기준 desc)
        List<Diary> firstPage = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage)
                .extracting(Diary::getTitle)
                .containsExactly("일기4", "일기3");

        // when - cursor와 after가 모두 있는 경우
        Diary lastDiary = firstPage.getLast();
        List<Diary> secondPage = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                lastDiary.getCreatedAt().toString(),
                lastDiary.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage)
                .extracting(Diary::getTitle)
                .containsExactly("일기2", "일기1");
    }

    @Test
    @DisplayName("cursor와 after가 모두 있고 여러 데이터 페이지네이션 - asc")
    void findAllByCursor_withBothCursorAndAfterMultipleData_asc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여러 데이터 정렬 asc 테스트");
        TripJoin tripJoin = createTripJoin(trip, user);

        // createdAt이 다르도록 순차적으로 생성
        createAndSaveDiary("일기1", "내용1", trip, tripJoin);
        createAndSaveDiary("일기2", "내용2", trip, tripJoin);
        createAndSaveDiary("일기3", "내용3", trip, tripJoin);
        createAndSaveDiary("일기4", "내용4", trip, tripJoin);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회 (createdAt 기준 asc)
        List<Diary> firstPage = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage)
                .extracting(Diary::getTitle)
                .containsExactly("일기1", "일기2");

        // when - cursor와 after가 모두 있는 경우
        Diary lastDiary = firstPage.getLast();
        List<Diary> secondPage = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                lastDiary.getCreatedAt().toString(),
                lastDiary.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage)
                .extracting(Diary::getTitle)
                .containsExactly("일기3", "일기4");
    }

    @Test
    @DisplayName("cursor와 after가 모두 있고 마지막 페이지인 경우 - 부분 결과 반환")
    void findAllByCursor_withBothCursorAndAfterLastPagePartialResults() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("마지막 페이지 테스트");
        TripJoin tripJoin = createTripJoin(trip, user);

        createAndSaveDiary("일기1", "내용1", trip, tripJoin);
        createAndSaveDiary("일기2", "내용2", trip, tripJoin);
        createAndSaveDiary("일기3", "내용3", trip, tripJoin);
        createAndSaveDiary("일기4", "내용4", trip, tripJoin);
        createAndSaveDiary("일기5", "내용5", trip, tripJoin);

        em.flush();
        em.clear();

        // when - 첫 페이지
        List<Diary> firstPage = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                3
        );

        // then
        assertThat(firstPage).hasSize(3);

        // when - 마지막 페이지 (limit 3이지만 2개만 남음)
        Diary lastDiary = firstPage.getLast();
        List<Diary> lastPage = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                lastDiary.getCreatedAt().toString(),
                lastDiary.getId(),
                3
        );

        // then
        assertThat(lastPage).hasSize(2);
    }

    @Test
    @DisplayName("cursor와 after가 모두 있고 동일한 createdAt을 가진 데이터가 여러 개인 경우")
    void findAllByCursor_withBothCursorAndAfterSameCreatedAt() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("동일 시간 테스트");
        TripJoin tripJoin = createTripJoin(trip, user);

        // 동일한 시간에 여러 일기 생성 (실제로는 milliseconds 차이가 있을 수 있음)
        createAndSaveDiary("일기1", "내용1", trip, tripJoin);
        createAndSaveDiary("일기2", "내용2", trip, tripJoin);
        createAndSaveDiary("일기3", "내용3", trip, tripJoin);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Diary> firstPage = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                1
        );

        // then
        assertThat(firstPage).hasSize(1);

        // when - after를 사용하여 다음 페이지 조회 (동일 시간에 대한 처리)
        Diary lastDiary = firstPage.getFirst();
        List<Diary> secondPage = diaryRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                lastDiary.getCreatedAt().toString(),
                lastDiary.getId(),
                1
        );

        // then - after로 인해 다음 데이터가 조회됨
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.getFirst().getId()).isNotEqualTo(firstPage.getFirst().getId());
    }

    @Test
    @DisplayName("cursor와 after가 모두 있고 여러 조건이 복합된 경우 - tripId, 키워드, 정렬")
    void findAllByCursor_withAllConditions() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("복합 조건 전체 테스트");
        TripJoin tripJoin = createTripJoin(trip, user);

        createAndSaveDiary("제주도 여행 1일차", "한라산", trip, tripJoin);
        createAndSaveDiary("제주도 여행 2일차", "성산일출봉", trip, tripJoin);
        createAndSaveDiary("부산 여행", "해운대", trip, tripJoin);
        createAndSaveDiary("제주도 여행 3일차", "우도", trip, tripJoin);
        createAndSaveDiary("제주도 여행 4일차", "섭지코지", trip, tripJoin);

        em.flush();
        em.clear();

        // when - 첫 페이지 (tripId, 키워드, 정렬 모두 포함)
        List<Diary> firstPage = diaryRepository.findAllByCursor(
                trip.getId(),
                "제주도",
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage)
                .extracting(Diary::getTitle)
                .allMatch(title -> title.contains("제주도"));

        // when - cursor, after, tripId, 키워드, 정렬 모두 포함
        Diary lastDiary = firstPage.getLast();
        List<Diary> secondPage = diaryRepository.findAllByCursor(
                trip.getId(),
                "제주도",
                "createdAt",
                "desc",
                lastDiary.getCreatedAt().toString(),
                lastDiary.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage)
                .extracting(Diary::getTitle)
                .allMatch(title -> title.contains("제주도"));
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

    private Trip createTrip(String title) {
        Trip trip = Trip.createTrip(
                title,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 10),
                "https://example.com/image.jpg",
                Set.of(Country.JP)
        );
        return tripRepository.save(trip);
    }

    private TripJoin createTripJoin(Trip trip, User user) {
        TripJoin tripJoin = TripJoin.createTripJoin(trip, user);
        return tripJoinRepository.save(tripJoin);
    }

    private Diary createDiary(String title, String content, Trip trip, TripJoin tripJoin) {
        return Diary.createDiary(
                title,
                content,
                List.of("https://example.com/image.jpg"),
                LocalDate.of(2024, 1, 1),
                Emotion.HAPPY,
                trip,
                tripJoin
        );
    }

    private Diary createDiary(String title, String content, Emotion emotion, Trip trip, TripJoin tripJoin) {
        return Diary.createDiary(
                title,
                content,
                List.of("https://example.com/image.jpg"),
                LocalDate.of(2024, 1, 1),
                emotion,
                trip,
                tripJoin
        );
    }

    private Diary createAndSaveDiary(String title, String content, Trip trip, TripJoin tripJoin) {
        Diary diary = createDiary(title, content, trip, tripJoin);
        return diaryRepository.save(diary);
    }

    private Diary createAndSaveDiary(String title, String content, Emotion emotion, Trip trip, TripJoin tripJoin) {
        Diary diary = createDiary(title, content, emotion, trip, tripJoin);
        return diaryRepository.save(diary);
    }

    private Diary createAndSaveDiaryWithDate(String title, LocalDate date, Trip trip, TripJoin tripJoin) {
        Diary diary = Diary.createDiary(
                title,
                "내용",
                List.of("https://example.com/image.jpg"),
                date,
                Emotion.HAPPY,
                trip,
                tripJoin
        );
        return diaryRepository.save(diary);
    }
}