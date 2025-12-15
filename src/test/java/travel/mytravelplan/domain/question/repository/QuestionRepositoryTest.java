package travel.mytravelplan.domain.question.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.card.entity.Card;
import travel.mytravelplan.domain.card.repository.CardRepository;
import travel.mytravelplan.domain.deck.entity.Deck;
import travel.mytravelplan.domain.deck.repository.DeckRepository;
import travel.mytravelplan.domain.question.entity.Dictation;
import travel.mytravelplan.domain.question.entity.MultipleChoice;
import travel.mytravelplan.domain.question.entity.Question;
import travel.mytravelplan.domain.question.entity.SelfReview;
import travel.mytravelplan.domain.quiz.entity.Quiz;
import travel.mytravelplan.domain.quiz.enums.QuizType;
import travel.mytravelplan.domain.quiz.repository.QuizRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.support.RepositoryTestSupport;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("질문 레포지토리 테스트")
class QuestionRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("질문을 저장한다")
    void saveQuestion() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card = createAndSaveCard("앞면", "뒷면", deck);
        Quiz quiz = createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        Question question = createMultipleChoice(quiz, card);

        // when
        Question savedQuestion = questionRepository.save(question);
        em.flush();
        em.clear();

        // then
        assertThat(savedQuestion.getId()).isNotNull();
        assertThat(savedQuestion.getQuiz().getId()).isEqualTo(quiz.getId());
        assertThat(savedQuestion.getCard().getId()).isEqualTo(card.getId());
    }

    @Test
    @DisplayName("질문을 ID로 조회한다")
    void findQuestionById() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card = createAndSaveCard("앞면", "뒷면", deck);
        Quiz quiz = createAndSaveQuiz(QuizType.DICTATION, user);
        Question question = createAndSaveDictation(quiz, card);
        em.flush();
        em.clear();

        // when
        Question foundQuestion = questionRepository.findById(question.getId()).orElse(null);

        // then
        assertThat(foundQuestion).isNotNull();
        assertThat(foundQuestion.getId()).isEqualTo(question.getId());
        assertThat(foundQuestion.getQuiz().getId()).isEqualTo(quiz.getId());
        assertThat(foundQuestion.getCard().getId()).isEqualTo(card.getId());
    }

    @Test
    @DisplayName("질문을 삭제한다")
    void deleteQuestion() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card = createAndSaveCard("앞면", "뒷면", deck);
        Quiz quiz = createAndSaveQuiz(QuizType.SELF_REVIEW, user);
        Question question = createAndSaveSelfReview(quiz, card);
        em.flush();
        em.clear();

        // when
        questionRepository.deleteById(question.getId());
        em.flush();
        em.clear();

        // then
        Question deletedQuestion = questionRepository.findById(question.getId()).orElse(null);
        assertThat(deletedQuestion).isNull();
    }

    @Test
    @DisplayName("퀴즈 ID로 질문 목록을 조회한다")
    void findAllByQuizId() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card1 = createAndSaveCard("앞면1", "뒷면1", deck);
        Card card2 = createAndSaveCard("앞면2", "뒷면2", deck);
        Card card3 = createAndSaveCard("앞면3", "뒷면3", deck);

        Quiz quiz1 = createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        Quiz quiz2 = createAndSaveQuiz(QuizType.DICTATION, user);

        Question question1 = createAndSaveMultipleChoice(quiz1, card1);
        Question question2 = createAndSaveMultipleChoice(quiz1, card2);
        createAndSaveDictation(quiz2, card3);
        em.flush();
        em.clear();

        // when
        List<Question> questions = questionRepository.findAllByCursor(
                quiz1.getId(), "createdAt", "desc", null, null, 10
        );

        // then
        assertThat(questions).hasSize(2);
        assertThat(questions)
                .extracting(Question::getId)
                .containsExactlyInAnyOrder(question1.getId(), question2.getId());
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 질문 목록을 조회한다 - 생성일 기준 내림차순")
    void findAllByCursorWithCreatedAtDesc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card1 = createAndSaveCard("앞면1", "뒷면1", deck);
        Card card2 = createAndSaveCard("앞면2", "뒷면2", deck);
        Card card3 = createAndSaveCard("앞면3", "뒷면3", deck);

        Quiz quiz = createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);

        createAndSaveMultipleChoice(quiz, card1);
        createAndSaveMultipleChoice(quiz, card2);
        createAndSaveMultipleChoice(quiz, card3);
        em.flush();
        em.clear();

        // when
        List<Question> firstPage = questionRepository.findAllByCursor(
                quiz.getId(), "createdAt", "desc", null, null, 2
        );

        // then
        assertThat(firstPage).hasSize(2);
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 질문 목록을 조회한다 - 생성일 기준 오름차순")
    void findAllByCursorWithCreatedAtAsc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card1 = createAndSaveCard("앞면1", "뒷면1", deck);
        Card card2 = createAndSaveCard("앞면2", "뒷면2", deck);
        Card card3 = createAndSaveCard("앞면3", "뒷면3", deck);

        Quiz quiz = createAndSaveQuiz(QuizType.DICTATION, user);

        createAndSaveDictation(quiz, card1);
        createAndSaveDictation(quiz, card2);
        createAndSaveDictation(quiz, card3);
        em.flush();
        em.clear();

        // when
        List<Question> firstPage = questionRepository.findAllByCursor(
                quiz.getId(), "createdAt", "asc", null, null, 2
        );

        // then
        assertThat(firstPage).hasSize(2);
    }

    @Test
    @DisplayName("limit을 설정하여 질문 목록을 조회한다")
    void findAllByCursorWithLimit() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card1 = createAndSaveCard("앞면1", "뒷면1", deck);
        Card card2 = createAndSaveCard("앞면2", "뒷면2", deck);
        Card card3 = createAndSaveCard("앞면3", "뒷면3", deck);
        Card card4 = createAndSaveCard("앞면4", "뒷면4", deck);
        Card card5 = createAndSaveCard("앞면5", "뒷면5", deck);

        Quiz quiz = createAndSaveQuiz(QuizType.SELF_REVIEW, user);

        createAndSaveSelfReview(quiz, card1);
        createAndSaveSelfReview(quiz, card2);
        createAndSaveSelfReview(quiz, card3);
        createAndSaveSelfReview(quiz, card4);
        createAndSaveSelfReview(quiz, card5);
        em.flush();
        em.clear();

        // when
        List<Question> questions = questionRepository.findAllByCursor(
                quiz.getId(), "createdAt", "desc", null, null, 3
        );

        // then
        assertThat(questions).hasSize(3);
    }

    @Test
    @DisplayName("퀴즈에 질문을 추가한다")
    void addQuestionToQuiz() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card = createAndSaveCard("앞면", "뒷면", deck);
        Quiz quiz = createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        Question question = createMultipleChoice(quiz, card);
        em.flush();
        em.clear();

        // when
        Quiz foundQuiz = quizRepository.findById(quiz.getId()).orElseThrow();
        foundQuiz.addQuestion(question);
        questionRepository.save(question);
        em.flush();
        em.clear();

        // then
        Quiz updatedQuiz = quizRepository.findById(quiz.getId()).orElseThrow();
        assertThat(updatedQuiz.getQuestions()).hasSize(1);
        assertThat(updatedQuiz.getQuestions().getFirst().getCard().getId()).isEqualTo(card.getId());
    }

    @Test
    @DisplayName("다양한 타입의 질문을 저장한다")
    void saveDifferentTypeQuestions() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card1 = createAndSaveCard("앞면1", "뒷면1", deck);
        Card card2 = createAndSaveCard("앞면2", "뒷면2", deck);
        Card card3 = createAndSaveCard("앞면3", "뒷면3", deck);

        Quiz quiz = createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);

        // when
        Question multipleChoice = createAndSaveMultipleChoice(quiz, card1);
        Question dictation = createAndSaveDictation(quiz, card2);
        Question selfReview = createAndSaveSelfReview(quiz, card3);
        em.flush();
        em.clear();

        // then
        Question foundMultipleChoice = questionRepository.findById(multipleChoice.getId()).orElse(null);
        Question foundDictation = questionRepository.findById(dictation.getId()).orElse(null);
        Question foundSelfReview = questionRepository.findById(selfReview.getId()).orElse(null);

        assertThat(foundMultipleChoice).isInstanceOf(MultipleChoice.class);
        assertThat(foundDictation).isInstanceOf(Dictation.class);
        assertThat(foundSelfReview).isInstanceOf(SelfReview.class);
    }

    @Test
    @DisplayName("커서와 after를 사용하여 다음 페이지를 조회한다 - 내림차순")
    void findNextPageByCursorDesc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card1 = createAndSaveCard("앞면1", "뒷면1", deck);
        Card card2 = createAndSaveCard("앞면2", "뒷면2", deck);
        Card card3 = createAndSaveCard("앞면3", "뒷면3", deck);
        Card card4 = createAndSaveCard("앞면4", "뒷면4", deck);
        Card card5 = createAndSaveCard("앞면5", "뒷면5", deck);

        Quiz quiz = createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);

        createAndSaveMultipleChoice(quiz, card1);
        createAndSaveMultipleChoice(quiz, card2);
        createAndSaveMultipleChoice(quiz, card3);
        createAndSaveMultipleChoice(quiz, card4);
        createAndSaveMultipleChoice(quiz, card5);
        em.flush();
        em.clear();

        // when
        List<Question> firstPage = questionRepository.findAllByCursor(
                quiz.getId(), "createdAt", "desc", null, null, 2
        );

        Question lastQuestionOfFirstPage = firstPage.getLast();
        String cursor = lastQuestionOfFirstPage.getCreatedAt().toString();
        Long after = lastQuestionOfFirstPage.getId();

        List<Question> secondPage = questionRepository.findAllByCursor(
                quiz.getId(), "createdAt", "desc", cursor, after, 2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage).doesNotContainAnyElementsOf(firstPage);
    }

    @Test
    @DisplayName("커서와 after를 사용하여 다음 페이지를 조회한다 - 오름차순")
    void findNextPageByCursorAsc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card1 = createAndSaveCard("앞면1", "뒷면1", deck);
        Card card2 = createAndSaveCard("앞면2", "뒷면2", deck);
        Card card3 = createAndSaveCard("앞면3", "뒷면3", deck);
        Card card4 = createAndSaveCard("앞면4", "뒷면4", deck);
        Card card5 = createAndSaveCard("앞면5", "뒷면5", deck);

        Quiz quiz = createAndSaveQuiz(QuizType.DICTATION, user);

        createAndSaveDictation(quiz, card1);
        createAndSaveDictation(quiz, card2);
        createAndSaveDictation(quiz, card3);
        createAndSaveDictation(quiz, card4);
        createAndSaveDictation(quiz, card5);
        em.flush();
        em.clear();

        // when
        List<Question> firstPage = questionRepository.findAllByCursor(
                quiz.getId(), "createdAt", "asc", null, null, 2
        );

        Question lastQuestionOfFirstPage = firstPage.getLast();
        String cursor = lastQuestionOfFirstPage.getCreatedAt().toString();
        Long after = lastQuestionOfFirstPage.getId();

        List<Question> secondPage = questionRepository.findAllByCursor(
                quiz.getId(), "createdAt", "asc", cursor, after, 2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage).doesNotContainAnyElementsOf(firstPage);
    }

    @Test
    @DisplayName("퀴즈 ID가 null일 때 모든 질문을 조회한다")
    void findAllByCursorWithNullQuizId() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card1 = createAndSaveCard("앞면1", "뒷면1", deck);
        Card card2 = createAndSaveCard("앞면2", "뒷면2", deck);
        Card card3 = createAndSaveCard("앞면3", "뒷면3", deck);

        Quiz quiz1 = createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        Quiz quiz2 = createAndSaveQuiz(QuizType.DICTATION, user);

        createAndSaveMultipleChoice(quiz1, card1);
        createAndSaveMultipleChoice(quiz1, card2);
        createAndSaveDictation(quiz2, card3);
        em.flush();
        em.clear();

        // when
        List<Question> questions = questionRepository.findAllByCursor(
                null, "createdAt", "desc", null, null, 10
        );

        // then
        assertThat(questions.size()).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("질문이 없을 때 빈 리스트를 반환한다")
    void findAllByCursorWhenNoQuestions() {
        // given
        User user = createUser("testUser", "test@email.com");
        Quiz quiz = createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        em.flush();
        em.clear();

        // when
        List<Question> questions = questionRepository.findAllByCursor(
                quiz.getId(), "createdAt", "desc", null, null, 10
        );

        // then
        assertThat(questions).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 퀴즈 ID로 조회하면 빈 리스트를 반환한다")
    void findAllByCursorWithNonExistentQuizId() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card = createAndSaveCard("앞면", "뒷면", deck);
        Quiz quiz = createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        createAndSaveMultipleChoice(quiz, card);
        em.flush();
        em.clear();

        // when
        List<Question> questions = questionRepository.findAllByCursor(
                99999L, "createdAt", "desc", null, null, 10
        );

        // then
        assertThat(questions).isEmpty();
    }

    @Test
    @DisplayName("limit보다 적은 수의 질문이 있을 때 모두 조회된다")
    void findAllByCursorWhenLessThanLimit() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card1 = createAndSaveCard("앞면1", "뒷면1", deck);
        Card card2 = createAndSaveCard("앞면2", "뒷면2", deck);

        Quiz quiz = createAndSaveQuiz(QuizType.DICTATION, user);

        createAndSaveDictation(quiz, card1);
        createAndSaveDictation(quiz, card2);
        em.flush();
        em.clear();

        // when
        List<Question> questions = questionRepository.findAllByCursor(
                quiz.getId(), "createdAt", "desc", null, null, 10
        );

        // then
        assertThat(questions).hasSize(2);
    }

    @Test
    @DisplayName("퀴즈에 속한 질문들을 모두 삭제한다")
    void deleteAllQuestionsByQuizId() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card1 = createAndSaveCard("앞면1", "뒷면1", deck);
        Card card2 = createAndSaveCard("앞면2", "뒷면2", deck);
        Card card3 = createAndSaveCard("앞면3", "뒷면3", deck);

        Quiz quiz = createAndSaveQuiz(QuizType.SELF_REVIEW, user);

        createAndSaveSelfReview(quiz, card1);
        createAndSaveSelfReview(quiz, card2);
        createAndSaveSelfReview(quiz, card3);
        em.flush();
        em.clear();

        // when
        List<Question> questions = questionRepository.findAllByCursor(
                quiz.getId(), "createdAt", "desc", null, null, 10
        );
        questionRepository.deleteAll(questions);
        em.flush();
        em.clear();

        // then
        List<Question> remainingQuestions = questionRepository.findAllByCursor(
                quiz.getId(), "createdAt", "desc", null, null, 10
        );
        assertThat(remainingQuestions).isEmpty();
    }

    @Test
    @DisplayName("커서와 after가 null인 경우 첫 페이지를 조회한다")
    void findAllByCursor_withNullCursorAndAfter() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card1 = createAndSaveCard("앞면1", "뒷면1", deck);
        Card card2 = createAndSaveCard("앞면2", "뒷면2", deck);

        Quiz quiz = createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);

        createAndSaveMultipleChoice(quiz, card1);
        createAndSaveMultipleChoice(quiz, card2);
        em.flush();
        em.clear();

        // when
        List<Question> questions = questionRepository.findAllByCursor(
                quiz.getId(), "createdAt", "asc", null, null, 10
        );

        // then
        assertThat(questions).hasSize(2);
    }

    @Test
    @DisplayName("커서만 null인 경우에도 정상 조회된다")
    void findAllByCursor_withNullCursorOnly() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card1 = createAndSaveCard("앞면1", "뒷면1", deck);
        Card card2 = createAndSaveCard("앞면2", "뒷면2", deck);

        Quiz quiz = createAndSaveQuiz(QuizType.DICTATION, user);

        createAndSaveDictation(quiz, card1);
        createAndSaveDictation(quiz, card2);
        em.flush();
        em.clear();

        // when
        List<Question> questions = questionRepository.findAllByCursor(
                quiz.getId(), "createdAt", "desc", null, 1L, 10
        );

        // then
        assertThat(questions).hasSize(2);
    }

    @Test
    @DisplayName("after만 null인 경우에도 정상 조회된다")
    void findAllByCursor_withNullAfterOnly() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card1 = createAndSaveCard("앞면1", "뒷면1", deck);
        Card card2 = createAndSaveCard("앞면2", "뒷면2", deck);

        Quiz quiz = createAndSaveQuiz(QuizType.SELF_REVIEW, user);

        createAndSaveSelfReview(quiz, card1);
        createAndSaveSelfReview(quiz, card2);
        em.flush();
        em.clear();

        // when
        List<Question> questions = questionRepository.findAllByCursor(
                quiz.getId(), "createdAt", "asc", "2024-01-01T00:00:00", null, 10
        );

        // then
        assertThat(questions).hasSize(2);
    }

    @Test
    @DisplayName("동일한 createdAt을 가진 질문들을 ID로 정렬한다 - 오름차순")
    void findAllByCursor_withSameCreatedAtOrderByIdAsc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card1 = createAndSaveCard("앞면1", "뒷면1", deck);
        Card card2 = createAndSaveCard("앞면2", "뒷면2", deck);
        Card card3 = createAndSaveCard("앞면3", "뒷면3", deck);

        Quiz quiz = createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);

        createAndSaveMultipleChoice(quiz, card1);
        createAndSaveMultipleChoice(quiz, card2);
        createAndSaveMultipleChoice(quiz, card3);
        em.flush();
        em.clear();

        // when
        List<Question> questions = questionRepository.findAllByCursor(
                quiz.getId(), "createdAt", "asc", null, null, 10
        );

        // then
        assertThat(questions).hasSize(3);
        // ID 순으로 정렬되어야 함
        for (int i = 0; i < questions.size() - 1; i++) {
            assertThat(questions.get(i).getId()).isLessThanOrEqualTo(questions.get(i + 1).getId());
        }
    }

    @Test
    @DisplayName("동일한 createdAt을 가진 질문들을 ID로 정렬한다 - 내림차순")
    void findAllByCursor_withSameCreatedAtOrderByIdDesc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card1 = createAndSaveCard("앞면1", "뒷면1", deck);
        Card card2 = createAndSaveCard("앞면2", "뒷면2", deck);
        Card card3 = createAndSaveCard("앞면3", "뒷면3", deck);

        Quiz quiz = createAndSaveQuiz(QuizType.DICTATION, user);

        createAndSaveDictation(quiz, card1);
        createAndSaveDictation(quiz, card2);
        createAndSaveDictation(quiz, card3);
        em.flush();
        em.clear();

        // when
        List<Question> questions = questionRepository.findAllByCursor(
                quiz.getId(), "createdAt", "desc", null, null, 10
        );

        // then
        assertThat(questions).hasSize(3);
        // ID 역순으로 정렬되어야 함
        for (int i = 0; i < questions.size() - 1; i++) {
            assertThat(questions.get(i).getId()).isGreaterThanOrEqualTo(questions.get(i + 1).getId());
        }
    }

    @Test
    @DisplayName("내림차순 정렬 시 커서 기반 페이지네이션이 정상 작동한다")
    void findAllByCursor_withDescOrderAndCursor() throws InterruptedException {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card1 = createAndSaveCard("앞면1", "뒷면1", deck);
        Card card2 = createAndSaveCard("앞면2", "뒷면2", deck);
        Card card3 = createAndSaveCard("앞면3", "뒷면3", deck);

        Quiz quiz = createAndSaveQuiz(QuizType.SELF_REVIEW, user);

        createAndSaveSelfReview(quiz, card1);
        Thread.sleep(100); // 시간 차이를 두기 위해
        createAndSaveSelfReview(quiz, card2);
        Thread.sleep(100);
        createAndSaveSelfReview(quiz, card3);
        em.flush();
        em.clear();

        // when - 첫 번째 페이지
        List<Question> firstPage = questionRepository.findAllByCursor(
                quiz.getId(), "createdAt", "desc", null, null, 2
        );

        // then
        assertThat(firstPage).hasSize(2);

        // when - 두 번째 페이지
        Question lastQuestion = firstPage.getLast();
        List<Question> secondPage = questionRepository.findAllByCursor(
                quiz.getId(), "createdAt", "desc",
                lastQuestion.getCreatedAt().toString(),
                lastQuestion.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage).doesNotContainAnyElementsOf(firstPage);
    }

    @Test
    @DisplayName("오름차순 정렬 시 커서 기반 페이지네이션이 정상 작동한다")
    void findAllByCursor_withAscOrderAndCursor() throws InterruptedException {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card1 = createAndSaveCard("앞면1", "뒷면1", deck);
        Card card2 = createAndSaveCard("앞면2", "뒷면2", deck);
        Card card3 = createAndSaveCard("앞면3", "뒷면3", deck);

        Quiz quiz = createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);

        createAndSaveMultipleChoice(quiz, card1);
        Thread.sleep(100); // 시간 차이를 두기 위해
        createAndSaveMultipleChoice(quiz, card2);
        Thread.sleep(100);
        createAndSaveMultipleChoice(quiz, card3);
        em.flush();
        em.clear();

        // when - 첫 번째 페이지
        List<Question> firstPage = questionRepository.findAllByCursor(
                quiz.getId(), "createdAt", "asc", null, null, 2
        );

        // then
        assertThat(firstPage).hasSize(2);

        // when - 두 번째 페이지
        Question lastQuestion = firstPage.getLast();
        List<Question> secondPage = questionRepository.findAllByCursor(
                quiz.getId(), "createdAt", "asc",
                lastQuestion.getCreatedAt().toString(),
                lastQuestion.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage).doesNotContainAnyElementsOf(firstPage);
    }

    // TestFixture 메서드들
    private User createUser(String username, String email) {
        User user = User.createUser(
                username,
                "password123",
                email,
                SocialType.LOCAL,
                null,
                LocalDate.of(1990, 1, 1),
                "010-1234-5678",
                Gender.MALE,
                Set.of(Role.USER)
        );
        return userRepository.save(user);
    }

    private Deck createAndSaveDeck(String name, User user) {
        Deck deck = Deck.createDeck(name, user);
        return deckRepository.save(deck);
    }

    private Card createAndSaveCard(String front, String back, Deck deck) {
        Card card = Card.createCard(front, back, deck);
        return cardRepository.save(card);
    }

    private Quiz createAndSaveQuiz(QuizType quizType, User user) {
        Quiz quiz = Quiz.createQuiz(quizType, user);
        return quizRepository.save(quiz);
    }

    private MultipleChoice createMultipleChoice(Quiz quiz, Card card) {
        return MultipleChoice.createMultipleChoice(quiz, card);
    }

    private MultipleChoice createAndSaveMultipleChoice(Quiz quiz, Card card) {
        MultipleChoice multipleChoice = MultipleChoice.createMultipleChoice(quiz, card);
        return questionRepository.save(multipleChoice);
    }

    private Dictation createAndSaveDictation(Quiz quiz, Card card) {
        Dictation dictation = Dictation.createDictation(quiz, card);
        return questionRepository.save(dictation);
    }

    private SelfReview createAndSaveSelfReview(Quiz quiz, Card card) {
        SelfReview selfReview = SelfReview.createSelfReview(quiz, card);
        return questionRepository.save(selfReview);
    }
}