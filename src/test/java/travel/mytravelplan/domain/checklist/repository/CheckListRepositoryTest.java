package travel.mytravelplan.domain.checklist.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.checklist.entity.CheckList;
import travel.mytravelplan.domain.checklist.entity.PersonalCheckList;
import travel.mytravelplan.domain.checklist.entity.SharedCheckList;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("체크리스트 리포지토리 테스트")
class CheckListRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private CheckListRepository checkListRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripJoinRepository tripJoinRepository;

    @Test
    @DisplayName("공유 체크리스트를 저장한다")
    void saveSharedCheckList() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);

        // when
        CheckList savedCheckList = checkListRepository.save(checkList);
        em.flush();
        em.clear();

        // then
        assertThat(savedCheckList.getId()).isNotNull();
        assertThat(savedCheckList.getName()).isEqualTo("공유 체크리스트");
        assertThat(savedCheckList.getTrip().getId()).isEqualTo(trip.getId());
    }

    @Test
    @DisplayName("개인 체크리스트를 저장한다")
    void savePersonalCheckList() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList checkList = createPersonalCheckList("개인 체크리스트", trip, tripJoin);

        // when
        CheckList savedCheckList = checkListRepository.save(checkList);
        em.flush();
        em.clear();

        // then
        assertThat(savedCheckList.getId()).isNotNull();
        assertThat(savedCheckList.getName()).isEqualTo("개인 체크리스트");
        assertThat(savedCheckList.getTrip().getId()).isEqualTo(trip.getId());
    }

    @Test
    @DisplayName("체크리스트를 ID로 조회한다")
    void findCheckListById() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);
        CheckList savedCheckList = checkListRepository.save(checkList);
        em.flush();
        em.clear();

        // when
        CheckList foundCheckList = checkListRepository.findById(savedCheckList.getId()).orElse(null);

        // then
        assertThat(foundCheckList).isNotNull();
        assertThat(foundCheckList.getId()).isEqualTo(savedCheckList.getId());
        assertThat(foundCheckList.getName()).isEqualTo("공유 체크리스트");
    }

    @Test
    @DisplayName("체크리스트를 수정한다")
    void updateCheckList() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);
        CheckList savedCheckList = checkListRepository.save(checkList);
        em.flush();
        em.clear();

        // when
        SharedCheckList foundCheckList = (SharedCheckList) checkListRepository.findById(savedCheckList.getId()).orElseThrow();
        foundCheckList.update("수정된 체크리스트");
        em.flush();
        em.clear();

        // then
        CheckList updatedCheckList = checkListRepository.findById(savedCheckList.getId()).orElse(null);
        assertThat(updatedCheckList).isNotNull();
        assertThat(updatedCheckList.getName()).isEqualTo("수정된 체크리스트");
    }

    @Test
    @DisplayName("체크리스트를 삭제한다")
    void deleteCheckList() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);
        CheckList savedCheckList = checkListRepository.save(checkList);
        em.flush();
        em.clear();

        // when
        checkListRepository.deleteById(savedCheckList.getId());
        em.flush();
        em.clear();

        // then
        CheckList deletedCheckList = checkListRepository.findById(savedCheckList.getId()).orElse(null);
        assertThat(deletedCheckList).isNull();
    }

    @Test
    @DisplayName("여행 ID로 체크리스트를 조회한다")
    void findAllByCursor_byTripId() {
        // given
        Trip trip1 = createTrip("여행1");
        Trip trip2 = createTrip("여행2");

        SharedCheckList checkList1 = createAndSaveSharedCheckList("체크리스트1", trip1);
        SharedCheckList checkList2 = createAndSaveSharedCheckList("체크리스트2", trip1);
        SharedCheckList checkList3 = createAndSaveSharedCheckList("체크리스트3", trip2);

        em.flush();
        em.clear();

        // when
        List<CheckList> checkLists = checkListRepository.findAllByCursor(
                trip1.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(checkLists).hasSize(2);
        assertThat(checkLists)
                .extracting(CheckList::getName)
                .containsExactlyInAnyOrder("체크리스트1", "체크리스트2");
    }

    @Test
    @DisplayName("키워드로 체크리스트를 검색한다")
    void findAllByCursor_byKeyword() {
        // given
        Trip trip = createTrip("여행 계획");

        SharedCheckList checkList1 = createAndSaveSharedCheckList("짐 목록", trip);
        SharedCheckList checkList2 = createAndSaveSharedCheckList("준비물 체크", trip);
        SharedCheckList checkList3 = createAndSaveSharedCheckList("짐 싸기", trip);

        em.flush();
        em.clear();

        // when
        List<CheckList> checkLists = checkListRepository.findAllByCursor(
                trip.getId(),
                "짐",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(checkLists).hasSize(2);
        assertThat(checkLists)
                .extracting(CheckList::getName)
                .containsExactlyInAnyOrder("짐 목록", "짐 싸기");
    }

    @Test
    @DisplayName("생성일 기준 내림차순으로 체크리스트를 조회한다")
    void findAllByCursor_orderByCreatedAtDesc() {
        // given
        Trip trip = createTrip("여행 계획");

        SharedCheckList checkList1 = createAndSaveSharedCheckList("체크리스트1", trip);
        SharedCheckList checkList2 = createAndSaveSharedCheckList("체크리스트2", trip);
        SharedCheckList checkList3 = createAndSaveSharedCheckList("체크리스트3", trip);

        em.flush();
        em.clear();

        // when
        List<CheckList> checkLists = checkListRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(checkLists).hasSize(3);
        assertThat(checkLists.get(0).getName()).isEqualTo("체크리스트3");
        assertThat(checkLists.get(1).getName()).isEqualTo("체크리스트2");
        assertThat(checkLists.get(2).getName()).isEqualTo("체크리스트1");
    }

    @Test
    @DisplayName("생성일 기준 오름차순으로 체크리스트를 조회한다")
    void findAllByCursor_orderByCreatedAtAsc() {
        // given
        Trip trip = createTrip("여행 계획");

        SharedCheckList checkList1 = createAndSaveSharedCheckList("체크리스트1", trip);
        SharedCheckList checkList2 = createAndSaveSharedCheckList("체크리스트2", trip);
        SharedCheckList checkList3 = createAndSaveSharedCheckList("체크리스트3", trip);

        em.flush();
        em.clear();

        // when
        List<CheckList> checkLists = checkListRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(checkLists).hasSize(3);
        assertThat(checkLists.get(0).getName()).isEqualTo("체크리스트1");
        assertThat(checkLists.get(1).getName()).isEqualTo("체크리스트2");
        assertThat(checkLists.get(2).getName()).isEqualTo("체크리스트3");
    }

    @Test
    @DisplayName("limit 개수만큼 체크리스트를 조회한다")
    void findAllByCursor_withLimit() {
        // given
        Trip trip = createTrip("여행 계획");

        SharedCheckList checkList1 = createAndSaveSharedCheckList("체크리스트1", trip);
        SharedCheckList checkList2 = createAndSaveSharedCheckList("체크리스트2", trip);
        SharedCheckList checkList3 = createAndSaveSharedCheckList("체크리스트3", trip);
        SharedCheckList checkList4 = createAndSaveSharedCheckList("체크리스트4", trip);
        SharedCheckList checkList5 = createAndSaveSharedCheckList("체크리스트5", trip);

        em.flush();
        em.clear();

        // when
        List<CheckList> checkLists = checkListRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                3
        );

        // then
        assertThat(checkLists).hasSize(3);
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 체크리스트를 조회한다 - 내림차순")
    void findAllByCursor_withCursor_desc() {
        // given
        Trip trip = createTrip("여행 계획");

        SharedCheckList checkList1 = createAndSaveSharedCheckList("체크리스트1", trip);
        SharedCheckList checkList2 = createAndSaveSharedCheckList("체크리스트2", trip);
        SharedCheckList checkList3 = createAndSaveSharedCheckList("체크리스트3", trip);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<CheckList> firstPage = checkListRepository.findAllByCursor(
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
        assertThat(firstPage.get(0).getName()).isEqualTo("체크리스트3");
        assertThat(firstPage.get(1).getName()).isEqualTo("체크리스트2");

        // when - 두 번째 페이지 조회
        CheckList lastCheckList = firstPage.get(firstPage.size() - 1);
        List<CheckList> secondPage = checkListRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                lastCheckList.getCreatedAt().toString(),
                lastCheckList.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getName()).isEqualTo("체크리스트1");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 체크리스트를 조회한다 - 오름차순")
    void findAllByCursor_withCursor_asc() {
        // given
        Trip trip = createTrip("여행 계획");

        SharedCheckList checkList1 = createAndSaveSharedCheckList("체크리스트1", trip);
        SharedCheckList checkList2 = createAndSaveSharedCheckList("체크리스트2", trip);
        SharedCheckList checkList3 = createAndSaveSharedCheckList("체크리스트3", trip);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<CheckList> firstPage = checkListRepository.findAllByCursor(
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
        assertThat(firstPage.get(0).getName()).isEqualTo("체크리스트1");
        assertThat(firstPage.get(1).getName()).isEqualTo("체크리스트2");

        // when - 두 번째 페이지 조회
        CheckList lastCheckList = firstPage.get(firstPage.size() - 1);
        List<CheckList> secondPage = checkListRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                lastCheckList.getCreatedAt().toString(),
                lastCheckList.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getName()).isEqualTo("체크리스트3");
    }

    @Test
    @DisplayName("여행 ID와 키워드로 체크리스트를 조회한다")
    void findAllByCursor_byTripIdAndKeyword() {
        // given
        Trip trip1 = createTrip("여행1");
        Trip trip2 = createTrip("여행2");

        SharedCheckList checkList1 = createAndSaveSharedCheckList("짐 목록", trip1);
        SharedCheckList checkList2 = createAndSaveSharedCheckList("준비물 체크", trip1);
        SharedCheckList checkList3 = createAndSaveSharedCheckList("짐 싸기", trip1);
        SharedCheckList checkList4 = createAndSaveSharedCheckList("짐 준비", trip2);

        em.flush();
        em.clear();

        // when
        List<CheckList> checkLists = checkListRepository.findAllByCursor(
                trip1.getId(),
                "짐",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(checkLists).hasSize(2);
        assertThat(checkLists)
                .extracting(CheckList::getName)
                .containsExactlyInAnyOrder("짐 목록", "짐 싸기");
    }

    @Test
    @DisplayName("개인 체크리스트와 공유 체크리스트를 함께 조회한다")
    void findAllByCursor_mixedCheckLists() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);

        PersonalCheckList personalCheckList1 = createAndSavePersonalCheckList("개인 체크리스트1", trip, tripJoin);
        SharedCheckList sharedCheckList1 = createAndSaveSharedCheckList("공유 체크리스트1", trip);
        PersonalCheckList personalCheckList2 = createAndSavePersonalCheckList("개인 체크리스트2", trip, tripJoin);

        em.flush();
        em.clear();

        // when
        List<CheckList> checkLists = checkListRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(checkLists).hasSize(3);
        assertThat(checkLists)
                .extracting(CheckList::getName)
                .containsExactlyInAnyOrder("개인 체크리스트1", "공유 체크리스트1", "개인 체크리스트2");
    }

    @Test
    @DisplayName("커서와 after가 null인 경우 첫 페이지를 조회한다")
    void findAllByCursor_withNullCursorAndAfter() {
        // given
        Trip trip = createTrip("커서 테스트");

        SharedCheckList checkList1 = createAndSaveSharedCheckList("체크리스트1", trip);
        SharedCheckList checkList2 = createAndSaveSharedCheckList("체크리스트2", trip);

        em.flush();
        em.clear();

        // when
        List<CheckList> checkLists = checkListRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(checkLists).hasSize(2);
        assertThat(checkLists.get(0).getName()).isEqualTo("체크리스트1");
        assertThat(checkLists.get(1).getName()).isEqualTo("체크리스트2");
    }

    @Test
    @DisplayName("tripId가 null인 경우 모든 여행의 체크리스트를 조회한다")
    void findAllByCursor_withNullTripId() {
        // given
        Trip trip1 = createTrip("여행A");
        Trip trip2 = createTrip("여행B");

        SharedCheckList checkList1 = createAndSaveSharedCheckList("여행A 체크리스트", trip1);
        SharedCheckList checkList2 = createAndSaveSharedCheckList("여행B 체크리스트", trip2);

        em.flush();
        em.clear();

        // when
        List<CheckList> checkLists = checkListRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(checkLists).hasSize(2);
    }

    @Test
    @DisplayName("키워드가 빈 문자열인 경우 모든 체크리스트를 조회한다")
    void findAllByCursor_withEmptyKeyword() {
        // given
        Trip trip = createTrip("빈 키워드 테스트");

        SharedCheckList checkList1 = createAndSaveSharedCheckList("호텔", trip);
        SharedCheckList checkList2 = createAndSaveSharedCheckList("식비", trip);

        em.flush();
        em.clear();

        // when
        List<CheckList> checkLists = checkListRepository.findAllByCursor(
                trip.getId(),
                "",
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(checkLists).hasSize(2);
    }

    @Test
    @DisplayName("키워드 검색 시 대소문자를 구분하지 않는다")
    void findAllByCursor_withKeywordCaseInsensitive() {
        // given
        Trip trip = createTrip("대소문자 테스트");

        SharedCheckList checkList1 = createAndSaveSharedCheckList("Hotel Checklist", trip);
        SharedCheckList checkList2 = createAndSaveSharedCheckList("restaurant list", trip);

        em.flush();
        em.clear();

        // when
        List<CheckList> checkLists = checkListRepository.findAllByCursor(
                trip.getId(),
                "hotel",
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(checkLists).hasSize(1);
        assertThat(checkLists.get(0).getName()).isEqualTo("Hotel Checklist");
    }

    @Test
    @DisplayName("내림차순 정렬 시 커서 기반 페이지네이션이 정상 작동한다")
    void findAllByCursor_withDescOrderAndCursor() {
        // given
        Trip trip = createTrip("내림차순 커서 테스트");

        SharedCheckList checkList1 = createAndSaveSharedCheckList("체크리스트1", trip);
        SharedCheckList checkList2 = createAndSaveSharedCheckList("체크리스트2", trip);
        SharedCheckList checkList3 = createAndSaveSharedCheckList("체크리스트3", trip);

        em.flush();
        em.clear();

        // when - 첫 번째 페이지
        List<CheckList> firstPage = checkListRepository.findAllByCursor(
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
        assertThat(firstPage.get(0).getName()).isEqualTo("체크리스트3");
        assertThat(firstPage.get(1).getName()).isEqualTo("체크리스트2");

        // when - 두 번째 페이지
        CheckList lastCheckList = firstPage.get(firstPage.size() - 1);
        List<CheckList> secondPage = checkListRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                lastCheckList.getCreatedAt().toString(),
                lastCheckList.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getName()).isEqualTo("체크리스트1");
    }

    @Test
    @DisplayName("동일한 createdAt을 가진 체크리스트들을 ID로 정렬한다 - 오름차순")
    void findAllByCursor_withSameCreatedAtOrderByIdAsc() {
        // given
        Trip trip = createTrip("동일 시간 테스트");

        SharedCheckList checkList1 = createAndSaveSharedCheckList("체크리스트A", trip);
        SharedCheckList checkList2 = createAndSaveSharedCheckList("체크리스트B", trip);
        SharedCheckList checkList3 = createAndSaveSharedCheckList("체크리스트C", trip);

        em.flush();
        em.clear();

        // when
        List<CheckList> checkLists = checkListRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(checkLists).hasSize(3);
        // ID 순으로 정렬되어야 함
        for (int i = 0; i < checkLists.size() - 1; i++) {
            assertThat(checkLists.get(i).getId()).isLessThan(checkLists.get(i + 1).getId());
        }
    }

    @Test
    @DisplayName("동일한 createdAt을 가진 체크리스트들을 ID로 정렬한다 - 내림차순")
    void findAllByCursor_withSameCreatedAtOrderByIdDesc() {
        // given
        Trip trip = createTrip("동일 시간 내림차순");

        SharedCheckList checkList1 = createAndSaveSharedCheckList("체크리스트X", trip);
        SharedCheckList checkList2 = createAndSaveSharedCheckList("체크리스트Y", trip);
        SharedCheckList checkList3 = createAndSaveSharedCheckList("체크리스트Z", trip);

        em.flush();
        em.clear();

        // when
        List<CheckList> checkLists = checkListRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(checkLists).hasSize(3);
        // ID 역순으로 정렬되어야 함
        for (int i = 0; i < checkLists.size() - 1; i++) {
            assertThat(checkLists.get(i).getId()).isGreaterThan(checkLists.get(i + 1).getId());
        }
    }

    @Test
    @DisplayName("커서 기반 페이지네이션에서 마지막 페이지를 조회한다")
    void findAllByCursor_lastPage() {
        // given
        Trip trip = createTrip("마지막 페이지 테스트");

        SharedCheckList checkList1 = createAndSaveSharedCheckList("체크리스트1", trip);
        SharedCheckList checkList2 = createAndSaveSharedCheckList("체크리스트2", trip);
        SharedCheckList checkList3 = createAndSaveSharedCheckList("체크리스트3", trip);
        SharedCheckList checkList4 = createAndSaveSharedCheckList("체크리스트4", trip);
        SharedCheckList checkList5 = createAndSaveSharedCheckList("체크리스트5", trip);

        em.flush();
        em.clear();

        // when - 첫 번째 페이지
        List<CheckList> firstPage = checkListRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                3
        );

        // then
        assertThat(firstPage).hasSize(3);

        // when - 두 번째 페이지 (마지막 페이지)
        CheckList lastCheckList = firstPage.get(firstPage.size() - 1);
        List<CheckList> secondPage = checkListRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                lastCheckList.getCreatedAt().toString(),
                lastCheckList.getId(),
                3
        );

        // then
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage.get(0).getName()).isEqualTo("체크리스트4");
        assertThat(secondPage.get(1).getName()).isEqualTo("체크리스트5");
    }

    @Test
    @DisplayName("키워드와 커서를 함께 사용하여 검색한다")
    void findAllByCursor_withKeywordAndCursor() {
        // given
        Trip trip = createTrip("키워드 커서 조합 테스트");

        SharedCheckList checkList1 = createAndSaveSharedCheckList("짐 목록1", trip);
        SharedCheckList checkList2 = createAndSaveSharedCheckList("짐 목록2", trip);
        SharedCheckList checkList3 = createAndSaveSharedCheckList("짐 목록3", trip);
        SharedCheckList checkList4 = createAndSaveSharedCheckList("준비물", trip);

        em.flush();
        em.clear();

        // when - 첫 번째 페이지
        List<CheckList> firstPage = checkListRepository.findAllByCursor(
                trip.getId(),
                "짐",
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getName()).isEqualTo("짐 목록1");
        assertThat(firstPage.get(1).getName()).isEqualTo("짐 목록2");

        // when - 두 번째 페이지
        CheckList lastCheckList = firstPage.get(firstPage.size() - 1);
        List<CheckList> secondPage = checkListRepository.findAllByCursor(
                trip.getId(),
                "짐",
                "createdAt",
                "asc",
                lastCheckList.getCreatedAt().toString(),
                lastCheckList.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getName()).isEqualTo("짐 목록3");
    }

    @Test
    @DisplayName("여행 ID와 키워드를 함께 사용하여 커서 기반 페이지네이션을 수행한다")
    void findAllByCursor_withTripIdKeywordAndCursor() {
        // given
        Trip trip1 = createTrip("여행1");
        Trip trip2 = createTrip("여행2");

        SharedCheckList checkList1 = createAndSaveSharedCheckList("짐 싸기1", trip1);
        SharedCheckList checkList2 = createAndSaveSharedCheckList("짐 싸기2", trip1);
        SharedCheckList checkList3 = createAndSaveSharedCheckList("짐 싸기3", trip1);
        SharedCheckList checkList4 = createAndSaveSharedCheckList("짐 싸기", trip2);
        SharedCheckList checkList5 = createAndSaveSharedCheckList("준비물", trip1);

        em.flush();
        em.clear();

        // when - 여행1에서 '짐' 키워드로 검색
        List<CheckList> firstPage = checkListRepository.findAllByCursor(
                trip1.getId(),
                "짐",
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage)
                .extracting(CheckList::getName)
                .containsExactly("짐 싸기1", "짐 싸기2");

        // when - 다음 페이지
        CheckList lastCheckList = firstPage.get(firstPage.size() - 1);
        List<CheckList> secondPage = checkListRepository.findAllByCursor(
                trip1.getId(),
                "짐",
                "createdAt",
                "asc",
                lastCheckList.getCreatedAt().toString(),
                lastCheckList.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getName()).isEqualTo("짐 싸기3");
    }

    @Test
    @DisplayName("cursor만 null이고 after가 있는 경우 정상적으로 조회한다")
    void findAllByCursor_withNullCursorOnly() {
        // given
        Trip trip = createTrip("Cursor 테스트");

        SharedCheckList checkList1 = createAndSaveSharedCheckList("체크리스트A", trip);
        SharedCheckList checkList2 = createAndSaveSharedCheckList("체크리스트B", trip);
        SharedCheckList checkList3 = createAndSaveSharedCheckList("체크리스트C", trip);

        em.flush();
        em.clear();

        // when - cursor는 null이지만 after만 있는 경우
        List<CheckList> checkLists = checkListRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                null,
                1L,
                10
        );

        // then - cursor가 null이면 커서 조건이 적용되지 않고 모든 데이터 조회
        assertThat(checkLists).hasSize(3);
    }

    @Test
    @DisplayName("after만 null이고 cursor가 있는 경우 정상적으로 조회한다")
    void findAllByCursor_withNullAfterOnly() {
        // given
        Trip trip = createTrip("After 테스트");

        SharedCheckList checkList1 = createAndSaveSharedCheckList("체크리스트1", trip);
        SharedCheckList checkList2 = createAndSaveSharedCheckList("체크리스트2", trip);
        SharedCheckList checkList3 = createAndSaveSharedCheckList("체크리스트3", trip);

        em.flush();
        em.clear();

        // when - after는 null이지만 cursor만 있는 경우
        List<CheckList> checkLists = checkListRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                LocalDateTime.now().toString(),
                null,
                10
        );

        // then - after가 null이면 커서 조건이 적용되지 않고 모든 데이터 조회
        assertThat(checkLists).hasSize(3);
    }

    @Test
    @DisplayName("cursor와 after가 모두 있는 경우 - 오름차순 정렬")
    void findAllByCursor_withBothCursorAndAfter_asc() {
        // given
        Trip trip = createTrip("커서와 After 조합 테스트");

        SharedCheckList checkList1 = createAndSaveSharedCheckList("체크리스트1", trip);
        SharedCheckList checkList2 = createAndSaveSharedCheckList("체크리스트2", trip);
        SharedCheckList checkList3 = createAndSaveSharedCheckList("체크리스트3", trip);
        SharedCheckList checkList4 = createAndSaveSharedCheckList("체크리스트4", trip);

        em.flush();
        em.clear();

        // when - 첫 페이지
        List<CheckList> firstPage = checkListRepository.findAllByCursor(
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
        assertThat(firstPage.get(0).getName()).isEqualTo("체크리스트1");
        assertThat(firstPage.get(1).getName()).isEqualTo("체크리스트2");

        // when - cursor와 after 모두 사용하여 두 번째 페이지 조회
        CheckList lastItem = firstPage.get(firstPage.size() - 1);
        List<CheckList> secondPage = checkListRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                lastItem.getCreatedAt().toString(),
                lastItem.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage.get(0).getName()).isEqualTo("체크리스트3");
        assertThat(secondPage.get(1).getName()).isEqualTo("체크리스트4");
    }

    @Test
    @DisplayName("cursor와 after가 모두 있는 경우 - 내림차순 정렬")
    void findAllByCursor_withBothCursorAndAfter_desc() {
        // given
        Trip trip = createTrip("내림차순 커서와 After 테스트");

        SharedCheckList checkList1 = createAndSaveSharedCheckList("체크리스트1", trip);
        SharedCheckList checkList2 = createAndSaveSharedCheckList("체크리스트2", trip);
        SharedCheckList checkList3 = createAndSaveSharedCheckList("체크리스트3", trip);
        SharedCheckList checkList4 = createAndSaveSharedCheckList("체크리스트4", trip);

        em.flush();
        em.clear();

        // when - 첫 페이지 (내림차순)
        List<CheckList> firstPage = checkListRepository.findAllByCursor(
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
        assertThat(firstPage.get(0).getName()).isEqualTo("체크리스트4");
        assertThat(firstPage.get(1).getName()).isEqualTo("체크리스트3");

        // when - cursor와 after 모두 사용하여 두 번째 페이지 조회
        CheckList lastItem = firstPage.get(firstPage.size() - 1);
        List<CheckList> secondPage = checkListRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                lastItem.getCreatedAt().toString(),
                lastItem.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage.get(0).getName()).isEqualTo("체크리스트2");
        assertThat(secondPage.get(1).getName()).isEqualTo("체크리스트1");
    }

    @Test
    @DisplayName("cursor와 after 모두 있고 키워드 검색도 함께 사용")
    void findAllByCursor_withCursorAfterAndKeyword() {
        // given
        Trip trip = createTrip("커서 After 키워드 조합");

        SharedCheckList checkList1 = createAndSaveSharedCheckList("여행 준비1", trip);
        SharedCheckList checkList2 = createAndSaveSharedCheckList("여행 준비2", trip);
        SharedCheckList checkList3 = createAndSaveSharedCheckList("쇼핑 목록", trip);
        SharedCheckList checkList4 = createAndSaveSharedCheckList("여행 준비3", trip);

        em.flush();
        em.clear();

        // when - 첫 페이지 ('여행' 키워드로 검색)
        List<CheckList> firstPage = checkListRepository.findAllByCursor(
                trip.getId(),
                "여행",
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getName()).isEqualTo("여행 준비1");
        assertThat(firstPage.get(1).getName()).isEqualTo("여행 준비2");

        // when - cursor와 after 사용하여 다음 페이지
        CheckList lastItem = firstPage.get(firstPage.size() - 1);
        List<CheckList> secondPage = checkListRepository.findAllByCursor(
                trip.getId(),
                "여행",
                "createdAt",
                "asc",
                lastItem.getCreatedAt().toString(),
                lastItem.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getName()).isEqualTo("여행 준비3");
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

    private SharedCheckList createSharedCheckList(String name, Trip trip) {
        return SharedCheckList.createSharedCheckList(name, trip);
    }

    private PersonalCheckList createPersonalCheckList(String name, Trip trip, TripJoin tripJoin) {
        return PersonalCheckList.createPersonalCheckList(name, trip, tripJoin);
    }

    private SharedCheckList createAndSaveSharedCheckList(String name, Trip trip) {
        SharedCheckList checkList = SharedCheckList.createSharedCheckList(name, trip);
        return (SharedCheckList) checkListRepository.save(checkList);
    }

    private PersonalCheckList createAndSavePersonalCheckList(String name, Trip trip, TripJoin tripJoin) {
        PersonalCheckList checkList = PersonalCheckList.createPersonalCheckList(name, trip, tripJoin);
        return (PersonalCheckList) checkListRepository.save(checkList);
    }
}