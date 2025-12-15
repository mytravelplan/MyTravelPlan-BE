package travel.mytravelplan.domain.expense.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.expense.dto.*;
import travel.mytravelplan.domain.expense.enums.CalculateType;
import travel.mytravelplan.domain.expense.enums.ExpenseCategory;
import travel.mytravelplan.domain.expense.enums.ExpenseType;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;
import travel.mytravelplan.domain.expense.service.ExpenseService;
import travel.mytravelplan.domain.schedule.entity.Schedule;
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

@WebMvcTest(ExpenseController.class)
@DisplayName("지출 컨트롤러 테스트")
class ExpenseControllerTest extends ControllerTestSupport {

    @MockitoBean
    private ExpenseService expenseService;

    private String accessToken;
    private Long userId;
    private Long tripId;
    private Long scheduleId;
    private Long expenseId;
    private User testUser;
    private Trip testTrip;
    private TripJoin testTripJoin;
    private Schedule testSchedule;
    private PersonalExpenseCreateRequestDto personalExpenseCreateRequestDto;
    private SharedExpenseCreateRequestDto sharedExpenseCreateRequestDto;
    private PersonalExpenseUpdateRequestDto personalExpenseUpdateRequestDto;
    private SharedExpenseUpdateRequestDto sharedExpenseUpdateRequestDto;
    private PersonalExpenseDto personalExpenseDto;
    private SharedExpenseDto sharedExpenseDto;

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

        scheduleId = 1L;
        testSchedule = Schedule.createSchedule(
                "도쿄 관광",
                LocalDateTime.of(2025, 1, 5, 9, 0),
                LocalDateTime.of(2025, 1, 5, 18, 0),
                "도쿄 시내 관광",
                1L,
                null,
                testTrip,
                null
        );
        ReflectionTestUtils.setField(testSchedule, "id", scheduleId);

        expenseId = 1L;

        // PersonalExpense 요청 DTO
        personalExpenseCreateRequestDto = PersonalExpenseCreateRequestDto.builder()
                .expenseType(ExpenseType.PERSONAL)
                .dateTime(LocalDateTime.of(2025, 1, 5, 10, 0))
                .memo("개인 지출")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.KRW)
                .exchangeRate(BigDecimal.valueOf(1.0))
                .category(ExpenseCategory.FOOD)
                .imageUrls(List.of("http://example.com/image1.jpg"))
                .totalAmount(BigDecimal.valueOf(50000))
                .build();

        personalExpenseUpdateRequestDto = PersonalExpenseUpdateRequestDto.builder()
                .datetime(LocalDateTime.of(2025, 1, 6, 10, 0))
                .memo("수정된 개인 지출")
                .paymentMethod(PaymentMethod.CASH)
                .currencyType(CurrencyType.JPY)
                .exchangeRate(BigDecimal.valueOf(900.0))
                .category(ExpenseCategory.SHOPPING)
                .imageUrls(List.of("http://example.com/image2.jpg"))
                .totalAmount(BigDecimal.valueOf(100000))
                .build();

        personalExpenseDto = PersonalExpenseDto.builder()
                .id(expenseId)
                .datetime(LocalDateTime.of(2025, 1, 5, 10, 0))
                .memo("개인 지출")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.KRW)
                .exchangeRate(BigDecimal.valueOf(1.0))
                .category(ExpenseCategory.FOOD)
                .imageUrls(List.of("http://example.com/image1.jpg"))
                .totalAmount(BigDecimal.valueOf(50000))
                .build();

        // SharedExpense 요청 DTO
        ExpenseParticipantRequestDto participant1 = ExpenseParticipantRequestDto.builder()
                .id(userId)
                .amount(BigDecimal.valueOf(25000))
                .build();

        ExpenseParticipantRequestDto participant2 = ExpenseParticipantRequestDto.builder()
                .id(2L)
                .amount(BigDecimal.valueOf(25000))
                .build();

        sharedExpenseCreateRequestDto = SharedExpenseCreateRequestDto.builder()
                .expenseType(ExpenseType.SHARED)
                .dateTime(LocalDateTime.of(2025, 1, 5, 12, 0))
                .memo("공유 지출")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.KRW)
                .exchangeRate(BigDecimal.valueOf(1.0))
                .category(ExpenseCategory.ACCOMMODATION)
                .imageUrls(List.of("http://example.com/image3.jpg"))
                .calculateType(CalculateType.EQUAL)
                .payerId(userId)
                .expenseParticipants(List.of(participant1, participant2))
                .build();

        sharedExpenseUpdateRequestDto = SharedExpenseUpdateRequestDto.builder()
                .datetime(LocalDateTime.of(2025, 1, 6, 12, 0))
                .memo("수정된 공유 지출")
                .paymentMethod(PaymentMethod.CASH)
                .currencyType(CurrencyType.JPY)
                .exchangeRate(BigDecimal.valueOf(900.0))
                .category(ExpenseCategory.TRANSPORTATION)
                .imageUrls(List.of("http://example.com/image4.jpg"))
                .calculateType(CalculateType.EACH)
                .payerId(userId)
                .expenseParticipants(List.of(participant1, participant2))
                .build();

        ExpenseParticipantDto participantDto1 = ExpenseParticipantDto.builder()
                .userId(userId)
                .amount(BigDecimal.valueOf(25000))
                .build();

        ExpenseParticipantDto participantDto2 = ExpenseParticipantDto.builder()
                .userId(2L)
                .amount(BigDecimal.valueOf(25000))
                .build();

        sharedExpenseDto = SharedExpenseDto.builder()
                .id(expenseId)
                .datetime(LocalDateTime.of(2025, 1, 5, 12, 0))
                .memo("공유 지출")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.KRW)
                .exchangeRate(BigDecimal.valueOf(1.0))
                .category(ExpenseCategory.ACCOMMODATION)
                .imageUrls(List.of("http://example.com/image3.jpg"))
                .calculateType(CalculateType.EQUAL)
                .payerId(userId)
                .expenseParticipants(List.of(participantDto1, participantDto2))
                .build();
    }

    @Test
    @DisplayName("개인 지출 생성 성공")
    void createPersonalExpense_Success() throws Exception {
        // given
        given(expenseService.createExpense(eq(tripId), eq(scheduleId), any(ExpenseCreateRequestDto.class)))
                .willReturn(personalExpenseDto);

        // when & then
        mockMvc.perform(post("/api/trips/{tripId}/schedules/{scheduleId}/expenses", tripId, scheduleId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(personalExpenseCreateRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(expenseId))
                .andExpect(jsonPath("$.data.memo").value("개인 지출"))
                .andExpect(jsonPath("$.data.paymentMethod").value("CARD"))
                .andExpect(jsonPath("$.data.totalAmount").value(50000))
                .andDo(document("expense-create",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("scheduleId").description("일정 ID")
                        ),
                        requestFields(
                                fieldWithPath("expenseType").description("지출 타입 (PERSONAL, SHARED)"),
                                fieldWithPath("dateTime").description("지출 일시"),
                                fieldWithPath("memo").description("메모"),
                                fieldWithPath("paymentMethod").description("결제 수단 (CARD, CASH, ETC)"),
                                fieldWithPath("currencyType").description("통화 타입"),
                                fieldWithPath("exchangeRate").description("환율"),
                                fieldWithPath("category").description("지출 카테고리"),
                                fieldWithPath("imageUrls").description("이미지 URL 목록"),
                                fieldWithPath("totalAmount").description("총 금액 (개인 지출)")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("지출 ID"),
                                fieldWithPath("data.dateTime").description("지출 일시"),
                                fieldWithPath("data.memo").description("메모"),
                                fieldWithPath("data.paymentMethod").description("결제 수단"),
                                fieldWithPath("data.currencyType").description("통화 타입"),
                                fieldWithPath("data.exchangeRate").description("환율"),
                                fieldWithPath("data.category").description("지출 카테고리"),
                                fieldWithPath("data.imageUrls").description("이미지 URL 목록"),
                                fieldWithPath("data.totalAmount").description("총 금액")
                        )
                ));

        // then
        assertThat(personalExpenseDto).isNotNull();
        assertThat(personalExpenseDto.getMemo()).isEqualTo("개인 지출");
        then(expenseService).should().createExpense(eq(tripId), eq(scheduleId), any(ExpenseCreateRequestDto.class));
    }

    @Test
    @DisplayName("공유 지출 생성 성공")
    void createSharedExpense_Success() throws Exception {
        // given
        given(expenseService.createExpense(eq(tripId), eq(scheduleId), any(ExpenseCreateRequestDto.class)))
                .willReturn(sharedExpenseDto);

        // when & then
        mockMvc.perform(post("/api/trips/{tripId}/schedules/{scheduleId}/expenses", tripId, scheduleId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sharedExpenseCreateRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(expenseId))
                .andExpect(jsonPath("$.data.memo").value("공유 지출"))
                .andExpect(jsonPath("$.data.calculateType").value("EQUAL"))
                .andExpect(jsonPath("$.data.expenseParticipants").isArray())
                .andDo(document("expense-create-shared",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("scheduleId").description("일정 ID")
                        ),
                        requestFields(
                                fieldWithPath("expenseType").description("지출 타입 (PERSONAL, SHARED)"),
                                fieldWithPath("dateTime").description("지출 일시"),
                                fieldWithPath("memo").description("메모"),
                                fieldWithPath("paymentMethod").description("결제 수단 (CARD, CASH, ETC)"),
                                fieldWithPath("currencyType").description("통화 타입"),
                                fieldWithPath("exchangeRate").description("환율"),
                                fieldWithPath("category").description("지출 카테고리"),
                                fieldWithPath("imageUrls").description("이미지 URL 목록"),
                                fieldWithPath("calculateType").description("정산 타입 (EQUAL, EACH)"),
                                fieldWithPath("payerId").description("지불자 ID"),
                                fieldWithPath("expenseParticipants").description("지출 참여자 목록"),
                                fieldWithPath("expenseParticipants[].id").description("참여자 ID"),
                                fieldWithPath("expenseParticipants[].amount").description("참여자 금액")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("지출 ID"),
                                fieldWithPath("data.dateTime").description("지출 일시"),
                                fieldWithPath("data.memo").description("메모"),
                                fieldWithPath("data.paymentMethod").description("결제 수단"),
                                fieldWithPath("data.currencyType").description("통화 타입"),
                                fieldWithPath("data.exchangeRate").description("환율"),
                                fieldWithPath("data.category").description("지출 카테고리"),
                                fieldWithPath("data.imageUrls").description("이미지 URL 목록"),
                                fieldWithPath("data.calculateType").description("정산 타입"),
                                fieldWithPath("data.payerId").description("지불자 ID"),
                                fieldWithPath("data.expenseParticipants").description("지출 참여자 목록"),
                                fieldWithPath("data.expenseParticipants[].userId").description("참여자 ID"),
                                fieldWithPath("data.expenseParticipants[].amount").description("참여자 금액")
                        )
                ));

        // then
        assertThat(sharedExpenseDto).isNotNull();
        assertThat(sharedExpenseDto.getMemo()).isEqualTo("공유 지출");
        then(expenseService).should().createExpense(eq(tripId), eq(scheduleId), any(ExpenseCreateRequestDto.class));
    }

    @Test
    @DisplayName("개인 지출 조회 성공")
    void getPersonalExpense_Success() throws Exception {
        // given
        given(expenseService.getExpense(eq(tripId), eq(scheduleId), eq(expenseId))).willReturn(personalExpenseDto);

        // when & then
        mockMvc.perform(get("/api/trips/{tripId}/schedules/{scheduleId}/expenses/{expenseId}", tripId, scheduleId, expenseId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(expenseId))
                .andExpect(jsonPath("$.data.memo").value("개인 지출"))
                .andExpect(jsonPath("$.data.totalAmount").value(50000))
                .andDo(document("expense-get",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("scheduleId").description("일정 ID"),
                                parameterWithName("expenseId").description("지출 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("지출 ID"),
                                fieldWithPath("data.dateTime").description("지출 일시"),
                                fieldWithPath("data.memo").description("메모"),
                                fieldWithPath("data.paymentMethod").description("결제 수단"),
                                fieldWithPath("data.currencyType").description("통화 타입"),
                                fieldWithPath("data.exchangeRate").description("환율"),
                                fieldWithPath("data.category").description("지출 카테고리"),
                                fieldWithPath("data.imageUrls").description("이미지 URL 목록"),
                                fieldWithPath("data.totalAmount").description("총 금액")
                        )
                ));

        // then
        assertThat(personalExpenseDto).isNotNull();
        assertThat(personalExpenseDto.getId()).isEqualTo(expenseId);
        assertThat(personalExpenseDto.getTotalAmount()).isEqualTo(BigDecimal.valueOf(50000));
        then(expenseService).should().getExpense(eq(tripId), eq(scheduleId), eq(expenseId));
    }

    @Test
    @DisplayName("공유 지출 조회 성공")
    void getSharedExpense_Success() throws Exception {
        // given
        given(expenseService.getExpense(eq(tripId), eq(scheduleId), eq(expenseId))).willReturn(sharedExpenseDto);

        // when & then
        mockMvc.perform(get("/api/trips/{tripId}/schedules/{scheduleId}/expenses/{expenseId}", tripId, scheduleId, expenseId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(expenseId))
                .andExpect(jsonPath("$.data.memo").value("공유 지출"))
                .andExpect(jsonPath("$.data.calculateType").value("EQUAL"))
                .andExpect(jsonPath("$.data.expenseParticipants").isArray())
                .andExpect(jsonPath("$.data.expenseParticipants.length()").value(2))
                .andDo(document("expense-get-shared",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("scheduleId").description("일정 ID"),
                                parameterWithName("expenseId").description("지출 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("지출 ID"),
                                fieldWithPath("data.dateTime").description("지출 일시"),
                                fieldWithPath("data.memo").description("메모"),
                                fieldWithPath("data.paymentMethod").description("결제 수단"),
                                fieldWithPath("data.currencyType").description("통화 타입"),
                                fieldWithPath("data.exchangeRate").description("환율"),
                                fieldWithPath("data.category").description("지출 카테고리"),
                                fieldWithPath("data.imageUrls").description("이미지 URL 목록"),
                                fieldWithPath("data.calculateType").description("정산 타입"),
                                fieldWithPath("data.payerId").description("지불자 ID"),
                                fieldWithPath("data.expenseParticipants").description("지출 참여자 목록"),
                                fieldWithPath("data.expenseParticipants[].userId").description("참여자 ID"),
                                fieldWithPath("data.expenseParticipants[].amount").description("참여자 금액")
                        )
                ));

        // then
        assertThat(sharedExpenseDto).isNotNull();
        assertThat(sharedExpenseDto.getId()).isEqualTo(expenseId);
        assertThat(sharedExpenseDto.getCalculateType()).isEqualTo(CalculateType.EQUAL);
        assertThat(sharedExpenseDto.getExpenseParticipants()).hasSize(2);
        then(expenseService).should().getExpense(eq(tripId), eq(scheduleId), eq(expenseId));
    }

    @Test
    @DisplayName("지출 목록 조회 성공")
    void getExpenses_Success() throws Exception {
        // given
        PersonalExpenseDto expenseDto2 = PersonalExpenseDto.builder()
                .id(2L)
                .datetime(LocalDateTime.of(2025, 1, 6, 10, 0))
                .memo("개인 지출 2")
                .paymentMethod(PaymentMethod.CASH)
                .currencyType(CurrencyType.JPY)
                .exchangeRate(BigDecimal.valueOf(900.0))
                .category(ExpenseCategory.SHOPPING)
                .imageUrls(List.of("http://example.com/image2.jpg"))
                .totalAmount(BigDecimal.valueOf(100000))
                .build();

        CursorPageResponseDto<ExpenseDto> pageResponse = CursorPageResponseDto.<ExpenseDto>builder()
                .content(List.of(personalExpenseDto, expenseDto2))
                .nextCursor("2025-01-06T10:00:00")
                .nextAfter(2L)
                .size(2)
                .hasNext(false)
                .build();

        given(expenseService.getExpenses(
                eq(tripId),
                eq(scheduleId),
                eq("개인"),
                eq("createdAt"),
                eq("ASC"),
                eq(null),
                eq(null),
                eq(10)
        )).willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/trips/{tripId}/schedules/{scheduleId}/expenses", tripId, scheduleId)
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
                .andDo(document("expense-list",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("scheduleId").description("일정 ID")
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
                                fieldWithPath("data.content").description("지출 목록"),
                                fieldWithPath("data.content[].id").description("지출 ID"),
                                fieldWithPath("data.content[].dateTime").description("지출 일시"),
                                fieldWithPath("data.content[].memo").description("메모"),
                                fieldWithPath("data.content[].paymentMethod").description("결제 수단"),
                                fieldWithPath("data.content[].currencyType").description("통화 타입"),
                                fieldWithPath("data.content[].exchangeRate").description("환율"),
                                fieldWithPath("data.content[].category").description("지출 카테고리"),
                                fieldWithPath("data.content[].imageUrls").description("이미지 URL 목록"),
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
        then(expenseService).should().getExpenses(eq(tripId), eq(scheduleId), eq("개인"), eq("createdAt"), eq("ASC"), eq(null), eq(null), eq(10));
    }

    @Test
    @DisplayName("개인 지출 수정 성공")
    void updatePersonalExpense_Success() throws Exception {
        // given
        PersonalExpenseDto updatedExpenseDto = PersonalExpenseDto.builder()
                .id(expenseId)
                .datetime(LocalDateTime.of(2025, 1, 6, 10, 0))
                .memo("수정된 개인 지출")
                .paymentMethod(PaymentMethod.CASH)
                .currencyType(CurrencyType.JPY)
                .exchangeRate(BigDecimal.valueOf(900.0))
                .category(ExpenseCategory.SHOPPING)
                .imageUrls(List.of("http://example.com/image2.jpg"))
                .totalAmount(BigDecimal.valueOf(100000))
                .build();

        given(expenseService.updateExpense(eq(tripId), eq(scheduleId), eq(expenseId), any(ExpenseUpdateRequestDto.class)))
                .willReturn(updatedExpenseDto);

        // when & then
        mockMvc.perform(patch("/api/trips/{tripId}/schedules/{scheduleId}/expenses/{expenseId}", tripId, scheduleId, expenseId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(personalExpenseUpdateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(expenseId))
                .andExpect(jsonPath("$.data.memo").value("수정된 개인 지출"))
                .andExpect(jsonPath("$.data.totalAmount").value(100000))
                .andDo(document("expense-update",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("scheduleId").description("일정 ID"),
                                parameterWithName("expenseId").description("지출 ID")
                        ),
                        requestFields(
                                fieldWithPath("expenseType").description("지출 타입 (PERSONAL, SHARED)"),
                                fieldWithPath("dateTime").description("지출 일시"),
                                fieldWithPath("memo").description("메모"),
                                fieldWithPath("paymentMethod").description("결제 수단 (CARD, CASH, ETC)"),
                                fieldWithPath("currencyType").description("통화 타입"),
                                fieldWithPath("exchangeRate").description("환율"),
                                fieldWithPath("category").description("지출 카테고리"),
                                fieldWithPath("imageUrls").description("이미지 URL 목록"),
                                fieldWithPath("totalAmount").description("총 금액 (개인 지출)")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("지출 ID"),
                                fieldWithPath("data.dateTime").description("지출 일시"),
                                fieldWithPath("data.memo").description("메모"),
                                fieldWithPath("data.paymentMethod").description("결제 수단"),
                                fieldWithPath("data.currencyType").description("통화 타입"),
                                fieldWithPath("data.exchangeRate").description("환율"),
                                fieldWithPath("data.category").description("지출 카테고리"),
                                fieldWithPath("data.imageUrls").description("이미지 URL 목록"),
                                fieldWithPath("data.totalAmount").description("총 금액")
                        )
                ));

        // then
        assertThat(updatedExpenseDto).isNotNull();
        assertThat(updatedExpenseDto.getMemo()).isEqualTo("수정된 개인 지출");
        then(expenseService).should().updateExpense(eq(tripId), eq(scheduleId), eq(expenseId), any(ExpenseUpdateRequestDto.class));
    }

    @Test
    @DisplayName("공유 지출 수정 성공")
    void updateSharedExpense_Success() throws Exception {
        // given
        ExpenseParticipantDto participantDto1 = ExpenseParticipantDto.builder()
                .userId(userId)
                .amount(BigDecimal.valueOf(25000))
                .build();

        ExpenseParticipantDto participantDto2 = ExpenseParticipantDto.builder()
                .userId(2L)
                .amount(BigDecimal.valueOf(25000))
                .build();

        SharedExpenseDto updatedExpenseDto = SharedExpenseDto.builder()
                .id(expenseId)
                .datetime(LocalDateTime.of(2025, 1, 6, 12, 0))
                .memo("수정된 공유 지출")
                .paymentMethod(PaymentMethod.CASH)
                .currencyType(CurrencyType.JPY)
                .exchangeRate(BigDecimal.valueOf(900.0))
                .category(ExpenseCategory.TRANSPORTATION)
                .imageUrls(List.of("http://example.com/image4.jpg"))
                .calculateType(CalculateType.EACH)
                .payerId(userId)
                .expenseParticipants(List.of(participantDto1, participantDto2))
                .build();

        given(expenseService.updateExpense(eq(tripId), eq(scheduleId), eq(expenseId), any(ExpenseUpdateRequestDto.class)))
                .willReturn(updatedExpenseDto);

        // when & then
        mockMvc.perform(patch("/api/trips/{tripId}/schedules/{scheduleId}/expenses/{expenseId}", tripId, scheduleId, expenseId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sharedExpenseUpdateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(expenseId))
                .andExpect(jsonPath("$.data.memo").value("수정된 공유 지출"))
                .andExpect(jsonPath("$.data.calculateType").value("EACH"))
                .andDo(document("expense-update-shared",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("scheduleId").description("일정 ID"),
                                parameterWithName("expenseId").description("지출 ID")
                        ),
                        requestFields(
                                fieldWithPath("expenseType").description("지출 타입 (PERSONAL, SHARED)"),
                                fieldWithPath("dateTime").description("지출 일시"),
                                fieldWithPath("memo").description("메모"),
                                fieldWithPath("paymentMethod").description("결제 수단 (CARD, CASH, ETC)"),
                                fieldWithPath("currencyType").description("통화 타입"),
                                fieldWithPath("exchangeRate").description("환율"),
                                fieldWithPath("category").description("지출 카테고리"),
                                fieldWithPath("imageUrls").description("이미지 URL 목록"),
                                fieldWithPath("calculateType").description("정산 타입 (EQUAL, EACH)"),
                                fieldWithPath("payerId").description("지불자 ID"),
                                fieldWithPath("expenseParticipants").description("지출 참여자 목록"),
                                fieldWithPath("expenseParticipants[].id").description("참여자 ID"),
                                fieldWithPath("expenseParticipants[].amount").description("참여자 금액")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("지출 ID"),
                                fieldWithPath("data.dateTime").description("지출 일시"),
                                fieldWithPath("data.memo").description("메모"),
                                fieldWithPath("data.paymentMethod").description("결제 수단"),
                                fieldWithPath("data.currencyType").description("통화 타입"),
                                fieldWithPath("data.exchangeRate").description("환율"),
                                fieldWithPath("data.category").description("지출 카테고리"),
                                fieldWithPath("data.imageUrls").description("이미지 URL 목록"),
                                fieldWithPath("data.calculateType").description("정산 타입"),
                                fieldWithPath("data.payerId").description("지불자 ID"),
                                fieldWithPath("data.expenseParticipants").description("지출 참여자 목록"),
                                fieldWithPath("data.expenseParticipants[].userId").description("참여자 ID"),
                                fieldWithPath("data.expenseParticipants[].amount").description("참여자 금액")
                        )
                ));

        // then
        assertThat(updatedExpenseDto).isNotNull();
        assertThat(updatedExpenseDto.getMemo()).isEqualTo("수정된 공유 지출");
        then(expenseService).should().updateExpense(eq(tripId), eq(scheduleId), eq(expenseId), any(ExpenseUpdateRequestDto.class));
    }

    @Test
    @DisplayName("지출 삭제 성공")
    void deleteExpense_Success() throws Exception {
        // given
        willDoNothing().given(expenseService).deleteExpense(eq(tripId), eq(scheduleId), eq(expenseId));

        // when & then
        mockMvc.perform(delete("/api/trips/{tripId}/schedules/{scheduleId}/expenses/{expenseId}", tripId, scheduleId, expenseId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(document("expense-delete",
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID"),
                                parameterWithName("scheduleId").description("일정 ID"),
                                parameterWithName("expenseId").description("지출 ID")
                        )
                ));

        // then
        then(expenseService).should().deleteExpense(eq(tripId), eq(scheduleId), eq(expenseId));
    }
}