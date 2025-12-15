package travel.mytravelplan.domain.checklist.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.checklist.entity.PersonalCheckList;
import travel.mytravelplan.domain.checklist.entity.PersonalCheckListItem;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("개인 체크리스트 항목 레포지토리 테스트")
class PersonalCheckListItemRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private PersonalCheckListItemRepository personalCheckListItemRepository;

    @Autowired
    private CheckListRepository checkListRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripJoinRepository tripJoinRepository;

    @Test
    @DisplayName("개인 체크리스트 항목을 저장한다")
    void savePersonalCheckListItem() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList personalCheckList = createAndSavePersonalCheckList("개인 체크리스트", trip, tripJoin);
        PersonalCheckListItem item = createPersonalCheckListItem("짐 싸기", personalCheckList);

        // when
        PersonalCheckListItem savedItem = personalCheckListItemRepository.save(item);
        em.flush();
        em.clear();

        // then
        assertThat(savedItem.getId()).isNotNull();
        assertThat(savedItem.getText()).isEqualTo("짐 싸기");
        assertThat(savedItem.getPersonalCheckList().getId()).isEqualTo(personalCheckList.getId());
        assertThat(savedItem.isChecked()).isFalse();
    }

    @Test
    @DisplayName("개인 체크리스트 항목을 ID로 조회한다")
    void findPersonalCheckListItemById() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList personalCheckList = createAndSavePersonalCheckList("개인 체크리스트", trip, tripJoin);
        PersonalCheckListItem item = createPersonalCheckListItem("짐 싸기", personalCheckList);
        PersonalCheckListItem savedItem = personalCheckListItemRepository.save(item);
        em.flush();
        em.clear();

        // when
        PersonalCheckListItem foundItem = personalCheckListItemRepository.findById(savedItem.getId()).orElse(null);

        // then
        assertThat(foundItem).isNotNull();
        assertThat(foundItem.getId()).isEqualTo(savedItem.getId());
        assertThat(foundItem.getText()).isEqualTo("짐 싸기");
        assertThat(foundItem.isChecked()).isFalse();
    }

    @Test
    @DisplayName("개인 체크리스트 항목을 수정한다")
    void updatePersonalCheckListItem() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList personalCheckList = createAndSavePersonalCheckList("개인 체크리스트", trip, tripJoin);
        PersonalCheckListItem item = createPersonalCheckListItem("짐 싸기", personalCheckList);
        PersonalCheckListItem savedItem = personalCheckListItemRepository.save(item);
        em.flush();
        em.clear();

        // when
        PersonalCheckListItem foundItem = personalCheckListItemRepository.findById(savedItem.getId()).orElse(null);
        assertThat(foundItem).isNotNull();
        foundItem.update("여권 챙기기", true);
        em.flush();
        em.clear();

        // then
        PersonalCheckListItem updatedItem = personalCheckListItemRepository.findById(savedItem.getId()).orElse(null);
        assertThat(updatedItem).isNotNull();
        assertThat(updatedItem.getText()).isEqualTo("여권 챙기기");
        assertThat(updatedItem.isChecked()).isTrue();
    }

    @Test
    @DisplayName("개인 체크리스트 항목을 삭제한다")
    void deletePersonalCheckListItem() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList personalCheckList = createAndSavePersonalCheckList("개인 체크리스트", trip, tripJoin);
        PersonalCheckListItem item = createPersonalCheckListItem("짐 싸기", personalCheckList);
        PersonalCheckListItem savedItem = personalCheckListItemRepository.save(item);
        em.flush();
        em.clear();

        // when
        personalCheckListItemRepository.deleteById(savedItem.getId());
        em.flush();
        em.clear();

        // then
        PersonalCheckListItem deletedItem = personalCheckListItemRepository.findById(savedItem.getId()).orElse(null);
        assertThat(deletedItem).isNull();
    }

    @Test
    @DisplayName("체크리스트 ID로 개인 체크리스트 항목 목록을 조회한다")
    void findAllByCursor_WithCheckListId() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList personalCheckList1 = createAndSavePersonalCheckList("개인 체크리스트1", trip, tripJoin);
        PersonalCheckList personalCheckList2 = createAndSavePersonalCheckList("개인 체크리스트2", trip, tripJoin);

        createAndSavePersonalCheckListItem("짐 싸기", personalCheckList1);
        createAndSavePersonalCheckListItem("여권 챙기기", personalCheckList1);
        createAndSavePersonalCheckListItem("비행기 예약", personalCheckList2);
        em.flush();
        em.clear();

        // when
        List<PersonalCheckListItem> items = personalCheckListItemRepository.findAllByCursor(
                personalCheckList1.getId(), null, "createdAt", "asc", null, null, 10
        );

        // then
        assertThat(items).hasSize(2);
        assertThat(items).extracting("text")
                .containsExactly("짐 싸기", "여권 챙기기");
    }

    @Test
    @DisplayName("키워드로 개인 체크리스트 항목 목록을 검색한다")
    void findAllByCursor_WithKeyword() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList personalCheckList = createAndSavePersonalCheckList("개인 체크리스트", trip, tripJoin);

        createAndSavePersonalCheckListItem("짐 싸기", personalCheckList);
        createAndSavePersonalCheckListItem("여권 챙기기", personalCheckList);
        createAndSavePersonalCheckListItem("비행기 예약", personalCheckList);
        em.flush();
        em.clear();

        // when
        List<PersonalCheckListItem> items = personalCheckListItemRepository.findAllByCursor(
                personalCheckList.getId(), "챙기기", "createdAt", "asc", null, null, 10
        );

        // then
        assertThat(items).hasSize(1);
        assertThat(items.getFirst().getText()).isEqualTo("여권 챙기기");
    }

    @Test
    @DisplayName("생성일 기준 오름차순으로 개인 체크리스트 항목 목록을 조회한다")
    void findAllByCursor_OrderByCreatedAtAsc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList personalCheckList = createAndSavePersonalCheckList("개인 체크리스트", trip, tripJoin);

        createAndSavePersonalCheckListItem("세 번째", personalCheckList);
        createAndSavePersonalCheckListItem("두 번째", personalCheckList);
        createAndSavePersonalCheckListItem("첫 번째", personalCheckList);
        em.flush();
        em.clear();

        // when
        List<PersonalCheckListItem> items = personalCheckListItemRepository.findAllByCursor(
                personalCheckList.getId(), null, "createdAt", "asc", null, null, 10
        );

        // then
        assertThat(items).hasSize(3);
        assertThat(items).extracting("text")
                .containsExactly("세 번째", "두 번째", "첫 번째");
    }

    @Test
    @DisplayName("생성일 기준 내림차순으로 개인 체크리스트 항목 목록을 조회한다")
    void findAllByCursor_OrderByCreatedAtDesc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList personalCheckList = createAndSavePersonalCheckList("개인 체크리스트", trip, tripJoin);

        createAndSavePersonalCheckListItem("세 번째", personalCheckList);
        createAndSavePersonalCheckListItem("두 번째", personalCheckList);
        createAndSavePersonalCheckListItem("첫 번째", personalCheckList);
        em.flush();
        em.clear();

        // when
        List<PersonalCheckListItem> items = personalCheckListItemRepository.findAllByCursor(
                personalCheckList.getId(), null, "createdAt", "desc", null, null, 10
        );

        // then
        assertThat(items).hasSize(3);
        assertThat(items).extracting("text")
                .containsExactly("첫 번째", "두 번째", "세 번째");
    }

    @Test
    @DisplayName("limit 조건으로 개인 체크리스트 항목 목록을 조회한다")
    void findAllByCursor_WithLimit() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList personalCheckList = createAndSavePersonalCheckList("개인 체크리스트", trip, tripJoin);

        createAndSavePersonalCheckListItem("첫 번째", personalCheckList);
        createAndSavePersonalCheckListItem("두 번째", personalCheckList);
        createAndSavePersonalCheckListItem("세 번째", personalCheckList);
        em.flush();
        em.clear();

        // when
        List<PersonalCheckListItem> items = personalCheckListItemRepository.findAllByCursor(
                personalCheckList.getId(), null, "createdAt", "asc", null, null, 2
        );

        // then
        assertThat(items).hasSize(2);
        assertThat(items).extracting("text")
                .containsExactly("첫 번째", "두 번째");
    }

    @Test
    @DisplayName("여러 조건을 조합하여 개인 체크리스트 항목 목록을 조회한다")
    void findAllByCursor_WithMultipleConditions() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList personalCheckList = createAndSavePersonalCheckList("개인 체크리스트", trip, tripJoin);

        createAndSavePersonalCheckListItem("짐 싸기", personalCheckList);
        createAndSavePersonalCheckListItem("여권 챙기기", personalCheckList);
        createAndSavePersonalCheckListItem("비행기 예약 챙기기", personalCheckList);
        createAndSavePersonalCheckListItem("호텔 예약", personalCheckList);
        em.flush();
        em.clear();

        // when
        List<PersonalCheckListItem> items = personalCheckListItemRepository.findAllByCursor(
                personalCheckList.getId(), "챙기기", "createdAt", "desc", null, null, 2
        );

        // then
        assertThat(items).hasSize(2);
        assertThat(items).extracting("text")
                .containsExactly("비행기 예약 챙기기", "여권 챙기기");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 항목을 조회한다 - 오름차순")
    void findAllByCursor_WithCursorPagination_Asc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList personalCheckList = createAndSavePersonalCheckList("개인 체크리스트", trip, tripJoin);

        createAndSavePersonalCheckListItem("항목1", personalCheckList);
        createAndSavePersonalCheckListItem("항목2", personalCheckList);
        createAndSavePersonalCheckListItem("항목3", personalCheckList);
        createAndSavePersonalCheckListItem("항목4", personalCheckList);
        em.flush();
        em.clear();

        // when - 첫 번째 페이지
        List<PersonalCheckListItem> firstPage = personalCheckListItemRepository.findAllByCursor(
                personalCheckList.getId(), null, "createdAt", "asc", null, null, 2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getText()).isEqualTo("항목1");
        assertThat(firstPage.get(1).getText()).isEqualTo("항목2");

        // when - 두 번째 페이지
        PersonalCheckListItem lastItem = firstPage.get(firstPage.size() - 1);
        List<PersonalCheckListItem> secondPage = personalCheckListItemRepository.findAllByCursor(
                personalCheckList.getId(), null, "createdAt", "asc",
                lastItem.getCreatedAt().toString(), lastItem.getId(), 2
        );

        // then
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage.get(0).getText()).isEqualTo("항목3");
        assertThat(secondPage.get(1).getText()).isEqualTo("항목4");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 항목을 조회한다 - 내림차순")
    void findAllByCursor_WithCursorPagination_Desc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList personalCheckList = createAndSavePersonalCheckList("개인 체크리스트", trip, tripJoin);

        createAndSavePersonalCheckListItem("항목1", personalCheckList);
        createAndSavePersonalCheckListItem("항목2", personalCheckList);
        createAndSavePersonalCheckListItem("항목3", personalCheckList);
        createAndSavePersonalCheckListItem("항목4", personalCheckList);
        em.flush();
        em.clear();

        // when - 첫 번째 페이지 (내림차순)
        List<PersonalCheckListItem> firstPage = personalCheckListItemRepository.findAllByCursor(
                personalCheckList.getId(), null, "createdAt", "desc", null, null, 2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getText()).isEqualTo("항목4");
        assertThat(firstPage.get(1).getText()).isEqualTo("항목3");

        // when - 두 번째 페이지
        PersonalCheckListItem lastItem = firstPage.get(firstPage.size() - 1);
        List<PersonalCheckListItem> secondPage = personalCheckListItemRepository.findAllByCursor(
                personalCheckList.getId(), null, "createdAt", "desc",
                lastItem.getCreatedAt().toString(), lastItem.getId(), 2
        );

        // then
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage.get(0).getText()).isEqualTo("항목2");
        assertThat(secondPage.get(1).getText()).isEqualTo("항목1");
    }

    @Test
    @DisplayName("커서와 after가 null인 경우 첫 페이지를 조회한다")
    void findAllByCursor_WithNullCursorAndAfter() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList personalCheckList = createAndSavePersonalCheckList("개인 체크리스트", trip, tripJoin);

        createAndSavePersonalCheckListItem("항목1", personalCheckList);
        createAndSavePersonalCheckListItem("항목2", personalCheckList);
        em.flush();
        em.clear();

        // when
        List<PersonalCheckListItem> items = personalCheckListItemRepository.findAllByCursor(
                personalCheckList.getId(), null, "createdAt", "asc", null, null, 10
        );

        // then
        assertThat(items).hasSize(2);
        assertThat(items.get(0).getText()).isEqualTo("항목1");
        assertThat(items.get(1).getText()).isEqualTo("항목2");
    }

    @Test
    @DisplayName("checkListId가 null인 경우 모든 항목을 조회한다")
    void findAllByCursor_WithNullCheckListId() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList personalCheckList1 = createAndSavePersonalCheckList("체크리스트1", trip, tripJoin);
        PersonalCheckList personalCheckList2 = createAndSavePersonalCheckList("체크리스트2", trip, tripJoin);

        createAndSavePersonalCheckListItem("항목A", personalCheckList1);
        createAndSavePersonalCheckListItem("항목B", personalCheckList2);
        em.flush();
        em.clear();

        // when
        List<PersonalCheckListItem> items = personalCheckListItemRepository.findAllByCursor(
                null, null, "createdAt", "asc", null, null, 10
        );

        // then
        assertThat(items).hasSize(2);
    }

    @Test
    @DisplayName("키워드가 빈 문자열인 경우 모든 항목을 조회한다")
    void findAllByCursor_WithEmptyKeyword() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList personalCheckList = createAndSavePersonalCheckList("개인 체크리스트", trip, tripJoin);

        createAndSavePersonalCheckListItem("호텔", personalCheckList);
        createAndSavePersonalCheckListItem("식비", personalCheckList);
        em.flush();
        em.clear();

        // when
        List<PersonalCheckListItem> items = personalCheckListItemRepository.findAllByCursor(
                personalCheckList.getId(), "", "createdAt", "asc", null, null, 10
        );

        // then
        assertThat(items).hasSize(2);
    }

    @Test
    @DisplayName("키워드 검색 시 대소문자를 구분하지 않는다")
    void findAllByCursor_WithKeywordCaseInsensitive() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList personalCheckList = createAndSavePersonalCheckList("개인 체크리스트", trip, tripJoin);

        createAndSavePersonalCheckListItem("Hotel Booking", personalCheckList);
        createAndSavePersonalCheckListItem("restaurant", personalCheckList);
        em.flush();
        em.clear();

        // when
        List<PersonalCheckListItem> items = personalCheckListItemRepository.findAllByCursor(
                personalCheckList.getId(), "hotel", "createdAt", "asc", null, null, 10
        );

        // then
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getText()).isEqualTo("Hotel Booking");
    }

    @Test
    @DisplayName("cursor만 null이고 after가 있는 경우 정상적으로 조회한다")
    void findAllByCursor_WithNullCursorOnly() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList personalCheckList = createAndSavePersonalCheckList("개인 체크리스트", trip, tripJoin);

        createAndSavePersonalCheckListItem("항목A", personalCheckList);
        createAndSavePersonalCheckListItem("항목B", personalCheckList);
        createAndSavePersonalCheckListItem("항목C", personalCheckList);
        em.flush();
        em.clear();

        // when - cursor는 null이지만 after만 있는 경우
        List<PersonalCheckListItem> items = personalCheckListItemRepository.findAllByCursor(
                personalCheckList.getId(), null, "createdAt", "asc", null, 1L, 10
        );

        // then - cursor가 null이면 커서 조건이 적용되지 않고 모든 데이터 조회
        assertThat(items).hasSize(3);
    }

    @Test
    @DisplayName("after만 null이고 cursor가 있는 경우 정상적으로 조회한다")
    void findAllByCursor_WithNullAfterOnly() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList personalCheckList = createAndSavePersonalCheckList("개인 체크리스트", trip, tripJoin);

        createAndSavePersonalCheckListItem("항목1", personalCheckList);
        createAndSavePersonalCheckListItem("항목2", personalCheckList);
        createAndSavePersonalCheckListItem("항목3", personalCheckList);
        em.flush();
        em.clear();

        // when - after는 null이지만 cursor만 있는 경우
        List<PersonalCheckListItem> items = personalCheckListItemRepository.findAllByCursor(
                personalCheckList.getId(), null, "createdAt", "asc",
                java.time.LocalDateTime.now().toString(), null, 10
        );

        // then - after가 null이면 커서 조건이 적용되지 않고 모든 데이터 조회
        assertThat(items).hasSize(3);
    }

    @Test
    @DisplayName("동일한 createdAt을 가진 항목들을 ID로 정렬한다 - 오름차순")
    void findAllByCursor_WithSameCreatedAtOrderByIdAsc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList personalCheckList = createAndSavePersonalCheckList("개인 체크리스트", trip, tripJoin);

        createAndSavePersonalCheckListItem("항목A", personalCheckList);
        createAndSavePersonalCheckListItem("항목B", personalCheckList);
        createAndSavePersonalCheckListItem("항목C", personalCheckList);
        em.flush();
        em.clear();

        // when
        List<PersonalCheckListItem> items = personalCheckListItemRepository.findAllByCursor(
                personalCheckList.getId(), null, "createdAt", "asc", null, null, 10
        );

        // then - ID 순으로 정렬되어야 함
        assertThat(items).hasSize(3);
        for (int i = 0; i < items.size() - 1; i++) {
            assertThat(items.get(i).getId()).isLessThan(items.get(i + 1).getId());
        }
    }

    @Test
    @DisplayName("동일한 createdAt을 가진 항목들을 ID로 정렬한다 - 내림차순")
    void findAllByCursor_WithSameCreatedAtOrderByIdDesc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList personalCheckList = createAndSavePersonalCheckList("개인 체크리스트", trip, tripJoin);

        createAndSavePersonalCheckListItem("항목X", personalCheckList);
        createAndSavePersonalCheckListItem("항목Y", personalCheckList);
        createAndSavePersonalCheckListItem("항목Z", personalCheckList);
        em.flush();
        em.clear();

        // when
        List<PersonalCheckListItem> items = personalCheckListItemRepository.findAllByCursor(
                personalCheckList.getId(), null, "createdAt", "desc", null, null, 10
        );

        // then - ID 역순으로 정렬되어야 함
        assertThat(items).hasSize(3);
        for (int i = 0; i < items.size() - 1; i++) {
            assertThat(items.get(i).getId()).isGreaterThan(items.get(i + 1).getId());
        }
    }

    @Test
    @DisplayName("키워드와 커서를 함께 사용하여 검색한다")
    void findAllByCursor_WithKeywordAndCursor() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList personalCheckList = createAndSavePersonalCheckList("개인 체크리스트", trip, tripJoin);

        createAndSavePersonalCheckListItem("준비 항목1", personalCheckList);
        createAndSavePersonalCheckListItem("준비 항목2", personalCheckList);
        createAndSavePersonalCheckListItem("준비 항목3", personalCheckList);
        createAndSavePersonalCheckListItem("기타 항목", personalCheckList);
        em.flush();
        em.clear();

        // when - 첫 번째 페이지
        List<PersonalCheckListItem> firstPage = personalCheckListItemRepository.findAllByCursor(
                personalCheckList.getId(), "준비", "createdAt", "asc", null, null, 2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getText()).isEqualTo("준비 항목1");
        assertThat(firstPage.get(1).getText()).isEqualTo("준비 항목2");

        // when - 두 번째 페이지
        PersonalCheckListItem lastItem = firstPage.get(firstPage.size() - 1);
        List<PersonalCheckListItem> secondPage = personalCheckListItemRepository.findAllByCursor(
                personalCheckList.getId(), "준비", "createdAt", "asc",
                lastItem.getCreatedAt().toString(), lastItem.getId(), 2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getText()).isEqualTo("준비 항목3");
    }

    @Test
    @DisplayName("여러 체크리스트 중 특정 체크리스트의 항목만 조회한다")
    void findAllByCursor_FilterByCheckListId() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList personalCheckList1 = createAndSavePersonalCheckList("체크리스트1", trip, tripJoin);
        PersonalCheckList personalCheckList2 = createAndSavePersonalCheckList("체크리스트2", trip, tripJoin);

        createAndSavePersonalCheckListItem("체크리스트1 항목", personalCheckList1);
        createAndSavePersonalCheckListItem("체크리스트2 항목", personalCheckList2);
        em.flush();
        em.clear();

        // when
        List<PersonalCheckListItem> items = personalCheckListItemRepository.findAllByCursor(
                personalCheckList1.getId(), null, "createdAt", "asc", null, null, 10
        );

        // then
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getText()).isEqualTo("체크리스트1 항목");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션에서 마지막 페이지를 조회한다")
    void findAllByCursor_LastPage() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList personalCheckList = createAndSavePersonalCheckList("개인 체크리스트", trip, tripJoin);

        createAndSavePersonalCheckListItem("항목1", personalCheckList);
        createAndSavePersonalCheckListItem("항목2", personalCheckList);
        createAndSavePersonalCheckListItem("항목3", personalCheckList);
        createAndSavePersonalCheckListItem("항목4", personalCheckList);
        createAndSavePersonalCheckListItem("항목5", personalCheckList);
        em.flush();
        em.clear();

        // when - 첫 번째 페이지
        List<PersonalCheckListItem> firstPage = personalCheckListItemRepository.findAllByCursor(
                personalCheckList.getId(), null, "createdAt", "asc", null, null, 3
        );

        // then
        assertThat(firstPage).hasSize(3);

        // when - 두 번째 페이지 (마지막 페이지)
        PersonalCheckListItem lastItem = firstPage.get(firstPage.size() - 1);
        List<PersonalCheckListItem> secondPage = personalCheckListItemRepository.findAllByCursor(
                personalCheckList.getId(), null, "createdAt", "asc",
                lastItem.getCreatedAt().toString(), lastItem.getId(), 3
        );

        // then
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage.get(0).getText()).isEqualTo("항목4");
        assertThat(secondPage.get(1).getText()).isEqualTo("항목5");
    }

    @Test
    @DisplayName("체크리스트 ID와 키워드를 함께 사용하여 커서 기반 페이지네이션을 수행한다")
    void findAllByCursor_WithCheckListIdKeywordAndCursor() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList personalCheckList1 = createAndSavePersonalCheckList("체크리스트1", trip, tripJoin);
        PersonalCheckList personalCheckList2 = createAndSavePersonalCheckList("체크리스트2", trip, tripJoin);

        createAndSavePersonalCheckListItem("준비 항목1", personalCheckList1);
        createAndSavePersonalCheckListItem("준비 항목2", personalCheckList1);
        createAndSavePersonalCheckListItem("준비 항목3", personalCheckList1);
        createAndSavePersonalCheckListItem("준비 항목", personalCheckList2);
        createAndSavePersonalCheckListItem("기타 항목", personalCheckList1);
        em.flush();
        em.clear();

        // when - 체크리스트1에서 '준비' 키워드로 검색
        List<PersonalCheckListItem> firstPage = personalCheckListItemRepository.findAllByCursor(
                personalCheckList1.getId(), "준비", "createdAt", "asc", null, null, 2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage).extracting("text")
                .containsExactly("준비 항목1", "준비 항목2");

        // when - 다음 페이지
        PersonalCheckListItem lastItem = firstPage.get(firstPage.size() - 1);
        List<PersonalCheckListItem> secondPage = personalCheckListItemRepository.findAllByCursor(
                personalCheckList1.getId(), "준비", "createdAt", "asc",
                lastItem.getCreatedAt().toString(), lastItem.getId(), 2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getText()).isEqualTo("준비 항목3");
    }

    @Test
    @DisplayName("체크 상태를 변경한다")
    void updateCheckStatus() {
        // given
        User user = createUser("testUser", "test@email.com");
        Trip trip = createTrip("여행 계획");
        TripJoin tripJoin = createTripJoin(trip, user);
        PersonalCheckList personalCheckList = createAndSavePersonalCheckList("개인 체크리스트", trip, tripJoin);
        PersonalCheckListItem item = createPersonalCheckListItem("짐 싸기", personalCheckList);
        PersonalCheckListItem savedItem = personalCheckListItemRepository.save(item);
        em.flush();
        em.clear();

        // when - 체크 상태를 true로 변경
        PersonalCheckListItem foundItem = personalCheckListItemRepository.findById(savedItem.getId()).orElse(null);
        assertThat(foundItem).isNotNull();
        foundItem.update("짐 싸기", true);
        em.flush();
        em.clear();

        // then
        PersonalCheckListItem checkedItem = personalCheckListItemRepository.findById(savedItem.getId()).orElse(null);
        assertThat(checkedItem).isNotNull();
        assertThat(checkedItem.isChecked()).isTrue();

        // when - 체크 상태를 false로 변경
        PersonalCheckListItem foundItem2 = personalCheckListItemRepository.findById(savedItem.getId()).orElse(null);
        assertThat(foundItem2).isNotNull();
        foundItem2.update("짐 싸기", false);
        em.flush();
        em.clear();

        // then
        PersonalCheckListItem uncheckedItem = personalCheckListItemRepository.findById(savedItem.getId()).orElse(null);
        assertThat(uncheckedItem).isNotNull();
        assertThat(uncheckedItem.isChecked()).isFalse();
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

    private PersonalCheckList createAndSavePersonalCheckList(String name, Trip trip, TripJoin tripJoin) {
        PersonalCheckList checkList = PersonalCheckList.createPersonalCheckList(name, trip, tripJoin);
        checkListRepository.save(checkList);
        return checkList;
    }

    private PersonalCheckListItem createPersonalCheckListItem(String text, PersonalCheckList personalCheckList) {
        return PersonalCheckListItem.createPersonalCheckListItem(text, personalCheckList);
    }

    private PersonalCheckListItem createAndSavePersonalCheckListItem(String text, PersonalCheckList personalCheckList) {
        PersonalCheckListItem item = PersonalCheckListItem.createPersonalCheckListItem(text, personalCheckList);
        return personalCheckListItemRepository.save(item);
    }
}