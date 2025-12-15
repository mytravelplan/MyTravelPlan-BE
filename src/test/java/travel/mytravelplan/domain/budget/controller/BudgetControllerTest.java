package travel.mytravelplan.domain.budget.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.budget.dto.*;
import travel.mytravelplan.domain.budget.enums.BudgetType;
import travel.mytravelplan.domain.budget.service.BudgetService;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.expense.enums.CalculateType;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;
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

@WebMvcTest(BudgetController.class)
@DisplayName("예산 컨트롤러 테스트")
class BudgetControllerTest extends ControllerTestSupport {

    @MockitoBean
    private BudgetService budgetService;

    private String accessToken;
    private Long userId;
    private Long tripId;
    private Long budgetId;
    private User testUser;
    private Trip testTrip;
    private TripJoin testTripJoin;
    private PersonalBudgetCreateRequestDto personalBudgetCreateRequestDto;
    private SharedBudgetCreateRequestDto sharedBudgetCreateRequestDto;
    private PersonalBudgetUpdateRequestDto personalBudgetUpdateRequestDto;
    private SharedBudgetUpdateRequestDto sharedBudgetUpdateRequestDto;
    private PersonalBudgetDto personalBudgetDto;
    private SharedBudgetDto sharedBudgetDto;

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

        budgetId = 1L;

        // PersonalBudget 요청 DTO
        personalBudgetCreateRequestDto = PersonalBudgetCreateRequestDto.builder()
                .dateTime(LocalDateTime.of(2025, 1, 5, 10, 0))
                .memo("개인 예산")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.KRW)
                .exchangeRate(BigDecimal.valueOf(1.0))
                .totalAmount(BigDecimal.valueOf(100000))
                .build();

        personalBudgetUpdateRequestDto = PersonalBudgetUpdateRequestDto.builder()
                .budgetType(BudgetType.PERSONAL)
                .datetime(LocalDateTime.of(2025, 1, 6, 10, 0))
                .memo("수정된 개인 예산")
                .paymentMethod(PaymentMethod.CASH)
                .currencyType(CurrencyType.JPY)
                .exchangeRate(BigDecimal.valueOf(900.0))
                .totalAmount(BigDecimal.valueOf(200000))
                .build();

        personalBudgetDto = PersonalBudgetDto.builder()
                .id(budgetId)
                .dateTime(LocalDateTime.of(2025, 1, 5, 10, 0))
                .memo("개인 예산")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.KRW)
                .exchangeRate(BigDecimal.valueOf(1.0))
                .totalAmount(BigDecimal.valueOf(100000))
                .build();

        // SharedBudget 요청 DTO
        BudgetParticipantRequestDto participant1 = BudgetParticipantRequestDto.builder()
                .id(userId)
                .amount(BigDecimal.valueOf(50000))
                .build();

        BudgetParticipantRequestDto participant2 = BudgetParticipantRequestDto.builder()
                .id(2L)
                .amount(BigDecimal.valueOf(50000))
                .build();

        sharedBudgetCreateRequestDto = SharedBudgetCreateRequestDto.builder()
                .dateTime(LocalDateTime.of(2025, 1, 5, 10, 0))
                .memo("공유 예산")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.KRW)
                .exchangeRate(BigDecimal.valueOf(1.0))
                .calculateType(CalculateType.EQUAL)
                .budgetParticipants(List.of(participant1, participant2))
                .build();

        sharedBudgetUpdateRequestDto = SharedBudgetUpdateRequestDto.builder()
                .budgetType(BudgetType.SHARED)
                .datetime(LocalDateTime.of(2025, 1, 6, 10, 0))
                .memo("수정된 공유 예산")
                .paymentMethod(PaymentMethod.CASH)
                .currencyType(CurrencyType.JPY)
                .exchangeRate(BigDecimal.valueOf(900.0))
                .calculateType(CalculateType.EACH)
                .budgetParticipants(List.of(participant1, participant2))
                .build();

        BudgetParticipantDto participantDto1 = BudgetParticipantDto.builder()
                .userId(userId)
                .amount(BigDecimal.valueOf(50000))
                .build();

        BudgetParticipantDto participantDto2 = BudgetParticipantDto.builder()
                .userId(2L)
                .amount(BigDecimal.valueOf(50000))
                .build();

        sharedBudgetDto = SharedBudgetDto.builder()
                .id(budgetId)
                .dateTime(LocalDateTime.of(2025, 1, 5, 10, 0))
                .memo("공유 예산")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.KRW)
                .exchangeRate(BigDecimal.valueOf(1.0))
                .calculateType(CalculateType.EQUAL)
                .budgetParticipants(List.of(participantDto1, participantDto2))
                .build();
    }

    @Test
    @DisplayName("개인 예산 생성 성공")
    void createPersonalBudget_Success() throws Exception {
        // given
        given(budgetService.createBudget(eq(tripId), any(BudgetCreateRequestDto.class)))
                .willReturn(personalBudgetDto);

        // when
        mockMvc.perform(post("/api/trips/{tripId}/budgets", tripId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(personalBudgetCreateRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(budgetId))
                .andExpect(jsonPath("$.data.memo").value("개인 예산"))
                .andExpect(jsonPath("$.data.paymentMethod").value("CARD"))
                .andExpect(jsonPath("$.data.totalAmount").value(100000))
                .andDo(document("budget-create",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID")
                        ),
                        requestFields(
                                fieldWithPath("budgetType").description("예산 타입 (PERSONAL, SHARED)"),
                                fieldWithPath("dateTime").description("예산 일시"),
                                fieldWithPath("memo").description("메모"),
                                fieldWithPath("paymentMethod").description("결제 수단 (CARD, CASH, ETC)"),
                                fieldWithPath("currencyType").description("통화 타입"),
                                fieldWithPath("exchangeRate").description("환율"),
                                fieldWithPath("totalAmount").description("총 금액 (개인 예산)")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("예산 ID"),
                                fieldWithPath("data.dateTime").description("예산 일시"),
                                fieldWithPath("data.memo").description("메모"),
                                fieldWithPath("data.paymentMethod").description("결제 수단"),
                                fieldWithPath("data.currencyType").description("통화 타입"),
                                fieldWithPath("data.exchangeRate").description("환율"),
                                fieldWithPath("data.totalAmount").description("총 금액")
                        )
                ));

        // then
        assertThat(personalBudgetDto).isNotNull();
        assertThat(personalBudgetDto.getMemo()).isEqualTo("개인 예산");
        then(budgetService).should().createBudget(eq(tripId), any(BudgetCreateRequestDto.class));
    }

    @Test
    @DisplayName("공유 예산 생성 성공")
    void createSharedBudget_Success() throws Exception {
        // given
        given(budgetService.createBudget(eq(tripId), any(BudgetCreateRequestDto.class)))
                .willReturn(sharedBudgetDto);

        // when
        mockMvc.perform(post("/api/trips/{tripId}/budgets", tripId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sharedBudgetCreateRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(budgetId))
                .andExpect(jsonPath("$.data.memo").value("공유 예산"))
                .andExpect(jsonPath("$.data.calculateType").value("EQUAL"))
                .andExpect(jsonPath("$.data.budgetParticipants").isArray())
                .andDo(document("budget-create-shared",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID")
                        ),
                        requestFields(
                                fieldWithPath("budgetType").description("예산 타입 (PERSONAL, SHARED)"),
                                fieldWithPath("dateTime").description("예산 일시"),
                                fieldWithPath("memo").description("메모"),
                                fieldWithPath("paymentMethod").description("결제 수단 (CARD, CASH, ETC)"),
                                fieldWithPath("currencyType").description("통화 타입"),
                                fieldWithPath("exchangeRate").description("환율"),
                                fieldWithPath("calculateType").description("정산 타입 (EQUAL, CUSTOM)"),
                                fieldWithPath("budgetParticipants").description("예산 참여자 목록"),
                                fieldWithPath("budgetParticipants[].id").description("참여자 ID"),
                                fieldWithPath("budgetParticipants[].amount").description("참여자 금액")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("예산 ID"),
                                fieldWithPath("data.dateTime").description("예산 일시"),
                                fieldWithPath("data.memo").description("메모"),
                                fieldWithPath("data.paymentMethod").description("결제 수단"),
                                fieldWithPath("data.currencyType").description("통화 타입"),
                                fieldWithPath("data.exchangeRate").description("환율"),
                                fieldWithPath("data.calculateType").description("정산 타입"),
                                fieldWithPath("data.budgetParticipants").description("예산 참여자 목록"),
                                fieldWithPath("data.budgetParticipants[].userId").description("참여자 ID"),
                                fieldWithPath("data.budgetParticipants[].amount").description("참여자 금액")
                        )
                ));

        // then
        assertThat(sharedBudgetDto).isNotNull();
        assertThat(sharedBudgetDto.getMemo()).isEqualTo("공유 예산");
        then(budgetService).should().createBudget(eq(tripId), any(BudgetCreateRequestDto.class));
    }

    @Test
    @DisplayName("개인 예산 조회 성공")
    void getPersonalBudget_Success() throws Exception {
        // given
        given(budgetService.getBudget(eq(tripId), eq(budgetId))).willReturn(personalBudgetDto);

        // when
        mockMvc.perform(get("/api/trips/{tripId}/budgets/{budgetId}", tripId, budgetId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(budgetId))
                .andExpect(jsonPath("$.data.memo").value("개인 예산"))
                .andExpect(jsonPath("$.data.totalAmount").value(100000))
                .andDo(document("budget-get-personal",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("budgetId").description("예산 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("예산 ID"),
                                fieldWithPath("data.dateTime").description("예산 일시"),
                                fieldWithPath("data.memo").description("메모"),
                                fieldWithPath("data.paymentMethod").description("결제 수단"),
                                fieldWithPath("data.currencyType").description("통화 타입"),
                                fieldWithPath("data.exchangeRate").description("환율"),
                                fieldWithPath("data.totalAmount").description("총 금액")
                        )
                ));

        // then
        assertThat(personalBudgetDto).isNotNull();
        assertThat(personalBudgetDto.getId()).isEqualTo(budgetId);
        assertThat(personalBudgetDto.getTotalAmount()).isEqualTo(BigDecimal.valueOf(100000));
        then(budgetService).should().getBudget(eq(tripId), eq(budgetId));
    }

    @Test
    @DisplayName("공유 예산 조회 성공")
    void getSharedBudget_Success() throws Exception {
        // given
        given(budgetService.getBudget(eq(tripId), eq(budgetId))).willReturn(sharedBudgetDto);

        // when
        mockMvc.perform(get("/api/trips/{tripId}/budgets/{budgetId}", tripId, budgetId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(budgetId))
                .andExpect(jsonPath("$.data.memo").value("공유 예산"))
                .andExpect(jsonPath("$.data.calculateType").value("EQUAL"))
                .andExpect(jsonPath("$.data.budgetParticipants").isArray())
                .andExpect(jsonPath("$.data.budgetParticipants.length()").value(2))
                .andDo(document("budget-get-shared",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("budgetId").description("예산 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("예산 ID"),
                                fieldWithPath("data.dateTime").description("예산 일시"),
                                fieldWithPath("data.memo").description("메모"),
                                fieldWithPath("data.paymentMethod").description("결제 수단"),
                                fieldWithPath("data.currencyType").description("통화 타입"),
                                fieldWithPath("data.exchangeRate").description("환율"),
                                fieldWithPath("data.calculateType").description("정산 타입"),
                                fieldWithPath("data.budgetParticipants").description("예산 참여자 목록"),
                                fieldWithPath("data.budgetParticipants[].userId").description("참여자 ID"),
                                fieldWithPath("data.budgetParticipants[].amount").description("참여자 금액")
                        )
                ));

        // then
        assertThat(sharedBudgetDto).isNotNull();
        assertThat(sharedBudgetDto.getId()).isEqualTo(budgetId);
        assertThat(sharedBudgetDto.getCalculateType()).isEqualTo(CalculateType.EQUAL);
        assertThat(sharedBudgetDto.getBudgetParticipants()).hasSize(2);
        then(budgetService).should().getBudget(eq(tripId), eq(budgetId));
    }

    @Test
    @DisplayName("개인 예산 목록 조회 성공")
    void getPersonalBudgets_Success() throws Exception {
        // given
        PersonalBudgetDto budgetDto2 = PersonalBudgetDto.builder()
                .id(2L)
                .dateTime(LocalDateTime.of(2025, 1, 6, 10, 0))
                .memo("개인 예산 2")
                .paymentMethod(PaymentMethod.CASH)
                .currencyType(CurrencyType.JPY)
                .exchangeRate(BigDecimal.valueOf(900.0))
                .totalAmount(BigDecimal.valueOf(200000))
                .build();

        CursorPageResponseDto<BudgetDto> pageResponse = CursorPageResponseDto.<BudgetDto>builder()
                .content(List.of(personalBudgetDto, budgetDto2))
                .nextCursor("2025-01-06T10:00:00")
                .nextAfter(2L)
                .size(2)
                .hasNext(false)
                .build();

        given(budgetService.getBudgets(
                eq(tripId),
                eq("개인"),
                eq("createdAt"),
                eq("ASC"),
                eq(null),
                eq(null),
                eq(10)
        )).willReturn(pageResponse);

        // when
        mockMvc.perform(get("/api/trips/{tripId}/budgets", tripId)
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
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andDo(document("budget-list-personal",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID")
                        ),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드 (선택)").optional(),
                                parameterWithName("orderBy").description("정렬 기준 (기본값: createdAt)").optional(),
                                parameterWithName("direction").description("정렬 방향 (ASC, DESC, 기본값: ASC)").optional(),
                                parameterWithName("cursor").description("다음 페이지 커서 (선택)").optional(),
                                parameterWithName("after").description("다음 페이지 after 값 (선택)").optional(),
                                parameterWithName("limit").description("페이지 크기 (기본값: 10)").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.content").description("예산 목록"),
                                fieldWithPath("data.content[].id").description("예산 ID"),
                                fieldWithPath("data.content[].dateTime").description("예산 일시"),
                                fieldWithPath("data.content[].memo").description("메모"),
                                fieldWithPath("data.content[].paymentMethod").description("결제 수단"),
                                fieldWithPath("data.content[].currencyType").description("통화 타입"),
                                fieldWithPath("data.content[].exchangeRate").description("환율"),
                                fieldWithPath("data.content[].totalAmount").description("총 금액"),
                                fieldWithPath("data.nextCursor").description("다음 페이지 커서"),
                                fieldWithPath("data.nextAfter").description("다음 페이지 after 값"),
                                fieldWithPath("data.size").description("현재 페이지 크기"),
                                fieldWithPath("data.hasNext").description("다음 페이지 존재 여부")
                        )
                ));

        // then
        assertThat(pageResponse).isNotNull();
        assertThat(pageResponse.getContent()).hasSize(2);
        then(budgetService).should().getBudgets(eq(tripId), eq("개인"), eq("createdAt"), eq("ASC"), eq(null), eq(null), eq(10));
    }

    @Test
    @DisplayName("공유 예산 목록 조회 성공")
    void getSharedBudgets_Success() throws Exception {
        // given
        BudgetParticipantDto participantDto1 = BudgetParticipantDto.builder()
                .userId(userId)
                .amount(BigDecimal.valueOf(30000))
                .build();

        BudgetParticipantDto participantDto2 = BudgetParticipantDto.builder()
                .userId(2L)
                .amount(BigDecimal.valueOf(30000))
                .build();

        SharedBudgetDto budgetDto2 = SharedBudgetDto.builder()
                .id(2L)
                .dateTime(LocalDateTime.of(2025, 1, 6, 10, 0))
                .memo("공유 예산 2")
                .paymentMethod(PaymentMethod.CASH)
                .currencyType(CurrencyType.JPY)
                .exchangeRate(BigDecimal.valueOf(900.0))
                .calculateType(CalculateType.EACH)
                .budgetParticipants(List.of(participantDto1, participantDto2))
                .build();

        CursorPageResponseDto<BudgetDto> pageResponse = CursorPageResponseDto.<BudgetDto>builder()
                .content(List.of(sharedBudgetDto, budgetDto2))
                .nextCursor("2025-01-06T10:00:00")
                .nextAfter(2L)
                .size(2)
                .hasNext(false)
                .build();

        given(budgetService.getBudgets(
                eq(tripId),
                eq("공유"),
                eq("dateTime"),
                eq("DESC"),
                eq(null),
                eq(null),
                eq(10)
        )).willReturn(pageResponse);

        // when
        mockMvc.perform(get("/api/trips/{tripId}/budgets", tripId)
                        .header("Authorization", "Bearer " + accessToken)
                        .param("keyword", "공유")
                        .param("orderBy", "dateTime")
                        .param("direction", "DESC")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andDo(document("budget-list-shared",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID")
                        ),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드 (선택)").optional(),
                                parameterWithName("orderBy").description("정렬 기준 (기본값: createdAt)").optional(),
                                parameterWithName("direction").description("정렬 방향 (ASC, DESC, 기본값: ASC)").optional(),
                                parameterWithName("cursor").description("다음 페이지 커서 (선택)").optional(),
                                parameterWithName("after").description("다음 페이지 after 값 (선택)").optional(),
                                parameterWithName("limit").description("페이지 크기 (기본값: 10)").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.content").description("예산 목록"),
                                fieldWithPath("data.content[].id").description("예산 ID"),
                                fieldWithPath("data.content[].dateTime").description("예산 일시"),
                                fieldWithPath("data.content[].memo").description("메모"),
                                fieldWithPath("data.content[].paymentMethod").description("결제 수단"),
                                fieldWithPath("data.content[].currencyType").description("통화 타입"),
                                fieldWithPath("data.content[].exchangeRate").description("환율"),
                                fieldWithPath("data.content[].calculateType").description("정산 타입"),
                                fieldWithPath("data.content[].budgetParticipants").description("예산 참여자 목록"),
                                fieldWithPath("data.content[].budgetParticipants[].userId").description("참여자 ID"),
                                fieldWithPath("data.content[].budgetParticipants[].amount").description("참여자 금액"),
                                fieldWithPath("data.nextCursor").description("다음 페이지 커서"),
                                fieldWithPath("data.nextAfter").description("다음 페이지 after 값"),
                                fieldWithPath("data.size").description("현재 페이지 크기"),
                                fieldWithPath("data.hasNext").description("다음 페이지 존재 여부")
                        )
                ));

        // then
        assertThat(pageResponse).isNotNull();
        assertThat(pageResponse.getContent()).hasSize(2);
        then(budgetService).should().getBudgets(eq(tripId), eq("공유"), eq("dateTime"), eq("DESC"), eq(null), eq(null), eq(10));
    }

    @Test
    @DisplayName("개인 예산 수정 성공")
    void updatePersonalBudget_Success() throws Exception {
        // given
        PersonalBudgetDto updatedBudgetDto = PersonalBudgetDto.builder()
                .id(budgetId)
                .dateTime(LocalDateTime.of(2025, 1, 6, 10, 0))
                .memo("수정된 개인 예산")
                .paymentMethod(PaymentMethod.CASH)
                .currencyType(CurrencyType.JPY)
                .exchangeRate(BigDecimal.valueOf(900.0))
                .totalAmount(BigDecimal.valueOf(200000))
                .build();

        given(budgetService.updateBudget(eq(tripId), eq(budgetId), any(BudgetUpdateRequestDto.class)))
                .willReturn(updatedBudgetDto);

        // when
        mockMvc.perform(patch("/api/trips/{tripId}/budgets/{budgetId}", tripId, budgetId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(personalBudgetUpdateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(budgetId))
                .andExpect(jsonPath("$.data.memo").value("수정된 개인 예산"))
                .andExpect(jsonPath("$.data.totalAmount").value(200000))
                .andDo(document("budget-update-personal",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("budgetId").description("예산 ID")
                        ),
                        requestFields(
                                fieldWithPath("budgetType").description("예산 타입 (PERSONAL, SHARED)"),
                                fieldWithPath("dateTime").description("예산 일시"),
                                fieldWithPath("memo").description("메모"),
                                fieldWithPath("paymentMethod").description("결제 수단 (CARD, CASH, ETC)"),
                                fieldWithPath("currencyType").description("통화 타입"),
                                fieldWithPath("exchangeRate").description("환율"),
                                fieldWithPath("totalAmount").description("총 금액")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("예산 ID"),
                                fieldWithPath("data.dateTime").description("예산 일시"),
                                fieldWithPath("data.memo").description("메모"),
                                fieldWithPath("data.paymentMethod").description("결제 수단"),
                                fieldWithPath("data.currencyType").description("통화 타입"),
                                fieldWithPath("data.exchangeRate").description("환율"),
                                fieldWithPath("data.totalAmount").description("총 금액")
                        )
                ));

        // then
        assertThat(updatedBudgetDto).isNotNull();
        assertThat(updatedBudgetDto.getMemo()).isEqualTo("수정된 개인 예산");
        assertThat(updatedBudgetDto.getTotalAmount()).isEqualTo(BigDecimal.valueOf(200000));
        then(budgetService).should().updateBudget(eq(tripId), eq(budgetId), any(BudgetUpdateRequestDto.class));
    }

    @Test
    @DisplayName("공유 예산 수정 성공")
    void updateSharedBudget_Success() throws Exception {
        // given
        BudgetParticipantDto participantDto1 = BudgetParticipantDto.builder()
                .userId(userId)
                .amount(BigDecimal.valueOf(60000))
                .build();

        BudgetParticipantDto participantDto2 = BudgetParticipantDto.builder()
                .userId(2L)
                .amount(BigDecimal.valueOf(40000))
                .build();

        SharedBudgetDto updatedBudgetDto = SharedBudgetDto.builder()
                .id(budgetId)
                .dateTime(LocalDateTime.of(2025, 1, 6, 10, 0))
                .memo("수정된 공유 예산")
                .paymentMethod(PaymentMethod.CASH)
                .currencyType(CurrencyType.JPY)
                .exchangeRate(BigDecimal.valueOf(900.0))
                .calculateType(CalculateType.EACH)
                .budgetParticipants(List.of(participantDto1, participantDto2))
                .build();

        given(budgetService.updateBudget(eq(tripId), eq(budgetId), any(BudgetUpdateRequestDto.class)))
                .willReturn(updatedBudgetDto);

        // when
        mockMvc.perform(patch("/api/trips/{tripId}/budgets/{budgetId}", tripId, budgetId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sharedBudgetUpdateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(budgetId))
                .andExpect(jsonPath("$.data.memo").value("수정된 공유 예산"))
                .andExpect(jsonPath("$.data.calculateType").value("EACH"))
                .andExpect(jsonPath("$.data.budgetParticipants").isArray())
                .andDo(document("budget-update-shared",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("budgetId").description("예산 ID")
                        ),
                        requestFields(
                                fieldWithPath("budgetType").description("예산 타입 (PERSONAL, SHARED)"),
                                fieldWithPath("dateTime").description("예산 일시"),
                                fieldWithPath("memo").description("메모"),
                                fieldWithPath("paymentMethod").description("결제 수단 (CARD, CASH, ETC)"),
                                fieldWithPath("currencyType").description("통화 타입"),
                                fieldWithPath("exchangeRate").description("환율"),
                                fieldWithPath("calculateType").description("정산 타입 (EQUAL, EACH)"),
                                fieldWithPath("budgetParticipants").description("예산 참여자 목록"),
                                fieldWithPath("budgetParticipants[].id").description("참여자 ID"),
                                fieldWithPath("budgetParticipants[].amount").description("참여자 금액")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("예산 ID"),
                                fieldWithPath("data.dateTime").description("예산 일시"),
                                fieldWithPath("data.memo").description("메모"),
                                fieldWithPath("data.paymentMethod").description("결제 수단"),
                                fieldWithPath("data.currencyType").description("통화 타입"),
                                fieldWithPath("data.exchangeRate").description("환율"),
                                fieldWithPath("data.calculateType").description("정산 타입"),
                                fieldWithPath("data.budgetParticipants").description("예산 참여자 목록"),
                                fieldWithPath("data.budgetParticipants[].userId").description("참여자 ID"),
                                fieldWithPath("data.budgetParticipants[].amount").description("참여자 금액")
                        )
                ));

        // then
        assertThat(updatedBudgetDto).isNotNull();
        assertThat(updatedBudgetDto.getMemo()).isEqualTo("수정된 공유 예산");
        assertThat(updatedBudgetDto.getCalculateType()).isEqualTo(CalculateType.EACH);
        assertThat(updatedBudgetDto.getBudgetParticipants()).hasSize(2);
        then(budgetService).should().updateBudget(eq(tripId), eq(budgetId), any(BudgetUpdateRequestDto.class));
    }

    @Test
    @DisplayName("개인 예산 삭제 성공")
    void deletePersonalBudget_Success() throws Exception {
        // given
        willDoNothing().given(budgetService).deleteBudget(eq(tripId), eq(budgetId));

        // when
        mockMvc.perform(delete("/api/trips/{tripId}/budgets/{budgetId}", tripId, budgetId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(document("budget-delete-personal",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("budgetId").description("예산 ID")
                        )
                ));

        // then
        then(budgetService).should().deleteBudget(eq(tripId), eq(budgetId));
    }

    @Test
    @DisplayName("공유 예산 삭제 성공")
    void deleteSharedBudget_Success() throws Exception {
        // given
        Long sharedBudgetId = 2L;
        willDoNothing().given(budgetService).deleteBudget(eq(tripId), eq(sharedBudgetId));

        // when
        mockMvc.perform(delete("/api/trips/{tripId}/budgets/{budgetId}", tripId, sharedBudgetId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(document("budget-delete-shared",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("budgetId").description("예산 ID")
                        )
                ));

        // then
        then(budgetService).should().deleteBudget(eq(tripId), eq(sharedBudgetId));
    }
}

