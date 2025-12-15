package travel.mytravelplan.domain.trip.repository;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.entity.TripJoin;
import travel.mytravelplan.domain.trip.enums.Country;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.support.RepositoryTestSupport;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("여행 레포지토리 테스트")
public class TripRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripJoinRepository tripJoinRepository;

    @Test
    @DisplayName("여행을 저장한다")
    void saveTrip() {
        // given
        Trip trip = createTrip("제주도 여행", LocalDate.of(2024, 7, 1), LocalDate.of(2024, 7, 5));

        // when
        Trip savedTrip = tripRepository.save(trip);
        em.flush();
        em.clear();

        // then
        assertThat(savedTrip.getId()).isNotNull();
        assertThat(savedTrip.getTitle()).isEqualTo("제주도 여행");
        assertThat(savedTrip.getStartDate()).isEqualTo(LocalDate.of(2024, 7, 1));
        assertThat(savedTrip.getEndDate()).isEqualTo(LocalDate.of(2024, 7, 5));
    }

    @Test
    @DisplayName("여행을 ID로 조회한다")
    void findTripById() {
        // given
        Trip trip = createTrip("일본 여행", LocalDate.of(2024, 8, 1), LocalDate.of(2024, 8, 10));
        Trip savedTrip = tripRepository.save(trip);
        em.flush();
        em.clear();

        // when
        Trip foundTrip = tripRepository.findById(savedTrip.getId()).orElse(null);

        // then
        assertThat(foundTrip).isNotNull();
        assertThat(foundTrip.getId()).isEqualTo(savedTrip.getId());
        assertThat(foundTrip.getTitle()).isEqualTo("일본 여행");
    }

    @Test
    @DisplayName("여행을 수정한다")
    void updateTrip() {
        // given
        Trip trip = createTrip("유럽 여행", LocalDate.of(2024, 9, 1), LocalDate.of(2024, 9, 15));
        Trip savedTrip = tripRepository.save(trip);
        em.flush();
        em.clear();

        // when
        Trip foundTrip = tripRepository.findById(savedTrip.getId()).orElseThrow();
        foundTrip.update(
                "수정된 유럽 여행",
                LocalDate.of(2024, 9, 5),
                LocalDate.of(2024, 9, 20),
                "https://example.com/updated-image.jpg",
                Set.of(Country.FR, Country.IT)
        );
        em.flush();
        em.clear();

        // then
        Trip updatedTrip = tripRepository.findById(savedTrip.getId()).orElse(null);
        assertThat(updatedTrip).isNotNull();
        assertThat(updatedTrip.getTitle()).isEqualTo("수정된 유럽 여행");
        assertThat(updatedTrip.getStartDate()).isEqualTo(LocalDate.of(2024, 9, 5));
        assertThat(updatedTrip.getEndDate()).isEqualTo(LocalDate.of(2024, 9, 20));
        assertThat(updatedTrip.getCountries()).containsExactlyInAnyOrder(Country.FR, Country.IT);
    }

    @Test
    @DisplayName("여행을 삭제한다")
    void deleteTrip() {
        // given
        Trip trip = createTrip("삭제할 여행", LocalDate.of(2024, 10, 1), LocalDate.of(2024, 10, 5));
        Trip savedTrip = tripRepository.save(trip);
        em.flush();
        em.clear();

        // when
        tripRepository.deleteById(savedTrip.getId());
        em.flush();
        em.clear();

        // then
        Trip deletedTrip = tripRepository.findById(savedTrip.getId()).orElse(null);
        assertThat(deletedTrip).isNull();
    }

    @Test
    @DisplayName("사용자 이름으로 여행을 조회한다")
    void findAllByUserCursor_byUsername() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");

        Trip trip1 = createAndSaveTrip("여행1", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 5));
        Trip trip2 = createAndSaveTrip("여행2", LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 5));
        Trip trip3 = createAndSaveTrip("여행3", LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 5));

        createTripJoin(trip1, user1);
        createTripJoin(trip2, user1);
        createTripJoin(trip3, user2);

        em.flush();
        em.clear();

        // when
        List<Trip> trips = tripRepository.findAllByUserCursor(
                "user1",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(trips).hasSize(2);
        assertThat(trips)
                .extracting(Trip::getTitle)
                .containsExactlyInAnyOrder("여행1", "여행2");
    }

    @Test
    @DisplayName("생성일 기준 내림차순으로 여행을 조회한다")
    void findAllByUserCursor_orderByCreatedAtDesc() {
        // given
        User user = createUser("testUser", "test@email.com");

        Trip trip1 = createAndSaveTrip("여행1", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 5));
        Trip trip2 = createAndSaveTrip("여행2", LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 5));
        Trip trip3 = createAndSaveTrip("여행3", LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 5));

        createTripJoin(trip1, user);
        createTripJoin(trip2, user);
        createTripJoin(trip3, user);

        em.flush();
        em.clear();

        // when
        List<Trip> trips = tripRepository.findAllByUserCursor(
                "testUser",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(trips).hasSize(3);
        assertThat(trips.get(0).getTitle()).isEqualTo("여행3");
        assertThat(trips.get(1).getTitle()).isEqualTo("여행2");
        assertThat(trips.get(2).getTitle()).isEqualTo("여행1");
    }

    @Test
    @DisplayName("생성일 기준 오름차순으로 여행을 조회한다")
    void findAllByUserCursor_orderByCreatedAtAsc() {
        // given
        User user = createUser("testUser", "test@email.com");

        Trip trip1 = createAndSaveTrip("여행1", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 5));
        Trip trip2 = createAndSaveTrip("여행2", LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 5));
        Trip trip3 = createAndSaveTrip("여행3", LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 5));

        createTripJoin(trip1, user);
        createTripJoin(trip2, user);
        createTripJoin(trip3, user);

        em.flush();
        em.clear();

        // when
        List<Trip> trips = tripRepository.findAllByUserCursor(
                "testUser",
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(trips).hasSize(3);
        assertThat(trips.get(0).getTitle()).isEqualTo("여행1");
        assertThat(trips.get(1).getTitle()).isEqualTo("여행2");
        assertThat(trips.get(2).getTitle()).isEqualTo("여행3");
    }

    @Test
    @DisplayName("시작일 기준 내림차순으로 여행을 조회한다")
    void findAllByUserCursor_orderByStartDateDesc() {
        // given
        User user = createUser("testUser", "test@email.com");

        Trip trip1 = createAndSaveTrip("여행1", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 5));
        Trip trip2 = createAndSaveTrip("여행2", LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 5));
        Trip trip3 = createAndSaveTrip("여행3", LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 5));

        createTripJoin(trip1, user);
        createTripJoin(trip2, user);
        createTripJoin(trip3, user);

        em.flush();
        em.clear();

        // when
        List<Trip> trips = tripRepository.findAllByUserCursor(
                "testUser",
                "startDate",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(trips).hasSize(3);
        assertThat(trips.get(0).getTitle()).isEqualTo("여행2");
        assertThat(trips.get(1).getTitle()).isEqualTo("여행3");
        assertThat(trips.get(2).getTitle()).isEqualTo("여행1");
    }

    @Test
    @DisplayName("시작일 기준 오름차순으로 여행을 조회한다")
    void findAllByUserCursor_orderByStartDateAsc() {
        // given
        User user = createUser("testUser", "test@email.com");

        Trip trip1 = createAndSaveTrip("여행1", LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 5));
        Trip trip2 = createAndSaveTrip("여행2", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 5));
        Trip trip3 = createAndSaveTrip("여행3", LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 5));

        createTripJoin(trip1, user);
        createTripJoin(trip2, user);
        createTripJoin(trip3, user);

        em.flush();
        em.clear();

        // when
        List<Trip> trips = tripRepository.findAllByUserCursor(
                "testUser",
                "startDate",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(trips).hasSize(3);
        assertThat(trips.get(0).getTitle()).isEqualTo("여행2");
        assertThat(trips.get(1).getTitle()).isEqualTo("여행3");
        assertThat(trips.get(2).getTitle()).isEqualTo("여행1");
    }

    @Test
    @DisplayName("limit 개수만큼 여행을 조회한다")
    void findAllByUserCursor_withLimit() {
        // given
        User user = createUser("testUser", "test@email.com");

        Trip trip1 = createAndSaveTrip("여행1", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 5));
        Trip trip2 = createAndSaveTrip("여행2", LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 5));
        Trip trip3 = createAndSaveTrip("여행3", LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 5));
        Trip trip4 = createAndSaveTrip("여행4", LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 5));
        Trip trip5 = createAndSaveTrip("여행5", LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 5));

        createTripJoin(trip1, user);
        createTripJoin(trip2, user);
        createTripJoin(trip3, user);
        createTripJoin(trip4, user);
        createTripJoin(trip5, user);

        em.flush();
        em.clear();

        // when
        List<Trip> trips = tripRepository.findAllByUserCursor(
                "testUser",
                "createdAt",
                "desc",
                null,
                null,
                3
        );

        // then
        assertThat(trips).hasSize(3);
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 여행을 조회한다 - 내림차순")
    void findAllByUserCursor_withCursor_desc() {
        // given
        User user = createUser("testUser", "test@email.com");

        Trip trip1 = createAndSaveTrip("여행1", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 5));
        Trip trip2 = createAndSaveTrip("여행2", LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 5));
        Trip trip3 = createAndSaveTrip("여행3", LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 5));

        createTripJoin(trip1, user);
        createTripJoin(trip2, user);
        createTripJoin(trip3, user);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Trip> firstPage = tripRepository.findAllByUserCursor(
                "testUser",
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getTitle()).isEqualTo("여행3");
        assertThat(firstPage.get(1).getTitle()).isEqualTo("여행2");

        // when - 두 번째 페이지 조회
        Trip lastTrip = firstPage.getLast();
        List<Trip> secondPage = tripRepository.findAllByUserCursor(
                "testUser",
                "createdAt",
                "desc",
                lastTrip.getCreatedAt().toString(),
                lastTrip.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.getFirst().getTitle()).isEqualTo("여행1");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 여행을 조회한다 - 오름차순")
    void findAllByUserCursor_withCursor_asc() {
        // given
        User user = createUser("testUser", "test@email.com");

        Trip trip1 = createAndSaveTrip("여행1", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 5));
        Trip trip2 = createAndSaveTrip("여행2", LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 5));
        Trip trip3 = createAndSaveTrip("여행3", LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 5));

        createTripJoin(trip1, user);
        createTripJoin(trip2, user);
        createTripJoin(trip3, user);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Trip> firstPage = tripRepository.findAllByUserCursor(
                "testUser",
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getTitle()).isEqualTo("여행1");
        assertThat(firstPage.get(1).getTitle()).isEqualTo("여행2");

        // when - 두 번째 페이지 조회
        Trip lastTrip = firstPage.getLast();
        List<Trip> secondPage = tripRepository.findAllByUserCursor(
                "testUser",
                "createdAt",
                "asc",
                lastTrip.getCreatedAt().toString(),
                lastTrip.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.getFirst().getTitle()).isEqualTo("여행3");
    }

    @Test
    @DisplayName("여러 국가를 포함하는 여행을 저장하고 조회한다")
    void saveTripWithMultipleCountries() {
        // given
        Trip trip = Trip.createTrip(
                "유럽 여행",
                LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 6, 20),
                "https://example.com/europe.jpg",
                Set.of(Country.FR, Country.IT, Country.ES, Country.DE)
        );

        // when
        Trip savedTrip = tripRepository.save(trip);
        em.flush();
        em.clear();

        // then
        Trip foundTrip = tripRepository.findById(savedTrip.getId()).orElse(null);
        assertThat(foundTrip).isNotNull();
        assertThat(foundTrip.getCountries()).hasSize(4);
        assertThat(foundTrip.getCountries()).containsExactlyInAnyOrder(
                Country.FR, Country.IT, Country.ES, Country.DE
        );
    }

    @Test
    @DisplayName("이미지 URL이 있는 여행을 저장하고 조회한다")
    void saveTripWithImageUrl() {
        // given
        Trip trip = Trip.createTrip(
                "제주도 여행",
                LocalDate.of(2024, 7, 1),
                LocalDate.of(2024, 7, 5),
                "https://example.com/jeju-image.jpg",
                Set.of(Country.KR)
        );

        // when
        Trip savedTrip = tripRepository.save(trip);
        em.flush();
        em.clear();

        // then
        Trip foundTrip = tripRepository.findById(savedTrip.getId()).orElse(null);
        assertThat(foundTrip).isNotNull();
        assertThat(foundTrip.getImageUrl()).isEqualTo("https://example.com/jeju-image.jpg");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 여행을 조회한다 - 시작일 내림차순")
    void findAllByUserCursor_withCursor_startDate_desc() {
        // given
        User user = createUser("testUser", "test@email.com");

        Trip trip1 = createAndSaveTrip("여행1", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 5));
        Trip trip2 = createAndSaveTrip("여행2", LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 5));
        Trip trip3 = createAndSaveTrip("여행3", LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 5));

        createTripJoin(trip1, user);
        createTripJoin(trip2, user);
        createTripJoin(trip3, user);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Trip> firstPage = tripRepository.findAllByUserCursor(
                "testUser",
                "startDate",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getTitle()).isEqualTo("여행3");
        assertThat(firstPage.get(1).getTitle()).isEqualTo("여행2");

        // when - 두 번째 페이지 조회
        Trip lastTrip = firstPage.getLast();
        List<Trip> secondPage = tripRepository.findAllByUserCursor(
                "testUser",
                "startDate",
                "desc",
                lastTrip.getStartDate().toString(),
                lastTrip.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.getFirst().getTitle()).isEqualTo("여행1");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 여행을 조회한다 - 시작일 오름차순")
    void findAllByUserCursor_withCursor_startDate_asc() {
        // given
        User user = createUser("testUser", "test@email.com");

        Trip trip1 = createAndSaveTrip("여행1", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 5));
        Trip trip2 = createAndSaveTrip("여행2", LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 5));
        Trip trip3 = createAndSaveTrip("여행3", LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 5));

        createTripJoin(trip1, user);
        createTripJoin(trip2, user);
        createTripJoin(trip3, user);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Trip> firstPage = tripRepository.findAllByUserCursor(
                "testUser",
                "startDate",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getTitle()).isEqualTo("여행1");
        assertThat(firstPage.get(1).getTitle()).isEqualTo("여행2");

        // when - 두 번째 페이지 조회
        Trip lastTrip = firstPage.getLast();
        List<Trip> secondPage = tripRepository.findAllByUserCursor(
                "testUser",
                "startDate",
                "asc",
                lastTrip.getStartDate().toString(),
                lastTrip.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.getFirst().getTitle()).isEqualTo("여행3");
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 여행을 조회하면 빈 리스트를 반환한다")
    void findAllByUserCursor_nonExistentUser() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createAndSaveTrip("여행1", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 5));
        createTripJoin(trip, user);

        em.flush();
        em.clear();

        // when
        List<Trip> trips = tripRepository.findAllByUserCursor(
                "nonExistentUser",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(trips).isEmpty();
    }

    @Test
    @DisplayName("username이 null일 때 모든 여행을 조회한다")
    void findAllByUserCursor_nullUsername() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");

        Trip trip1 = createAndSaveTrip("여행1", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 5));
        Trip trip2 = createAndSaveTrip("여행2", LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 5));

        createTripJoin(trip1, user1);
        createTripJoin(trip2, user2);

        em.flush();
        em.clear();

        // when
        List<Trip> trips = tripRepository.findAllByUserCursor(
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(trips).hasSize(2);
    }

    @Test
    @DisplayName("username이 빈 문자열일 때 모든 여행을 조회한다")
    void findAllByUserCursor_emptyUsername() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");

        Trip trip1 = createAndSaveTrip("여행1", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 5));
        Trip trip2 = createAndSaveTrip("여행2", LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 5));

        createTripJoin(trip1, user1);
        createTripJoin(trip2, user2);

        em.flush();
        em.clear();

        // when
        List<Trip> trips = tripRepository.findAllByUserCursor(
                "",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(trips).hasSize(2);
    }

    @Test
    @DisplayName("동일한 생성일을 가진 여행들을 ID로 정렬하여 조회한다")
    void findAllByUserCursor_sameCreatedAt() {
        // given
        User user = createUser("testUser", "test@email.com");

        Trip trip1 = createAndSaveTrip("여행1", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 5));
        Trip trip2 = createAndSaveTrip("여행2", LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 5));
        Trip trip3 = createAndSaveTrip("여행3", LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 5));

        createTripJoin(trip1, user);
        createTripJoin(trip2, user);
        createTripJoin(trip3, user);

        em.flush();
        em.clear();

        // when
        List<Trip> trips = tripRepository.findAllByUserCursor(
                "testUser",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(trips).hasSize(3);
        // ID 순서가 보장되어야 함
        for (int i = 0; i < trips.size() - 1; i++) {
            assertThat(trips.get(i).getId()).isGreaterThan(trips.get(i + 1).getId());
        }
    }

    @Test
    @DisplayName("동일한 시작일을 가진 여행들을 ID로 정렬하여 조회한다")
    void findAllByUserCursor_sameStartDate() {
        // given
        User user = createUser("testUser", "test@email.com");

        Trip trip1 = createAndSaveTrip("여행1", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 5));
        Trip trip2 = createAndSaveTrip("여행2", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10));
        Trip trip3 = createAndSaveTrip("여행3", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 15));

        createTripJoin(trip1, user);
        createTripJoin(trip2, user);
        createTripJoin(trip3, user);

        em.flush();
        em.clear();

        // when
        List<Trip> trips = tripRepository.findAllByUserCursor(
                "testUser",
                "startDate",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(trips).hasSize(3);
        // 모든 여행이 같은 시작일을 가지므로 ID 순서가 보장되어야 함
        for (int i = 0; i < trips.size() - 1; i++) {
            assertThat(trips.get(i).getId()).isGreaterThan(trips.get(i + 1).getId());
        }
    }

    @Test
    @DisplayName("국가 정보를 업데이트한다")
    void updateTripCountries() {
        // given
        Trip trip = createTrip("유럽 여행", LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 10));
        Trip savedTrip = tripRepository.save(trip);
        em.flush();
        em.clear();

        // when
        Trip foundTrip = tripRepository.findById(savedTrip.getId()).orElseThrow();
        foundTrip.update(
                "수정된 유럽 여행",
                LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 6, 10),
                "https://example.com/image.jpg",
                Set.of(Country.FR, Country.IT, Country.ES)
        );
        em.flush();
        em.clear();

        // then
        Trip updatedTrip = tripRepository.findById(savedTrip.getId()).orElse(null);
        assertThat(updatedTrip).isNotNull();
        assertThat(updatedTrip.getCountries()).hasSize(3);
        assertThat(updatedTrip.getCountries()).containsExactlyInAnyOrder(Country.FR, Country.IT, Country.ES);
    }

    @Test
    @DisplayName("이미지 URL을 업데이트한다")
    void updateTripImageUrl() {
        // given
        Trip trip = createTrip("제주도 여행", LocalDate.of(2024, 7, 1), LocalDate.of(2024, 7, 5));
        Trip savedTrip = tripRepository.save(trip);
        em.flush();
        em.clear();

        // when
        Trip foundTrip = tripRepository.findById(savedTrip.getId()).orElseThrow();
        foundTrip.update(
                "제주도 여행",
                LocalDate.of(2024, 7, 1),
                LocalDate.of(2024, 7, 5),
                "https://example.com/new-image.jpg",
                Set.of(Country.KR)
        );
        em.flush();
        em.clear();

        // then
        Trip updatedTrip = tripRepository.findById(savedTrip.getId()).orElse(null);
        assertThat(updatedTrip).isNotNull();
        assertThat(updatedTrip.getImageUrl()).isEqualTo("https://example.com/new-image.jpg");
    }

    @Test
    @DisplayName("여행 날짜만 업데이트한다")
    void updateTripDates() {
        // given
        Trip trip = createTrip("일본 여행", LocalDate.of(2024, 8, 1), LocalDate.of(2024, 8, 10));
        Trip savedTrip = tripRepository.save(trip);
        em.flush();
        em.clear();

        // when
        Trip foundTrip = tripRepository.findById(savedTrip.getId()).orElseThrow();
        foundTrip.update(
                "일본 여행",
                LocalDate.of(2024, 8, 5),
                LocalDate.of(2024, 8, 15),
                "https://example.com/image.jpg",
                Set.of(Country.KR)
        );
        em.flush();
        em.clear();

        // then
        Trip updatedTrip = tripRepository.findById(savedTrip.getId()).orElse(null);
        assertThat(updatedTrip).isNotNull();
        assertThat(updatedTrip.getStartDate()).isEqualTo(LocalDate.of(2024, 8, 5));
        assertThat(updatedTrip.getEndDate()).isEqualTo(LocalDate.of(2024, 8, 15));
    }

    @Test
    @DisplayName("여행 제목만 업데이트한다")
    void updateTripTitle() {
        // given
        Trip trip = createTrip("원래 제목", LocalDate.of(2024, 9, 1), LocalDate.of(2024, 9, 5));
        Trip savedTrip = tripRepository.save(trip);
        em.flush();
        em.clear();

        // when
        Trip foundTrip = tripRepository.findById(savedTrip.getId()).orElseThrow();
        foundTrip.update(
                "변경된 제목",
                LocalDate.of(2024, 9, 1),
                LocalDate.of(2024, 9, 5),
                "https://example.com/image.jpg",
                Set.of(Country.KR)
        );
        em.flush();
        em.clear();

        // then
        Trip updatedTrip = tripRepository.findById(savedTrip.getId()).orElse(null);
        assertThat(updatedTrip).isNotNull();
        assertThat(updatedTrip.getTitle()).isEqualTo("변경된 제목");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 여행을 조회하면 빈 Optional을 반환한다")
    void findTripById_notFound() {
        // when
        Trip foundTrip = tripRepository.findById(999999L).orElse(null);

        // then
        assertThat(foundTrip).isNull();
    }

    @Test
    @DisplayName("커서가 있지만 after가 null인 경우 정상적으로 조회한다")
    void findAllByUserCursor_cursorWithoutAfter() {
        // given
        User user = createUser("testUser", "test@email.com");

        Trip trip1 = createAndSaveTrip("여행1", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 5));
        Trip trip2 = createAndSaveTrip("여행2", LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 5));

        createTripJoin(trip1, user);
        createTripJoin(trip2, user);

        em.flush();
        em.clear();

        // when
        List<Trip> trips = tripRepository.findAllByUserCursor(
                "testUser",
                "createdAt",
                "desc",
                trip2.getCreatedAt().toString(),
                null,
                10
        );

        // then - cursor만 있고 after가 없으면 커서 조건이 적용되지 않음
        assertThat(trips).hasSize(2);
    }

    @Test
    @DisplayName("국가 정보가 빈 Set인 여행을 저장하고 조회한다")
    void saveTripWithEmptyCountries() {
        // given
        Trip trip = Trip.createTrip(
                "국가 미정 여행",
                LocalDate.of(2024, 10, 1),
                LocalDate.of(2024, 10, 5),
                "https://example.com/image.jpg",
                Set.of()
        );

        // when
        Trip savedTrip = tripRepository.save(trip);
        em.flush();
        em.clear();

        // then
        Trip foundTrip = tripRepository.findById(savedTrip.getId()).orElse(null);
        assertThat(foundTrip).isNotNull();
        assertThat(foundTrip.getCountries()).isEmpty();
    }

    @Test
    @DisplayName("이미지 URL이 null인 여행을 저장하고 조회한다")
    void saveTripWithNullImageUrl() {
        // given
        Trip trip = Trip.createTrip(
                "이미지 없는 여행",
                LocalDate.of(2024, 11, 1),
                LocalDate.of(2024, 11, 5),
                null,
                Set.of(Country.KR)
        );

        // when
        Trip savedTrip = tripRepository.save(trip);
        em.flush();
        em.clear();

        // then
        Trip foundTrip = tripRepository.findById(savedTrip.getId()).orElse(null);
        assertThat(foundTrip).isNotNull();
        assertThat(foundTrip.getImageUrl()).isNull();
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

    private Trip createTrip(String title, LocalDate startDate, LocalDate endDate) {
        return Trip.createTrip(
                title,
                startDate,
                endDate,
                "https://example.com/image.jpg",
                Set.of(Country.KR)
        );
    }

    private Trip createAndSaveTrip(String title, LocalDate startDate, LocalDate endDate) {
        Trip trip = createTrip(title, startDate, endDate);
        return tripRepository.save(trip);
    }

    private TripJoin createTripJoin(Trip trip, User user) {
        TripJoin tripJoin = TripJoin.createTripJoin(trip, user);
        return tripJoinRepository.save(tripJoin);
    }
}
