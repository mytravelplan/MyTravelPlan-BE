package travel.mytravelplan.domain.schedule.repository;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.place.entity.CustomPlace;
import travel.mytravelplan.domain.place.entity.TripPlace;
import travel.mytravelplan.domain.place.enums.PlaceCategory;
import travel.mytravelplan.domain.place.repository.PlaceRepository;
import travel.mytravelplan.domain.schedule.entity.Schedule;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.enums.Country;
import travel.mytravelplan.domain.trip.repository.TripRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.support.RepositoryTestSupport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("일정 레포지토리 테스트")
public class ScheduleRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("일정을 저장한다")
    void saveSchedule() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("서울 타워");
        Schedule schedule = createSchedule("서울 타워 방문", trip, place, 1L);

        // when
        Schedule savedSchedule = scheduleRepository.save(schedule);
        em.flush();
        em.clear();

        // then
        assertThat(savedSchedule.getId()).isNotNull();
        assertThat(savedSchedule.getTitle()).isEqualTo("서울 타워 방문");
        assertThat(savedSchedule.getTrip().getId()).isEqualTo(trip.getId());
        assertThat(savedSchedule.getPlace().getId()).isEqualTo(place.getId());
        assertThat(savedSchedule.getDisplayOrder()).isEqualTo(1L);
    }

    @Test
    @DisplayName("일정을 ID로 조회한다")
    void findScheduleById() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("에펠탑");
        Schedule schedule = createAndSaveSchedule("에펠탑 방문", trip, place, 1L);
        em.flush();
        em.clear();

        // when
        Schedule foundSchedule = scheduleRepository.findById(schedule.getId()).orElse(null);

        // then
        assertThat(foundSchedule).isNotNull();
        assertThat(foundSchedule.getId()).isEqualTo(schedule.getId());
        assertThat(foundSchedule.getTitle()).isEqualTo("에펠탑 방문");
    }

    @Test
    @DisplayName("일정을 수정한다")
    void updateSchedule() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("원본 장소");
        Schedule schedule = createAndSaveSchedule("원본 제목", trip, place, 1L);
        em.flush();
        em.clear();

        // when
        Schedule foundSchedule = scheduleRepository.findById(schedule.getId()).orElseThrow();
        TripPlace newPlace = createTripPlace("새로운 장소");
        foundSchedule.update(
                "수정된 제목",
                LocalDateTime.of(2024, 1, 2, 10, 0),
                LocalDateTime.of(2024, 1, 2, 12, 0),
                "수정된 메모",
                BigDecimal.valueOf(4.5),
                newPlace
        );
        em.flush();
        em.clear();

        // then
        Schedule updatedSchedule = scheduleRepository.findById(schedule.getId()).orElse(null);
        assertThat(updatedSchedule).isNotNull();
        assertThat(updatedSchedule.getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedSchedule.getMemo()).isEqualTo("수정된 메모");
        assertThat(updatedSchedule.getRating()).isEqualByComparingTo(BigDecimal.valueOf(4.5));
        assertThat(updatedSchedule.getPlace().getId()).isEqualTo(newPlace.getId());
    }

    @Test
    @DisplayName("일정을 삭제한다")
    void deleteSchedule() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("삭제할 장소");
        Schedule schedule = createAndSaveSchedule("삭제할 일정", trip, place, 1L);
        em.flush();
        em.clear();

        // when
        scheduleRepository.deleteById(schedule.getId());
        em.flush();
        em.clear();

        // then
        Schedule deletedSchedule = scheduleRepository.findById(schedule.getId()).orElse(null);
        assertThat(deletedSchedule).isNull();
    }

    @Test
    @DisplayName("여행 ID로 일정을 조회한다")
    void findAllByCursor_byTripId() {
        // given
        Trip trip1 = createTrip("여행1");
        Trip trip2 = createTrip("여행2");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("일정1", trip1, place, 1L);
        createAndSaveSchedule("일정2", trip1, place, 2L);
        createAndSaveSchedule("일정3", trip2, place, 1L);

        em.flush();
        em.clear();

        // when
        List<Schedule> schedules = scheduleRepository.findAllByCursor(
                trip1.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(schedules).hasSize(2);
        assertThat(schedules)
                .extracting(Schedule::getTitle)
                .containsExactlyInAnyOrder("일정1", "일정2");
    }

    @Test
    @DisplayName("키워드로 일정을 검색한다")
    void findAllByCursor_byKeyword() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("타워 방문", trip, place, 1L);
        createAndSaveSchedule("박물관 구경", trip, place, 2L);
        createAndSaveSchedule("타워 식사", trip, place, 3L);

        em.flush();
        em.clear();

        // when
        List<Schedule> schedules = scheduleRepository.findAllByCursor(
                trip.getId(),
                "타워",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(schedules).hasSize(2);
        assertThat(schedules)
                .extracting(Schedule::getTitle)
                .containsExactlyInAnyOrder("타워 방문", "타워 식사");
    }

    @Test
    @DisplayName("생성일 기준 내림차순으로 일정을 조회한다")
    void findAllByCursor_orderByCreatedAtDesc() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("일정1", trip, place, 1L);
        createAndSaveSchedule("일정2", trip, place, 2L);
        createAndSaveSchedule("일정3", trip, place, 3L);

        em.flush();
        em.clear();

        // when
        List<Schedule> schedules = scheduleRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(schedules).hasSize(3);
        assertThat(schedules.get(0).getTitle()).isEqualTo("일정3");
        assertThat(schedules.get(1).getTitle()).isEqualTo("일정2");
        assertThat(schedules.get(2).getTitle()).isEqualTo("일정1");
    }

    @Test
    @DisplayName("생성일 기준 오름차순으로 일정을 조회한다")
    void findAllByCursor_orderByCreatedAtAsc() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("일정1", trip, place, 1L);
        createAndSaveSchedule("일정2", trip, place, 2L);
        createAndSaveSchedule("일정3", trip, place, 3L);

        em.flush();
        em.clear();

        // when
        List<Schedule> schedules = scheduleRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(schedules).hasSize(3);
        assertThat(schedules.get(0).getTitle()).isEqualTo("일정1");
        assertThat(schedules.get(1).getTitle()).isEqualTo("일정2");
        assertThat(schedules.get(2).getTitle()).isEqualTo("일정3");
    }

    @Test
    @DisplayName("limit 개수만큼 일정을 조회한다")
    void findAllByCursor_withLimit() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("일정1", trip, place, 1L);
        createAndSaveSchedule("일정2", trip, place, 2L);
        createAndSaveSchedule("일정3", trip, place, 3L);
        createAndSaveSchedule("일정4", trip, place, 4L);
        createAndSaveSchedule("일정5", trip, place, 5L);

        em.flush();
        em.clear();

        // when
        List<Schedule> schedules = scheduleRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                3
        );

        // then
        assertThat(schedules).hasSize(3);
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 일정을 조회한다 - 내림차순")
    void findAllByCursor_withCursor_desc() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("일정1", trip, place, 1L);
        createAndSaveSchedule("일정2", trip, place, 2L);
        createAndSaveSchedule("일정3", trip, place, 3L);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Schedule> firstPage = scheduleRepository.findAllByCursor(
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
        assertThat(firstPage.get(0).getTitle()).isEqualTo("일정3");
        assertThat(firstPage.get(1).getTitle()).isEqualTo("일정2");

        // when - 두 번째 페이지 조회
        Schedule lastSchedule = firstPage.get(firstPage.size() - 1);
        List<Schedule> secondPage = scheduleRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                lastSchedule.getCreatedAt().toString(),
                lastSchedule.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getTitle()).isEqualTo("일정1");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 일정을 조회한다 - 오름차순")
    void findAllByCursor_withCursor_asc() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("일정1", trip, place, 1L);
        createAndSaveSchedule("일정2", trip, place, 2L);
        createAndSaveSchedule("일정3", trip, place, 3L);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Schedule> firstPage = scheduleRepository.findAllByCursor(
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
        assertThat(firstPage.get(0).getTitle()).isEqualTo("일정1");
        assertThat(firstPage.get(1).getTitle()).isEqualTo("일정2");

        // when - 두 번째 페이지 조회
        Schedule lastSchedule = firstPage.get(firstPage.size() - 1);
        List<Schedule> secondPage = scheduleRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                lastSchedule.getCreatedAt().toString(),
                lastSchedule.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getTitle()).isEqualTo("일정3");
    }

    @Test
    @DisplayName("여행 ID와 키워드로 일정을 조회한다")
    void findAllByCursor_byTripIdAndKeyword() {
        // given
        Trip trip1 = createTrip("여행1");
        Trip trip2 = createTrip("여행2");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("타워 방문", trip1, place, 1L);
        createAndSaveSchedule("박물관 구경", trip1, place, 2L);
        createAndSaveSchedule("타워 식사", trip1, place, 3L);
        createAndSaveSchedule("타워 구경", trip2, place, 1L);

        em.flush();
        em.clear();

        // when
        List<Schedule> schedules = scheduleRepository.findAllByCursor(
                trip1.getId(),
                "타워",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(schedules).hasSize(2);
        assertThat(schedules)
                .extracting(Schedule::getTitle)
                .containsExactlyInAnyOrder("타워 방문", "타워 식사");
    }

    @Test
    @DisplayName("displayOrder 기준으로 일정을 정렬하여 조회한다")
    void findAllByCursor_orderByDisplayOrder() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("일정A", trip, place, 3L);
        createAndSaveSchedule("일정B", trip, place, 1L);
        createAndSaveSchedule("일정C", trip, place, 2L);

        em.flush();
        em.clear();

        // when
        List<Schedule> schedules = scheduleRepository.findAllByCursor(
                trip.getId(),
                null,
                "displayOrder",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(schedules).hasSize(3);
        assertThat(schedules.get(0).getDisplayOrder()).isEqualTo(1L);
        assertThat(schedules.get(1).getDisplayOrder()).isEqualTo(2L);
        assertThat(schedules.get(2).getDisplayOrder()).isEqualTo(3L);
    }

    @Test
    @DisplayName("여행 ID로 최대 displayOrder를 조회한다")
    void findMaxDisplayOrderByTripId() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("일정1", trip, place, 1L);
        createAndSaveSchedule("일정2", trip, place, 5L);
        createAndSaveSchedule("일정3", trip, place, 3L);

        em.flush();
        em.clear();

        // when
        Long maxDisplayOrder = scheduleRepository.findMaxDisplayOrderByTripId(trip.getId());

        // then
        assertThat(maxDisplayOrder).isEqualTo(5L);
    }

    @Test
    @DisplayName("일정이 없는 여행의 최대 displayOrder는 0L이다")
    void findMaxDisplayOrderByTripId_noSchedule() {
        // given
        Trip trip = createTrip("여행 계획");
        em.flush();
        em.clear();

        // when
        Long maxDisplayOrder = scheduleRepository.findMaxDisplayOrderByTripId(trip.getId());

        // then
        assertThat(maxDisplayOrder).isEqualTo(0L);
    }

    @Test
    @DisplayName("CustomPlace와 TripPlace를 모두 일정에 사용할 수 있다")
    void saveSchedule_withDifferentPlaceTypes() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripPlace tripPlace = createTripPlace("여행 장소");
        CustomPlace customPlace = createCustomPlace("내 장소", user);

        createAndSaveSchedule("여행 장소 방문", trip, tripPlace, 1L);
        createAndSaveSchedule("내 장소 방문", trip, customPlace, 2L);

        em.flush();
        em.clear();

        // when
        List<Schedule> schedules = scheduleRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(schedules).hasSize(2);
        assertThat(Hibernate.unproxy(schedules.get(0).getPlace())).isInstanceOf(TripPlace.class);
        assertThat(Hibernate.unproxy(schedules.get(1).getPlace())).isInstanceOf(CustomPlace.class);
    }

    @Test
    @DisplayName("displayOrder 기준 커서 페이지네이션 - 내림차순")
    void findAllByCursor_withCursor_displayOrder_desc() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("일정1", trip, place, 1L);
        createAndSaveSchedule("일정2", trip, place, 2L);
        createAndSaveSchedule("일정3", trip, place, 3L);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Schedule> firstPage = scheduleRepository.findAllByCursor(
                trip.getId(),
                null,
                "displayOrder",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getDisplayOrder()).isEqualTo(3L);
        assertThat(firstPage.get(1).getDisplayOrder()).isEqualTo(2L);

        // when - 두 번째 페이지 조회
        Schedule lastSchedule = firstPage.get(firstPage.size() - 1);
        List<Schedule> secondPage = scheduleRepository.findAllByCursor(
                trip.getId(),
                null,
                "displayOrder",
                "desc",
                lastSchedule.getDisplayOrder().toString(),
                lastSchedule.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getDisplayOrder()).isEqualTo(1L);
    }

    @Test
    @DisplayName("displayOrder 기준 커서 페이지네이션 - 오름차순")
    void findAllByCursor_withCursor_displayOrder_asc() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("일정1", trip, place, 1L);
        createAndSaveSchedule("일정2", trip, place, 2L);
        createAndSaveSchedule("일정3", trip, place, 3L);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Schedule> firstPage = scheduleRepository.findAllByCursor(
                trip.getId(),
                null,
                "displayOrder",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getDisplayOrder()).isEqualTo(1L);
        assertThat(firstPage.get(1).getDisplayOrder()).isEqualTo(2L);

        // when - 두 번째 페이지 조회
        Schedule lastSchedule = firstPage.get(firstPage.size() - 1);
        List<Schedule> secondPage = scheduleRepository.findAllByCursor(
                trip.getId(),
                null,
                "displayOrder",
                "asc",
                lastSchedule.getDisplayOrder().toString(),
                lastSchedule.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getDisplayOrder()).isEqualTo(3L);
    }

    @Test
    @DisplayName("tripId가 null인 경우 모든 일정을 조회한다")
    void findAllByCursor_withNullTripId() {
        // given
        Trip trip1 = createTrip("여행1");
        Trip trip2 = createTrip("여행2");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("일정1", trip1, place, 1L);
        createAndSaveSchedule("일정2", trip2, place, 1L);

        em.flush();
        em.clear();

        // when
        List<Schedule> schedules = scheduleRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(schedules).hasSize(2);
    }

    @Test
    @DisplayName("키워드가 빈 문자열인 경우 모든 일정을 조회한다")
    void findAllByCursor_withEmptyKeyword() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("일정1", trip, place, 1L);
        createAndSaveSchedule("일정2", trip, place, 2L);

        em.flush();
        em.clear();

        // when
        List<Schedule> schedules = scheduleRepository.findAllByCursor(
                trip.getId(),
                "",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(schedules).hasSize(2);
    }

    @Test
    @DisplayName("키워드 검색은 대소문자를 구분하지 않는다")
    void findAllByCursor_keywordCaseInsensitive() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("Tower 방문", trip, place, 1L);
        createAndSaveSchedule("TOWER 식사", trip, place, 2L);
        createAndSaveSchedule("tower 관광", trip, place, 3L);

        em.flush();
        em.clear();

        // when
        List<Schedule> schedules = scheduleRepository.findAllByCursor(
                trip.getId(),
                "TOWER",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(schedules).hasSize(3);
    }

    @Test
    @DisplayName("displayOrder 내림차순으로 정렬한다")
    void findAllByCursor_orderByDisplayOrder_desc() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("일정A", trip, place, 1L);
        createAndSaveSchedule("일정B", trip, place, 2L);
        createAndSaveSchedule("일정C", trip, place, 3L);

        em.flush();
        em.clear();

        // when
        List<Schedule> schedules = scheduleRepository.findAllByCursor(
                trip.getId(),
                null,
                "displayOrder",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(schedules).hasSize(3);
        assertThat(schedules.get(0).getDisplayOrder()).isEqualTo(3L);
        assertThat(schedules.get(1).getDisplayOrder()).isEqualTo(2L);
        assertThat(schedules.get(2).getDisplayOrder()).isEqualTo(1L);
    }

    @Test
    @DisplayName("모든 조건이 조합된 복잡한 쿼리 - 키워드, 커서, 정렬")
    void findAllByCursor_complexQuery() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("타워 일정1", trip, place, 1L);
        createAndSaveSchedule("타워 일정2", trip, place, 2L);
        createAndSaveSchedule("타워 일정3", trip, place, 3L);
        createAndSaveSchedule("박물관 일정", trip, place, 4L);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Schedule> firstPage = scheduleRepository.findAllByCursor(
                trip.getId(),
                "타워",
                "displayOrder",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getDisplayOrder()).isEqualTo(1L);
        assertThat(firstPage.get(1).getDisplayOrder()).isEqualTo(2L);

        // when - 두 번째 페이지 조회
        Schedule lastSchedule = firstPage.get(firstPage.size() - 1);
        List<Schedule> secondPage = scheduleRepository.findAllByCursor(
                trip.getId(),
                "타워",
                "displayOrder",
                "asc",
                lastSchedule.getDisplayOrder().toString(),
                lastSchedule.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getDisplayOrder()).isEqualTo(3L);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 일정을 조회하면 빈 Optional을 반환한다")
    void findScheduleById_notFound() {
        // when
        Schedule foundSchedule = scheduleRepository.findById(999L).orElse(null);

        // then
        assertThat(foundSchedule).isNull();
    }

    @Test
    @DisplayName("일정의 모든 필드를 수정한다")
    void updateSchedule_allFields() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace originalPlace = createTripPlace("원본 장소");
        TripPlace newPlace = createTripPlace("새로운 장소");

        Schedule schedule = createAndSaveSchedule("원본 제목", trip, originalPlace, 1L);
        em.flush();
        em.clear();

        // when
        Schedule foundSchedule = scheduleRepository.findById(schedule.getId()).orElseThrow();
        foundSchedule.update(
                "완전히 새로운 제목",
                LocalDateTime.of(2024, 12, 31, 23, 0),
                LocalDateTime.of(2024, 12, 31, 23, 59),
                "완전히 새로운 메모",
                BigDecimal.valueOf(5.0),
                newPlace
        );
        em.flush();
        em.clear();

        // then
        Schedule updatedSchedule = scheduleRepository.findById(schedule.getId()).orElseThrow();
        assertThat(updatedSchedule.getTitle()).isEqualTo("완전히 새로운 제목");
        assertThat(updatedSchedule.getStartDateTime()).isEqualTo(LocalDateTime.of(2024, 12, 31, 23, 0));
        assertThat(updatedSchedule.getEndDateTime()).isEqualTo(LocalDateTime.of(2024, 12, 31, 23, 59));
        assertThat(updatedSchedule.getMemo()).isEqualTo("완전히 새로운 메모");
        assertThat(updatedSchedule.getRating()).isEqualByComparingTo(BigDecimal.valueOf(5.0));
        assertThat(updatedSchedule.getPlace().getId()).isEqualTo(newPlace.getId());
    }

    @Test
    @DisplayName("null 커서와 after가 함께 제공되어도 정상 작동한다")
    void findAllByCursor_withNullCursorButWithAfter() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("일정1", trip, place, 1L);
        createAndSaveSchedule("일정2", trip, place, 2L);

        em.flush();
        em.clear();

        // when - cursor는 null이고 after만 있는 경우
        List<Schedule> schedules = scheduleRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                null,
                999L,
                10
        );

        // then - 커서 조건이 적용되지 않고 모든 일정이 조회됨
        assertThat(schedules).hasSize(2);
    }

    @Test
    @DisplayName("같은 createdAt을 가진 일정들을 ID로 구분하여 페이지네이션한다")
    void findAllByCursor_withSameCreatedAt() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("장소");

        Schedule schedule1 = createSchedule("일정1", trip, place, 1L);
        Schedule schedule2 = createSchedule("일정2", trip, place, 2L);
        Schedule schedule3 = createSchedule("일정3", trip, place, 3L);

        scheduleRepository.save(schedule1);
        scheduleRepository.save(schedule2);
        scheduleRepository.save(schedule3);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Schedule> firstPage = scheduleRepository.findAllByCursor(
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

        // when - 두 번째 페이지 조회
        Schedule lastSchedule = firstPage.get(firstPage.size() - 1);
        List<Schedule> secondPage = scheduleRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                lastSchedule.getCreatedAt().toString(),
                lastSchedule.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSizeLessThanOrEqualTo(2);
    }

    @Test
    @DisplayName("같은 displayOrder를 가진 일정들을 ID로 구분하여 페이지네이션한다")
    void findAllByCursor_withSameDisplayOrder() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("일정1", trip, place, 1L);
        createAndSaveSchedule("일정2", trip, place, 1L);
        createAndSaveSchedule("일정3", trip, place, 2L);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Schedule> firstPage = scheduleRepository.findAllByCursor(
                trip.getId(),
                null,
                "displayOrder",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);

        // when - 두 번째 페이지 조회
        Schedule lastSchedule = firstPage.get(firstPage.size() - 1);
        List<Schedule> secondPage = scheduleRepository.findAllByCursor(
                trip.getId(),
                null,
                "displayOrder",
                "asc",
                lastSchedule.getDisplayOrder().toString(),
                lastSchedule.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSizeLessThanOrEqualTo(2);
    }

    @Test
    @DisplayName("여러 여행에 걸쳐 키워드로 일정을 검색한다")
    void findAllByCursor_multipleTripsWithKeyword() {
        // given
        Trip trip1 = createTrip("여행1");
        Trip trip2 = createTrip("여행2");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("서울 타워", trip1, place, 1L);
        createAndSaveSchedule("부산 타워", trip1, place, 2L);
        createAndSaveSchedule("제주 타워", trip2, place, 1L);
        createAndSaveSchedule("인천 박물관", trip2, place, 2L);

        em.flush();
        em.clear();

        // when - tripId 없이 키워드만으로 검색
        List<Schedule> schedules = scheduleRepository.findAllByCursor(
                null,
                "타워",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(schedules).hasSize(3);
        assertThat(schedules)
                .extracting(Schedule::getTitle)
                .allMatch(title -> title.contains("타워"));
    }

    @Test
    @DisplayName("cursor만 있고 after가 null인 경우 - 커서 조건이 적용되지 않는다")
    void findAllByCursor_withCursorButNullAfter() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("일정1", trip, place, 1L);
        createAndSaveSchedule("일정2", trip, place, 2L);
        createAndSaveSchedule("일정3", trip, place, 3L);

        em.flush();
        em.clear();

        // when - cursor는 있지만 after가 null인 경우
        List<Schedule> firstPage = scheduleRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        Schedule lastSchedule = firstPage.get(firstPage.size() - 1);
        List<Schedule> schedules = scheduleRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                lastSchedule.getCreatedAt().toString(),
                null,
                10
        );

        // then - after가 null이므로 커서 조건이 적용되지 않고 모든 일정 조회
        assertThat(schedules).hasSize(3);
    }

    @Test
    @DisplayName("after만 있고 cursor가 null인 경우 - 커서 조건이 적용되지 않는다")
    void findAllByCursor_withAfterButNullCursor() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("일정1", trip, place, 1L);
        createAndSaveSchedule("일정2", trip, place, 2L);
        createAndSaveSchedule("일정3", trip, place, 3L);

        em.flush();
        em.clear();

        // when - after는 있지만 cursor가 null인 경우
        List<Schedule> schedules = scheduleRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                null,
                999L,
                10
        );

        // then - cursor가 null이므로 커서 조건이 적용되지 않고 모든 일정 조회
        assertThat(schedules).hasSize(3);
    }

    @Test
    @DisplayName("cursor와 after가 모두 있는 경우 - displayOrder 기준 오름차순 커서 페이지네이션")
    void findAllByCursor_withCursorAndAfter_displayOrder_asc() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("일정1", trip, place, 1L);
        createAndSaveSchedule("일정2", trip, place, 2L);
        createAndSaveSchedule("일정3", trip, place, 3L);
        createAndSaveSchedule("일정4", trip, place, 4L);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Schedule> firstPage = scheduleRepository.findAllByCursor(
                trip.getId(),
                null,
                "displayOrder",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getDisplayOrder()).isEqualTo(1L);
        assertThat(firstPage.get(1).getDisplayOrder()).isEqualTo(2L);

        // when - cursor와 after 모두 사용하여 두 번째 페이지 조회
        Schedule lastSchedule = firstPage.get(firstPage.size() - 1);
        List<Schedule> secondPage = scheduleRepository.findAllByCursor(
                trip.getId(),
                null,
                "displayOrder",
                "asc",
                lastSchedule.getDisplayOrder().toString(),
                lastSchedule.getId(),
                2
        );

        // then - cursor와 after 조건에 의해 다음 페이지 조회
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage.get(0).getDisplayOrder()).isEqualTo(3L);
        assertThat(secondPage.get(1).getDisplayOrder()).isEqualTo(4L);
    }

    @Test
    @DisplayName("cursor와 after가 모두 있는 경우 - displayOrder 기준 내림차순 커서 페이지네이션")
    void findAllByCursor_withCursorAndAfter_displayOrder_desc() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("일정1", trip, place, 1L);
        createAndSaveSchedule("일정2", trip, place, 2L);
        createAndSaveSchedule("일정3", trip, place, 3L);
        createAndSaveSchedule("일정4", trip, place, 4L);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Schedule> firstPage = scheduleRepository.findAllByCursor(
                trip.getId(),
                null,
                "displayOrder",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getDisplayOrder()).isEqualTo(4L);
        assertThat(firstPage.get(1).getDisplayOrder()).isEqualTo(3L);

        // when - cursor와 after 모두 사용하여 두 번째 페이지 조회
        Schedule lastSchedule = firstPage.get(firstPage.size() - 1);
        List<Schedule> secondPage = scheduleRepository.findAllByCursor(
                trip.getId(),
                null,
                "displayOrder",
                "desc",
                lastSchedule.getDisplayOrder().toString(),
                lastSchedule.getId(),
                2
        );

        // then - cursor와 after 조건에 의해 다음 페이지 조회
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage.get(0).getDisplayOrder()).isEqualTo(2L);
        assertThat(secondPage.get(1).getDisplayOrder()).isEqualTo(1L);
    }

    @Test
    @DisplayName("cursor와 after가 모두 있는 경우 - createdAt 기준 오름차순 커서 페이지네이션")
    void findAllByCursor_withCursorAndAfter_createdAt_asc() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("일정1", trip, place, 1L);
        createAndSaveSchedule("일정2", trip, place, 2L);
        createAndSaveSchedule("일정3", trip, place, 3L);
        createAndSaveSchedule("일정4", trip, place, 4L);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Schedule> firstPage = scheduleRepository.findAllByCursor(
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

        // when - cursor와 after 모두 사용하여 두 번째 페이지 조회
        Schedule lastSchedule = firstPage.get(firstPage.size() - 1);
        List<Schedule> secondPage = scheduleRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                lastSchedule.getCreatedAt().toString(),
                lastSchedule.getId(),
                2
        );

        // then - cursor와 after 조건에 의해 다음 페이지 조회
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage.get(0).getId()).isGreaterThan(lastSchedule.getId());
    }

    @Test
    @DisplayName("cursor와 after가 모두 있는 경우 - createdAt 기준 내림차순 커서 페이지네이션")
    void findAllByCursor_withCursorAndAfter_createdAt_desc() {
        // given
        Trip trip = createTrip("여행 계획");
        TripPlace place = createTripPlace("장소");

        createAndSaveSchedule("일정1", trip, place, 1L);
        createAndSaveSchedule("일정2", trip, place, 2L);
        createAndSaveSchedule("일정3", trip, place, 3L);
        createAndSaveSchedule("일정4", trip, place, 4L);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Schedule> firstPage = scheduleRepository.findAllByCursor(
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

        // when - cursor와 after 모두 사용하여 두 번째 페이지 조회
        Schedule lastSchedule = firstPage.get(firstPage.size() - 1);
        List<Schedule> secondPage = scheduleRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                lastSchedule.getCreatedAt().toString(),
                lastSchedule.getId(),
                2
        );

        // then - cursor와 after 조건에 의해 다음 페이지 조회
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage.get(0).getId()).isLessThan(lastSchedule.getId());
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

    private TripPlace createTripPlace(String name) {
        TripPlace place = TripPlace.createTripPlace(
                name,
                "서울특별시 중구",
                "멋진 장소입니다",
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780),
                PlaceCategory.ATTRACTION,
                "https://example.com"
        );
        return placeRepository.save(place);
    }

    private CustomPlace createCustomPlace(String name, User user) {
        CustomPlace place = CustomPlace.createCustomPlace(
                name,
                "서울특별시 강남구",
                "나만의 장소",
                BigDecimal.valueOf(37.4979),
                BigDecimal.valueOf(127.0276),
                PlaceCategory.CAFE,
                user
        );
        return placeRepository.save(place);
    }

    private Schedule createSchedule(String title, Trip trip, TripPlace place, Long displayOrder) {
        return Schedule.createSchedule(
                title,
                LocalDateTime.of(2024, 1, 1, 9, 0),
                LocalDateTime.of(2024, 1, 1, 12, 0),
                "메모",
                displayOrder,
                place,
                trip,
                BigDecimal.valueOf(4.0)
        );
    }

    private Schedule createAndSaveSchedule(String title, Trip trip, TripPlace place, Long displayOrder) {
        Schedule schedule = Schedule.createSchedule(
                title,
                LocalDateTime.of(2024, 1, 1, 9, 0),
                LocalDateTime.of(2024, 1, 1, 12, 0),
                "메모",
                displayOrder,
                place,
                trip,
                BigDecimal.valueOf(4.0)
        );
        return scheduleRepository.save(schedule);
    }

    private Schedule createAndSaveSchedule(String title, Trip trip, CustomPlace place, Long displayOrder) {
        Schedule schedule = Schedule.createSchedule(
                title,
                LocalDateTime.of(2024, 1, 1, 9, 0),
                LocalDateTime.of(2024, 1, 1, 12, 0),
                "메모",
                displayOrder,
                place,
                trip,
                BigDecimal.valueOf(4.0)
        );
        return scheduleRepository.save(schedule);
    }
}
