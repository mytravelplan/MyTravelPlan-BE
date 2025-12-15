package travel.mytravelplan.domain.checklist.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.checklist.dto.*;
import travel.mytravelplan.domain.checklist.enums.CheckListType;
import travel.mytravelplan.domain.checklist.service.CheckListService;
import travel.mytravelplan.domain.trip.entity.Trip;
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
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CheckListController.class)
@DisplayName("체크리스트 컨트롤러 테스트")
public class CheckListControllerTest extends ControllerTestSupport {

    @MockitoBean
    private CheckListService checkListService;

    private String accessToken;
    private Long userId;
    private Long tripId;
    private Long checkListId;
    private User testUser;
    private Trip testTrip;
    private SharedCheckListCreateRequestDto createRequestDto;
    private SharedCheckListUpdateRequestDto updateRequestDto;
    private CheckListDto checkListDto;

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

        checkListId = 1L;

        createRequestDto = SharedCheckListCreateRequestDto.builder()
                .checkListType(CheckListType.SHARED)
                .name("테스트 체크리스트")
                .build();

        updateRequestDto = SharedCheckListUpdateRequestDto.builder()
                .checkListType(CheckListType.SHARED)
                .name("수정된 체크리스트")
                .build();

        checkListDto = SharedCheckListDto.builder()
                .id(1L)
                .name("테스트 체크리스트")
                .build();
    }

    @Test
    @DisplayName("체크리스트 생성 성공")
    void createCheckList_Success() throws Exception {
        // given
        given(checkListService.createCheckList(any(User.class), eq(tripId), any(CheckListCreateRequestDto.class)))
                .willReturn(checkListDto);

        // when
        mockMvc.perform(post("/api/trips/{tripId}/checkLists", tripId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("테스트 체크리스트"))
                .andDo(document("checkList-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID")
                        ),
                        requestFields(
                                fieldWithPath("checkListType").description("체크리스트 타입 (SHARED, PERSONAL)"),
                                fieldWithPath("name").description("체크리스트 이름")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("체크리스트 ID"),
                                fieldWithPath("data.name").description("체크리스트 이름")
                        )
                ));

        // then
        assertThat(checkListDto).isNotNull();
        assertThat(checkListDto.getName()).isEqualTo("테스트 체크리스트");
        then(checkListService).should().createCheckList(any(User.class), eq(tripId), any(CheckListCreateRequestDto.class));
    }

    @Test
    @DisplayName("체크리스트 조회 성공")
    void getCheckList_Success() throws Exception {
        // given
        given(checkListService.getCheckList(eq(tripId), eq(checkListId))).willReturn(checkListDto);

        // when
        mockMvc.perform(get("/api/trips/{tripId}/checkLists/{checkListId}", tripId, checkListId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("테스트 체크리스트"))
                .andDo(document("checkList-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("checkListId").description("체크리스트 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("체크리스트 ID"),
                                fieldWithPath("data.name").description("체크리스트 이름")
                        )
                ));

        // then
        assertThat(checkListDto).isNotNull();
        assertThat(checkListDto.getId()).isEqualTo(1L);
        then(checkListService).should().getCheckList(eq(tripId), eq(checkListId));
    }

    @Test
    @DisplayName("체크리스트 목록 조회 성공")
    void getCheckLists_Success() throws Exception {
        // given
        CheckListDto checkListDto2 = SharedCheckListDto.builder()
                .id(2L)
                .name("테스트 체크리스트 2")
                .build();

        CursorPageResponseDto<CheckListDto> pageResponse = CursorPageResponseDto.<CheckListDto>builder()
                .content(List.of(checkListDto, checkListDto2))
                .nextCursor("2025-01-01T00:00:00")
                .nextAfter(2L)
                .size(2)
                .hasNext(false)
                .build();

        given(checkListService.getCheckLists(
                eq(tripId),
                eq("테스트"),
                eq("createdAt"),
                eq("ASC"),
                eq(null),
                eq(null),
                eq(10)
        )).willReturn(pageResponse);

        // when
        mockMvc.perform(get("/api/trips/{tripId}/checkLists", tripId)
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
                .andDo(document("checkList-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID")
                        ),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드").optional(),
                                parameterWithName("orderBy").description("정렬 기준 (기본값: createdAt)").optional(),
                                parameterWithName("direction").description("정렬 방향 (ASC, DESC, 기본값: ASC)").optional(),
                                parameterWithName("cursor").description("커서").optional(),
                                parameterWithName("after").description("이후 ID").optional(),
                                parameterWithName("limit").description("조회 개수 (기본값: 10)").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.content").description("체크리스트 목록"),
                                fieldWithPath("data.content[].id").description("체크리스트 ID"),
                                fieldWithPath("data.content[].name").description("체크리스트 이름"),
                                fieldWithPath("data.nextCursor").description("다음 커서"),
                                fieldWithPath("data.nextAfter").description("다음 이후 ID"),
                                fieldWithPath("data.size").description("현재 페이지 크기"),
                                fieldWithPath("data.hasNext").description("다음 페이지 존재 여부")
                        )
                ));

        // then
        assertThat(pageResponse).isNotNull();
        assertThat(pageResponse.getContent()).hasSize(2);
        then(checkListService).should().getCheckLists(
                eq(tripId),
                eq("테스트"),
                eq("createdAt"),
                eq("ASC"),
                eq(null),
                eq(null),
                eq(10)
        );
    }

    @Test
    @DisplayName("체크리스트 수정 성공")
    void updateCheckList_Success() throws Exception {
        // given
        CheckListDto updatedCheckListDto = SharedCheckListDto.builder()
                .id(1L)
                .name("수정된 체크리스트")
                .build();

        given(checkListService.updateCheckList(eq(tripId), eq(checkListId), any(CheckListUpdateRequestDto.class)))
                .willReturn(updatedCheckListDto);

        // when
        mockMvc.perform(patch("/api/trips/{tripId}/checkLists/{checkListId}", tripId, checkListId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("수정된 체크리스트"))
                .andDo(document("checkList-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("checkListId").description("체크리스트 ID")
                        ),
                        requestFields(
                                fieldWithPath("checkListType").description("체크리스트 타입 (SHARED, PERSONAL)"),
                                fieldWithPath("name").description("수정할 체크리스트 이름")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("체크리스트 ID"),
                                fieldWithPath("data.name").description("수정된 체크리스트 이름")
                        )
                ));

        // then
        assertThat(updatedCheckListDto).isNotNull();
        assertThat(updatedCheckListDto.getName()).isEqualTo("수정된 체크리스트");
        then(checkListService).should().updateCheckList(eq(tripId), eq(checkListId), any(CheckListUpdateRequestDto.class));
    }

    @Test
    @DisplayName("체크리스트 삭제 성공")
    void deleteCheckList_Success() throws Exception {
        // given
        willDoNothing().given(checkListService).deleteCheckList(eq(tripId), eq(checkListId));

        // when
        mockMvc.perform(delete("/api/trips/{tripId}/checkLists/{checkListId}", tripId, checkListId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(document("checkList-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("checkListId").description("체크리스트 ID")
                        )
                ));

        // then
        then(checkListService).should().deleteCheckList(eq(tripId), eq(checkListId));
    }
}
