package travel.mytravelplan.domain.question.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.question.dto.QuestionDto;
import travel.mytravelplan.domain.question.service.QuestionService;
import travel.mytravelplan.domain.quiz.entity.Quiz;
import travel.mytravelplan.domain.quiz.enums.QuizType;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuestionController.class)
@DisplayName("질문 컨트롤러 테스트")
class QuestionControllerTest extends ControllerTestSupport {

    @MockitoBean
    private QuestionService questionService;

    private String accessToken;
    private Long quizId;

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
        ReflectionTestUtils.setField(testUser, "id", 1L);

        accessToken = jwtUtils.createAccessToken(1L, Set.of(Role.USER));

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        quizId = 1L;
        Quiz quiz = Quiz.createQuiz(QuizType.SELF_REVIEW, testUser);
        ReflectionTestUtils.setField(quiz, "id", quizId);

        given(quizRepository.findById(eq(quizId))).willReturn(Optional.of(quiz));
    }

    @Test
    @DisplayName("퀴즈 질문 목록 조회 성공")
    void getQuestions_Success() throws Exception {
        // given
        QuestionDto question1 = QuestionDto.builder()
                .id(1L)
                .front("질문1 앞면")
                .back("질문1 뒷면")
                .build();

        QuestionDto question2 = QuestionDto.builder()
                .id(2L)
                .front("질문2 앞면")
                .back("질문2 뒷면")
                .build();

        CursorPageResponseDto<QuestionDto> pageResponse = CursorPageResponseDto.<QuestionDto>builder()
                .content(List.of(question1, question2))
                .nextCursor("2025-01-01T00:00:00")
                .nextAfter(2L)
                .size(2)
                .hasNext(false)
                .build();

        given(questionService.getQuestions(eq(quizId), eq(false), eq("createdAt"), eq("ASC"), isNull(), isNull(), eq(10)))
                .willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/quizzes/{quizId}/questions", quizId)
                        .header("Authorization", "Bearer " + accessToken)
                        .param("shuffle", "false")
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].front").value("질문1 앞면"))
                .andExpect(jsonPath("$.data.content[0].back").value("질문1 뒷면"))
                .andExpect(jsonPath("$.data.content[1].id").value(2))
                .andExpect(jsonPath("$.data.content[1].front").value("질문2 앞면"))
                .andExpect(jsonPath("$.data.content[1].back").value("질문2 뒷면"))
                .andExpect(jsonPath("$.data.nextCursor").value("2025-01-01T00:00:00"))
                .andExpect(jsonPath("$.data.nextAfter").value(2))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andDo(document("question-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (USER 또는 ADMIN 권한 필요)")
                        ),
                        pathParameters(
                                parameterWithName("quizId").description("퀴즈 ID")
                        ),
                        queryParameters(
                                parameterWithName("shuffle").description("셔플 여부").optional(),
                                parameterWithName("orderBy").description("정렬 기준 (기본값: createdAt)").optional(),
                                parameterWithName("direction").description("정렬 방향 (ASC/DESC, 기본값: ASC)").optional(),
                                parameterWithName("cursor").description("커서 (페이징용)").optional(),
                                parameterWithName("after").description("이후 ID (페이징용)").optional(),
                                parameterWithName("limit").description("조회 개수 (기본값: 10)").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.content").description("질문 목록"),
                                fieldWithPath("data.content[].id").description("질문 ID"),
                                fieldWithPath("data.content[].front").description("질문 앞면"),
                                fieldWithPath("data.content[].back").description("질문 뒷면"),
                                fieldWithPath("data.nextCursor").description("다음 커서"),
                                fieldWithPath("data.nextAfter").description("다음 ID"),
                                fieldWithPath("data.size").description("현재 페이지 크기"),
                                fieldWithPath("data.hasNext").description("다음 페이지 존재 여부")
                        )
                ));

        then(questionService).should().getQuestions(eq(quizId), eq(false), eq("createdAt"), eq("ASC"), isNull(), isNull(), eq(10));
    }

    @Test
    @DisplayName("퀴즈 질문 목록 조회 성공 - shuffle=true")
    void getQuestions_WithShuffle_Success() throws Exception {
        // given
        QuestionDto question1 = QuestionDto.builder()
                .id(3L)
                .front("질문3 앞면")
                .back("질문3 뒷면")
                .build();

        QuestionDto question2 = QuestionDto.builder()
                .id(1L)
                .front("질문1 앞면")
                .back("질문1 뒷면")
                .build();

        CursorPageResponseDto<QuestionDto> pageResponse = CursorPageResponseDto.<QuestionDto>builder()
                .content(List.of(question1, question2))
                .nextCursor(null)
                .nextAfter(null)
                .size(2)
                .hasNext(false)
                .build();

        given(questionService.getQuestions(eq(quizId), eq(true), eq("createdAt"), eq("ASC"), isNull(), isNull(), eq(10)))
                .willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/quizzes/{quizId}/questions", quizId)
                        .header("Authorization", "Bearer " + accessToken)
                        .param("shuffle", "true")
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.hasNext").value(false));

        then(questionService).should().getQuestions(eq(quizId), eq(true), eq("createdAt"), eq("ASC"), isNull(), isNull(), eq(10));
    }

    @Test
    @DisplayName("퀴즈 질문 목록 조회 성공 - 커서 페이징")
    void getQuestions_WithCursorPaging_Success() throws Exception {
        // given
        QuestionDto question1 = QuestionDto.builder()
                .id(3L)
                .front("질문3 앞면")
                .back("질문3 뒷면")
                .build();

        CursorPageResponseDto<QuestionDto> pageResponse = CursorPageResponseDto.<QuestionDto>builder()
                .content(List.of(question1))
                .nextCursor("2025-01-02T00:00:00")
                .nextAfter(3L)
                .size(1)
                .hasNext(true)
                .build();

        given(questionService.getQuestions(eq(quizId), eq(false), eq("createdAt"), eq("ASC"), eq("2025-01-01T00:00:00"), eq(2L), eq(10)))
                .willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/quizzes/{quizId}/questions", quizId)
                        .header("Authorization", "Bearer " + accessToken)
                        .param("shuffle", "false")
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("cursor", "2025-01-01T00:00:00")
                        .param("after", "2")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(3))
                .andExpect(jsonPath("$.data.nextCursor").value("2025-01-02T00:00:00"))
                .andExpect(jsonPath("$.data.nextAfter").value(3))
                .andExpect(jsonPath("$.data.hasNext").value(true));

        then(questionService).should().getQuestions(eq(quizId), eq(false), eq("createdAt"), eq("ASC"), eq("2025-01-01T00:00:00"), eq(2L), eq(10));
    }
}