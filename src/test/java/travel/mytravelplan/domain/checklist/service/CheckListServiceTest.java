package travel.mytravelplan.domain.checklist.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.checklist.dto.*;
import travel.mytravelplan.domain.checklist.entity.CheckList;
import travel.mytravelplan.domain.checklist.entity.PersonalCheckList;
import travel.mytravelplan.domain.checklist.entity.SharedCheckList;
import travel.mytravelplan.domain.checklist.enums.CheckListType;
import travel.mytravelplan.domain.checklist.exception.CheckListException;
import travel.mytravelplan.domain.checklist.mapper.CheckListMapper;
import travel.mytravelplan.domain.checklist.repository.CheckListRepository;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.entity.TripJoin;
import travel.mytravelplan.domain.trip.exception.TripException;
import travel.mytravelplan.domain.trip.exception.TripJoinException;
import travel.mytravelplan.domain.trip.repository.TripJoinRepository;
import travel.mytravelplan.domain.trip.repository.TripRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.support.ServiceTestSupport;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

@DisplayName("체크리스트 서비스 테스트")
class CheckListServiceTest extends ServiceTestSupport {

    @Mock
    private CheckListRepository checkListRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripJoinRepository tripJoinRepository;

    @Mock
    private CheckListMapper checkListMapper;

    @InjectMocks
    private CheckListService checkListService;

    private User user;
    private Trip trip;
    private TripJoin tripJoin;
    private SharedCheckList sharedCheckList;
    private PersonalCheckList personalCheckList;
    private SharedCheckListDto sharedCheckListDto;
    private PersonalCheckListDto personalCheckListDto;
    private SharedCheckListCreateRequestDto sharedCreateRequestDto;
    private PersonalCheckListCreateRequestDto personalCreateRequestDto;
    private SharedCheckListUpdateRequestDto sharedUpdateRequestDto;
    private PersonalCheckListUpdateRequestDto personalUpdateRequestDto;

    @BeforeEach
    void setUp() {
        user = User.createUser("testuser", "password", "test@test.com", null, null, null);
        trip = Trip.createTrip("여행 제목", null, null, null, null);
        tripJoin = TripJoin.createTripJoin(trip, user);

        sharedCheckList = SharedCheckList.createSharedCheckList("공유 체크리스트", trip);
        personalCheckList = PersonalCheckList.createPersonalCheckList("개인 체크리스트", trip, tripJoin);

        sharedCheckListDto = SharedCheckListDto.builder()
                .id(1L)
                .name("공유 체크리스트")
                .build();

        personalCheckListDto = PersonalCheckListDto.builder()
                .id(2L)
                .name("개인 체크리스트")
                .build();

        sharedCreateRequestDto = SharedCheckListCreateRequestDto.builder()
                .checkListType(CheckListType.SHARED)
                .name("공유 체크리스트")
                .build();

        personalCreateRequestDto = PersonalCheckListCreateRequestDto.builder()
                .checkListType(CheckListType.PERSONAL)
                .name("개인 체크리스트")
                .build();

        sharedUpdateRequestDto = SharedCheckListUpdateRequestDto.builder()
                .checkListType(CheckListType.SHARED)
                .name("수정된 공유 체크리스트")
                .build();

        personalUpdateRequestDto = PersonalCheckListUpdateRequestDto.builder()
                .checkListType(CheckListType.PERSONAL)
                .name("수정된 개인 체크리스트")
                .build();
    }

    @Test
    @DisplayName("공유 체크리스트 생성 성공")
    void createCheckList_Shared_Success() {
        // given
        Long tripId = 1L;
        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(checkListRepository.save(any(SharedCheckList.class))).willReturn(sharedCheckList);
        lenient().when(checkListMapper.toDto(any(CheckList.class))).thenReturn(sharedCheckListDto);

        // when
        CheckListDto result = checkListService.createCheckList(user, tripId, sharedCreateRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(sharedCheckListDto);

        then(tripRepository).should().findById(eq(tripId));
        then(checkListRepository).should().save(any(SharedCheckList.class));
        then(checkListMapper).should().toDto(any(CheckList.class));
    }

    @Test
    @DisplayName("개인 체크리스트 생성 성공")
    void createCheckList_Personal_Success() {
        // given
        Long tripId = 1L;
        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(tripJoinRepository.findByUserAndTrip(eq(user), eq(trip))).willReturn(Optional.of(tripJoin));
        given(checkListRepository.save(any(PersonalCheckList.class))).willReturn(personalCheckList);
        lenient().when(checkListMapper.toDto(any(CheckList.class))).thenReturn(personalCheckListDto);

        // when
        CheckListDto result = checkListService.createCheckList(user, tripId, personalCreateRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(personalCheckListDto);

        then(tripRepository).should().findById(eq(tripId));
        then(tripJoinRepository).should().findByUserAndTrip(eq(user), eq(trip));
        then(checkListRepository).should().save(any(PersonalCheckList.class));
        then(checkListMapper).should().toDto(any(CheckList.class));
    }

    @Test
    @DisplayName("체크리스트 생성 실패 - 여행을 찾을 수 없음")
    void createCheckList_TripNotFound() {
        // given
        Long tripId = 999L;
        given(tripRepository.findById(eq(tripId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> checkListService.createCheckList(user, tripId, sharedCreateRequestDto))
                .isInstanceOf(TripException.class);

        then(tripRepository).should().findById(eq(tripId));
    }

    @Test
    @DisplayName("개인 체크리스트 생성 실패 - 여행 참여 정보를 찾을 수 없음")
    void createCheckList_Personal_TripJoinNotFound() {
        // given
        Long tripId = 1L;
        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(tripJoinRepository.findByUserAndTrip(eq(user), eq(trip))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> checkListService.createCheckList(user, tripId, personalCreateRequestDto))
                .isInstanceOf(TripJoinException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(tripJoinRepository).should().findByUserAndTrip(eq(user), eq(trip));
    }

    @Test
    @DisplayName("체크리스트 단건 조회 성공")
    void getCheckList_Success() {
        // given
        Long tripId = 1L;
        Long checkListId = 1L;
        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(checkListRepository.findById(eq(checkListId))).willReturn(Optional.of(sharedCheckList));
        lenient().when(checkListMapper.toDto(any(CheckList.class))).thenReturn(sharedCheckListDto);

        // when
        CheckListDto result = checkListService.getCheckList(tripId, checkListId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(sharedCheckListDto);

        then(tripRepository).should().findById(eq(tripId));
        then(checkListRepository).should().findById(eq(checkListId));
        then(checkListMapper).should().toDto(any(CheckList.class));
    }

    @Test
    @DisplayName("체크리스트 단건 조회 실패 - 여행을 찾을 수 없음")
    void getCheckList_TripNotFound() {
        // given
        Long tripId = 999L;
        Long checkListId = 1L;
        given(tripRepository.findById(eq(tripId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> checkListService.getCheckList(tripId, checkListId))
                .isInstanceOf(TripException.class);

        then(tripRepository).should().findById(eq(tripId));
    }

    @Test
    @DisplayName("체크리스트 단건 조회 실패 - 체크리스트를 찾을 수 없음")
    void getCheckList_CheckListNotFound() {
        // given
        Long tripId = 1L;
        Long checkListId = 999L;
        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(checkListRepository.findById(eq(checkListId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> checkListService.getCheckList(tripId, checkListId))
                .isInstanceOf(CheckListException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(checkListRepository).should().findById(eq(checkListId));
    }

    @Test
    @DisplayName("체크리스트 단건 조회 실패 - 체크리스트가 여행에 속하지 않음")
    void getCheckList_CheckListNotBelongToTrip() {
        // given
        Long tripId = 1L;
        Long checkListId = 1L;
        Trip anotherTrip = Trip.createTrip("다른 여행", null, null, null, null);
        SharedCheckList anotherCheckList = SharedCheckList.createSharedCheckList("다른 체크리스트", anotherTrip);

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(checkListRepository.findById(eq(checkListId))).willReturn(Optional.of(anotherCheckList));

        // when & then
        assertThatThrownBy(() -> checkListService.getCheckList(tripId, checkListId))
                .isInstanceOf(CheckListException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(checkListRepository).should().findById(eq(checkListId));
    }

    @Test
    @DisplayName("체크리스트 목록 조회 성공 - 다음 페이지 있음")
    void getCheckLists_Success_HasNext() {
        // given
        Long tripId = 1L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 2;

        SharedCheckList checkList1 = SharedCheckList.createSharedCheckList("체크리스트1", trip);
        ReflectionTestUtils.setField(checkList1, "id", 1L);
        ReflectionTestUtils.setField(checkList1, "createdAt", LocalDateTime.of(2025, 1, 1, 0, 0));

        PersonalCheckList checkList2 = PersonalCheckList.createPersonalCheckList("체크리스트2", trip, tripJoin);
        ReflectionTestUtils.setField(checkList2, "id", 2L);
        ReflectionTestUtils.setField(checkList2, "createdAt", LocalDateTime.of(2025, 1, 2, 0, 0));

        SharedCheckList checkList3 = SharedCheckList.createSharedCheckList("체크리스트3", trip);
        ReflectionTestUtils.setField(checkList3, "id", 3L);
        ReflectionTestUtils.setField(checkList3, "createdAt", LocalDateTime.of(2025, 1, 3, 0, 0));

        List<CheckList> checkLists = Arrays.asList(checkList1, checkList2, checkList3);

        given(checkListRepository.findAllByCursor(eq(tripId), eq(keyword), eq(orderBy), eq(direction),
                eq(cursor), eq(after), eq(limit + 1))).willReturn(checkLists);
        lenient().when(checkListMapper.toDto(any(CheckList.class)))
                .thenReturn(sharedCheckListDto)
                .thenReturn(personalCheckListDto);

        // when
        CursorPageResponseDto<CheckListDto> result = checkListService.getCheckLists(
                tripId, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isNotNull();
        assertThat(result.getNextAfter()).isNotNull();

        then(checkListRepository).should().findAllByCursor(eq(tripId), eq(keyword), eq(orderBy),
                eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("체크리스트 목록 조회 성공 - 다음 페이지 없음")
    void getCheckLists_Success_NoNext() {
        // given
        Long tripId = 1L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 2;

        SharedCheckList checkList1 = SharedCheckList.createSharedCheckList("체크리스트1", trip);
        ReflectionTestUtils.setField(checkList1, "id", 1L);
        ReflectionTestUtils.setField(checkList1, "createdAt", LocalDateTime.of(2025, 1, 1, 0, 0));

        PersonalCheckList checkList2 = PersonalCheckList.createPersonalCheckList("체크리스트2", trip, tripJoin);
        ReflectionTestUtils.setField(checkList2, "id", 2L);
        ReflectionTestUtils.setField(checkList2, "createdAt", LocalDateTime.of(2025, 1, 2, 0, 0));

        List<CheckList> checkLists = Arrays.asList(checkList1, checkList2);

        given(checkListRepository.findAllByCursor(eq(tripId), eq(keyword), eq(orderBy), eq(direction),
                eq(cursor), eq(after), eq(limit + 1))).willReturn(checkLists);
        lenient().when(checkListMapper.toDto(any(CheckList.class)))
                .thenReturn(sharedCheckListDto)
                .thenReturn(personalCheckListDto);

        // when
        CursorPageResponseDto<CheckListDto> result = checkListService.getCheckLists(
                tripId, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();

        then(checkListRepository).should().findAllByCursor(eq(tripId), eq(keyword), eq(orderBy),
                eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("공유 체크리스트 수정 성공")
    void updateCheckList_Shared_Success() {
        // given
        Long tripId = 1L;
        Long checkListId = 1L;
        SharedCheckListDto updatedDto = SharedCheckListDto.builder()
                .id(1L)
                .name("수정된 공유 체크리스트")
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(checkListRepository.findById(eq(checkListId))).willReturn(Optional.of(sharedCheckList));
        lenient().when(checkListMapper.toDto(any(CheckList.class))).thenReturn(updatedDto);

        // when
        CheckListDto result = checkListService.updateCheckList(tripId, checkListId, sharedUpdateRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(updatedDto);

        then(tripRepository).should().findById(eq(tripId));
        then(checkListRepository).should().findById(eq(checkListId));
        then(checkListMapper).should().toDto(any(CheckList.class));
    }

    @Test
    @DisplayName("개인 체크리스트 수정 성공")
    void updateCheckList_Personal_Success() {
        // given
        Long tripId = 1L;
        Long checkListId = 2L;
        PersonalCheckListDto updatedDto = PersonalCheckListDto.builder()
                .id(2L)
                .name("수정된 개인 체크리스트")
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(checkListRepository.findById(eq(checkListId))).willReturn(Optional.of(personalCheckList));
        lenient().when(checkListMapper.toDto(any(CheckList.class))).thenReturn(updatedDto);

        // when
        CheckListDto result = checkListService.updateCheckList(tripId, checkListId, personalUpdateRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(updatedDto);

        then(tripRepository).should().findById(eq(tripId));
        then(checkListRepository).should().findById(eq(checkListId));
        then(checkListMapper).should().toDto(any(CheckList.class));
    }

    @Test
    @DisplayName("체크리스트 수정 실패 - 여행을 찾을 수 없음")
    void updateCheckList_TripNotFound() {
        // given
        Long tripId = 999L;
        Long checkListId = 1L;
        given(tripRepository.findById(eq(tripId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> checkListService.updateCheckList(tripId, checkListId, sharedUpdateRequestDto))
                .isInstanceOf(TripException.class);

        then(tripRepository).should().findById(eq(tripId));
    }

    @Test
    @DisplayName("체크리스트 수정 실패 - 체크리스트를 찾을 수 없음")
    void updateCheckList_CheckListNotFound() {
        // given
        Long tripId = 1L;
        Long checkListId = 999L;
        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(checkListRepository.findById(eq(checkListId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> checkListService.updateCheckList(tripId, checkListId, sharedUpdateRequestDto))
                .isInstanceOf(CheckListException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(checkListRepository).should().findById(eq(checkListId));
    }

    @Test
    @DisplayName("체크리스트 수정 실패 - 체크리스트가 여행에 속하지 않음")
    void updateCheckList_CheckListNotBelongToTrip() {
        // given
        Long tripId = 1L;
        Long checkListId = 1L;
        Trip anotherTrip = Trip.createTrip("다른 여행", null, null, null, null);
        SharedCheckList anotherCheckList = SharedCheckList.createSharedCheckList("다른 체크리스트", anotherTrip);

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(checkListRepository.findById(eq(checkListId))).willReturn(Optional.of(anotherCheckList));

        // when & then
        assertThatThrownBy(() -> checkListService.updateCheckList(tripId, checkListId, sharedUpdateRequestDto))
                .isInstanceOf(CheckListException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(checkListRepository).should().findById(eq(checkListId));
    }

    @Test
    @DisplayName("체크리스트 삭제 성공")
    void deleteCheckList_Success() {
        // given
        Long tripId = 1L;
        Long checkListId = 1L;
        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(checkListRepository.findById(eq(checkListId))).willReturn(Optional.of(sharedCheckList));

        // when
        checkListService.deleteCheckList(tripId, checkListId);

        // then
        then(tripRepository).should().findById(eq(tripId));
        then(checkListRepository).should().findById(eq(checkListId));
        then(checkListRepository).should().delete(eq(sharedCheckList));
    }

    @Test
    @DisplayName("체크리스트 삭제 실패 - 여행을 찾을 수 없음")
    void deleteCheckList_TripNotFound() {
        // given
        Long tripId = 999L;
        Long checkListId = 1L;
        given(tripRepository.findById(eq(tripId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> checkListService.deleteCheckList(tripId, checkListId))
                .isInstanceOf(TripException.class);

        then(tripRepository).should().findById(eq(tripId));
    }

    @Test
    @DisplayName("체크리스트 삭제 실패 - 체크리스트를 찾을 수 없음")
    void deleteCheckList_CheckListNotFound() {
        // given
        Long tripId = 1L;
        Long checkListId = 999L;
        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(checkListRepository.findById(eq(checkListId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> checkListService.deleteCheckList(tripId, checkListId))
                .isInstanceOf(CheckListException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(checkListRepository).should().findById(eq(checkListId));
    }

    @Test
    @DisplayName("체크리스트 삭제 실패 - 체크리스트가 여행에 속하지 않음")
    void deleteCheckList_CheckListNotBelongToTrip() {
        // given
        Long tripId = 1L;
        Long checkListId = 1L;
        Trip anotherTrip = Trip.createTrip("다른 여행", null, null, null, null);
        SharedCheckList anotherCheckList = SharedCheckList.createSharedCheckList("다른 체크리스트", anotherTrip);

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(checkListRepository.findById(eq(checkListId))).willReturn(Optional.of(anotherCheckList));

        // when & then
        assertThatThrownBy(() -> checkListService.deleteCheckList(tripId, checkListId))
                .isInstanceOf(CheckListException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(checkListRepository).should().findById(eq(checkListId));
    }
}