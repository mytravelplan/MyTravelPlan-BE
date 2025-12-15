package travel.mytravelplan.domain.question.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.card.entity.Card;
import travel.mytravelplan.domain.deck.entity.Deck;
import travel.mytravelplan.domain.question.dto.QuestionDto;
import travel.mytravelplan.domain.question.entity.MultipleChoice;
import travel.mytravelplan.domain.question.entity.Question;
import travel.mytravelplan.domain.question.mapper.QuestionMapper;
import travel.mytravelplan.domain.question.repository.QuestionRepository;
import travel.mytravelplan.domain.quiz.entity.Quiz;
import travel.mytravelplan.domain.quiz.enums.QuizType;
import travel.mytravelplan.domain.quiz.exception.QuizException;
import travel.mytravelplan.domain.quiz.repository.QuizRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.error.code.QuizErrorCode;
import travel.mytravelplan.global.support.ServiceTestSupport;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("질문 서비스 테스트")
class QuestionServiceTest extends ServiceTestSupport {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionMapper questionMapper;

    @InjectMocks
    private QuestionService questionService;

    private User user;
    private Deck deck;
    private Card card1;
    private Card card2;
    private Card card3;
    private Quiz quiz;
    private Question question1;
    private Question question2;
    private Question question3;
    private QuestionDto questionDto1;
    private QuestionDto questionDto2;
    private QuestionDto questionDto3;

    @BeforeEach
    void setUp() {
        user = User.createUser("testuser", "password", "test@test.com", null, null, null);
        ReflectionTestUtils.setField(user, "id", 1L);

        deck = Deck.createDeck("테스트 덱", user);
        ReflectionTestUtils.setField(deck, "id", 1L);

        card1 = Card.createCard("질문1", "답변1", deck);
        ReflectionTestUtils.setField(card1, "id", 1L);
        ReflectionTestUtils.setField(card1, "createdAt", LocalDateTime.of(2025, 11, 27, 10, 0));

        card2 = Card.createCard("질문2", "답변2", deck);
        ReflectionTestUtils.setField(card2, "id", 2L);
        ReflectionTestUtils.setField(card2, "createdAt", LocalDateTime.of(2025, 11, 27, 11, 0));

        card3 = Card.createCard("질문3", "답변3", deck);
        ReflectionTestUtils.setField(card3, "id", 3L);
        ReflectionTestUtils.setField(card3, "createdAt", LocalDateTime.of(2025, 11, 27, 12, 0));

        quiz = Quiz.createQuiz(QuizType.MULTIPLE_CHOICE, user);
        ReflectionTestUtils.setField(quiz, "id", 1L);

        question1 = MultipleChoice.createMultipleChoice(quiz, card1);
        ReflectionTestUtils.setField(question1, "id", 1L);
        ReflectionTestUtils.setField(question1, "createdAt", LocalDateTime.of(2025, 11, 27, 10, 0));

        question2 = MultipleChoice.createMultipleChoice(quiz, card2);
        ReflectionTestUtils.setField(question2, "id", 2L);
        ReflectionTestUtils.setField(question2, "createdAt", LocalDateTime.of(2025, 11, 27, 11, 0));

        question3 = MultipleChoice.createMultipleChoice(quiz, card3);
        ReflectionTestUtils.setField(question3, "id", 3L);
        ReflectionTestUtils.setField(question3, "createdAt", LocalDateTime.of(2025, 11, 27, 12, 0));

        questionDto1 = QuestionDto.builder()
                .id(1L)
                .front("질문1")
                .back("답변1")
                .build();

        questionDto2 = QuestionDto.builder()
                .id(2L)
                .front("질문2")
                .back("답변2")
                .build();

        questionDto3 = QuestionDto.builder()
                .id(3L)
                .front("질문3")
                .back("답변3")
                .build();
    }

    @Test
    @DisplayName("퀴즈 질문 목록 조회 성공 - 셔플 없음")
    void getQuestions_Success_NoShuffle() {
        // given
        Long quizId = 1L;
        boolean shuffle = false;
        String orderBy = "createdAt";
        String direction = "ASC";
        String cursor = null;
        Long after = null;
        int limit = 10;

        List<Question> questions = Arrays.asList(question1, question2, question3);

        given(quizRepository.findById(eq(quizId))).willReturn(Optional.of(quiz));
        given(questionRepository.findAllByCursor(eq(quizId), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(questions);
        given(questionMapper.toDto(eq(question1))).willReturn(questionDto1);
        given(questionMapper.toDto(eq(question2))).willReturn(questionDto2);
        given(questionMapper.toDto(eq(question3))).willReturn(questionDto3);

        // when
        CursorPageResponseDto<QuestionDto> result = questionService.getQuestions(quizId, shuffle, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).containsExactly(questionDto1, questionDto2, questionDto3);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();
        assertThat(result.getSize()).isEqualTo(3);

        then(quizRepository).should().findById(eq(quizId));
        then(questionRepository).should().findAllByCursor(eq(quizId), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(questionMapper).should().toDto(eq(question1));
        then(questionMapper).should().toDto(eq(question2));
        then(questionMapper).should().toDto(eq(question3));
    }

    @Test
    @DisplayName("퀴즈 질문 목록 조회 성공 - 다음 페이지 있음")
    void getQuestions_Success_HasNext() {
        // given
        Long quizId = 1L;
        boolean shuffle = false;
        String orderBy = "createdAt";
        String direction = "ASC";
        String cursor = null;
        Long after = null;
        int limit = 2;

        List<Question> questions = Arrays.asList(question1, question2, question3);

        given(quizRepository.findById(eq(quizId))).willReturn(Optional.of(quiz));
        given(questionRepository.findAllByCursor(eq(quizId), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(questions);
        given(questionMapper.toDto(eq(question1))).willReturn(questionDto1);
        given(questionMapper.toDto(eq(question2))).willReturn(questionDto2);

        // when
        CursorPageResponseDto<QuestionDto> result = questionService.getQuestions(quizId, shuffle, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactly(questionDto1, questionDto2);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isEqualTo(LocalDateTime.of(2025, 11, 27, 11, 0).toString());
        assertThat(result.getNextAfter()).isEqualTo(2L);
        assertThat(result.getSize()).isEqualTo(2);

        then(quizRepository).should().findById(eq(quizId));
        then(questionRepository).should().findAllByCursor(eq(quizId), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(questionMapper).should().toDto(eq(question1));
        then(questionMapper).should().toDto(eq(question2));
    }

    @Test
    @DisplayName("퀴즈 질문 목록 조회 성공 - 커서 기반 페이징")
    void getQuestions_Success_WithCursor() {
        // given
        Long quizId = 1L;
        boolean shuffle = false;
        String orderBy = "createdAt";
        String direction = "ASC";
        String cursor = LocalDateTime.of(2025, 11, 27, 10, 0).toString();
        Long after = 1L;
        int limit = 10;

        List<Question> questions = Arrays.asList(question2, question3);

        given(quizRepository.findById(eq(quizId))).willReturn(Optional.of(quiz));
        given(questionRepository.findAllByCursor(eq(quizId), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(questions);
        given(questionMapper.toDto(eq(question2))).willReturn(questionDto2);
        given(questionMapper.toDto(eq(question3))).willReturn(questionDto3);

        // when
        CursorPageResponseDto<QuestionDto> result = questionService.getQuestions(quizId, shuffle, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactly(questionDto2, questionDto3);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();
        assertThat(result.getSize()).isEqualTo(2);

        then(quizRepository).should().findById(eq(quizId));
        then(questionRepository).should().findAllByCursor(eq(quizId), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(questionMapper).should().toDto(eq(question2));
        then(questionMapper).should().toDto(eq(question3));
    }

    @Test
    @DisplayName("퀴즈 질문 목록 조회 성공 - 셔플 모드")
    void getQuestions_Success_Shuffle() {
        // given
        Long quizId = 1L;
        boolean shuffle = true;
        String orderBy = "createdAt";
        String direction = "ASC";
        String cursor = null;
        Long after = null;
        int limit = 10;

        List<Question> questions = Arrays.asList(question1, question2, question3);

        given(quizRepository.findById(eq(quizId))).willReturn(Optional.of(quiz));
        given(questionRepository.findAllByCursor(eq(quizId), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(questions);
        given(questionMapper.toDto(eq(question1))).willReturn(questionDto1);
        given(questionMapper.toDto(eq(question2))).willReturn(questionDto2);
        given(questionMapper.toDto(eq(question3))).willReturn(questionDto3);

        // when
        CursorPageResponseDto<QuestionDto> result = questionService.getQuestions(quizId, shuffle, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).containsExactlyInAnyOrder(questionDto1, questionDto2, questionDto3);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();
        assertThat(result.getSize()).isEqualTo(3);

        then(quizRepository).should().findById(eq(quizId));
        then(questionRepository).should().findAllByCursor(eq(quizId), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(questionMapper).should().toDto(eq(question1));
        then(questionMapper).should().toDto(eq(question2));
        then(questionMapper).should().toDto(eq(question3));
    }

    @Test
    @DisplayName("퀴즈 질문 목록 조회 실패 - 퀴즈를 찾을 수 없음")
    void getQuestions_Fail_QuizNotFound() {
        // given
        Long quizId = 999L;
        boolean shuffle = false;
        String orderBy = "createdAt";
        String direction = "ASC";
        String cursor = null;
        Long after = null;
        int limit = 10;

        given(quizRepository.findById(eq(quizId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> questionService.getQuestions(quizId, shuffle, orderBy, direction, cursor, after, limit))
                .isInstanceOf(QuizException.class)
                .hasFieldOrPropertyWithValue("errorCode", QuizErrorCode.QUIZ_NOT_FOUND);

        then(quizRepository).should().findById(eq(quizId));
    }

    @Test
    @DisplayName("퀴즈 질문 목록 조회 성공 - 빈 결과")
    void getQuestions_Success_EmptyResult() {
        // given
        Long quizId = 1L;
        boolean shuffle = false;
        String orderBy = "createdAt";
        String direction = "ASC";
        String cursor = null;
        Long after = null;
        int limit = 10;

        List<Question> questions = Collections.emptyList();

        given(quizRepository.findById(eq(quizId))).willReturn(Optional.of(quiz));
        given(questionRepository.findAllByCursor(eq(quizId), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(questions);

        // when
        CursorPageResponseDto<QuestionDto> result = questionService.getQuestions(quizId, shuffle, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();
        assertThat(result.getSize()).isEqualTo(0);

        then(quizRepository).should().findById(eq(quizId));
        then(questionRepository).should().findAllByCursor(eq(quizId), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("퀴즈 질문 목록 조회 성공 - 셔플 모드와 다음 페이지 있음")
    void getQuestions_Success_ShuffleWithHasNext() {
        // given
        Long quizId = 1L;
        boolean shuffle = true;
        String orderBy = "createdAt";
        String direction = "ASC";
        String cursor = null;
        Long after = null;
        int limit = 2;

        List<Question> questions = Arrays.asList(question1, question2, question3);

        given(quizRepository.findById(eq(quizId))).willReturn(Optional.of(quiz));
        given(questionRepository.findAllByCursor(eq(quizId), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(questions);
        given(questionMapper.toDto(eq(question1))).willReturn(questionDto1);
        given(questionMapper.toDto(eq(question2))).willReturn(questionDto2);

        // when
        CursorPageResponseDto<QuestionDto> result = questionService.getQuestions(quizId, shuffle, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactlyInAnyOrder(questionDto1, questionDto2);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isEqualTo(LocalDateTime.of(2025, 11, 27, 11, 0).toString());
        assertThat(result.getNextAfter()).isEqualTo(2L);
        assertThat(result.getSize()).isEqualTo(2);

        then(quizRepository).should().findById(eq(quizId));
        then(questionRepository).should().findAllByCursor(eq(quizId), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(questionMapper).should().toDto(eq(question1));
        then(questionMapper).should().toDto(eq(question2));
    }
}