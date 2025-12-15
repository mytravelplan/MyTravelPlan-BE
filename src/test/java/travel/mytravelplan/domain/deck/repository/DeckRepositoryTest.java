package travel.mytravelplan.domain.deck.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.deck.entity.Deck;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.support.RepositoryTestSupport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("덱 레포지토리 테스트")
public class DeckRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("덱을 저장한다")
    void saveDeck() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createDeck("테스트 덱", user);

        // when
        Deck savedDeck = deckRepository.save(deck);
        em.flush();
        em.clear();

        // then
        assertThat(savedDeck.getId()).isNotNull();
        assertThat(savedDeck.getName()).isEqualTo("테스트 덱");
        assertThat(savedDeck.getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("덱을 ID로 조회한다")
    void findDeckById() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createDeck("테스트 덱", user);
        Deck savedDeck = deckRepository.save(deck);
        em.flush();
        em.clear();

        // when
        Deck foundDeck = deckRepository.findById(savedDeck.getId()).orElse(null);

        // then
        assertThat(foundDeck).isNotNull();
        assertThat(foundDeck.getId()).isEqualTo(savedDeck.getId());
        assertThat(foundDeck.getName()).isEqualTo("테스트 덱");
    }

    @Test
    @DisplayName("덱을 수정한다")
    void updateDeck() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createDeck("테스트 덱", user);
        Deck savedDeck = deckRepository.save(deck);
        em.flush();
        em.clear();

        // when
        Deck foundDeck = deckRepository.findById(savedDeck.getId()).orElseThrow();
        foundDeck.update("수정된 덱");
        em.flush();
        em.clear();

        // then
        Deck updatedDeck = deckRepository.findById(savedDeck.getId()).orElse(null);
        assertThat(updatedDeck).isNotNull();
        assertThat(updatedDeck.getName()).isEqualTo("수정된 덱");
    }

    @Test
    @DisplayName("덱을 삭제한다")
    void deleteDeck() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createDeck("테스트 덱", user);
        Deck savedDeck = deckRepository.save(deck);
        em.flush();
        em.clear();

        // when
        deckRepository.deleteById(savedDeck.getId());
        em.flush();
        em.clear();

        // then
        Deck deletedDeck = deckRepository.findById(savedDeck.getId()).orElse(null);
        assertThat(deletedDeck).isNull();
    }

    @Test
    @DisplayName("사용자 이름으로 덱을 조회한다")
    void findAllByCursor_byUsername() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");

        Deck deck1 = createAndSaveDeck("덱1", user1);
        Deck deck2 = createAndSaveDeck("덱2", user1);
        Deck deck3 = createAndSaveDeck("덱3", user2);

        em.flush();
        em.clear();

        // when
        List<Deck> decks = deckRepository.findAllByCursor(
                "user1",
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(decks).hasSize(2);
        assertThat(decks)
                .extracting(Deck::getName)
                .containsExactlyInAnyOrder("덱1", "덱2");
    }

    @Test
    @DisplayName("키워드로 덱을 검색한다")
    void findAllByCursor_byKeyword() {
        // given
        User user = createUser("testUser", "test@email.com");

        Deck deck1 = createAndSaveDeck("영어 단어장", user);
        Deck deck2 = createAndSaveDeck("수학 공식", user);
        Deck deck3 = createAndSaveDeck("영어 문법", user);

        em.flush();
        em.clear();

        // when
        List<Deck> decks = deckRepository.findAllByCursor(
                user.getUsername(),
                "영어",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(decks).hasSize(2);
        assertThat(decks)
                .extracting(Deck::getName)
                .containsExactlyInAnyOrder("영어 단어장", "영어 문법");
    }

    @Test
    @DisplayName("생성일 기준 내림차순으로 덱을 조회한다")
    void findAllByCursor_orderByCreatedAtDesc() {
        // given
        User user = createUser("testUser", "test@email.com");

        Deck deck1 = createAndSaveDeck("덱1", user);
        Deck deck2 = createAndSaveDeck("덱2", user);
        Deck deck3 = createAndSaveDeck("덱3", user);

        em.flush();
        em.clear();

        // when
        List<Deck> decks = deckRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(decks).hasSize(3);
        assertThat(decks.get(0).getName()).isEqualTo("덱3");
        assertThat(decks.get(1).getName()).isEqualTo("덱2");
        assertThat(decks.get(2).getName()).isEqualTo("덱1");
    }

    @Test
    @DisplayName("생성일 기준 오름차순으로 덱을 조회한다")
    void findAllByCursor_orderByCreatedAtAsc() {
        // given
        User user = createUser("testUser", "test@email.com");

        Deck deck1 = createAndSaveDeck("덱1", user);
        Deck deck2 = createAndSaveDeck("덱2", user);
        Deck deck3 = createAndSaveDeck("덱3", user);

        em.flush();
        em.clear();

        // when
        List<Deck> decks = deckRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(decks).hasSize(3);
        assertThat(decks.get(0).getName()).isEqualTo("덱1");
        assertThat(decks.get(1).getName()).isEqualTo("덱2");
        assertThat(decks.get(2).getName()).isEqualTo("덱3");
    }

    @Test
    @DisplayName("limit 개수만큼 덱을 조회한다")
    void findAllByCursor_withLimit() {
        // given
        User user = createUser("testUser", "test@email.com");

        Deck deck1 = createAndSaveDeck("덱1", user);
        Deck deck2 = createAndSaveDeck("덱2", user);
        Deck deck3 = createAndSaveDeck("덱3", user);
        Deck deck4 = createAndSaveDeck("덱4", user);
        Deck deck5 = createAndSaveDeck("덱5", user);

        em.flush();
        em.clear();

        // when
        List<Deck> decks = deckRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                3
        );

        // then
        assertThat(decks).hasSize(3);
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 덱을 조회한다 - 내림차순")
    void findAllByCursor_withCursor_desc() {
        // given
        User user = createUser("testUser", "test@email.com");

        Deck deck1 = createAndSaveDeck("덱1", user);
        Deck deck2 = createAndSaveDeck("덱2", user);
        Deck deck3 = createAndSaveDeck("덱3", user);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Deck> firstPage = deckRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getName()).isEqualTo("덱3");
        assertThat(firstPage.get(1).getName()).isEqualTo("덱2");

        // when - 두 번째 페이지 조회
        Deck lastDeck = firstPage.get(firstPage.size() - 1);
        List<Deck> secondPage = deckRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "desc",
                lastDeck.getCreatedAt().toString(),
                lastDeck.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getName()).isEqualTo("덱1");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 덱을 조회한다 - 오름차순")
    void findAllByCursor_withCursor_asc() {
        // given
        User user = createUser("testUser", "test@email.com");

        Deck deck1 = createAndSaveDeck("덱1", user);
        Deck deck2 = createAndSaveDeck("덱2", user);
        Deck deck3 = createAndSaveDeck("덱3", user);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Deck> firstPage = deckRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getName()).isEqualTo("덱1");
        assertThat(firstPage.get(1).getName()).isEqualTo("덱2");

        // when - 두 번째 페이지 조회
        Deck lastDeck = firstPage.get(firstPage.size() - 1);
        List<Deck> secondPage = deckRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "asc",
                lastDeck.getCreatedAt().toString(),
                lastDeck.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getName()).isEqualTo("덱3");
    }

    @Test
    @DisplayName("사용자 이름과 키워드로 덱을 조회한다")
    void findAllByCursor_byUsernameAndKeyword() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");

        Deck deck1 = createAndSaveDeck("자바 공부", user1);
        Deck deck2 = createAndSaveDeck("파이썬 공부", user1);
        Deck deck3 = createAndSaveDeck("자바스크립트 공부", user1);
        Deck deck4 = createAndSaveDeck("자바 기초", user2);

        em.flush();
        em.clear();

        // when
        List<Deck> decks = deckRepository.findAllByCursor(
                "user1",
                "자바",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(decks).hasSize(2);
        assertThat(decks)
                .extracting(Deck::getName)
                .containsExactlyInAnyOrder("자바 공부", "자바스크립트 공부");
    }

    @Test
    @DisplayName("커서와 after가 null인 경우 첫 페이지를 조회한다")
    void findAllByCursor_withNullCursorAndAfter() {
        // given
        User user = createUser("testUser", "test@email.com");

        Deck deck1 = createAndSaveDeck("덱1", user);
        Deck deck2 = createAndSaveDeck("덱2", user);

        em.flush();
        em.clear();

        // when
        List<Deck> decks = deckRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(decks).hasSize(2);
        assertThat(decks.get(0).getName()).isEqualTo("덱1");
        assertThat(decks.get(1).getName()).isEqualTo("덱2");
    }

    @Test
    @DisplayName("username이 null인 경우 모든 사용자의 덱을 조회한다")
    void findAllByCursor_withNullUsername() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");

        Deck deck1 = createAndSaveDeck("user1의 덱", user1);
        Deck deck2 = createAndSaveDeck("user2의 덱", user2);

        em.flush();
        em.clear();

        // when
        List<Deck> decks = deckRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(decks).hasSize(2);
    }

    @Test
    @DisplayName("키워드가 빈 문자열인 경우 모든 덱을 조회한다")
    void findAllByCursor_withEmptyKeyword() {
        // given
        User user = createUser("testUser", "test@email.com");

        Deck deck1 = createAndSaveDeck("영어", user);
        Deck deck2 = createAndSaveDeck("수학", user);

        em.flush();
        em.clear();

        // when
        List<Deck> decks = deckRepository.findAllByCursor(
                user.getUsername(),
                "",
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(decks).hasSize(2);
    }

    @Test
    @DisplayName("키워드 검색 시 대소문자를 구분하지 않는다")
    void findAllByCursor_withKeywordCaseInsensitive() {
        // given
        User user = createUser("testUser", "test@email.com");

        Deck deck1 = createAndSaveDeck("English Vocabulary", user);
        Deck deck2 = createAndSaveDeck("Korean Grammar", user);

        em.flush();
        em.clear();

        // when
        List<Deck> decks = deckRepository.findAllByCursor(
                user.getUsername(),
                "english",
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(decks).hasSize(1);
        assertThat(decks.get(0).getName()).isEqualTo("English Vocabulary");
    }

    @Test
    @DisplayName("내림차순 정렬 시 커서 기반 페이지네이션이 정상 작동한다")
    void findAllByCursor_withDescOrderAndCursor() {
        // given
        User user = createUser("testUser", "test@email.com");

        Deck deck1 = createAndSaveDeck("덱1", user);
        Deck deck2 = createAndSaveDeck("덱2", user);
        Deck deck3 = createAndSaveDeck("덱3", user);

        em.flush();
        em.clear();

        // when - 첫 번째 페이지
        List<Deck> firstPage = deckRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getName()).isEqualTo("덱3");
        assertThat(firstPage.get(1).getName()).isEqualTo("덱2");

        // when - 두 번째 페이지
        Deck lastDeck = firstPage.get(firstPage.size() - 1);
        List<Deck> secondPage = deckRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "desc",
                lastDeck.getCreatedAt().toString(),
                lastDeck.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getName()).isEqualTo("덱1");
    }

    @Test
    @DisplayName("동일한 createdAt을 가진 덱들을 ID로 정렬한다 - 오름차순")
    void findAllByCursor_withSameCreatedAtOrderByIdAsc() {
        // given
        User user = createUser("testUser", "test@email.com");

        Deck deck1 = createAndSaveDeck("덱A", user);
        Deck deck2 = createAndSaveDeck("덱B", user);
        Deck deck3 = createAndSaveDeck("덱C", user);

        em.flush();
        em.clear();

        // when
        List<Deck> decks = deckRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(decks).hasSize(3);
        // ID 순으로 정렬되어야 함
        for (int i = 0; i < decks.size() - 1; i++) {
            assertThat(decks.get(i).getId()).isLessThan(decks.get(i + 1).getId());
        }
    }

    @Test
    @DisplayName("동일한 createdAt을 가진 덱들을 ID로 정렬한다 - 내림차순")
    void findAllByCursor_withSameCreatedAtOrderByIdDesc() {
        // given
        User user = createUser("testUser", "test@email.com");

        Deck deck1 = createAndSaveDeck("덱X", user);
        Deck deck2 = createAndSaveDeck("덱Y", user);
        Deck deck3 = createAndSaveDeck("덱Z", user);

        em.flush();
        em.clear();

        // when
        List<Deck> decks = deckRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(decks).hasSize(3);
        // ID 역순으로 정렬되어야 함
        for (int i = 0; i < decks.size() - 1; i++) {
            assertThat(decks.get(i).getId()).isGreaterThan(decks.get(i + 1).getId());
        }
    }

    @Test
    @DisplayName("username이 빈 문자열인 경우 모든 사용자의 덱을 조회한다")
    void findAllByCursor_withEmptyUsername() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");

        Deck deck1 = createAndSaveDeck("덱1", user1);
        Deck deck2 = createAndSaveDeck("덱2", user2);

        em.flush();
        em.clear();

        // when
        List<Deck> decks = deckRepository.findAllByCursor(
                "",
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(decks).hasSize(2);
    }

    @Test
    @DisplayName("cursor만 있고 after가 null인 경우 첫 페이지를 조회한다")
    void findAllByCursor_withCursorOnlyAndNullAfter() {
        // given
        User user = createUser("testUser", "test@email.com");

        Deck deck1 = createAndSaveDeck("덱1", user);
        Deck deck2 = createAndSaveDeck("덱2", user);

        em.flush();
        em.clear();

        // when
        List<Deck> decks = deckRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "asc",
                LocalDateTime.now().toString(),
                null,
                10
        );

        // then
        assertThat(decks).hasSize(2);
    }

    @Test
    @DisplayName("after만 있고 cursor가 null인 경우 첫 페이지를 조회한다")
    void findAllByCursor_withAfterOnlyAndNullCursor() {
        // given
        User user = createUser("testUser", "test@email.com");

        Deck deck1 = createAndSaveDeck("덱1", user);
        Deck deck2 = createAndSaveDeck("덱2", user);

        em.flush();
        em.clear();

        // when
        List<Deck> decks = deckRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "asc",
                null,
                1L,
                10
        );

        // then
        assertThat(decks).hasSize(2);
    }

    @Test
    @DisplayName("커서 기반 페이지네이션 - createdAt이 동일하고 ID가 커서 after보다 큰 경우 조회 - 오름차순")
    void findAllByCursor_withSameCreatedAtAndGreaterIdAsc() {
        // given
        User user = createUser("testUser", "test@email.com");

        Deck deck1 = createAndSaveDeck("덱1", user);
        Deck deck2 = createAndSaveDeck("덱2", user);
        Deck deck3 = createAndSaveDeck("덱3", user);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Deck> firstPage = deckRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                1
        );

        // then
        assertThat(firstPage).hasSize(1);
        Deck firstDeck = firstPage.get(0);

        // when - 동일한 createdAt이지만 ID가 큰 덱을 조회
        List<Deck> nextPage = deckRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "asc",
                firstDeck.getCreatedAt().toString(),
                firstDeck.getId(),
                10
        );

        // then - ID가 더 큰 덱들만 조회됨
        assertThat(nextPage).isNotEmpty();
        for (Deck deck : nextPage) {
            if (deck.getCreatedAt().equals(firstDeck.getCreatedAt())) {
                assertThat(deck.getId()).isGreaterThan(firstDeck.getId());
            }
        }
    }

    @Test
    @DisplayName("커서 기반 페이지네이션 - createdAt이 동일하고 ID가 커서 after보다 작은 경우 조회 - 내림차순")
    void findAllByCursor_withSameCreatedAtAndLessIdDesc() {
        // given
        User user = createUser("testUser", "test@email.com");

        Deck deck1 = createAndSaveDeck("덱1", user);
        Deck deck2 = createAndSaveDeck("덱2", user);
        Deck deck3 = createAndSaveDeck("덱3", user);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회 (내림차순)
        List<Deck> firstPage = deckRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                1
        );

        // then
        assertThat(firstPage).hasSize(1);
        Deck firstDeck = firstPage.get(0);

        // when - 동일한 createdAt이지만 ID가 작은 덱을 조회
        List<Deck> nextPage = deckRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "desc",
                firstDeck.getCreatedAt().toString(),
                firstDeck.getId(),
                10
        );

        // then - ID가 더 작은 덱들만 조회됨
        assertThat(nextPage).isNotEmpty();
        for (Deck deck : nextPage) {
            if (deck.getCreatedAt().equals(firstDeck.getCreatedAt())) {
                assertThat(deck.getId()).isLessThan(firstDeck.getId());
            }
        }
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

    private Deck createDeck(String name, User user) {
        return Deck.createDeck(name, user);
    }

    private Deck createAndSaveDeck(String name, User user) {
        Deck deck = Deck.createDeck(name, user);
        return deckRepository.save(deck);
    }
}
