package travel.mytravelplan.domain.trip.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.budget.dto.BudgetSettleDto;
import travel.mytravelplan.domain.expense.dto.ExpenseRatioDto;
import travel.mytravelplan.domain.expense.dto.ExpenseStatisticsDto;
import travel.mytravelplan.domain.expense.dto.SettleExpenseDto;
import travel.mytravelplan.domain.expense.dto.TransferDto;
import travel.mytravelplan.domain.expense.dto.UserExpenseDto;
import travel.mytravelplan.domain.expense.enums.ExpenseCategory;
import travel.mytravelplan.domain.expense.enums.ExpenseType;
import travel.mytravelplan.domain.expense.enums.GroupByType;
import travel.mytravelplan.domain.expense.service.ExpenseService;
import travel.mytravelplan.domain.trip.dto.*;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.entity.TripJoin;
import travel.mytravelplan.domain.trip.enums.Country;
import travel.mytravelplan.domain.trip.service.TripService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TripController.class)
@DisplayName("여행 컨트롤러 테스트")
public class TripControllerTest extends ControllerTestSupport {

    @MockitoBean
    private TripService tripService;

    @MockitoBean
    private ExpenseService expenseService;

    private String accessToken;
    private Long userId;
    private Long tripId;
    private TripCreateRequestDto createRequestDto;
    private TripUpdateRequestDto updateRequestDto;
    private TripDto tripDto;
    private TripInviteLinkDto inviteLinkDto;
    private TripJoinRequestDto joinRequestDto;

    @BeforeEach
    void setUp() {
        given(jwtBlacklistService.isTokenBlacklisted(any(String.class))).willReturn(false);

        UserProfile userProfile = UserProfile.createUserProfile(
                "테스트 유저",
                "http://example.com/user.jpg"
        );

        User testUser = User.createUser(
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

        Trip testTrip = Trip.createTrip(
                "테스트 여행",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 10),
                "http://example.com/trip.jpg",
                Set.of(Country.JP)
        );
        ReflectionTestUtils.setField(testTrip, "id", tripId);

        TripJoin testTripJoin = TripJoin.createTripJoin(testTrip, testUser);
        Long tripJoinId = 1L;
        ReflectionTestUtils.setField(testTripJoin, "id", tripJoinId);

        given(tripJoinRepository.existsByUserIdAndTripId(eq(userId), eq(tripId))).willReturn(true);
        given(tripJoinRepository.findByUserIdAndTripId(eq(userId), eq(tripId))).willReturn(Optional.of(testTripJoin));

        createRequestDto = TripCreateRequestDto.builder()
                .title("테스트 여행")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 1, 10))
                .imageUrl("http://example.com/trip.jpg")
                .countries(Set.of(Country.JP))
                .build();

        updateRequestDto = TripUpdateRequestDto.builder()
                .title("수정된 여행")
                .startDate(LocalDate.of(2025, 2, 1))
                .endDate(LocalDate.of(2025, 2, 10))
                .imageUrl("http://example.com/trip2.jpg")
                .countries(Set.of(Country.KR))
                .build();

        tripDto = TripDto.builder()
                .id(tripId)
                .title("테스트 여행")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 1, 10))
                .imageUrl("http://example.com/trip.jpg")
                .build();

        inviteLinkDto = TripInviteLinkDto.builder()
                .inviteLink("http://example.com/invite/abc123")
                .build();

        joinRequestDto = TripJoinRequestDto.builder()
                .inviteLink("http://example.com/invite/abc123")
                .build();
    }

    @Test
    @DisplayName("여행 생성 성공")
    void createTrip_Success() throws Exception {
        // given
        given(tripService.createTrip(any(User.class), any(TripCreateRequestDto.class)))
                .willReturn(tripDto);

        // when & then
        mockMvc.perform(post("/api/trips")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(tripId))
                .andExpect(jsonPath("$.data.title").value("테스트 여행"))
                .andExpect(jsonPath("$.data.startDate").value("2025-01-01"))
                .andExpect(jsonPath("$.data.endDate").value("2025-01-10"))
                .andDo(document("trip-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        requestFields(
                                fieldWithPath("title").description("여행 제목"),
                                fieldWithPath("startDate").description("여행 시작일"),
                                fieldWithPath("endDate").description("여행 종료일"),
                                fieldWithPath("imageUrl").description("여행 이미지 URL"),
                                fieldWithPath("countries").description("여행 국가 목록")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("여행 ID"),
                                fieldWithPath("data.title").description("여행 제목"),
                                fieldWithPath("data.startDate").description("여행 시작일"),
                                fieldWithPath("data.endDate").description("여행 종료일"),
                                fieldWithPath("data.imageUrl").description("여행 이미지 URL")
                        )
                ));

        then(tripService).should().createTrip(any(User.class), any(TripCreateRequestDto.class));
    }

    @Test
    @DisplayName("여행 조회 성공")
    void getTrip_Success() throws Exception {
        // given
        given(tripService.getTrip(eq(tripId)))
                .willReturn(tripDto);

        // when & then
        mockMvc.perform(get("/api/trips/{tripId}", tripId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(tripId))
                .andExpect(jsonPath("$.data.title").value("테스트 여행"))
                .andDo(document("trip-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("여행 ID"),
                                fieldWithPath("data.title").description("여행 제목"),
                                fieldWithPath("data.startDate").description("여행 시작일"),
                                fieldWithPath("data.endDate").description("여행 종료일"),
                                fieldWithPath("data.imageUrl").description("여행 이미지 URL")
                        )
                ));

        then(tripService).should().getTrip(eq(tripId));
    }

    @Test
    @DisplayName("나의 여행 목록 조회 성공")
    void getUserTrips_Success() throws Exception {
        // given
        CursorPageResponseDto<TripDto> pageResponse = CursorPageResponseDto.<TripDto>builder()
                .content(List.of(tripDto))
                .hasNext(false)
                .build();

        given(tripService.getUserTrips(any(User.class), eq("createdAt"), eq("ASC"), isNull(), isNull(), eq(10)))
                .willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/trips/my-trips")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(tripId))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andDo(document("my-trip-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        queryParameters(
                                parameterWithName("orderBy").description("정렬 기준 (기본값: createdAt)").optional(),
                                parameterWithName("direction").description("정렬 방향 (ASC/DESC, 기본값: ASC)").optional(),
                                parameterWithName("limit").description("페이지 크기 (기본값: 10)").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.content[]").description("여행 목록"),
                                fieldWithPath("data.content[].id").description("여행 ID"),
                                fieldWithPath("data.content[].title").description("여행 제목"),
                                fieldWithPath("data.content[].startDate").description("여행 시작일"),
                                fieldWithPath("data.content[].endDate").description("여행 종료일"),
                                fieldWithPath("data.content[].imageUrl").description("여행 이미지 URL"),
                                fieldWithPath("data.nextCursor").description("다음 커서"),
                                fieldWithPath("data.nextAfter").description("다음 After ID"),
                                fieldWithPath("data.size").description("현재 페이지 크기"),
                                fieldWithPath("data.hasNext").description("다음 페이지 존재 여부")
                        )
                ));

        then(tripService).should().getUserTrips(any(User.class), eq("createdAt"), eq("ASC"), isNull(), isNull(), eq(10));
    }

    @Test
    @DisplayName("여행 수정 성공")
    void updateTrip_Success() throws Exception {
        // given
        TripDto updatedTripDto = TripDto.builder()
                .id(tripId)
                .title("수정된 여행")
                .startDate(LocalDate.of(2025, 2, 1))
                .endDate(LocalDate.of(2025, 2, 10))
                .imageUrl("http://example.com/trip2.jpg")
                .build();

        given(tripService.updateTrip(eq(tripId), any(TripUpdateRequestDto.class)))
                .willReturn(updatedTripDto);

        // when & then
        mockMvc.perform(patch("/api/trips/{tripId}", tripId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(tripId))
                .andExpect(jsonPath("$.data.title").value("수정된 여행"))
                .andDo(document("trip-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID")
                        ),
                        requestFields(
                                fieldWithPath("title").description("여행 제목"),
                                fieldWithPath("startDate").description("여행 시작일"),
                                fieldWithPath("endDate").description("여행 종료일"),
                                fieldWithPath("imageUrl").description("여행 이미지 URL"),
                                fieldWithPath("countries").description("여행 국가 목록")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("여행 ID"),
                                fieldWithPath("data.title").description("여행 제목"),
                                fieldWithPath("data.startDate").description("여행 시작일"),
                                fieldWithPath("data.endDate").description("여행 종료일"),
                                fieldWithPath("data.imageUrl").description("여행 이미지 URL")
                        )
                ));

        then(tripService).should().updateTrip(eq(tripId), any(TripUpdateRequestDto.class));
    }

    @Test
    @DisplayName("여행 삭제 성공")
    void deleteTrip_Success() throws Exception {
        // given
        willDoNothing().given(tripService).deleteTrip(eq(tripId));

        // when & then
        mockMvc.perform(delete("/api/trips/{tripId}", tripId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(document("trip-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID")
                        )
                ));

        then(tripService).should().deleteTrip(eq(tripId));
    }

    @Test
    @DisplayName("정산하기 성공")
    void settleExpenses_Success() throws Exception {
        // given
        SettleExpenseDto settleExpenseDto = SettleExpenseDto.builder()
                .budget(BudgetSettleDto.builder()
                        .totalCollectedAmount(BigDecimal.valueOf(200000))
                        .totalPaidAmount(BigDecimal.valueOf(250000))
                        .remainingAmount(BigDecimal.valueOf(-50000))
                        .build())
                .expenseList(List.of(
                        UserExpenseDto.builder()
                                .tripJoinId(1L)
                                .userId(userId)
                                .username("testUser")
                                .nickname("테스트 유저")
                                .profileImageUrl("http://example.com/user.jpg")
                                .paidAmount(BigDecimal.valueOf(150000))
                                .consumedAmount(BigDecimal.valueOf(100000))
                                .build()
                ))
                .transferList(List.of(
                        TransferDto.builder()
                                .from(TransferDto.UserDto.builder()
                                        .userId(2L)
                                        .username("user2")
                                        .nickname("유저2")
                                        .profileImageUrl("http://example.com/user2.jpg")
                                        .build())
                                .to(TransferDto.UserDto.builder()
                                        .userId(userId)
                                        .username("testUser")
                                        .nickname("테스트 유저")
                                        .profileImageUrl("http://example.com/user.jpg")
                                        .build())
                                .amount(BigDecimal.valueOf(50000))
                                .build()
                ))
                .build();

        given(expenseService.settleExpenses(eq(tripId)))
                .willReturn(settleExpenseDto);

        // when & then
        mockMvc.perform(get("/api/trips/{tripId}/settlements", tripId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.expenseList[0].userId").value(userId))
                .andDo(document("trip-settlement",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.budget").description("예산 정산 정보"),
                                fieldWithPath("data.budget.totalCollectedAmount").description("총 모인 금액"),
                                fieldWithPath("data.budget.totalPaidAmount").description("총 지출한 금액"),
                                fieldWithPath("data.budget.remainingAmount").description("남은 금액"),
                                fieldWithPath("data.expenseList[]").description("사용자별 지출 내역"),
                                fieldWithPath("data.expenseList[].tripJoinId").description("여행 참가 ID"),
                                fieldWithPath("data.expenseList[].userId").description("사용자 ID"),
                                fieldWithPath("data.expenseList[].username").description("사용자명"),
                                fieldWithPath("data.expenseList[].nickname").description("닉네임"),
                                fieldWithPath("data.expenseList[].profileImageUrl").description("프로필 이미지 URL"),
                                fieldWithPath("data.expenseList[].paidAmount").description("지불한 금액"),
                                fieldWithPath("data.expenseList[].consumedAmount").description("소비한 금액"),
                                fieldWithPath("data.transferList[]").description("송금 내역"),
                                fieldWithPath("data.transferList[].from").description("송금자 정보"),
                                fieldWithPath("data.transferList[].from.userId").description("송금자 ID"),
                                fieldWithPath("data.transferList[].from.username").description("송금자명"),
                                fieldWithPath("data.transferList[].from.nickname").description("송금자 닉네임"),
                                fieldWithPath("data.transferList[].from.profileImageUrl").description("송금자 프로필 이미지 URL"),
                                fieldWithPath("data.transferList[].to").description("수신자 정보"),
                                fieldWithPath("data.transferList[].to.userId").description("수신자 ID"),
                                fieldWithPath("data.transferList[].to.username").description("수신자명"),
                                fieldWithPath("data.transferList[].to.nickname").description("수신자 닉네임"),
                                fieldWithPath("data.transferList[].to.profileImageUrl").description("수신자 프로필 이미지 URL"),
                                fieldWithPath("data.transferList[].amount").description("송금액")
                        )
                ));

        then(expenseService).should().settleExpenses(eq(tripId));
    }

    @Test
    @DisplayName("지출 통계 조회 성공")
    void getExpenseStatistics_Success() throws Exception {
        // given
        ExpenseStatisticsDto statisticsDto = ExpenseStatisticsDto.builder()
                .totalAmount(BigDecimal.valueOf(500000))
                .statistics(List.of(
                        ExpenseRatioDto.builder()
                                .expenseCategory(ExpenseCategory.FOOD)
                                .amount(BigDecimal.valueOf(200000))
                                .percentage(BigDecimal.valueOf(40))
                                .build(),
                        ExpenseRatioDto.builder()
                                .expenseCategory(ExpenseCategory.ACCOMMODATION)
                                .amount(BigDecimal.valueOf(300000))
                                .percentage(BigDecimal.valueOf(60))
                                .build()
                ))
                .groupBy(GroupByType.CATEGORY)
                .build();

        given(expenseService.getExpenseStatistics(eq(tripId), eq(ExpenseType.PERSONAL), eq(GroupByType.CATEGORY), isNull()))
                .willReturn(statisticsDto);

        // when & then
        mockMvc.perform(get("/api/trips/{tripId}/stats/expenses", tripId)
                        .header("Authorization", "Bearer " + accessToken)
                        .param("expenseType", "PERSONAL")
                        .param("groupBy", "CATEGORY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalAmount").value(500000))
                .andExpect(jsonPath("$.data.groupBy").value("CATEGORY"))
                .andDo(document("trip-expense-stats",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID")
                        ),
                        queryParameters(
                                parameterWithName("expenseType").description("지출 타입 (PERSONAL/SHARED)"),
                                parameterWithName("groupBy").description("그룹화 기준 (CATEGORY/DATE/USER)"),
                                parameterWithName("date").description("날짜 (선택)").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.totalAmount").description("총 지출액"),
                                fieldWithPath("data.statistics[]").description("통계 목록"),
                                fieldWithPath("data.statistics[].expenseCategory").description("지출 카테고리"),
                                fieldWithPath("data.statistics[].amount").description("금액"),
                                fieldWithPath("data.statistics[].percentage").description("비율"),
                                fieldWithPath("data.groupBy").description("그룹화 기준")
                        )
                ));

        then(expenseService).should().getExpenseStatistics(eq(tripId), eq(ExpenseType.PERSONAL), eq(GroupByType.CATEGORY), isNull());
    }

    @Test
    @DisplayName("지출 CSV 다운로드 성공")
    void exportExpensesToExcel_Success() throws Exception {
        // given
        byte[] csvData = "ID,Title,Amount\n1,Test,10000".getBytes();
        ByteArrayResource resource = new ByteArrayResource(csvData);

        given(expenseService.exportExpensesToExcel(eq(tripId)))
                .willReturn(resource);

        // when & then
        mockMvc.perform(get("/api/trips/{tripId}/expenses/export", tripId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().contentType("text/csv"))
                .andDo(document("trip-expense-export",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID")
                        ),
                        responseHeaders(
                                headerWithName("Content-Disposition").description("파일 다운로드 헤더"),
                                headerWithName("Content-Type").description("응답 컨텐츠 타입")
                        )
                ));

        then(expenseService).should().exportExpensesToExcel(eq(tripId));
    }

/*
    @Test
    @DisplayName("여행 초대 링크 생성 성공")
    void generateTripInviteLink_Success() throws Exception {
        // given
        given(tripService.generateTripInviteLink(eq(tripId)))
                .willReturn(inviteLinkDto);

        // when & then
        mockMvc.perform(post("/api/trips/{tripId}/generate-invite-link", tripId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.inviteLink").value("http://example.com/invite/abc123"))
                .andDo(document("trip-invite-link-generate",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("tripId").description("여행 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.inviteLink").description("초대 링크")
                        )
                ));

        then(tripService).should().generateTripInviteLink(eq(tripId));
    }
*/

/*
    @Test
    @DisplayName("여행 초대 링크로 여행 참가 성공")
    void join_Success() throws Exception {
        // given
        given(tripService.join(any(User.class), any(TripJoinRequestDto.class)))
                .willReturn(tripDto);

        // when & then
        mockMvc.perform(post("/api/trips/join")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(tripId))
                .andExpect(jsonPath("$.data.title").value("테스트 여행"))
                .andDo(document("trip-join",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        requestFields(
                                fieldWithPath("inviteLink").description("초대 링크")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("여행 ID"),
                                fieldWithPath("data.title").description("여행 제목"),
                                fieldWithPath("data.startDate").description("여행 시작일"),
                                fieldWithPath("data.endDate").description("여행 종료일"),
                                fieldWithPath("data.imageUrl").description("여행 이미지 URL")
                        )
                ));

        then(tripService).should().join(any(User.class), any(TripJoinRequestDto.class));
    }
*/
}
