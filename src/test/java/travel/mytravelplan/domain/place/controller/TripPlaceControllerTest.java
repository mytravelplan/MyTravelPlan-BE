package travel.mytravelplan.domain.place.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.place.dto.*;
import travel.mytravelplan.domain.place.entity.TripPlace;
import travel.mytravelplan.domain.place.enums.PlaceCategory;
import travel.mytravelplan.domain.place.enums.PlaceType;
import travel.mytravelplan.domain.place.repository.TripPlaceRepository;
import travel.mytravelplan.domain.place.service.TripPlaceService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.entity.UserProfile;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.global.common.enums.Period;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.support.ControllerTestSupport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.then;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TripPlaceController.class)
@DisplayName("여행 장소 컨트롤러 테스트")
class TripPlaceControllerTest extends ControllerTestSupport {

    @MockitoBean
    private TripPlaceService tripPlaceService;

    @MockitoBean
    private TripPlaceRepository tripPlaceRepository;

    private String accessToken;
    private String adminAccessToken;
    private Long userId;
    private Long adminUserId;
    private Long tripPlaceId;
    private User testUser;
    private User adminUser;
    private TripPlace testTripPlace;
    private TripPlaceCreateRequestDto createRequestDto;
    private TripPlaceUpdateRequestDto updateRequestDto;
    private TripPlaceDto tripPlaceDto;
    private TripPlaceBookMarkDto bookmarkDto;

    @BeforeEach
    void setUp() {
        given(jwtBlacklistService.isTokenBlacklisted(any(String.class))).willReturn(false);

        // 일반 사용자 설정
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

        // 관리자 사용자 설정
        UserProfile adminProfile = UserProfile.createUserProfile(
                "관리자",
                "http://example.com/admin.jpg"
        );

        adminUser = User.createUser(
                "adminUser",
                "password",
                "admin@test.com",
                SocialType.LOCAL,
                null,
                LocalDate.of(1985, 1, 1),
                "010-9999-9999",
                Gender.MALE,
                Set.of(Role.ADMIN)
        );

        adminUser.setUserProfile(adminProfile);
        adminUserId = 2L;
        ReflectionTestUtils.setField(adminUser, "id", adminUserId);
        adminAccessToken = jwtUtils.createAccessToken(adminUserId, Set.of(Role.ADMIN));
        given(userRepository.findById(eq(adminUserId))).willReturn(Optional.of(adminUser));

        // 여행 장소 설정
        tripPlaceId = 1L;
        testTripPlace = TripPlace.createTripPlace(
                "에펠탑",
                "프랑스 파리",
                "파리의 랜드마크",
                new BigDecimal("48.8584"),
                new BigDecimal("2.2945"),
                PlaceCategory.ATTRACTION,
                "https://www.toureiffel.paris"
        );
        ReflectionTestUtils.setField(testTripPlace, "id", tripPlaceId);

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(testTripPlace));

        // 요청 DTO 설정
        createRequestDto = TripPlaceCreateRequestDto.builder()
                .placeType(PlaceType.TRIP)
                .name("에펠탑")
                .address("프랑스 파리")
                .description("파리의 랜드마크")
                .latitude(new BigDecimal("48.8584"))
                .longitude(new BigDecimal("2.2945"))
                .category(PlaceCategory.ATTRACTION)
                .externalUrl("https://www.toureiffel.paris")
                .build();

        updateRequestDto = TripPlaceUpdateRequestDto.builder()
                .placeType(PlaceType.TRIP)
                .name("에펠탑 (수정)")
                .address("프랑스 파리 7구")
                .description("파리의 상징적인 랜드마크")
                .latitude(new BigDecimal("48.8584"))
                .longitude(new BigDecimal("2.2945"))
                .category(PlaceCategory.ATTRACTION)
                .externalUrl("https://www.toureiffel.paris/en")
                .build();

        // 응답 DTO 설정
        tripPlaceDto = TripPlaceDto.builder()
                .id(tripPlaceId)
                .name("에펠탑")
                .address("프랑스 파리")
                .description("파리의 랜드마크")
                .latitude(new BigDecimal("48.8584"))
                .longitude(new BigDecimal("2.2945"))
                .externalUrl("https://www.toureiffel.paris")
                .build();

        bookmarkDto = TripPlaceBookMarkDto.builder()
                .tripPlaceId(tripPlaceId)
                .userId(userId)
                .bookmarked(true)
                .build();
    }

    @Test
    @DisplayName("여행 장소 생성 성공 - 관리자")
    void createTripPlace_Success() throws Exception {
        // given
        given(tripPlaceService.createTripPlace(any(User.class), any(TripPlaceCreateRequestDto.class)))
                .willReturn(tripPlaceDto);

        // when
        mockMvc.perform(post("/api/trip-places")
                        .header("Authorization", "Bearer " + adminAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(tripPlaceId))
                .andExpect(jsonPath("$.data.name").value("에펠탑"))
                .andExpect(jsonPath("$.data.address").value("프랑스 파리"))
                .andExpect(jsonPath("$.data.description").value("파리의 랜드마크"))
                .andExpect(jsonPath("$.data.externalUrl").value("https://www.toureiffel.paris"))
                .andDo(document("trip-place-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (관리자 권한 필요)")
                        ),
                        requestFields(
                                fieldWithPath("placeType").description("장소 타입 (TRIP)"),
                                fieldWithPath("name").description("여행 장소 이름"),
                                fieldWithPath("address").description("여행 장소 주소"),
                                fieldWithPath("description").description("여행 장소 설명"),
                                fieldWithPath("latitude").description("위도"),
                                fieldWithPath("longitude").description("경도"),
                                fieldWithPath("category").description("장소 카테고리 (CAFE, RESTAURANT, HOTEL, ATTRACTION, SHOPPING, NATURE, CULTURE, NIGHTLIFE, OTHER)"),
                                fieldWithPath("externalUrl").description("외부 URL (선택)")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("여행 장소 ID"),
                                fieldWithPath("data.name").description("여행 장소 이름"),
                                fieldWithPath("data.address").description("여행 장소 주소"),
                                fieldWithPath("data.description").description("여행 장소 설명"),
                                fieldWithPath("data.longitude").description("경도"),
                                fieldWithPath("data.latitude").description("위도"),
                                fieldWithPath("data.externalUrl").description("외부 URL")
                        )
                ));

        // then
        assertThat(tripPlaceDto).isNotNull();
        assertThat(tripPlaceDto.getName()).isEqualTo("에펠탑");
        then(tripPlaceService).should().createTripPlace(any(User.class), any(TripPlaceCreateRequestDto.class));
    }

    @Test
    @DisplayName("여행 장소 조회 성공")
    void getTripPlace_Success() throws Exception {
        // given
        given(tripPlaceService.getTripPlace(any(User.class), eq(tripPlaceId))).willReturn(tripPlaceDto);

        // when
        mockMvc.perform(get("/api/trip-places/{tripPlaceId}", tripPlaceId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(tripPlaceId))
                .andExpect(jsonPath("$.data.name").value("에펠탑"))
                .andDo(document("trip-place-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripPlaceId").description("여행 장소 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("여행 장소 ID"),
                                fieldWithPath("data.name").description("여행 장소 이름"),
                                fieldWithPath("data.address").description("여행 장소 주소"),
                                fieldWithPath("data.description").description("여행 장소 설명"),
                                fieldWithPath("data.longitude").description("경도"),
                                fieldWithPath("data.latitude").description("위도"),
                                fieldWithPath("data.externalUrl").description("외부 URL")
                        )
                ));

        // then
        assertThat(tripPlaceDto).isNotNull();
        assertThat(tripPlaceDto.getId()).isEqualTo(tripPlaceId);
        then(tripPlaceService).should().getTripPlace(any(User.class), eq(tripPlaceId));
    }

    @Test
    @DisplayName("여행 장소 목록 조회 성공")
    void getTripPlaces_Success() throws Exception {
        // given
        CursorPageResponseDto<TripPlaceDto> pageResponse = CursorPageResponseDto.<TripPlaceDto>builder()
                .content(List.of(tripPlaceDto))
                .nextCursor("nextCursor")
                .nextAfter(2L)
                .size(1)
                .hasNext(false)
                .build();

        given(tripPlaceService.getTripPlaces(
                any(User.class),
                eq("에펠탑"),
                eq("createdAt"),
                eq("ASC"),
                any(),
                any(),
                eq(10)
        )).willReturn(pageResponse);

        // when
        mockMvc.perform(get("/api/trip-places")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("keyword", "에펠탑")
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].name").value("에펠탑"))
                .andExpect(jsonPath("$.data.size").value(1))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andDo(document("trip-place-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드 (선택)").optional(),
                                parameterWithName("orderBy").description("정렬 기준 (기본값: createdAt)").optional(),
                                parameterWithName("direction").description("정렬 방향 (ASC, DESC) (기본값: ASC)").optional(),
                                parameterWithName("cursor").description("커서 (페이징용, 선택)").optional(),
                                parameterWithName("after").description("이후 ID (페이징용, 선택)").optional(),
                                parameterWithName("limit").description("페이지 크기 (기본값: 10)").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.content[]").description("여행 장소 목록"),
                                fieldWithPath("data.content[].id").description("여행 장소 ID"),
                                fieldWithPath("data.content[].name").description("여행 장소 이름"),
                                fieldWithPath("data.content[].address").description("여행 장소 주소"),
                                fieldWithPath("data.content[].description").description("여행 장소 설명"),
                                fieldWithPath("data.content[].longitude").description("경도"),
                                fieldWithPath("data.content[].latitude").description("위도"),
                                fieldWithPath("data.content[].externalUrl").description("외부 URL"),
                                fieldWithPath("data.nextCursor").description("다음 커서"),
                                fieldWithPath("data.nextAfter").description("다음 이후 ID"),
                                fieldWithPath("data.size").description("현재 페이지 크기"),
                                fieldWithPath("data.hasNext").description("다음 페이지 존재 여부")
                        )
                ));

        // then
        assertThat(pageResponse).isNotNull();
        assertThat(pageResponse.getContent()).hasSize(1);
        then(tripPlaceService).should().getTripPlaces(
                any(User.class),
                eq("에펠탑"),
                eq("createdAt"),
                eq("ASC"),
                any(),
                any(),
                eq(10)
        );
    }

    @Test
    @DisplayName("여행 장소 수정 성공 - 관리자")
    void updateTripPlace_Success() throws Exception {
        // given
        TripPlaceDto updatedDto = TripPlaceDto.builder()
                .id(tripPlaceId)
                .name("에펠탑 (수정)")
                .address("프랑스 파리 7구")
                .description("파리의 상징적인 랜드마크")
                .latitude(new BigDecimal("48.8584"))
                .longitude(new BigDecimal("2.2945"))
                .externalUrl("https://www.toureiffel.paris/en")
                .build();

        given(tripPlaceService.updateTripPlace(any(User.class), eq(tripPlaceId), any(TripPlaceUpdateRequestDto.class)))
                .willReturn(updatedDto);

        // when
        mockMvc.perform(patch("/api/trip-places/{tripPlaceId}", tripPlaceId)
                        .header("Authorization", "Bearer " + adminAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(tripPlaceId))
                .andExpect(jsonPath("$.data.name").value("에펠탑 (수정)"))
                .andExpect(jsonPath("$.data.address").value("프랑스 파리 7구"))
                .andDo(document("trip-place-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (관리자 권한 필요)")
                        ),
                        pathParameters(
                                parameterWithName("tripPlaceId").description("여행 장소 ID")
                        ),
                        requestFields(
                                fieldWithPath("placeType").description("장소 타입 (TRIP)"),
                                fieldWithPath("name").description("여행 장소 이름"),
                                fieldWithPath("address").description("여행 장소 주소"),
                                fieldWithPath("description").description("여행 장소 설명"),
                                fieldWithPath("latitude").description("위도"),
                                fieldWithPath("longitude").description("경도"),
                                fieldWithPath("category").description("장소 카테고리"),
                                fieldWithPath("externalUrl").description("외부 URL (선택)")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("여행 장소 ID"),
                                fieldWithPath("data.name").description("여행 장소 이름"),
                                fieldWithPath("data.address").description("여행 장소 주소"),
                                fieldWithPath("data.description").description("여행 장소 설명"),
                                fieldWithPath("data.longitude").description("경도"),
                                fieldWithPath("data.latitude").description("위도"),
                                fieldWithPath("data.externalUrl").description("외부 URL")
                        )
                ));

        // then
        assertThat(updatedDto).isNotNull();
        assertThat(updatedDto.getName()).isEqualTo("에펠탑 (수정)");
        then(tripPlaceService).should().updateTripPlace(any(User.class), eq(tripPlaceId), any(TripPlaceUpdateRequestDto.class));
    }

    @Test
    @DisplayName("여행 장소 삭제 성공 - 관리자")
    void deleteTripPlace_Success() throws Exception {
        // given
        willDoNothing().given(tripPlaceService).deleteTripPlace(eq(tripPlaceId));

        // when
        mockMvc.perform(delete("/api/trip-places/{tripPlaceId}", tripPlaceId)
                        .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isNoContent())
                .andDo(document("trip-place-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (관리자 권한 필요)")
                        ),
                        pathParameters(
                                parameterWithName("tripPlaceId").description("여행 장소 ID")
                        )
                ));

        // then
        then(tripPlaceService).should().deleteTripPlace(eq(tripPlaceId));
    }

    @Test
    @DisplayName("여행 장소 북마크 성공")
    void bookmarkTripPlace_Success() throws Exception {
        // given
        given(tripPlaceService.bookmarkTripPlace(any(User.class), eq(tripPlaceId)))
                .willReturn(bookmarkDto);

        // when
        mockMvc.perform(post("/api/trip-places/{tripPlaceId}/bookmark", tripPlaceId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tripPlaceId").value(tripPlaceId))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.bookmarked").value(true))
                .andDo(document("trip-place-bookmark",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripPlaceId").description("여행 장소 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.tripPlaceId").description("여행 장소 ID"),
                                fieldWithPath("data.userId").description("사용자 ID"),
                                fieldWithPath("data.bookmarked").description("북마크 상태 (true: 북마크 추가, false: 북마크 제거)")
                        )
                ));

        // then
        assertThat(bookmarkDto).isNotNull();
        assertThat(bookmarkDto.isBookmarked()).isTrue();
        then(tripPlaceService).should().bookmarkTripPlace(any(User.class), eq(tripPlaceId));
    }
}