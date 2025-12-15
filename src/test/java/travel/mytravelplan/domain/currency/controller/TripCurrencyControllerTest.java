package travel.mytravelplan.domain.currency.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.currency.dto.TripCurrencyCreateRequestDto;
import travel.mytravelplan.domain.currency.dto.TripCurrencyDto;
import travel.mytravelplan.domain.currency.dto.TripCurrencyUpdateRequestDto;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.currency.service.TripCurrencyService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.entity.UserProfile;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.global.support.ControllerTestSupport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
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

@WebMvcTest(TripCurrencyController.class)
@DisplayName("여행 환율 컨트롤러 테스트")
class TripCurrencyControllerTest extends ControllerTestSupport {

    @MockitoBean
    private TripCurrencyService tripCurrencyService;

    private String accessToken;
    private Long tripId;
    private TripCurrencyCreateRequestDto createRequestDto;
    private TripCurrencyUpdateRequestDto updateRequestDto;
    private TripCurrencyDto tripCurrencyDto;

    @BeforeEach
    void setUp() {
        given(jwtBlacklistService.isTokenBlacklisted(any(String.class))).willReturn(false);

        UserProfile userProfile = UserProfile.createUserProfile(
                "테스트 사용자",
                "http://example.com/user.jpg"
        );

        User testUser = User.createUser(
                "testUser",
                "password",
                "user@test.com",
                SocialType.LOCAL,
                null,
                LocalDate.of(1995, 5, 15),
                "010-1234-5678",
                Gender.MALE,
                Set.of(Role.USER)
        );

        testUser.setUserProfile(userProfile);

        Long userId = 1L;
        tripId = 1L;

        ReflectionTestUtils.setField(testUser, "id", userId);

        accessToken = jwtUtils.createAccessToken(userId, Set.of(Role.USER));

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));

        given(tripJoinRepository.existsByUserIdAndTripId(eq(userId), eq(tripId))).willReturn(true);

        createRequestDto = TripCurrencyCreateRequestDto.builder()
                .currencyType(CurrencyType.USD)
                .build();

        updateRequestDto = TripCurrencyUpdateRequestDto.builder()
                .exchangeRate(new BigDecimal("1300.50"))
                .build();

        tripCurrencyDto = TripCurrencyDto.builder()
                .name("미국 달러")
                .currencyType(CurrencyType.USD)
                .exchangeRate(new BigDecimal("1250.00"))
                .build();
    }

    @Test
    @DisplayName("여행 통화 추가 성공")
    void createTripCurrency_Success() throws Exception {
        // given
        given(tripCurrencyService.createTripCurrency(eq(tripId), any(TripCurrencyCreateRequestDto.class)))
                .willReturn(tripCurrencyDto);

        // when & then
        mockMvc.perform(post("/api/trips/{tripId}/trip-currencies", tripId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("미국 달러"))
                .andExpect(jsonPath("$.data.currencyType").value("USD"))
                .andExpect(jsonPath("$.data.exchangeRate").value(1250.00))
                .andDo(document("trip-currency-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (USER 또는 ADMIN 권한 필요)")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID")
                        ),
                        requestFields(
                                fieldWithPath("currencyType").description("통화 타입 (예: USD, EUR, JPY 등)")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.name").description("통화 이름"),
                                fieldWithPath("data.currencyType").description("통화 타입"),
                                fieldWithPath("data.exchangeRate").description("환율")
                        )
                ));

        // then
        then(tripCurrencyService).should().createTripCurrency(eq(tripId), any(TripCurrencyCreateRequestDto.class));
    }

    @Test
    @DisplayName("여행 통화 목록 조회 성공")
    void getTripCurrencies_Success() throws Exception {
        // given
        TripCurrencyDto tripCurrencyDto1 = TripCurrencyDto.builder()
                .name("미국 달러")
                .currencyType(CurrencyType.USD)
                .exchangeRate(new BigDecimal("1250.00"))
                .build();

        TripCurrencyDto tripCurrencyDto2 = TripCurrencyDto.builder()
                .name("유로")
                .currencyType(CurrencyType.EUR)
                .exchangeRate(new BigDecimal("1400.00"))
                .build();

        List<TripCurrencyDto> tripCurrencies = List.of(tripCurrencyDto1, tripCurrencyDto2);

        given(tripCurrencyService.getTripCurrencies(eq(tripId))).willReturn(tripCurrencies);

        // when & then
        mockMvc.perform(get("/api/trips/{tripId}/trip-currencies", tripId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("미국 달러"))
                .andExpect(jsonPath("$.data[0].currencyType").value("USD"))
                .andExpect(jsonPath("$.data[0].exchangeRate").value(1250.00))
                .andExpect(jsonPath("$.data[1].name").value("유로"))
                .andExpect(jsonPath("$.data[1].currencyType").value("EUR"))
                .andExpect(jsonPath("$.data[1].exchangeRate").value(1400.00))
                .andDo(document("trip-currency-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (USER 또는 ADMIN 권한 필요)")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data").description("여행 통화 목록"),
                                fieldWithPath("data[].name").description("통화 이름"),
                                fieldWithPath("data[].currencyType").description("통화 타입"),
                                fieldWithPath("data[].exchangeRate").description("환율")
                        )
                ));

        // then
        then(tripCurrencyService).should().getTripCurrencies(eq(tripId));
    }

    @Test
    @DisplayName("여행 통화 수정 성공")
    void updateTripCurrency_Success() throws Exception {
        // given
        TripCurrencyDto updatedTripCurrencyDto = TripCurrencyDto.builder()
                .name("미국 달러")
                .currencyType(CurrencyType.USD)
                .exchangeRate(new BigDecimal("1300.50"))
                .build();

        given(tripCurrencyService.updateTripCurrency(eq(tripId), eq(CurrencyType.USD), any(TripCurrencyUpdateRequestDto.class)))
                .willReturn(updatedTripCurrencyDto);

        // when & then
        mockMvc.perform(patch("/api/trips/{tripId}/trip-currencies/{currencyType}", tripId, CurrencyType.USD)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("미국 달러"))
                .andExpect(jsonPath("$.data.currencyType").value("USD"))
                .andExpect(jsonPath("$.data.exchangeRate").value(1300.50))
                .andDo(document("trip-currency-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (USER 또는 ADMIN 권한 필요)")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("currencyType").description("통화 타입 (예: USD, EUR, JPY 등)")
                        ),
                        requestFields(
                                fieldWithPath("exchangeRate").description("변경할 환율")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.name").description("통화 이름"),
                                fieldWithPath("data.currencyType").description("통화 타입"),
                                fieldWithPath("data.exchangeRate").description("환율")
                        )
                ));

        // then
        then(tripCurrencyService).should().updateTripCurrency(eq(tripId), eq(CurrencyType.USD), any(TripCurrencyUpdateRequestDto.class));
    }
}