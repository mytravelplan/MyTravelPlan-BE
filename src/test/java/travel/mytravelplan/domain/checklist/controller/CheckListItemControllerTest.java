package travel.mytravelplan.domain.checklist.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.checklist.dto.*;
import travel.mytravelplan.domain.checklist.entity.CheckList;
import travel.mytravelplan.domain.checklist.entity.PersonalCheckList;
import travel.mytravelplan.domain.checklist.entity.PersonalCheckListItem;
import travel.mytravelplan.domain.checklist.entity.SharedCheckList;
import travel.mytravelplan.domain.checklist.enums.CheckListType;
import travel.mytravelplan.domain.checklist.service.CheckListItemService;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.entity.TripJoin;
import travel.mytravelplan.domain.trip.enums.Country;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.entity.UserProfile;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.support.ControllerTestSupport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CheckListItemController.class)
@DisplayName("체크 리스트 항목 컨트롤러 테스트")
class CheckListItemControllerTest extends ControllerTestSupport {

    @MockitoBean
    private CheckListItemService checkListItemService;

    private String accessToken;
    private Long userId;
    private Long tripId;
    private Long checkListId;
    private Long checkListItemId;
    private User testUser;
    private Trip testTrip;
    private TripJoin testTripJoin;
    private CheckList testCheckList;
    private SharedCheckListItemCreateRequestDto createRequestDto;
    private SharedCheckListItemUpdateRequestDto updateRequestDto;
    private CheckListItemDto checkListItemDto;

    @BeforeEach
    void setUp() {
        given(jwtBlacklistService.isTokenBlacklisted(any(String.class))).willReturn(false);

        UserProfile userProfile = UserProfile.createUserProfile(
                "테스트 유저",
                "http://example.com/user.jpg"
        );

        testUser = User.createUser(
                "testUser",
                "password",
                "test@test.com",
                SocialType.LOCAL,
                null,
                LocalDate.of(1990, 1, 1),
                "010-1234-5678",
                Gender.MALE,
                Set.of(Role.USER)
        );

        testUser.setUserProfile(userProfile);

        userId = 1L;
        ReflectionTestUtils.setField(testUser, "id", userId);

        accessToken = jwtUtils.createAccessToken(userId, Set.of(Role.USER));

        given(userRepository.findById(eq(userId))).willReturn(Optional.of(testUser));

        tripId = 1L;

        given(tripJoinRepository.existsByUserIdAndTripId(eq(userId), eq(tripId))).willReturn(true);

        testTrip = Trip.createTrip(
                "테스트 여행",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 10),
                "http://example.com/trip.jpg",
                Set.of(Country.JP)
        );
        ReflectionTestUtils.setField(testTrip, "id", tripId);

        testTripJoin = TripJoin.createTripJoin(testTrip, testUser);
        ReflectionTestUtils.setField(testTripJoin, "id", 1L);

        checkListId = 1L;
        testCheckList = SharedCheckList.createSharedCheckList("테스트 체크리스트", testTrip);
        ReflectionTestUtils.setField(testCheckList, "id", checkListId);

        given(checkListRepository.findById(eq(checkListId))).willReturn(Optional.of(testCheckList));

        checkListItemId = 1L;

        createRequestDto = SharedCheckListItemCreateRequestDto.builder()
                .checkListType(CheckListType.SHARED)
                .text("테스트 체크리스트 항목")
                .checked(false)
                .build();

        updateRequestDto = SharedCheckListItemUpdateRequestDto.builder()
                .checkListType(CheckListType.SHARED)
                .text("수정된 체크리스트 항목")
                .checked(true)
                .build();

        checkListItemDto = SharedCheckListItemDto.builder()
                .id(1L)
                .text("테스트 체크리스트 항목")
                .checked(false)
                .build();
    }

    @Test
    @DisplayName("[공유] 체크리스트 항목 생성 성공")
    void createCheckListItem_Success() throws Exception {
        // given
        given(checkListItemService.createCheckListItem(
                any(User.class),
                eq(tripId),
                eq(checkListId),
                any(CheckListItemCreateRequestDto.class)
        )).willReturn(checkListItemDto);

        // when
        mockMvc.perform(post("/api/trips/{tripId}/checkLists/{checkListId}/checkListItems", tripId, checkListId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.text").value("테스트 체크리스트 항목"))
                .andExpect(jsonPath("$.data.checked").value(false))
                .andDo(document("checkListItem-create",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("checkListId").description("체크리스트 ID")
                        ),
                        requestFields(
                                fieldWithPath("checkListType").description("체크리스트 타입 (SHARED, PERSONAL)"),
                                fieldWithPath("text").description("체크리스트 항목 내용"),
                                fieldWithPath("checked").description("체크 여부")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("체크리스트 항목 ID"),
                                fieldWithPath("data.text").description("체크리스트 항목 내용"),
                                fieldWithPath("data.checked").description("체크 여부")
                        )
                ));

        // then
        assertThat(checkListItemDto).isNotNull();
        assertThat(checkListItemDto.getText()).isEqualTo("테스트 체크리스트 항목");
        then(checkListItemService).should().createCheckListItem(
                any(User.class),
                eq(tripId),
                eq(checkListId),
                any(CheckListItemCreateRequestDto.class)
        );
    }

    @Test
    @DisplayName("[개인] 체크리스트 항목 생성 성공")
    void createPersonalCheckListItem_Success() throws Exception {
        // given
        Long personalCheckListId = 2L;
        CheckList personalCheckList = PersonalCheckList.createPersonalCheckList("개인 체크리스트", testTrip, testTripJoin);
        ReflectionTestUtils.setField(personalCheckList, "id", personalCheckListId);

        given(checkListRepository.findById(eq(personalCheckListId))).willReturn(Optional.of(personalCheckList));

        PersonalCheckListItemCreateRequestDto personalCreateRequestDto = PersonalCheckListItemCreateRequestDto.builder()
                .checkListType(CheckListType.PERSONAL)
                .text("개인 체크리스트 항목")
                .checked(false)
                .build();

        PersonalCheckListItemDto personalCheckListItemDto = PersonalCheckListItemDto.builder()
                .id(2L)
                .text("개인 체크리스트 항목")
                .checked(false)
                .build();

        given(checkListItemService.createCheckListItem(
                any(User.class),
                eq(tripId),
                eq(personalCheckListId),
                any(CheckListItemCreateRequestDto.class)
        )).willReturn(personalCheckListItemDto);

        // when
        mockMvc.perform(post("/api/trips/{tripId}/checkLists/{checkListId}/checkListItems", tripId, personalCheckListId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(personalCreateRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.text").value("개인 체크리스트 항목"))
                .andExpect(jsonPath("$.data.checked").value(false));

        // then
        assertThat(personalCheckListItemDto).isNotNull();
        assertThat(personalCheckListItemDto.getText()).isEqualTo("개인 체크리스트 항목");
        then(checkListItemService).should().createCheckListItem(
                any(User.class),
                eq(tripId),
                eq(personalCheckListId),
                any(CheckListItemCreateRequestDto.class)
        );
    }

    @Test
    @DisplayName("[공유] 체크리스트 항목 목록 조회 성공")
    void getCheckListItems_Success() throws Exception {
        // given
        SharedCheckListItemDto checkListItemDto1 = SharedCheckListItemDto.builder()
                .id(1L)
                .text("테스트 체크리스트 항목 1")
                .checked(false)
                .build();

        SharedCheckListItemDto checkListItemDto2 = SharedCheckListItemDto.builder()
                .id(2L)
                .text("테스트 체크리스트 항목 2")
                .checked(true)
                .build();

        CursorPageResponseDto<? extends CheckListItemDto> pageResponse = CursorPageResponseDto.<CheckListItemDto>builder()
                .content(List.of(checkListItemDto1, checkListItemDto2))
                .nextCursor("2025-01-01T00:00:00")
                .nextAfter(2L)
                .size(2)
                .hasNext(false)
                .build();

        given(checkListItemService.getCheckListItems(
                eq(tripId),
                eq(checkListId),
                eq("테스트"),
                eq("createdAt"),
                eq("ASC"),
                eq(null),
                eq(null),
                eq(10)
        )).will(invocation -> pageResponse);

        // when
        mockMvc.perform(get("/api/trips/{tripId}/checkLists/{checkListId}/checkListItems", tripId, checkListId)
                        .header("Authorization", "Bearer " + accessToken)
                        .param("keyword", "테스트")
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andDo(document("checkListItem-list",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("checkListId").description("체크리스트 ID")
                        ),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드").optional(),
                                parameterWithName("orderBy").description("정렬 기준").optional(),
                                parameterWithName("direction").description("정렬 방향").optional(),
                                parameterWithName("limit").description("조회 개수").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.content").description("체크리스트 항목 목록"),
                                fieldWithPath("data.content[].id").description("체크리스트 항목 ID"),
                                fieldWithPath("data.content[].text").description("체크리스트 항목 내용"),
                                fieldWithPath("data.content[].checked").description("체크 여부"),
                                fieldWithPath("data.nextCursor").description("다음 커서"),
                                fieldWithPath("data.nextAfter").description("다음 after"),
                                fieldWithPath("data.size").description("조회된 개수"),
                                fieldWithPath("data.hasNext").description("다음 페이지 존재 여부")
                        )
                ));

        // then
        assertThat(pageResponse).isNotNull();
        assertThat(pageResponse.getContent()).hasSize(2);
        then(checkListItemService).should().getCheckListItems(
                eq(tripId),
                eq(checkListId),
                eq("테스트"),
                eq("createdAt"),
                eq("ASC"),
                eq(null),
                eq(null),
                eq(10)
        );
    }

    @Test
    @DisplayName("[공유] 체크리스트 항목 조회 성공")
    void getCheckListItem_Success() throws Exception {
        // given
        CheckListItemDto checkListItemDto = SharedCheckListItemDto.builder()
                .id(1L)
                .text("테스트 체크리스트 항목")
                .checked(false)
                .build();

        given(checkListItemService.getCheckListItem(eq(tripId), eq(checkListId), eq(checkListItemId)))
                .willReturn(checkListItemDto);

        // when
        mockMvc.perform(get("/api/trips/{tripId}/checkLists/{checkListId}/checkListItems/{checkListItemId}",
                        tripId, checkListId, checkListItemId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.text").value("테스트 체크리스트 항목"))
                .andExpect(jsonPath("$.data.checked").value(false))
                .andDo(document("checkListItem-get",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("checkListId").description("체크리스트 ID"),
                                parameterWithName("checkListItemId").description("체크리스트 항목 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("체크리스트 항목 ID"),
                                fieldWithPath("data.text").description("체크리스트 항목 내용"),
                                fieldWithPath("data.checked").description("체크 여부")
                        )
                ));

        // then
        assertThat(checkListItemDto).isNotNull();
        assertThat(checkListItemDto.getId()).isEqualTo(1L);
        then(checkListItemService).should().getCheckListItem(eq(tripId), eq(checkListId), eq(checkListItemId));
    }

    @Test
    @DisplayName("[개인] 체크리스트 항목 조회 성공")
    void getPersonalCheckListItem_Success() throws Exception {
        // given
        Long personalCheckListId = 2L;
        Long personalCheckListItemId = 2L;

        CheckList personalCheckList = PersonalCheckList.createPersonalCheckList("개인 체크리스트", testTrip, testTripJoin);
        ReflectionTestUtils.setField(personalCheckList, "id", personalCheckListId);

        given(checkListRepository.findById(eq(personalCheckListId))).willReturn(Optional.of(personalCheckList));

        PersonalCheckListItemDto personalCheckListItemDto = PersonalCheckListItemDto.builder()
                .id(personalCheckListItemId)
                .text("개인 체크리스트 항목")
                .checked(false)
                .build();

        given(checkListItemService.getCheckListItem(eq(tripId), eq(personalCheckListId), eq(personalCheckListItemId)))
                .willReturn(personalCheckListItemDto);

        // when
        mockMvc.perform(get("/api/trips/{tripId}/checkLists/{checkListId}/checkListItems/{checkListItemId}",
                        tripId, personalCheckListId, personalCheckListItemId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.text").value("개인 체크리스트 항목"))
                .andExpect(jsonPath("$.data.checked").value(false));

        // then
        assertThat(personalCheckListItemDto).isNotNull();
        assertThat(personalCheckListItemDto.getId()).isEqualTo(2L);
        then(checkListItemService).should().getCheckListItem(eq(tripId), eq(personalCheckListId), eq(personalCheckListItemId));
    }

    @Test
    @DisplayName("[공유] 체크리스트 항목 수정 성공")
    void updateCheckListItem_Success() throws Exception {
        // given
        CheckListItemDto updatedCheckListItemDto = SharedCheckListItemDto.builder()
                .id(1L)
                .text("수정된 체크리스트 항목")
                .checked(true)
                .build();

        given(checkListItemService.updateCheckListItem(
                eq(tripId),
                eq(checkListId),
                eq(checkListItemId),
                any(CheckListItemUpdateRequestDto.class)
        )).willReturn(updatedCheckListItemDto);

        // when
        mockMvc.perform(patch("/api/trips/{tripId}/checkLists/{checkListId}/checkListItems/{checkListItemId}",
                        tripId, checkListId, checkListItemId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.text").value("수정된 체크리스트 항목"))
                .andExpect(jsonPath("$.data.checked").value(true))
                .andDo(document("checkListItem-update",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("checkListId").description("체크리스트 ID"),
                                parameterWithName("checkListItemId").description("체크리스트 항목 ID")
                        ),
                        requestFields(
                                fieldWithPath("checkListType").description("체크리스트 타입 (SHARED, PERSONAL)"),
                                fieldWithPath("text").description("수정할 체크리스트 항목 내용"),
                                fieldWithPath("checked").description("체크 여부")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("체크리스트 항목 ID"),
                                fieldWithPath("data.text").description("수정된 체크리스트 항목 내용"),
                                fieldWithPath("data.checked").description("체크 여부")
                        )
                ));

        // then
        assertThat(updatedCheckListItemDto).isNotNull();
        assertThat(updatedCheckListItemDto.getText()).isEqualTo("수정된 체크리스트 항목");
        assertThat(updatedCheckListItemDto.isChecked()).isTrue();
        then(checkListItemService).should().updateCheckListItem(
                eq(tripId),
                eq(checkListId),
                eq(checkListItemId),
                any(CheckListItemUpdateRequestDto.class)
        );
    }

    @Test
    @DisplayName("[공유] 체크리스트 항목 삭제 성공")
    void deleteCheckListItem_Success() throws Exception {
        // given
        willDoNothing().given(checkListItemService).deleteCheckListItem(eq(tripId), eq(checkListId), eq(checkListItemId));

        // when
        mockMvc.perform(delete("/api/trips/{tripId}/checkLists/{checkListId}/checkListItems/{checkListItemId}",
                        tripId, checkListId, checkListItemId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(document("checkListItem-delete",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("checkListId").description("체크리스트 ID"),
                                parameterWithName("checkListItemId").description("체크리스트 항목 ID")
                        )
                ));

        // then
        then(checkListItemService).should().deleteCheckListItem(eq(tripId), eq(checkListId), eq(checkListItemId));
    }

    @Test
    @DisplayName("[개인] 체크리스트 항목 수정 성공")
    void updatePersonalCheckListItem_Success() throws Exception {
        // given
        Long personalCheckListId = 2L;
        Long personalCheckListItemId = 2L;

        PersonalCheckList personalCheckList = PersonalCheckList.createPersonalCheckList("개인 체크리스트", testTrip, testTripJoin);
        ReflectionTestUtils.setField(personalCheckList, "id", personalCheckListId);

        // 개인 체크리스트 항목 생성
        PersonalCheckListItem personalCheckListItem = PersonalCheckListItem.createPersonalCheckListItem(
                "개인 체크리스트 항목",
                personalCheckList
        );
        ReflectionTestUtils.setField(personalCheckListItem, "id", personalCheckListItemId);

        given(checkListRepository.findById(eq(personalCheckListId))).willReturn(Optional.of(personalCheckList));
        given(tripJoinRepository.findByUserIdAndTripId(eq(userId), eq(tripId))).willReturn(Optional.of(testTripJoin));

        PersonalCheckListItemUpdateRequestDto personalUpdateRequestDto = PersonalCheckListItemUpdateRequestDto.builder()
                .checkListType(CheckListType.PERSONAL)
                .text("수정된 개인 체크리스트 항목")
                .checked(true)
                .build();

        PersonalCheckListItemDto updatedPersonalCheckListItemDto = PersonalCheckListItemDto.builder()
                .id(personalCheckListItemId)
                .text("수정된 개인 체크리스트 항목")
                .checked(true)
                .build();

        given(checkListItemService.updateCheckListItem(
                eq(tripId),
                eq(personalCheckListId),
                eq(personalCheckListItemId),
                any(CheckListItemUpdateRequestDto.class)
        )).willReturn(updatedPersonalCheckListItemDto);

        // when
        mockMvc.perform(patch("/api/trips/{tripId}/checkLists/{checkListId}/checkListItems/{checkListItemId}",
                        tripId, personalCheckListId, personalCheckListItemId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(personalUpdateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.text").value("수정된 개인 체크리스트 항목"))
                .andExpect(jsonPath("$.data.checked").value(true))
                .andDo(document("personalCheckListItem-update",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("checkListId").description("개인 체크리스트 ID"),
                                parameterWithName("checkListItemId").description("개인 체크리스트 항목 ID")
                        ),
                        requestFields(
                                fieldWithPath("checkListType").description("체크리스트 타입 (PERSONAL)"),
                                fieldWithPath("text").description("수정할 체크리스트 항목 내용"),
                                fieldWithPath("checked").description("체크 여부")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("체크리스트 항목 ID"),
                                fieldWithPath("data.text").description("수정된 체크리스트 항목 내용"),
                                fieldWithPath("data.checked").description("체크 여부")
                        )
                ));

        // then
        assertThat(updatedPersonalCheckListItemDto).isNotNull();
        assertThat(updatedPersonalCheckListItemDto.getText()).isEqualTo("수정된 개인 체크리스트 항목");
        assertThat(updatedPersonalCheckListItemDto.isChecked()).isTrue();
        then(checkListItemService).should().updateCheckListItem(
                eq(tripId),
                eq(personalCheckListId),
                eq(personalCheckListItemId),
                any(CheckListItemUpdateRequestDto.class)
        );
    }

    @Test
    @DisplayName("[개인] 체크리스트 항목 삭제 성공")
    void deletePersonalCheckListItem_Success() throws Exception {
        // given
        Long personalCheckListId = 2L;
        Long personalCheckListItemId = 2L;

        PersonalCheckList personalCheckList = PersonalCheckList.createPersonalCheckList("개인 체크리스트", testTrip, testTripJoin);
        ReflectionTestUtils.setField(personalCheckList, "id", personalCheckListId);

        // 개인 체크리스트 항목 생성
        PersonalCheckListItem personalCheckListItem = PersonalCheckListItem.createPersonalCheckListItem(
                "개인 체크리스트 항목",
                personalCheckList
        );
        ReflectionTestUtils.setField(personalCheckListItem, "id", personalCheckListItemId);

        given(checkListRepository.findById(eq(personalCheckListId))).willReturn(Optional.of(personalCheckList));
        given(tripJoinRepository.findByUserIdAndTripId(eq(userId), eq(tripId))).willReturn(Optional.of(testTripJoin));

        willDoNothing().given(checkListItemService).deleteCheckListItem(eq(tripId), eq(personalCheckListId), eq(personalCheckListItemId));

        // when
        mockMvc.perform(delete("/api/trips/{tripId}/checkLists/{checkListId}/checkListItems/{checkListItemId}",
                        tripId, personalCheckListId, personalCheckListItemId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(document("personalCheckListItem-delete",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("checkListId").description("개인 체크리스트 ID"),
                                parameterWithName("checkListItemId").description("개인 체크리스트 항목 ID")
                        )
                ));

        // then
        then(checkListItemService).should().deleteCheckListItem(eq(tripId), eq(personalCheckListId), eq(personalCheckListItemId));
    }

    @Test
    @DisplayName("[개인] 체크리스트 항목 목록 조회 성공")
    void getPersonalCheckListItems_Success() throws Exception {
        // given
        Long personalCheckListId = 2L;

        CheckList personalCheckList = PersonalCheckList.createPersonalCheckList("개인 체크리스트", testTrip, testTripJoin);
        ReflectionTestUtils.setField(personalCheckList, "id", personalCheckListId);

        given(checkListRepository.findById(eq(personalCheckListId))).willReturn(Optional.of(personalCheckList));

        PersonalCheckListItemDto personalCheckListItemDto1 = PersonalCheckListItemDto.builder()
                .id(1L)
                .text("개인 체크리스트 항목 1")
                .checked(false)
                .build();

        PersonalCheckListItemDto personalCheckListItemDto2 = PersonalCheckListItemDto.builder()
                .id(2L)
                .text("개인 체크리스트 항목 2")
                .checked(true)
                .build();

        CursorPageResponseDto<? extends CheckListItemDto> pageResponse = CursorPageResponseDto.<CheckListItemDto>builder()
                .content(List.of(personalCheckListItemDto1, personalCheckListItemDto2))
                .nextCursor("2025-01-01T00:00:00")
                .nextAfter(2L)
                .size(2)
                .hasNext(false)
                .build();

        given(checkListItemService.getCheckListItems(
                eq(tripId),
                eq(personalCheckListId),
                eq("개인"),
                eq("createdAt"),
                eq("ASC"),
                eq(null),
                eq(null),
                eq(10)
        )).will(invocation -> pageResponse);

        // when
        mockMvc.perform(get("/api/trips/{tripId}/checkLists/{checkListId}/checkListItems", tripId, personalCheckListId)
                        .header("Authorization", "Bearer " + accessToken)
                        .param("keyword", "개인")
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.hasNext").value(false));

        // then
        assertThat(pageResponse).isNotNull();
        assertThat(pageResponse.getContent()).hasSize(2);
        then(checkListItemService).should().getCheckListItems(
                eq(tripId),
                eq(personalCheckListId),
                eq("개인"),
                eq("createdAt"),
                eq("ASC"),
                eq(null),
                eq(null),
                eq(10)
        );
    }
}
