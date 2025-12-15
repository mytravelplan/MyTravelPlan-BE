package travel.mytravelplan.domain.place.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.place.dto.CustomPlaceCreateRequestDto;
import travel.mytravelplan.domain.place.dto.CustomPlaceDto;
import travel.mytravelplan.domain.place.dto.CustomPlaceUpdateRequestDto;
import travel.mytravelplan.domain.place.entity.CustomPlace;
import travel.mytravelplan.domain.place.enums.PlaceCategory;
import travel.mytravelplan.domain.place.enums.PlaceType;
import travel.mytravelplan.domain.place.service.CustomPlaceService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.entity.UserProfile;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
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

@WebMvcTest(CustomPlaceController.class)
@DisplayName("커스텀 장소 컨트롤러 테스트")
class CustomPlaceControllerTest extends ControllerTestSupport {

    @MockitoBean
    private CustomPlaceService customPlaceService;

    private String accessToken;
    private Long userId;
    private Long customPlaceId;
    private User testUser;
    private CustomPlace testCustomPlace;
    private CustomPlaceCreateRequestDto createRequestDto;
    private CustomPlaceUpdateRequestDto updateRequestDto;
    private CustomPlaceDto customPlaceDto;

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

        customPlaceId = 1L;

        testCustomPlace = CustomPlace.createCustomPlace(
                "테스트 장소",
                "서울시 강남구",
                "테스트 설명",
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780"),
                PlaceCategory.RESTAURANT,
                testUser
        );
        ReflectionTestUtils.setField(testCustomPlace, "id", customPlaceId);

        given(customPlaceRepository.findById(eq(customPlaceId))).willReturn(Optional.of(testCustomPlace));

        createRequestDto = CustomPlaceCreateRequestDto.builder()
                .placeType(PlaceType.CUSTOM)
                .name("테스트 장소")
                .address("서울시 강남구")
                .description("테스트 설명")
                .latitude(new BigDecimal("37.5665"))
                .longitude(new BigDecimal("126.9780"))
                .category(PlaceCategory.RESTAURANT)
                .build();

        updateRequestDto = CustomPlaceUpdateRequestDto.builder()
                .placeType(PlaceType.CUSTOM)
                .name("수정된 장소")
                .address("서울시 서초구")
                .description("수정된 설명")
                .latitude(new BigDecimal("37.4833"))
                .longitude(new BigDecimal("127.0322"))
                .category(PlaceCategory.CAFE)
                .build();

        customPlaceDto = CustomPlaceDto.builder()
                .id(customPlaceId)
                .name("테스트 장소")
                .address("서울시 강남구")
                .description("테스트 설명")
                .latitude(new BigDecimal("37.5665"))
                .longitude(new BigDecimal("126.9780"))
                .build();
    }

    @Test
    @DisplayName("나만의 장소 생성 성공")
    void createCustomPlace_Success() throws Exception {
        // given
        given(customPlaceService.createCustomPlace(any(User.class), any(CustomPlaceCreateRequestDto.class)))
                .willReturn(customPlaceDto);

        // when
        mockMvc.perform(post("/api/custom-places")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(customPlaceId))
                .andExpect(jsonPath("$.data.name").value("테스트 장소"))
                .andExpect(jsonPath("$.data.address").value("서울시 강남구"))
                .andExpect(jsonPath("$.data.description").value("테스트 설명"))
                .andDo(document("custom-place-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        requestFields(
                                fieldWithPath("placeType").description("장소 타입 (CUSTOM)"),
                                fieldWithPath("name").description("장소 이름"),
                                fieldWithPath("address").description("장소 주소"),
                                fieldWithPath("description").description("장소 설명"),
                                fieldWithPath("latitude").description("위도"),
                                fieldWithPath("longitude").description("경도"),
                                fieldWithPath("category").description("장소 카테고리 (CAFE, RESTAURANT, HOTEL, ATTRACTION, SHOPPING, NATURE, CULTURE, NIGHTLIFE, OTHER)")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("장소 ID"),
                                fieldWithPath("data.name").description("장소 이름"),
                                fieldWithPath("data.address").description("장소 주소"),
                                fieldWithPath("data.description").description("장소 설명"),
                                fieldWithPath("data.longitude").description("경도"),
                                fieldWithPath("data.latitude").description("위도")
                        )
                ));

        // then
        assertThat(customPlaceDto).isNotNull();
        assertThat(customPlaceDto.getName()).isEqualTo("테스트 장소");
        then(customPlaceService).should().createCustomPlace(any(User.class), any(CustomPlaceCreateRequestDto.class));
    }

    @Test
    @DisplayName("나만의 장소 조회 성공")
    void getCustomPlace_Success() throws Exception {
        // given
        given(customPlaceService.getCustomPlace(eq(customPlaceId))).willReturn(customPlaceDto);

        // when
        mockMvc.perform(get("/api/custom-places/{customPlaceId}", customPlaceId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(customPlaceId))
                .andExpect(jsonPath("$.data.name").value("테스트 장소"))
                .andDo(document("custom-place-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("customPlaceId").description("장소 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("장소 ID"),
                                fieldWithPath("data.name").description("장소 이름"),
                                fieldWithPath("data.address").description("장소 주소"),
                                fieldWithPath("data.description").description("장소 설명"),
                                fieldWithPath("data.longitude").description("경도"),
                                fieldWithPath("data.latitude").description("위도")
                        )
                ));

        // then
        assertThat(customPlaceDto).isNotNull();
        assertThat(customPlaceDto.getId()).isEqualTo(customPlaceId);
        then(customPlaceService).should().getCustomPlace(eq(customPlaceId));
    }

    @Test
    @DisplayName("나의 나만의 장소 목록 조회 성공")
    void getUserCustomPlaces_Success() throws Exception {
        // given
        CursorPageResponseDto<CustomPlaceDto> pageResponse = CursorPageResponseDto.<CustomPlaceDto>builder()
                .content(List.of(customPlaceDto))
                .nextCursor("nextCursor")
                .nextAfter(2L)
                .size(1)
                .hasNext(false)
                .build();

        given(customPlaceService.getCustomPlaces(
                any(User.class),
                any(),
                eq("createdAt"),
                eq("ASC"),
                any(),
                any(),
                eq(10)
        )).willReturn(pageResponse);

        // when
        mockMvc.perform(get("/api/custom-places/my-custom-places")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.size").value(1))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andDo(document("my-custom-place-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드").optional(),
                                parameterWithName("orderBy").description("정렬 기준 (기본값: createdAt)").optional(),
                                parameterWithName("direction").description("정렬 방향 (ASC/DESC, 기본값: ASC)").optional(),
                                parameterWithName("cursor").description("커서").optional(),
                                parameterWithName("after").description("이후 ID").optional(),
                                parameterWithName("limit").description("페이지 크기 (기본값: 10)").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.content").description("장소 목록"),
                                fieldWithPath("data.content[].id").description("장소 ID"),
                                fieldWithPath("data.content[].name").description("장소 이름"),
                                fieldWithPath("data.content[].address").description("장소 주소"),
                                fieldWithPath("data.content[].description").description("장소 설명"),
                                fieldWithPath("data.content[].longitude").description("경도"),
                                fieldWithPath("data.content[].latitude").description("위도"),
                                fieldWithPath("data.nextCursor").description("다음 커서"),
                                fieldWithPath("data.nextAfter").description("다음 ID"),
                                fieldWithPath("data.size").description("현재 페이지 크기"),
                                fieldWithPath("data.hasNext").description("다음 페이지 존재 여부")
                        )
                ));

        // then
        assertThat(pageResponse).isNotNull();
        assertThat(pageResponse.getContent()).hasSize(1);
        then(customPlaceService).should().getCustomPlaces(
                any(User.class),
                any(),
                eq("createdAt"),
                eq("ASC"),
                any(),
                any(),
                eq(10)
        );
    }

    @Test
    @DisplayName("나만의 장소 수정 성공")
    void updateCustomPlace_Success() throws Exception {
        // given
        CustomPlaceDto updatedCustomPlaceDto = CustomPlaceDto.builder()
                .id(customPlaceId)
                .name("수정된 장소")
                .address("서울시 서초구")
                .description("수정된 설명")
                .latitude(new BigDecimal("37.4833"))
                .longitude(new BigDecimal("127.0322"))
                .build();

        given(customPlaceService.updateCustomPlace(eq(customPlaceId), any(CustomPlaceUpdateRequestDto.class)))
                .willReturn(updatedCustomPlaceDto);

        // when
        mockMvc.perform(patch("/api/custom-places/{customPlaceId}", customPlaceId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(customPlaceId))
                .andExpect(jsonPath("$.data.name").value("수정된 장소"))
                .andExpect(jsonPath("$.data.address").value("서울시 서초구"))
                .andDo(document("custom-place-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("customPlaceId").description("장소 ID")
                        ),
                        requestFields(
                                fieldWithPath("placeType").description("장소 타입 (CUSTOM)"),
                                fieldWithPath("name").description("수정할 장소 이름").optional(),
                                fieldWithPath("address").description("수정할 장소 주소").optional(),
                                fieldWithPath("description").description("수정할 장소 설명").optional(),
                                fieldWithPath("latitude").description("수정할 위도").optional(),
                                fieldWithPath("longitude").description("수정할 경도").optional(),
                                fieldWithPath("category").description("수정할 장소 카테고리").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("장소 ID"),
                                fieldWithPath("data.name").description("수정된 장소 이름"),
                                fieldWithPath("data.address").description("수정된 장소 주소"),
                                fieldWithPath("data.description").description("수정된 장소 설명"),
                                fieldWithPath("data.longitude").description("수정된 경도"),
                                fieldWithPath("data.latitude").description("수정된 위도")
                        )
                ));

        // then
        assertThat(updatedCustomPlaceDto).isNotNull();
        assertThat(updatedCustomPlaceDto.getName()).isEqualTo("수정된 장소");
        then(customPlaceService).should().updateCustomPlace(eq(customPlaceId), any(CustomPlaceUpdateRequestDto.class));
    }

    @Test
    @DisplayName("나만의 장소 삭제 성공")
    void deleteCustomPlace_Success() throws Exception {
        // given
        willDoNothing().given(customPlaceService).deleteCustomPlace(eq(customPlaceId));

        // when
        mockMvc.perform(delete("/api/custom-places/{customPlaceId}", customPlaceId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(document("custom-place-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("customPlaceId").description("장소 ID")
                        )
                ));

        // then
        then(customPlaceService).should().deleteCustomPlace(eq(customPlaceId));
    }
}