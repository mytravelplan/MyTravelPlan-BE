package travel.mytravelplan.domain.schedule.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.schedule.dto.ScheduleCreateRequestDto;
import travel.mytravelplan.domain.schedule.dto.ScheduleDto;
import travel.mytravelplan.domain.schedule.dto.ScheduleUpdateRequestDto;
import travel.mytravelplan.domain.schedule.entity.Schedule;
import travel.mytravelplan.domain.schedule.service.ScheduleService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
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

@WebMvcTest(ScheduleController.class)
@DisplayName("일정 컨트롤러 테스트")
class ScheduleControllerTest extends ControllerTestSupport {

    @MockitoBean
    private ScheduleService scheduleService;

    private String accessToken;
    private Long userId;
    private Long tripId;
    private Long scheduleId;
    private User testUser;
    private Trip testTrip;
    private TripJoin testTripJoin;
    private Schedule testSchedule;
    private ScheduleCreateRequestDto createRequestDto;
    private ScheduleUpdateRequestDto updateRequestDto;
    private ScheduleDto scheduleDto;

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

        testTrip = Trip.createTrip(
                "테스트 여행",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 10),
                "http://example.com/trip.jpg",
                Set.of(Country.JP)
        );
        ReflectionTestUtils.setField(testTrip, "id", tripId);

        testTripJoin = TripJoin.createTripJoin(testTrip, testUser);
        Long tripJoinId = 1L;
        ReflectionTestUtils.setField(testTripJoin, "id", tripJoinId);

        given(tripJoinRepository.existsByUserIdAndTripId(eq(userId), eq(tripId))).willReturn(true);

        scheduleId = 1L;

        testSchedule = Schedule.createSchedule(
                "도쿄 타워 방문",
                LocalDateTime.of(2025, 1, 5, 10, 0),
                LocalDateTime.of(2025, 1, 5, 12, 0),
                "도쿄 타워에서 사진 찍기",
                1L,
                null,
                testTrip,
                BigDecimal.valueOf(4.5)
        );
        ReflectionTestUtils.setField(testSchedule, "id", scheduleId);

        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(testSchedule));

        createRequestDto = ScheduleCreateRequestDto.builder()
                .title("도쿄 타워 방문")
                .startDateTime(LocalDateTime.of(2025, 1, 5, 10, 0))
                .endDateTime(LocalDateTime.of(2025, 1, 5, 12, 0))
                .memo("도쿄 타워에서 사진 찍기")
                .rating(BigDecimal.valueOf(4.5))
                .placeId(null)
                .build();

        updateRequestDto = ScheduleUpdateRequestDto.builder()
                .title("도쿄 스카이트리 방문")
                .startDateTime(LocalDateTime.of(2025, 1, 5, 14, 0))
                .endDateTime(LocalDateTime.of(2025, 1, 5, 16, 0))
                .memo("스카이트리에서 일몰 보기")
                .rating(BigDecimal.valueOf(5.0))
                .placeId(null)
                .build();

        scheduleDto = ScheduleDto.builder()
                .id(scheduleId)
                .tripId(tripId)
                .title("도쿄 타워 방문")
                .memo("도쿄 타워에서 사진 찍기")
                .startDateTime(LocalDateTime.of(2025, 1, 5, 10, 0))
                .endDateTime(LocalDateTime.of(2025, 1, 5, 12, 0))
                .placeName(null)
                .rating(BigDecimal.valueOf(4.5))
                .build();
    }

    @Test
    @DisplayName("일정 생성 성공")
    void createSchedule_Success() throws Exception {
        // given
        given(scheduleService.createSchedule(eq(tripId), any(ScheduleCreateRequestDto.class)))
                .willReturn(scheduleDto);

        // when & then
        mockMvc.perform(post("/api/trips/{tripId}/schedules", tripId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(scheduleId))
                .andExpect(jsonPath("$.data.title").value("도쿄 타워 방문"))
                .andExpect(jsonPath("$.data.memo").value("도쿄 타워에서 사진 찍기"))
                .andExpect(jsonPath("$.data.rating").value(4.5))
                .andDo(document("schedule-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID")
                        ),
                        requestFields(
                                fieldWithPath("title").description("일정 제목"),
                                fieldWithPath("startDateTime").description("일정 시작 일시"),
                                fieldWithPath("endDateTime").description("일정 종료 일시"),
                                fieldWithPath("memo").description("일정 메모"),
                                fieldWithPath("rating").description("평점"),
                                fieldWithPath("placeId").description("장소 ID (nullable)").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("일정 ID"),
                                fieldWithPath("data.tripId").description("여행 ID"),
                                fieldWithPath("data.title").description("일정 제목"),
                                fieldWithPath("data.memo").description("일정 메모"),
                                fieldWithPath("data.startDateTime").description("일정 시작 일시"),
                                fieldWithPath("data.endDateTime").description("일정 종료 일시"),
                                fieldWithPath("data.placeName").description("장소 이름"),
                                fieldWithPath("data.rating").description("평점")
                        )
                ));

        // then
        then(scheduleService).should().createSchedule(eq(tripId), any(ScheduleCreateRequestDto.class));
    }

    @Test
    @DisplayName("일정 조회 성공")
    void getSchedule_Success() throws Exception {
        // given
        given(scheduleService.getSchedule(eq(tripId), eq(scheduleId)))
                .willReturn(scheduleDto);

        // when & then
        mockMvc.perform(get("/api/trips/{tripId}/schedules/{scheduleId}", tripId, scheduleId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(scheduleId))
                .andExpect(jsonPath("$.data.title").value("도쿄 타워 방문"))
                .andExpect(jsonPath("$.data.memo").value("도쿄 타워에서 사진 찍기"))
                .andDo(document("schedule-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("scheduleId").description("일정 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("일정 ID"),
                                fieldWithPath("data.tripId").description("여행 ID"),
                                fieldWithPath("data.title").description("일정 제목"),
                                fieldWithPath("data.memo").description("일정 메모"),
                                fieldWithPath("data.startDateTime").description("일정 시작 일시"),
                                fieldWithPath("data.endDateTime").description("일정 종료 일시"),
                                fieldWithPath("data.placeName").description("장소 이름"),
                                fieldWithPath("data.rating").description("평점")
                        )
                ));

        // then
        then(scheduleService).should().getSchedule(eq(tripId), eq(scheduleId));
    }

    @Test
    @DisplayName("일정 목록 조회 성공")
    void getSchedules_Success() throws Exception {
        // given
        ScheduleDto scheduleDto2 = ScheduleDto.builder()
                .id(2L)
                .tripId(tripId)
                .title("아사쿠사 신사 방문")
                .memo("전통 문화 체험")
                .startDateTime(LocalDateTime.of(2025, 1, 6, 9, 0))
                .endDateTime(LocalDateTime.of(2025, 1, 6, 11, 0))
                .placeName(null)
                .rating(BigDecimal.valueOf(4.0))
                .build();

        CursorPageResponseDto<ScheduleDto> response = CursorPageResponseDto.<ScheduleDto>builder()
                .content(List.of(scheduleDto, scheduleDto2))
                .nextCursor(null)
                .nextAfter(null)
                .size(2)
                .hasNext(false)
                .build();

        given(scheduleService.getSchedules(
                eq(tripId),
                eq("도쿄"),
                eq("createdAt"),
                eq("ASC"),
                eq(null),
                eq(null),
                eq(10)
        )).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/trips/{tripId}/schedules", tripId)
                        .header("Authorization", "Bearer " + accessToken)
                        .param("keyword", "도쿄")
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andDo(document("schedule-list",
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
                                parameterWithName("direction").description("정렬 방향 (ASC/DESC, 기본값: ASC)").optional(),
                                parameterWithName("limit").description("페이지 크기 (기본값: 10)").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.content").description("일정 목록"),
                                fieldWithPath("data.content[].id").description("일정 ID"),
                                fieldWithPath("data.content[].tripId").description("여행 ID"),
                                fieldWithPath("data.content[].title").description("일정 제목"),
                                fieldWithPath("data.content[].memo").description("일정 메모"),
                                fieldWithPath("data.content[].startDateTime").description("일정 시작 일시"),
                                fieldWithPath("data.content[].endDateTime").description("일정 종료 일시"),
                                fieldWithPath("data.content[].placeName").description("장소 이름"),
                                fieldWithPath("data.content[].rating").description("평점"),
                                fieldWithPath("data.nextCursor").description("다음 커서"),
                                fieldWithPath("data.nextAfter").description("다음 After ID"),
                                fieldWithPath("data.size").description("현재 페이지 크기"),
                                fieldWithPath("data.hasNext").description("다음 페이지 존재 여부")
                        )
                ));

        // then
        then(scheduleService).should().getSchedules(
                eq(tripId),
                eq("도쿄"),
                eq("createdAt"),
                eq("ASC"),
                eq(null),
                eq(null),
                eq(10)
        );
    }

    @Test
    @DisplayName("일정 수정 성공")
    void updateSchedule_Success() throws Exception {
        // given
        ScheduleDto updatedDto = ScheduleDto.builder()
                .id(scheduleId)
                .tripId(tripId)
                .title("도쿄 스카이트리 방문")
                .memo("스카이트리에서 일몰 보기")
                .startDateTime(LocalDateTime.of(2025, 1, 5, 14, 0))
                .endDateTime(LocalDateTime.of(2025, 1, 5, 16, 0))
                .placeName(null)
                .rating(BigDecimal.valueOf(5.0))
                .build();

        given(scheduleService.updateSchedule(eq(tripId), eq(scheduleId), any(ScheduleUpdateRequestDto.class)))
                .willReturn(updatedDto);

        // when & then
        mockMvc.perform(patch("/api/trips/{tripId}/schedules/{scheduleId}", tripId, scheduleId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(scheduleId))
                .andExpect(jsonPath("$.data.title").value("도쿄 스카이트리 방문"))
                .andExpect(jsonPath("$.data.memo").value("스카이트리에서 일몰 보기"))
                .andExpect(jsonPath("$.data.rating").value(5.0))
                .andDo(document("schedule-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("scheduleId").description("일정 ID")
                        ),
                        requestFields(
                                fieldWithPath("title").description("수정할 일정 제목"),
                                fieldWithPath("startDateTime").description("수정할 일정 시작 일시"),
                                fieldWithPath("endDateTime").description("수정할 일정 종료 일시"),
                                fieldWithPath("memo").description("수정할 일정 메모"),
                                fieldWithPath("rating").description("수정할 평점"),
                                fieldWithPath("placeId").description("수정할 장소 ID (nullable)").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("일정 ID"),
                                fieldWithPath("data.tripId").description("여행 ID"),
                                fieldWithPath("data.title").description("일정 제목"),
                                fieldWithPath("data.memo").description("일정 메모"),
                                fieldWithPath("data.startDateTime").description("일정 시작 일시"),
                                fieldWithPath("data.endDateTime").description("일정 종료 일시"),
                                fieldWithPath("data.placeName").description("장소 이름"),
                                fieldWithPath("data.rating").description("평점")
                        )
                ));

        // then
        then(scheduleService).should().updateSchedule(eq(tripId), eq(scheduleId), any(ScheduleUpdateRequestDto.class));
    }

    @Test
    @DisplayName("일정 삭제 성공")
    void deleteSchedule_Success() throws Exception {
        // given
        willDoNothing().given(scheduleService).deleteSchedule(eq(tripId), eq(scheduleId));

        // when & then
        mockMvc.perform(delete("/api/trips/{tripId}/schedules/{scheduleId}", tripId, scheduleId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(document("schedule-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("scheduleId").description("일정 ID")
                        )
                ));

        // then
        then(scheduleService).should().deleteSchedule(eq(tripId), eq(scheduleId));
    }
}