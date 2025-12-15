package travel.mytravelplan.domain.diary.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.diary.dto.DiaryCreateRequestDto;
import travel.mytravelplan.domain.diary.dto.DiaryDto;
import travel.mytravelplan.domain.diary.dto.DiaryUpdateRequestDto;
import travel.mytravelplan.domain.diary.enums.Emotion;
import travel.mytravelplan.domain.diary.entity.Diary;
import travel.mytravelplan.domain.diary.service.DiaryService;
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
import java.time.LocalDateTime;
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

@WebMvcTest(DiaryController.class)
@DisplayName("일기 컨트롤러 테스트")
class DiaryControllerTest extends ControllerTestSupport {

    @MockitoBean
    private DiaryService diaryService;

    private String accessToken;
    private Long userId;
    private Long tripId;
    private Long diaryId;
    private User testUser;
    private Trip testTrip;
    private TripJoin testTripJoin;
    private Diary testDiary;
    private DiaryCreateRequestDto createRequestDto;
    private DiaryUpdateRequestDto updateRequestDto;
    private DiaryDto diaryDto;

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
        given(tripJoinRepository.findByUserIdAndTripId(eq(userId), eq(tripId))).willReturn(Optional.of(testTripJoin));

        diaryId = 1L;

        testDiary = Diary.createDiary(
                "테스트 일기",
                "오늘은 정말 즐거운 하루였다.",
                List.of("http://example.com/image1.jpg", "http://example.com/image2.jpg"),
                LocalDate.of(2025, 1, 5),
                Emotion.HAPPY,
                testTrip,
                testTripJoin
        );
        ReflectionTestUtils.setField(testDiary, "id", diaryId);

        given(diaryRepository.findById(eq(diaryId))).willReturn(Optional.of(testDiary));

        createRequestDto = DiaryCreateRequestDto.builder()
                .title("테스트 일기")
                .content("오늘은 정말 즐거운 하루였다.")
                .date(LocalDate.of(2025, 1, 5))
                .emotion(Emotion.HAPPY)
                .imageUrls(List.of("http://example.com/image1.jpg", "http://example.com/image2.jpg"))
                .build();

        updateRequestDto = DiaryUpdateRequestDto.builder()
                .title("수정된 일기")
                .content("수정된 내용입니다.")
                .date(LocalDate.of(2025, 1, 5))
                .emotion(Emotion.EXCITED)
                .imageUrls(List.of("http://example.com/image3.jpg"))
                .build();

        diaryDto = DiaryDto.builder()
                .id(diaryId)
                .title("테스트 일기")
                .content("오늘은 정말 즐거운 하루였다.")
                .date(LocalDate.of(2025, 1, 5))
                .emotion(Emotion.HAPPY)
                .imageUrls(List.of("http://example.com/image1.jpg", "http://example.com/image2.jpg"))
                .createdAt(LocalDateTime.of(2025, 1, 5, 10, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 5, 10, 0))
                .build();
    }

    @Test
    @DisplayName("일기 생성 성공")
    void createDiary_Success() throws Exception {
        // given
        given(diaryService.createDiary(eq(tripId), any(User.class), any(DiaryCreateRequestDto.class)))
                .willReturn(diaryDto);

        // when
        mockMvc.perform(post("/api/trips/{tripId}/diaries", tripId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(diaryId))
                .andExpect(jsonPath("$.data.title").value("테스트 일기"))
                .andExpect(jsonPath("$.data.content").value("오늘은 정말 즐거운 하루였다."))
                .andExpect(jsonPath("$.data.emotion").value("HAPPY"))
                .andDo(document("diary-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID")
                        ),
                        requestFields(
                                fieldWithPath("title").description("일기 제목"),
                                fieldWithPath("content").description("일기 내용"),
                                fieldWithPath("date").description("일기 날짜"),
                                fieldWithPath("emotion").description("감정 (HAPPY, SAD, EXCITED, TIRED, PEACEFUL)"),
                                fieldWithPath("imageUrls").description("이미지 URL 목록")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("일기 ID"),
                                fieldWithPath("data.title").description("일기 제목"),
                                fieldWithPath("data.content").description("일기 내용"),
                                fieldWithPath("data.date").description("일기 날짜"),
                                fieldWithPath("data.emotion").description("감정"),
                                fieldWithPath("data.imageUrls").description("이미지 URL 목록"),
                                fieldWithPath("data.createdAt").description("생성 일시"),
                                fieldWithPath("data.updatedAt").description("수정 일시")
                        )
                ));

        // then
        assertThat(diaryDto).isNotNull();
        assertThat(diaryDto.getTitle()).isEqualTo("테스트 일기");
        then(diaryService).should().createDiary(eq(tripId), any(User.class), any(DiaryCreateRequestDto.class));
    }

    @Test
    @DisplayName("일기 조회 성공")
    void getDiary_Success() throws Exception {
        // given
        given(diaryService.getDiary(eq(tripId), eq(diaryId))).willReturn(diaryDto);

        // when
        mockMvc.perform(get("/api/trips/{tripId}/diaries/{diaryId}", tripId, diaryId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(diaryId))
                .andExpect(jsonPath("$.data.title").value("테스트 일기"))
                .andExpect(jsonPath("$.data.content").value("오늘은 정말 즐거운 하루였다."))
                .andDo(document("diary-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("diaryId").description("일기 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("일기 ID"),
                                fieldWithPath("data.title").description("일기 제목"),
                                fieldWithPath("data.content").description("일기 내용"),
                                fieldWithPath("data.date").description("일기 날짜"),
                                fieldWithPath("data.emotion").description("감정"),
                                fieldWithPath("data.imageUrls").description("이미지 URL 목록"),
                                fieldWithPath("data.createdAt").description("생성 일시"),
                                fieldWithPath("data.updatedAt").description("수정 일시")
                        )
                ));

        // then
        assertThat(diaryDto).isNotNull();
        assertThat(diaryDto.getId()).isEqualTo(diaryId);
        then(diaryService).should().getDiary(eq(tripId), eq(diaryId));
    }

    @Test
    @DisplayName("일기 목록 조회 성공")
    void getDiaries_Success() throws Exception {
        // given
        DiaryDto diaryDto2 = DiaryDto.builder()
                .id(2L)
                .title("테스트 일기 2")
                .content("두 번째 일기")
                .date(LocalDate.of(2025, 1, 6))
                .emotion(Emotion.PEACEFUL)
                .imageUrls(List.of())
                .createdAt(LocalDateTime.of(2025, 1, 6, 10, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 6, 10, 0))
                .build();

        CursorPageResponseDto<DiaryDto> pageResponse = CursorPageResponseDto.<DiaryDto>builder()
                .content(List.of(diaryDto, diaryDto2))
                .nextCursor("2025-01-06T10:00:00")
                .nextAfter(2L)
                .size(2)
                .hasNext(false)
                .build();

        given(diaryService.getDiaries(
                eq(tripId),
                eq("테스트"),
                eq("createdAt"),
                eq("ASC"),
                eq(null),
                eq(null),
                eq(10)
        )).willReturn(pageResponse);

        // when
        mockMvc.perform(get("/api/trips/{tripId}/diaries", tripId)
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
                .andDo(document("diary-list",
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
                                fieldWithPath("data.content").description("일기 목록"),
                                fieldWithPath("data.content[].id").description("일기 ID"),
                                fieldWithPath("data.content[].title").description("일기 제목"),
                                fieldWithPath("data.content[].content").description("일기 내용"),
                                fieldWithPath("data.content[].date").description("일기 날짜"),
                                fieldWithPath("data.content[].emotion").description("감정"),
                                fieldWithPath("data.content[].imageUrls").description("이미지 URL 목록"),
                                fieldWithPath("data.content[].createdAt").description("생성 일시"),
                                fieldWithPath("data.content[].updatedAt").description("수정 일시"),
                                fieldWithPath("data.nextCursor").description("다음 커서"),
                                fieldWithPath("data.nextAfter").description("다음 이후 ID"),
                                fieldWithPath("data.size").description("현재 페이지 크기"),
                                fieldWithPath("data.hasNext").description("다음 페이지 존재 여부")
                        )
                ));

        // then
        assertThat(pageResponse).isNotNull();
        assertThat(pageResponse.getContent()).hasSize(2);
        then(diaryService).should().getDiaries(
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
    @DisplayName("일기 수정 성공")
    void updateDiary_Success() throws Exception {
        // given
        DiaryDto updatedDiaryDto = DiaryDto.builder()
                .id(diaryId)
                .title("수정된 일기")
                .content("수정된 내용입니다.")
                .date(LocalDate.of(2025, 1, 5))
                .emotion(Emotion.EXCITED)
                .imageUrls(List.of("http://example.com/image3.jpg"))
                .createdAt(LocalDateTime.of(2025, 1, 5, 10, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 5, 11, 0))
                .build();

        given(diaryService.updateDiary(eq(tripId), eq(diaryId), any(DiaryUpdateRequestDto.class)))
                .willReturn(updatedDiaryDto);

        // when
        mockMvc.perform(patch("/api/trips/{tripId}/diaries/{diaryId}", tripId, diaryId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(diaryId))
                .andExpect(jsonPath("$.data.title").value("수정된 일기"))
                .andExpect(jsonPath("$.data.content").value("수정된 내용입니다."))
                .andExpect(jsonPath("$.data.emotion").value("EXCITED"))
                .andDo(document("diary-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("diaryId").description("일기 ID")
                        ),
                        requestFields(
                                fieldWithPath("title").description("수정할 일기 제목"),
                                fieldWithPath("content").description("수정할 일기 내용"),
                                fieldWithPath("date").description("수정할 일기 날짜"),
                                fieldWithPath("emotion").description("수정할 감정 (HAPPY, SAD, EXCITED, TIRED, PEACEFUL)"),
                                fieldWithPath("imageUrls").description("수정할 이미지 URL 목록")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("일기 ID"),
                                fieldWithPath("data.title").description("수정된 일기 제목"),
                                fieldWithPath("data.content").description("수정된 일기 내용"),
                                fieldWithPath("data.date").description("수정된 일기 날짜"),
                                fieldWithPath("data.emotion").description("수정된 감정"),
                                fieldWithPath("data.imageUrls").description("수정된 이미지 URL 목록"),
                                fieldWithPath("data.createdAt").description("생성 일시"),
                                fieldWithPath("data.updatedAt").description("수정 일시")
                        )
                ));

        // then
        assertThat(updatedDiaryDto).isNotNull();
        assertThat(updatedDiaryDto.getTitle()).isEqualTo("수정된 일기");
        then(diaryService).should().updateDiary(eq(tripId), eq(diaryId), any(DiaryUpdateRequestDto.class));
    }

    @Test
    @DisplayName("일기 삭제 성공")
    void deleteDiary_Success() throws Exception {
        // given
        willDoNothing().given(diaryService).deleteDiary(eq(tripId), eq(diaryId));

        // when
        mockMvc.perform(delete("/api/trips/{tripId}/diaries/{diaryId}", tripId, diaryId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(document("diary-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("diaryId").description("일기 ID")
                        )
                ));

        // then
        then(diaryService).should().deleteDiary(eq(tripId), eq(diaryId));
    }
}