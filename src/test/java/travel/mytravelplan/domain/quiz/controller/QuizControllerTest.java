package travel.mytravelplan.domain.quiz.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.answer.dto.AnswerRequestDto;
import travel.mytravelplan.domain.answer.dto.SelfReviewAnswerRequestDto;
import travel.mytravelplan.domain.answer.enums.ANSWER_TYPE;
import travel.mytravelplan.domain.answer.enums.SelfReviewStatus;
import travel.mytravelplan.domain.quiz.dto.QuizCreateRequestDto;
import travel.mytravelplan.domain.quiz.dto.QuizDto;
import travel.mytravelplan.domain.quiz.dto.QuizResultDto;
import travel.mytravelplan.domain.quiz.entity.Quiz;
import travel.mytravelplan.domain.quiz.enums.QuizType;
import travel.mytravelplan.domain.quiz.service.QuizService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.entity.UserProfile;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.support.ControllerTestSupport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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

@WebMvcTest(QuizController.class)
@DisplayName("퀴즈 컨트롤러 테스트")
class QuizControllerTest extends ControllerTestSupport {

    @MockitoBean
    private QuizService quizService;

    private String accessToken;
    private Long userId;
    private Long quizId;
    private User testUser;
    private Quiz testQuiz;
    private QuizCreateRequestDto createRequestDto;
    private QuizDto quizDto;
    private QuizResultDto quizResultDto;

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

        quizId = 1L;

        testQuiz = Quiz.createQuiz(QuizType.SELF_REVIEW, testUser);
        ReflectionTestUtils.setField(testQuiz, "id", quizId);
        given(quizRepository.findById(eq(quizId))).willReturn(Optional.of(testQuiz));

        createRequestDto = QuizCreateRequestDto.builder()
                .quizType(QuizType.SELF_REVIEW)
                .deckIds(List.of(1L, 2L))
                .build();

        quizDto = QuizDto.builder()
                .id(quizId)
                .quizType(QuizType.SELF_REVIEW)
                .build();

        quizResultDto = QuizResultDto.builder()
                .quizId(quizId)
                .quizType(QuizType.SELF_REVIEW)
                .finishedAt(LocalDateTime.now())
                .questions(Collections.emptyList())
                .statistics(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("퀴즈 시작 성공")
    void createQuiz_Success() throws Exception {
        // given
        given(quizService.startQuiz(any(User.class), any(QuizCreateRequestDto.class))).willReturn(quizDto);

        // when
        mockMvc.perform(post("/api/quizzes/start")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(quizId))
                .andExpect(jsonPath("$.data.quizType").value("SELF_REVIEW"))
                .andDo(document("quiz-start",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        requestFields(
                                fieldWithPath("quizType").description("퀴즈 타입 (SELF_REVIEW, MULTIPLE_CHOICE, DICTATION)"),
                                fieldWithPath("deckIds").description("덱 ID 리스트")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("퀴즈 ID"),
                                fieldWithPath("data.quizType").description("퀴즈 타입")
                        )
                ));

        // then
        assertThat(quizDto).isNotNull();
        assertThat(quizDto.getId()).isEqualTo(quizId);
        assertThat(quizDto.getQuizType()).isEqualTo(QuizType.SELF_REVIEW);
        then(quizService).should().startQuiz(any(User.class), any(QuizCreateRequestDto.class));
    }

    @Test
    @DisplayName("퀴즈 종료 성공")
    void finishQuiz_Success() throws Exception {
        // given
        List<AnswerRequestDto> answerRequestDtos = List.of(
                SelfReviewAnswerRequestDto.builder()
                        .answerType(ANSWER_TYPE.SELF_REVIEW)
                        .selfReviewStatus(SelfReviewStatus.PERFECT)
                        .build()
        );

        given(quizService.finishQuiz(eq(quizId), anyList())).willReturn(quizDto);

        // when
        mockMvc.perform(post("/api/quizzes/{quizId}/finish", quizId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answerRequestDtos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(quizId))
                .andExpect(jsonPath("$.data.quizType").value("SELF_REVIEW"))
                .andDo(document("quiz-finish",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("quizId").description("퀴즈 ID")
                        ),
                        requestFields(
                                fieldWithPath("[].answerType").description("답변 타입 (SELF_REVIEW, MULTIPLE_CHOICE, DICTATION)"),
                                fieldWithPath("[].selfReviewStatus").description("자가 평가 상태 (NO_IDEA, NOT_SURE, PERFECT)").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("퀴즈 ID"),
                                fieldWithPath("data.quizType").description("퀴즈 타입")
                        )
                ));

        // then
        assertThat(quizDto).isNotNull();
        assertThat(quizDto.getId()).isEqualTo(quizId);
        then(quizService).should().finishQuiz(eq(quizId), anyList());
    }

    @Test
    @DisplayName("퀴즈 결과 조회 성공")
    void getQuizResult_Success() throws Exception {
        // given
        given(quizService.getQuizResult(eq(quizId))).willReturn(quizResultDto);

        // when
        mockMvc.perform(get("/api/quizzes/{quizId}/result", quizId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.quizId").value(quizId))
                .andExpect(jsonPath("$.data.quizType").value("SELF_REVIEW"))
                .andDo(document("quiz-result-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("quizId").description("퀴즈 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.quizId").description("퀴즈 ID"),
                                fieldWithPath("data.quizType").description("퀴즈 타입"),
                                fieldWithPath("data.finishedAt").description("퀴즈 완료 시간"),
                                fieldWithPath("data.questions").description("문제 목록"),
                                fieldWithPath("data.statistics").description("통계 정보")
                        )
                ));

        // then
        assertThat(quizResultDto).isNotNull();
        assertThat(quizResultDto.getQuizId()).isEqualTo(quizId);
        then(quizService).should().getQuizResult(eq(quizId));
    }

    @Test
    @DisplayName("퀴즈 결과 목록 조회 성공")
    void getQuizResults_Success() throws Exception {
        // given
        CursorPageResponseDto<QuizResultDto> pageResponse = CursorPageResponseDto.<QuizResultDto>builder()
                .content(List.of(quizResultDto))
                .nextCursor(null)
                .hasNext(false)
                .size(10)
                .build();

        given(quizService.getQuizResults(
                eq("testUser"),
                eq(QuizType.SELF_REVIEW),
                eq("createdAt"),
                eq("ASC"),
                eq("cursor"),
                eq(1L),
                eq(10)
        )).willReturn(pageResponse);

        // when
        mockMvc.perform(get("/api/quizzes/results")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("quizType", "SELF_REVIEW")
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("cursor", "cursor")
                        .param("after", "1")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andDo(document("quiz-results-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        queryParameters(
                                parameterWithName("quizType").description("퀴즈 타입 (SELF_REVIEW, MULTIPLE_CHOICE, DICTATION)").optional(),
                                parameterWithName("orderBy").description("정렬 기준 (기본값: createdAt)").optional(),
                                parameterWithName("direction").description("정렬 방향 (ASC, DESC) (기본값: ASC)").optional(),
                                parameterWithName("cursor").description("커서").optional(),
                                parameterWithName("after").description("After ID").optional(),
                                parameterWithName("limit").description("페이지 크기 (기본값: 10)").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.content").description("퀴즈 결과 목록"),
                                fieldWithPath("data.content[].quizId").description("퀴즈 ID"),
                                fieldWithPath("data.content[].quizType").description("퀴즈 타입"),
                                fieldWithPath("data.content[].finishedAt").description("퀴즈 완료 시간"),
                                fieldWithPath("data.content[].questions").description("문제 목록"),
                                fieldWithPath("data.content[].statistics").description("통계 정보"),
                                fieldWithPath("data.nextCursor").description("다음 커서"),
                                fieldWithPath("data.nextAfter").description("다음 After ID"),
                                fieldWithPath("data.hasNext").description("다음 페이지 존재 여부"),
                                fieldWithPath("data.size").description("페이지 크기")
                        )
                ));

        // then
        assertThat(pageResponse).isNotNull();
        assertThat(pageResponse.getContent()).hasSize(1);
        then(quizService).should().getQuizResults(
                eq("testUser"),
                eq(QuizType.SELF_REVIEW),
                eq("createdAt"),
                eq("ASC"),
                eq("cursor"),
                eq(1L),
                eq(10)
        );
    }

    @Test
    @DisplayName("퀴즈 결과 삭제 성공")
    void deleteQuizResult_Success() throws Exception {
        // given
        willDoNothing().given(quizService).deleteQuizResult(eq(quizId));

        // when
        mockMvc.perform(delete("/api/quizzes/{quizId}/result", quizId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(document("quiz-result-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("quizId").description("퀴즈 ID")
                        )
                ));

        // then
        then(quizService).should().deleteQuizResult(eq(quizId));
    }
}