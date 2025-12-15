package travel.mytravelplan.domain.quiz.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.quiz.entity.Quiz;
import travel.mytravelplan.domain.quiz.enums.QuizType;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.support.RepositoryTestSupport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("퀴즈 리포지토리 테스트")
class QuizRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("퀴즈를 저장하고 조회할 수 있다")
    void save_and_findById() {
        // given
        User user = createUser("testUser", "test@email.com");
        Quiz quiz = createQuiz(QuizType.MULTIPLE_CHOICE, user);
        Quiz savedQuiz = quizRepository.save(quiz);

        em.flush();
        em.clear();

        // when
        Optional<Quiz> foundQuiz = quizRepository.findById(savedQuiz.getId());

        // then
        assertThat(foundQuiz).isPresent();
        assertThat(foundQuiz.get().getQuizType()).isEqualTo(QuizType.MULTIPLE_CHOICE);
        assertThat(foundQuiz.get().getUser().getUsername()).isEqualTo("testUser");
    }

    @Test
    @DisplayName("퀴즈를 삭제할 수 있다")
    void delete() {
        // given
        User user = createUser("testUser", "test@email.com");
        Quiz quiz = createAndSaveQuiz(QuizType.SELF_REVIEW, user);
        Long quizId = quiz.getId();

        em.flush();
        em.clear();

        // when
        Quiz foundQuiz = quizRepository.findById(quizId).orElseThrow();
        quizRepository.delete(foundQuiz);

        em.flush();
        em.clear();

        // then
        Optional<Quiz> deletedQuiz = quizRepository.findById(quizId);
        assertThat(deletedQuiz).isEmpty();
    }

    @Test
    @DisplayName("퀴즈를 완료 처리할 수 있다")
    void finish() {
        // given
        User user = createUser("testUser", "test@email.com");
        Quiz quiz = createAndSaveQuiz(QuizType.DICTATION, user);
        Long quizId = quiz.getId();

        em.flush();
        em.clear();

        // when
        Quiz foundQuiz = quizRepository.findById(quizId).orElseThrow();

        LocalDateTime finishedTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        foundQuiz.finish(finishedTime);

        em.flush();
        em.clear();

        // then
        Quiz finishedQuiz = quizRepository.findById(quizId).orElseThrow();

        assertThat(finishedQuiz.getFinishedAt()).isNotNull();
        assertThat(finishedQuiz.getFinishedAt()).isEqualTo(finishedTime);
    }

    @Test
    @DisplayName("사용자명으로 퀴즈 목록을 조회할 수 있다")
    void findAllByCursor_byUsername() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");

        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user1);
        createAndSaveQuiz(QuizType.SELF_REVIEW, user1);
        createAndSaveQuiz(QuizType.DICTATION, user2);

        em.flush();
        em.clear();

        // when
        List<Quiz> quizzes = quizRepository.findAllByCursor(
                "user1",
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(quizzes).hasSize(2);
        assertThat(quizzes)
                .allMatch(quiz -> quiz.getUser().getUsername().equals("user1"));
    }

    @Test
    @DisplayName("퀴즈 타입으로 퀴즈 목록을 필터링할 수 있다")
    void findAllByCursor_byQuizType() {
        // given
        User user = createUser("testUser", "test@email.com");

        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        createAndSaveQuiz(QuizType.SELF_REVIEW, user);
        createAndSaveQuiz(QuizType.DICTATION, user);

        em.flush();
        em.clear();

        // when
        List<Quiz> quizzes = quizRepository.findAllByCursor(
                null,
                QuizType.MULTIPLE_CHOICE,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(quizzes).hasSize(2);
        assertThat(quizzes)
                .allMatch(quiz -> quiz.getQuizType() == QuizType.MULTIPLE_CHOICE);
    }

    @Test
    @DisplayName("사용자명과 퀴즈 타입으로 퀴즈를 필터링할 수 있다")
    void findAllByCursor_byUsernameAndQuizType() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");

        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user1);
        createAndSaveQuiz(QuizType.SELF_REVIEW, user1);
        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user2);

        em.flush();
        em.clear();

        // when
        List<Quiz> quizzes = quizRepository.findAllByCursor(
                "user1",
                QuizType.MULTIPLE_CHOICE,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(quizzes).hasSize(1);
        assertThat(quizzes.getFirst().getUser().getUsername()).isEqualTo("user1");
        assertThat(quizzes.getFirst().getQuizType()).isEqualTo(QuizType.MULTIPLE_CHOICE);
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 퀴즈 목록을 조회할 수 있다 - 내림차순")
    void findAllByCursor_pagination_desc() {
        // given
        User user = createUser("testUser", "test@email.com");

        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        createAndSaveQuiz(QuizType.SELF_REVIEW, user);
        createAndSaveQuiz(QuizType.DICTATION, user);

        em.flush();
        em.clear();

        // when
        List<Quiz> quizzes = quizRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(quizzes).hasSize(3);
        assertThat(quizzes.get(0).getCreatedAt()).isAfterOrEqualTo(quizzes.get(1).getCreatedAt());
        assertThat(quizzes.get(1).getCreatedAt()).isAfterOrEqualTo(quizzes.get(2).getCreatedAt());
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 퀴즈 목록을 조회할 수 있다 - 오름차순")
    void findAllByCursor_pagination_asc() {
        // given
        User user = createUser("testUser", "test@email.com");

        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        createAndSaveQuiz(QuizType.SELF_REVIEW, user);
        createAndSaveQuiz(QuizType.DICTATION, user);

        em.flush();
        em.clear();

        // when
        List<Quiz> quizzes = quizRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(quizzes).hasSize(3);
        assertThat(quizzes.get(0).getCreatedAt()).isBeforeOrEqualTo(quizzes.get(1).getCreatedAt());
        assertThat(quizzes.get(1).getCreatedAt()).isBeforeOrEqualTo(quizzes.get(2).getCreatedAt());
    }

    @Test
    @DisplayName("limit을 적용하여 퀴즈 목록을 조회할 수 있다")
    void findAllByCursor_withLimit() {
        // given
        User user = createUser("testUser", "test@email.com");

        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        createAndSaveQuiz(QuizType.SELF_REVIEW, user);
        createAndSaveQuiz(QuizType.DICTATION, user);
        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        createAndSaveQuiz(QuizType.SELF_REVIEW, user);

        em.flush();
        em.clear();

        // when
        List<Quiz> quizzes = quizRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                3
        );

        // then
        assertThat(quizzes).hasSize(3);
    }

    @Test
    @DisplayName("커서와 after를 사용하여 다음 페이지를 조회할 수 있다 - 내림차순")
    void findAllByCursor_withCursorAndAfter_desc() {
        // given
        User user = createUser("testUser", "test@email.com");

        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        createAndSaveQuiz(QuizType.SELF_REVIEW, user);
        createAndSaveQuiz(QuizType.DICTATION, user);
        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Quiz> firstPage = quizRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then - 첫 페이지 검증
        assertThat(firstPage).hasSize(2);

        // when - 두 번째 페이지 조회
        Quiz lastQuiz = firstPage.getLast();
        List<Quiz> secondPage = quizRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                lastQuiz.getCreatedAt().toString(),
                lastQuiz.getId(),
                2
        );

        // then - 두 번째 페이지 검증
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage).allMatch(quiz ->
                quiz.getCreatedAt().isBefore(lastQuiz.getCreatedAt()) ||
                (quiz.getCreatedAt().isEqual(lastQuiz.getCreatedAt()) && quiz.getId() < lastQuiz.getId())
        );
    }

    @Test
    @DisplayName("커서와 after를 사용하여 다음 페이지를 조회할 수 있다 - 오름차순")
    void findAllByCursor_withCursorAndAfter_asc() {
        // given
        User user = createUser("testUser", "test@email.com");

        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        createAndSaveQuiz(QuizType.SELF_REVIEW, user);
        createAndSaveQuiz(QuizType.DICTATION, user);
        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Quiz> firstPage = quizRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then - 첫 페이지 검증
        assertThat(firstPage).hasSize(2);

        // when - 두 번째 페이지 조회
        Quiz lastQuiz = firstPage.getLast();
        List<Quiz> secondPage = quizRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "asc",
                lastQuiz.getCreatedAt().toString(),
                lastQuiz.getId(),
                2
        );

        // then - 두 번째 페이지 검증
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage).allMatch(quiz ->
                quiz.getCreatedAt().isAfter(lastQuiz.getCreatedAt()) ||
                (quiz.getCreatedAt().isEqual(lastQuiz.getCreatedAt()) && quiz.getId() > lastQuiz.getId())
        );
    }

    @Test
    @DisplayName("cursor만 있고 after가 null이면 커서 조건을 무시한다")
    void findAllByCursor_cursorWithoutAfter() {
        // given
        User user = createUser("testUser", "test@email.com");

        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        createAndSaveQuiz(QuizType.SELF_REVIEW, user);
        createAndSaveQuiz(QuizType.DICTATION, user);

        em.flush();
        em.clear();

        // when
        List<Quiz> quizzes = quizRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                "2024-01-01T00:00:00",
                null,
                10
        );

        // then
        assertThat(quizzes).hasSize(3);
    }

    @Test
    @DisplayName("after만 있고 cursor가 null이면 커서 조건을 무시한다")
    void findAllByCursor_afterWithoutCursor() {
        // given
        User user = createUser("testUser", "test@email.com");

        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        createAndSaveQuiz(QuizType.SELF_REVIEW, user);
        createAndSaveQuiz(QuizType.DICTATION, user);

        em.flush();
        em.clear();

        // when
        List<Quiz> quizzes = quizRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                null,
                1L,
                10
        );

        // then
        assertThat(quizzes).hasSize(3);
    }

    @Test
    @DisplayName("존재하지 않는 사용자명으로 조회하면 빈 리스트를 반환한다")
    void findAllByCursor_nonExistentUsername() {
        // given
        User user = createUser("testUser", "test@email.com");

        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);

        em.flush();
        em.clear();

        // when
        List<Quiz> quizzes = quizRepository.findAllByCursor(
                "nonExistentUser",
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(quizzes).isEmpty();
    }

    @Test
    @DisplayName("username이 null이면 모든 사용자의 퀴즈를 조회한다")
    void findAllByCursor_nullUsername() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");

        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user1);
        createAndSaveQuiz(QuizType.SELF_REVIEW, user2);

        em.flush();
        em.clear();

        // when
        List<Quiz> quizzes = quizRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(quizzes).hasSize(2);
    }

    @Test
    @DisplayName("quizType이 null이면 모든 타입의 퀴즈를 조회한다")
    void findAllByCursor_nullQuizType() {
        // given
        User user = createUser("testUser", "test@email.com");

        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        createAndSaveQuiz(QuizType.SELF_REVIEW, user);
        createAndSaveQuiz(QuizType.DICTATION, user);

        em.flush();
        em.clear();

        // when
        List<Quiz> quizzes = quizRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(quizzes).hasSize(3);
    }

    @Test
    @DisplayName("동일한 사용자의 여러 퀴즈를 정확하게 페이지네이션한다")
    void findAllByCursor_sameUserMultipleQuizzes() {
        // given
        User user = createUser("testUser", "test@email.com");

        for (int i = 0; i < 10; i++) {
            createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        }

        em.flush();
        em.clear();

        // when - 첫 번째 페이지
        List<Quiz> page1 = quizRepository.findAllByCursor(
                "testUser",
                null,
                "createdAt",
                "desc",
                null,
                null,
                3
        );

        // then
        assertThat(page1).hasSize(3);

        // when - 두 번째 페이지
        Quiz cursor1 = page1.getLast();
        List<Quiz> page2 = quizRepository.findAllByCursor(
                "testUser",
                null,
                "createdAt",
                "desc",
                cursor1.getCreatedAt().toString(),
                cursor1.getId(),
                3
        );

        // then
        assertThat(page2).hasSize(3);

        // when - 세 번째 페이지
        Quiz cursor2 = page2.getLast();
        List<Quiz> page3 = quizRepository.findAllByCursor(
                "testUser",
                null,
                "createdAt",
                "desc",
                cursor2.getCreatedAt().toString(),
                cursor2.getId(),
                3
        );

        // then
        assertThat(page3).hasSize(3);

        // when - 네 번째 페이지
        Quiz cursor3 = page3.getLast();
        List<Quiz> page4 = quizRepository.findAllByCursor(
                "testUser",
                null,
                "createdAt",
                "desc",
                cursor3.getCreatedAt().toString(),
                cursor3.getId(),
                3
        );

        // then
        assertThat(page4).hasSize(1);
    }

    @Test
    @DisplayName("direction이 대문자 ASC일 때도 정상 작동한다")
    void findAllByCursor_directionUpperCaseASC() {
        // given
        User user = createUser("testUser", "test@email.com");

        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        createAndSaveQuiz(QuizType.SELF_REVIEW, user);
        createAndSaveQuiz(QuizType.DICTATION, user);

        em.flush();
        em.clear();

        // when
        List<Quiz> quizzes = quizRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "ASC",
                null,
                null,
                10
        );

        // then
        assertThat(quizzes).hasSize(3);
        assertThat(quizzes.get(0).getCreatedAt()).isBeforeOrEqualTo(quizzes.get(1).getCreatedAt());
        assertThat(quizzes.get(1).getCreatedAt()).isBeforeOrEqualTo(quizzes.get(2).getCreatedAt());
    }

    @Test
    @DisplayName("direction이 대문자 DESC일 때도 정상 작동한다")
    void findAllByCursor_directionUpperCaseDESC() {
        // given
        User user = createUser("testUser", "test@email.com");

        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        createAndSaveQuiz(QuizType.SELF_REVIEW, user);
        createAndSaveQuiz(QuizType.DICTATION, user);

        em.flush();
        em.clear();

        // when
        List<Quiz> quizzes = quizRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "DESC",
                null,
                null,
                10
        );

        // then
        assertThat(quizzes).hasSize(3);
        assertThat(quizzes.get(0).getCreatedAt()).isAfterOrEqualTo(quizzes.get(1).getCreatedAt());
        assertThat(quizzes.get(1).getCreatedAt()).isAfterOrEqualTo(quizzes.get(2).getCreatedAt());
    }

    @Test
    @DisplayName("direction이 혼합된 케이스일 때도 정상 작동한다")
    void findAllByCursor_directionMixedCase() {
        // given
        User user = createUser("testUser", "test@email.com");

        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        createAndSaveQuiz(QuizType.SELF_REVIEW, user);

        em.flush();
        em.clear();

        // when
        List<Quiz> quizzes = quizRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "AsC",
                null,
                null,
                10
        );

        // then
        assertThat(quizzes).hasSize(2);
        assertThat(quizzes.get(0).getCreatedAt()).isBeforeOrEqualTo(quizzes.get(1).getCreatedAt());
    }

    @Test
    @DisplayName("limit 1로 단일 퀴즈를 조회한다")
    void findAllByCursor_limitOne() {
        // given
        User user = createUser("testUser", "test@email.com");

        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        createAndSaveQuiz(QuizType.SELF_REVIEW, user);
        createAndSaveQuiz(QuizType.DICTATION, user);

        em.flush();
        em.clear();

        // when
        List<Quiz> quizzes = quizRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                1
        );

        // then
        assertThat(quizzes).hasSize(1);
    }

    @Test
    @DisplayName("매우 큰 limit 값으로 조회해도 정상 작동한다")
    void findAllByCursor_largeLimit() {
        // given
        User user = createUser("testUser", "test@email.com");

        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        createAndSaveQuiz(QuizType.SELF_REVIEW, user);

        em.flush();
        em.clear();

        // when
        List<Quiz> quizzes = quizRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                1000
        );

        // then
        assertThat(quizzes).hasSize(2);
    }

    @Test
    @DisplayName("같은 createdAt 시간에 여러 퀴즈가 있을 때 id로 정확히 페이지네이션한다 - 내림차순")
    void findAllByCursor_sameCreatedAtMultipleQuizzes_desc() {
        // given
        User user = createUser("testUser", "test@email.com");

        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        createAndSaveQuiz(QuizType.SELF_REVIEW, user);
        createAndSaveQuiz(QuizType.DICTATION, user);

        em.flush();
        em.clear();

        // when - 첫 페이지
        List<Quiz> page1 = quizRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(page1).hasSize(2);

        // when - cursor의 id보다 작은 id를 가진 퀴즈만 조회
        Quiz cursor = page1.getLast();
        List<Quiz> page2 = quizRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                cursor.getCreatedAt().toString(),
                cursor.getId(),
                10
        );

        // then - cursor의 id보다 작은 퀴즈들만 조회됨
        assertThat(page2).allMatch(quiz ->
                quiz.getCreatedAt().isBefore(cursor.getCreatedAt()) ||
                (quiz.getCreatedAt().isEqual(cursor.getCreatedAt()) && quiz.getId() < cursor.getId())
        );
    }

    @Test
    @DisplayName("같은 createdAt 시간에 여러 퀴즈가 있을 때 id로 정확히 페이지네이션한다 - 오름차순")
    void findAllByCursor_sameCreatedAtMultipleQuizzes_asc() {
        // given
        User user = createUser("testUser", "test@email.com");

        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        createAndSaveQuiz(QuizType.SELF_REVIEW, user);
        createAndSaveQuiz(QuizType.DICTATION, user);

        em.flush();
        em.clear();

        // when - 첫 페이지
        List<Quiz> page1 = quizRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(page1).hasSize(2);

        // when - cursor의 id보다 큰 id를 가진 퀴즈만 조회
        Quiz cursor = page1.getLast();
        List<Quiz> page2 = quizRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "asc",
                cursor.getCreatedAt().toString(),
                cursor.getId(),
                10
        );

        // then - cursor의 id보다 큰 퀴즈들만 조회됨
        assertThat(page2).allMatch(quiz ->
                quiz.getCreatedAt().isAfter(cursor.getCreatedAt()) ||
                (quiz.getCreatedAt().isEqual(cursor.getCreatedAt()) && quiz.getId() > cursor.getId())
        );
    }

    @Test
    @DisplayName("모든 퀴즈 타입을 조회할 수 있다")
    void findAllByCursor_allQuizTypes() {
        // given
        User user = createUser("testUser", "test@email.com");

        createAndSaveQuiz(QuizType.SELF_REVIEW, user);
        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user);
        createAndSaveQuiz(QuizType.DICTATION, user);

        em.flush();
        em.clear();

        // when
        List<Quiz> quizzes = quizRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(quizzes).hasSize(3);
        assertThat(quizzes)
                .extracting(Quiz::getQuizType)
                .containsExactlyInAnyOrder(
                        QuizType.SELF_REVIEW,
                        QuizType.MULTIPLE_CHOICE,
                        QuizType.DICTATION
                );
    }

    @Test
    @DisplayName("여러 조건으로 필터링 후 결과가 없으면 빈 리스트를 반환한다")
    void findAllByCursor_multipleFiltersNoResult() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");

        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user1);
        createAndSaveQuiz(QuizType.SELF_REVIEW, user2);

        em.flush();
        em.clear();

        // when
        List<Quiz> quizzes = quizRepository.findAllByCursor(
                "user1",
                QuizType.SELF_REVIEW,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(quizzes).isEmpty();
    }

    @Test
    @DisplayName("사용자명과 퀴즈 타입과 커서를 모두 사용하여 페이지네이션할 수 있다")
    void findAllByCursor_allParametersCombined() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");

        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user1);
        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user1);
        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user1);
        createAndSaveQuiz(QuizType.SELF_REVIEW, user1);
        createAndSaveQuiz(QuizType.MULTIPLE_CHOICE, user2);

        em.flush();
        em.clear();

        // when - 첫 페이지
        List<Quiz> firstPage = quizRepository.findAllByCursor(
                "user1",
                QuizType.MULTIPLE_CHOICE,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);

        // when - 두 번째 페이지
        Quiz lastQuiz = firstPage.getLast();
        List<Quiz> secondPage = quizRepository.findAllByCursor(
                "user1",
                QuizType.MULTIPLE_CHOICE,
                "createdAt",
                "desc",
                lastQuiz.getCreatedAt().toString(),
                lastQuiz.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
    }

    @Test
    @DisplayName("퀴즈가 없을 때 빈 리스트를 반환한다")
    void findAllByCursor_noQuizzes() {
        // given
        // 퀴즈를 생성하지 않음

        // when
        List<Quiz> quizzes = quizRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(quizzes).isEmpty();
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

    private Quiz createQuiz(QuizType quizType, User user) {
        return Quiz.createQuiz(quizType, user);
    }

    private Quiz createAndSaveQuiz(QuizType quizType, User user) {
        Quiz quiz = createQuiz(quizType, user);
        return quizRepository.save(quiz);
    }
}