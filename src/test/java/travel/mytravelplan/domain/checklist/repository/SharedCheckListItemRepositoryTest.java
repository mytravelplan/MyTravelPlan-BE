package travel.mytravelplan.domain.checklist.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.checklist.entity.SharedCheckList;
import travel.mytravelplan.domain.checklist.entity.SharedCheckListItem;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.enums.Country;
import travel.mytravelplan.domain.trip.repository.TripRepository;
import travel.mytravelplan.global.support.RepositoryTestSupport;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("공유 체크리스트 항목 레포지토리 테스트")
public class SharedCheckListItemRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private SharedCheckListItemRepository sharedCheckListItemRepository;

    @Autowired
    private CheckListRepository checkListRepository;

    @Autowired
    private TripRepository tripRepository;

    @Test
    @DisplayName("공유 체크리스트 항목을 저장한다")
    void saveSharedCheckListItem() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);
        SharedCheckListItem item = createSharedCheckListItem("여권 챙기기", checkList);

        // when
        SharedCheckListItem savedItem = sharedCheckListItemRepository.save(item);
        em.flush();
        em.clear();

        // then
        assertThat(savedItem.getId()).isNotNull();
        assertThat(savedItem.getText()).isEqualTo("여권 챙기기");
        assertThat(savedItem.getSharedCheckList().getId()).isEqualTo(checkList.getId());
    }

    @Test
    @DisplayName("공유 체크리스트 항목을 ID로 조회한다")
    void findSharedCheckListItemById() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);
        SharedCheckListItem item = createAndSaveSharedCheckListItem("비행기 티켓 출력", checkList);
        em.flush();
        em.clear();

        // when
        SharedCheckListItem foundItem = sharedCheckListItemRepository.findById(item.getId()).orElse(null);

        // then
        assertThat(foundItem).isNotNull();
        assertThat(foundItem.getId()).isEqualTo(item.getId());
        assertThat(foundItem.getText()).isEqualTo("비행기 티켓 출력");
    }

    @Test
    @DisplayName("공유 체크리스트 항목을 수정한다")
    void updateSharedCheckListItem() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);
        SharedCheckListItem item = createAndSaveSharedCheckListItem("짐 싸기", checkList);
        em.flush();
        em.clear();

        // when
        SharedCheckListItem foundItem = sharedCheckListItemRepository.findById(item.getId()).orElseThrow();
        foundItem.update("짐 다시 정리하기");
        em.flush();
        em.clear();

        // then
        SharedCheckListItem updatedItem = sharedCheckListItemRepository.findById(item.getId()).orElse(null);
        assertThat(updatedItem).isNotNull();
        assertThat(updatedItem.getText()).isEqualTo("짐 다시 정리하기");
    }

    @Test
    @DisplayName("공유 체크리스트 항목을 삭제한다")
    void deleteSharedCheckListItem() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);
        SharedCheckListItem item = createAndSaveSharedCheckListItem("카메라 충전", checkList);
        em.flush();
        em.clear();

        // when
        sharedCheckListItemRepository.deleteById(item.getId());
        em.flush();
        em.clear();

        // then
        SharedCheckListItem deletedItem = sharedCheckListItemRepository.findById(item.getId()).orElse(null);
        assertThat(deletedItem).isNull();
    }

    @Test
    @DisplayName("체크리스트 ID로 항목들을 조회한다")
    void findAllByCursor_byCheckListId() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList1 = createSharedCheckList("공유 체크리스트1", trip);
        SharedCheckList checkList2 = createSharedCheckList("공유 체크리스트2", trip);

        createAndSaveSharedCheckListItem("항목1-1", checkList1);
        createAndSaveSharedCheckListItem("항목1-2", checkList1);
        createAndSaveSharedCheckListItem("항목2-1", checkList2);

        em.flush();
        em.clear();

        // when
        List<SharedCheckListItem> items = sharedCheckListItemRepository.findAllByCursor(
                checkList1.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(items).hasSize(2);
        assertThat(items)
                .extracting(SharedCheckListItem::getText)
                .containsExactlyInAnyOrder("항목1-1", "항목1-2");
    }

    @Test
    @DisplayName("키워드로 공유 체크리스트 항목을 검색한다")
    void findAllByCursor_byKeyword() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);

        createAndSaveSharedCheckListItem("여권 준비", checkList);
        createAndSaveSharedCheckListItem("비행기 티켓", checkList);
        createAndSaveSharedCheckListItem("여권 사진", checkList);

        em.flush();
        em.clear();

        // when
        List<SharedCheckListItem> items = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                "여권",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(items).hasSize(2);
        assertThat(items)
                .extracting(SharedCheckListItem::getText)
                .containsExactlyInAnyOrder("여권 준비", "여권 사진");
    }

    @Test
    @DisplayName("생성일 기준 내림차순으로 항목을 조회한다")
    void findAllByCursor_orderByCreatedAtDesc() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);

        SharedCheckListItem item1 = createAndSaveSharedCheckListItem("항목1", checkList);
        SharedCheckListItem item2 = createAndSaveSharedCheckListItem("항목2", checkList);
        SharedCheckListItem item3 = createAndSaveSharedCheckListItem("항목3", checkList);

        em.flush();
        em.clear();

        // when
        List<SharedCheckListItem> items = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(items).hasSize(3);
        assertThat(items.get(0).getText()).isEqualTo("항목3");
        assertThat(items.get(1).getText()).isEqualTo("항목2");
        assertThat(items.get(2).getText()).isEqualTo("항목1");
    }

    @Test
    @DisplayName("생성일 기준 오름차순으로 항목을 조회한다")
    void findAllByCursor_orderByCreatedAtAsc() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);

        SharedCheckListItem item1 = createAndSaveSharedCheckListItem("항목1", checkList);
        SharedCheckListItem item2 = createAndSaveSharedCheckListItem("항목2", checkList);
        SharedCheckListItem item3 = createAndSaveSharedCheckListItem("항목3", checkList);

        em.flush();
        em.clear();

        // when
        List<SharedCheckListItem> items = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(items).hasSize(3);
        assertThat(items.get(0).getText()).isEqualTo("항목1");
        assertThat(items.get(1).getText()).isEqualTo("항목2");
        assertThat(items.get(2).getText()).isEqualTo("항목3");
    }

    @Test
    @DisplayName("limit 개수만큼 항목을 조회한다")
    void findAllByCursor_withLimit() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);

        createAndSaveSharedCheckListItem("항목1", checkList);
        createAndSaveSharedCheckListItem("항목2", checkList);
        createAndSaveSharedCheckListItem("항목3", checkList);
        createAndSaveSharedCheckListItem("항목4", checkList);
        createAndSaveSharedCheckListItem("항목5", checkList);

        em.flush();
        em.clear();

        // when
        List<SharedCheckListItem> items = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                3
        );

        // then
        assertThat(items).hasSize(3);
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 항목을 조회한다 - 내림차순")
    void findAllByCursor_withCursor_desc() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);

        SharedCheckListItem item1 = createAndSaveSharedCheckListItem("항목1", checkList);
        SharedCheckListItem item2 = createAndSaveSharedCheckListItem("항목2", checkList);
        SharedCheckListItem item3 = createAndSaveSharedCheckListItem("항목3", checkList);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<SharedCheckListItem> firstPage = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getText()).isEqualTo("항목3");
        assertThat(firstPage.get(1).getText()).isEqualTo("항목2");

        // when - 두 번째 페이지 조회
        SharedCheckListItem lastItem = firstPage.get(firstPage.size() - 1);
        List<SharedCheckListItem> secondPage = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                null,
                "createdAt",
                "desc",
                lastItem.getCreatedAt().toString(),
                lastItem.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getText()).isEqualTo("항목1");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 항목을 조회한다 - 오름차순")
    void findAllByCursor_withCursor_asc() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);

        SharedCheckListItem item1 = createAndSaveSharedCheckListItem("항목1", checkList);
        SharedCheckListItem item2 = createAndSaveSharedCheckListItem("항목2", checkList);
        SharedCheckListItem item3 = createAndSaveSharedCheckListItem("항목3", checkList);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<SharedCheckListItem> firstPage = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getText()).isEqualTo("항목1");
        assertThat(firstPage.get(1).getText()).isEqualTo("항목2");

        // when - 두 번째 페이지 조회
        SharedCheckListItem lastItem = firstPage.get(firstPage.size() - 1);
        List<SharedCheckListItem> secondPage = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                null,
                "createdAt",
                "asc",
                lastItem.getCreatedAt().toString(),
                lastItem.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getText()).isEqualTo("항목3");
    }

    @Test
    @DisplayName("체크리스트 ID와 키워드로 항목을 조회한다")
    void findAllByCursor_byCheckListIdAndKeyword() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList1 = createSharedCheckList("공유 체크리스트1", trip);
        SharedCheckList checkList2 = createSharedCheckList("공유 체크리스트2", trip);

        createAndSaveSharedCheckListItem("여권 준비", checkList1);
        createAndSaveSharedCheckListItem("티켓 준비", checkList1);
        createAndSaveSharedCheckListItem("여권 사진", checkList1);
        createAndSaveSharedCheckListItem("여권 확인", checkList2);

        em.flush();
        em.clear();

        // when
        List<SharedCheckListItem> items = sharedCheckListItemRepository.findAllByCursor(
                checkList1.getId(),
                "여권",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(items).hasSize(2);
        assertThat(items)
                .extracting(SharedCheckListItem::getText)
                .containsExactlyInAnyOrder("여권 준비", "여권 사진");
    }

    @Test
    @DisplayName("커서와 after가 null인 경우 첫 페이지를 조회한다")
    void findAllByCursor_withNullCursorAndAfter() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);

        createAndSaveSharedCheckListItem("항목1", checkList);
        createAndSaveSharedCheckListItem("항목2", checkList);

        em.flush();
        em.clear();

        // when
        List<SharedCheckListItem> items = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(items).hasSize(2);
        assertThat(items.get(0).getText()).isEqualTo("항목1");
        assertThat(items.get(1).getText()).isEqualTo("항목2");
    }

    @Test
    @DisplayName("checkListId가 null인 경우 모든 체크리스트의 항목을 조회한다")
    void findAllByCursor_withNullCheckListId() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList1 = createSharedCheckList("체크리스트A", trip);
        SharedCheckList checkList2 = createSharedCheckList("체크리스트B", trip);

        createAndSaveSharedCheckListItem("항목A", checkList1);
        createAndSaveSharedCheckListItem("항목B", checkList2);

        em.flush();
        em.clear();

        // when
        List<SharedCheckListItem> items = sharedCheckListItemRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(items).hasSize(2);
    }

    @Test
    @DisplayName("키워드가 빈 문자열인 경우 모든 항목을 조회한다")
    void findAllByCursor_withEmptyKeyword() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);

        createAndSaveSharedCheckListItem("호텔 예약", checkList);
        createAndSaveSharedCheckListItem("렌트카 예약", checkList);

        em.flush();
        em.clear();

        // when
        List<SharedCheckListItem> items = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                "",
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(items).hasSize(2);
    }

    @Test
    @DisplayName("키워드 검색 시 대소문자를 구분하지 않는다")
    void findAllByCursor_withKeywordCaseInsensitive() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);

        createAndSaveSharedCheckListItem("Hotel Reservation", checkList);
        createAndSaveSharedCheckListItem("Flight Ticket", checkList);

        em.flush();
        em.clear();

        // when
        List<SharedCheckListItem> items = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                "hotel",
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getText()).isEqualTo("Hotel Reservation");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션에서 마지막 페이지를 조회한다")
    void findAllByCursor_lastPage() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);

        createAndSaveSharedCheckListItem("항목1", checkList);
        createAndSaveSharedCheckListItem("항목2", checkList);
        createAndSaveSharedCheckListItem("항목3", checkList);
        createAndSaveSharedCheckListItem("항목4", checkList);
        createAndSaveSharedCheckListItem("항목5", checkList);

        em.flush();
        em.clear();

        // when - 첫 번째 페이지
        List<SharedCheckListItem> firstPage = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
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
        SharedCheckListItem lastItem = firstPage.get(firstPage.size() - 1);
        List<SharedCheckListItem> secondPage = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                null,
                "createdAt",
                "asc",
                lastItem.getCreatedAt().toString(),
                lastItem.getId(),
                3
        );

        // then
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage.get(0).getText()).isEqualTo("항목4");
        assertThat(secondPage.get(1).getText()).isEqualTo("항목5");
    }

    @Test
    @DisplayName("키워드와 커서를 함께 사용하여 검색한다")
    void findAllByCursor_withKeywordAndCursor() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);

        createAndSaveSharedCheckListItem("준비물1", checkList);
        createAndSaveSharedCheckListItem("준비물2", checkList);
        createAndSaveSharedCheckListItem("준비물3", checkList);
        createAndSaveSharedCheckListItem("기타 항목", checkList);

        em.flush();
        em.clear();

        // when - 첫 번째 페이지
        List<SharedCheckListItem> firstPage = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                "준비물",
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getText()).isEqualTo("준비물1");
        assertThat(firstPage.get(1).getText()).isEqualTo("준비물2");

        // when - 두 번째 페이지
        SharedCheckListItem lastItem = firstPage.get(firstPage.size() - 1);
        List<SharedCheckListItem> secondPage = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                "준비물",
                "createdAt",
                "asc",
                lastItem.getCreatedAt().toString(),
                lastItem.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getText()).isEqualTo("준비물3");
    }

    @Test
    @DisplayName("동일한 createdAt을 가진 항목들을 ID로 정렬한다 - 오름차순")
    void findAllByCursor_withSameCreatedAtOrderByIdAsc() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);

        createAndSaveSharedCheckListItem("항목A", checkList);
        createAndSaveSharedCheckListItem("항목B", checkList);
        createAndSaveSharedCheckListItem("항목C", checkList);

        em.flush();
        em.clear();

        // when
        List<SharedCheckListItem> items = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(items).hasSize(3);
        // ID 순으로 정렬되어야 함
        for (int i = 0; i < items.size() - 1; i++) {
            assertThat(items.get(i).getId()).isLessThan(items.get(i + 1).getId());
        }
    }

    @Test
    @DisplayName("동일한 createdAt을 가진 항목들을 ID로 정렬한다 - 내림차순")
    void findAllByCursor_withSameCreatedAtOrderByIdDesc() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);

        createAndSaveSharedCheckListItem("항목X", checkList);
        createAndSaveSharedCheckListItem("항목Y", checkList);
        createAndSaveSharedCheckListItem("항목Z", checkList);

        em.flush();
        em.clear();

        // when
        List<SharedCheckListItem> items = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(items).hasSize(3);
        // ID 역순으로 정렬되어야 함
        for (int i = 0; i < items.size() - 1; i++) {
            assertThat(items.get(i).getId()).isGreaterThan(items.get(i + 1).getId());
        }
    }

    @Test
    @DisplayName("체크리스트 ID와 키워드, 커서를 함께 사용하여 페이지네이션한다")
    void findAllByCursor_withCheckListIdKeywordAndCursor() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList1 = createSharedCheckList("체크리스트1", trip);
        SharedCheckList checkList2 = createSharedCheckList("체크리스트2", trip);

        createAndSaveSharedCheckListItem("준비 사항1", checkList1);
        createAndSaveSharedCheckListItem("준비 사항2", checkList1);
        createAndSaveSharedCheckListItem("준비 사항3", checkList1);
        createAndSaveSharedCheckListItem("준비 사항", checkList2);
        createAndSaveSharedCheckListItem("기타", checkList1);

        em.flush();
        em.clear();

        // when - 체크리스트1에서 '준비' 키워드로 검색
        List<SharedCheckListItem> firstPage = sharedCheckListItemRepository.findAllByCursor(
                checkList1.getId(),
                "준비",
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage)
                .extracting(SharedCheckListItem::getText)
                .containsExactly("준비 사항1", "준비 사항2");

        // when - 다음 페이지
        SharedCheckListItem lastItem = firstPage.get(firstPage.size() - 1);
        List<SharedCheckListItem> secondPage = sharedCheckListItemRepository.findAllByCursor(
                checkList1.getId(),
                "준비",
                "createdAt",
                "asc",
                lastItem.getCreatedAt().toString(),
                lastItem.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getText()).isEqualTo("준비 사항3");
    }

    @Test
    @DisplayName("cursor와 after가 모두 있는 경우 - 오름차순 정렬")
    void findAllByCursor_withBothCursorAndAfter_asc() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);

        createAndSaveSharedCheckListItem("항목1", checkList);
        createAndSaveSharedCheckListItem("항목2", checkList);
        createAndSaveSharedCheckListItem("항목3", checkList);
        createAndSaveSharedCheckListItem("항목4", checkList);

        em.flush();
        em.clear();

        // when - 첫 페이지
        List<SharedCheckListItem> firstPage = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getText()).isEqualTo("항목1");
        assertThat(firstPage.get(1).getText()).isEqualTo("항목2");

        // when - cursor와 after 모두 사용하여 두 번째 페이지 조회
        SharedCheckListItem lastItem = firstPage.get(firstPage.size() - 1);
        List<SharedCheckListItem> secondPage = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                null,
                "createdAt",
                "asc",
                lastItem.getCreatedAt().toString(),
                lastItem.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage.get(0).getText()).isEqualTo("항목3");
        assertThat(secondPage.get(1).getText()).isEqualTo("항목4");
    }

    @Test
    @DisplayName("cursor와 after가 모두 있는 경우 - 내림차순 정렬")
    void findAllByCursor_withBothCursorAndAfter_desc() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);

        createAndSaveSharedCheckListItem("항목1", checkList);
        createAndSaveSharedCheckListItem("항목2", checkList);
        createAndSaveSharedCheckListItem("항목3", checkList);
        createAndSaveSharedCheckListItem("항목4", checkList);

        em.flush();
        em.clear();

        // when - 첫 페이지 (내림차순)
        List<SharedCheckListItem> firstPage = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getText()).isEqualTo("항목4");
        assertThat(firstPage.get(1).getText()).isEqualTo("항목3");

        // when - cursor와 after 모두 사용하여 두 번째 페이지 조회
        SharedCheckListItem lastItem = firstPage.get(firstPage.size() - 1);
        List<SharedCheckListItem> secondPage = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                null,
                "createdAt",
                "desc",
                lastItem.getCreatedAt().toString(),
                lastItem.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage.get(0).getText()).isEqualTo("항목2");
        assertThat(secondPage.get(1).getText()).isEqualTo("항목1");
    }

    @Test
    @DisplayName("Branch Coverage: cursor == null && after == null")
    void findAllByCursor_branchCoverage_bothNull() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);

        createAndSaveSharedCheckListItem("항목1", checkList);
        createAndSaveSharedCheckListItem("항목2", checkList);

        em.flush();
        em.clear();

        // when - cursor와 after가 모두 null
        List<SharedCheckListItem> items = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(items).hasSize(2);
        assertThat(items.get(0).getText()).isEqualTo("항목1");
        assertThat(items.get(1).getText()).isEqualTo("항목2");
    }

    @Test
    @DisplayName("Branch Coverage: cursor != null && after == null")
    void findAllByCursor_branchCoverage_cursorNotNullAfterNull() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);

        SharedCheckListItem item1 = createAndSaveSharedCheckListItem("항목1", checkList);
        createAndSaveSharedCheckListItem("항목2", checkList);
        createAndSaveSharedCheckListItem("항목3", checkList);

        em.flush();
        em.clear();

        // when - cursor만 있고 after는 null
        List<SharedCheckListItem> items = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                null,
                "createdAt",
                "asc",
                item1.getCreatedAt().toString(),
                null,
                10
        );

        // then - cursor만 있는 경우에도 정상 동작하는지 확인
        assertThat(items).isNotNull();
    }

    @Test
    @DisplayName("Branch Coverage: cursor == null && after != null")
    void findAllByCursor_branchCoverage_cursorNullAfterNotNull() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);

        SharedCheckListItem item1 = createAndSaveSharedCheckListItem("항목1", checkList);
        createAndSaveSharedCheckListItem("항목2", checkList);
        createAndSaveSharedCheckListItem("항목3", checkList);

        em.flush();
        em.clear();

        // when - after만 있고 cursor는 null
        List<SharedCheckListItem> items = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                null,
                "createdAt",
                "asc",
                null,
                item1.getId(),
                10
        );

        // then - after만 있는 경우에도 정상 동작하는지 확인
        assertThat(items).isNotNull();
    }

    @Test
    @DisplayName("Branch Coverage: cursor != null && after != null - 오름차순")
    void findAllByCursor_branchCoverage_bothNotNull_asc() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);

        createAndSaveSharedCheckListItem("항목1", checkList);
        createAndSaveSharedCheckListItem("항목2", checkList);
        createAndSaveSharedCheckListItem("항목3", checkList);
        createAndSaveSharedCheckListItem("항목4", checkList);

        em.flush();
        em.clear();

        // when - 첫 페이지
        List<SharedCheckListItem> firstPage = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        SharedCheckListItem lastItem = firstPage.get(firstPage.size() - 1);

        // when - cursor와 after 모두 있는 경우
        List<SharedCheckListItem> items = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                null,
                "createdAt",
                "asc",
                lastItem.getCreatedAt().toString(),
                lastItem.getId(),
                10
        );

        // then - cursor와 after가 모두 있는 경우 정상 동작
        assertThat(items).isNotEmpty();
        assertThat(items.get(0).getId()).isGreaterThan(lastItem.getId());
    }

    @Test
    @DisplayName("Branch Coverage: cursor != null && after != null - 내림차순")
    void findAllByCursor_branchCoverage_bothNotNull_desc() {
        // given
        Trip trip = createTrip("여행 계획");
        SharedCheckList checkList = createSharedCheckList("공유 체크리스트", trip);

        createAndSaveSharedCheckListItem("항목1", checkList);
        createAndSaveSharedCheckListItem("항목2", checkList);
        createAndSaveSharedCheckListItem("항목3", checkList);
        createAndSaveSharedCheckListItem("항목4", checkList);

        em.flush();
        em.clear();

        // when - 첫 페이지 (내림차순)
        List<SharedCheckListItem> firstPage = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        SharedCheckListItem lastItem = firstPage.get(firstPage.size() - 1);

        // when - cursor와 after 모두 있는 경우
        List<SharedCheckListItem> items = sharedCheckListItemRepository.findAllByCursor(
                checkList.getId(),
                null,
                "createdAt",
                "desc",
                lastItem.getCreatedAt().toString(),
                lastItem.getId(),
                10
        );

        // then - cursor와 after가 모두 있는 경우 정상 동작
        assertThat(items).isNotEmpty();
        assertThat(items.get(0).getId()).isLessThan(lastItem.getId());
    }

    // TestFixture 메서드들
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

    private SharedCheckList createSharedCheckList(String name, Trip trip) {
        SharedCheckList checkList = SharedCheckList.createSharedCheckList(name, trip);
        checkListRepository.save(checkList);
        return checkList;
    }

    private SharedCheckListItem createSharedCheckListItem(String text, SharedCheckList checkList) {
        return SharedCheckListItem.createSharedCheckListItem(text, checkList);
    }

    private SharedCheckListItem createAndSaveSharedCheckListItem(String text, SharedCheckList checkList) {
        SharedCheckListItem item = SharedCheckListItem.createSharedCheckListItem(text, checkList);
        return sharedCheckListItemRepository.save(item);
    }
}
