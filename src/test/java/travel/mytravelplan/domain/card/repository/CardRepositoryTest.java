package travel.mytravelplan.domain.card.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.card.entity.Card;
import travel.mytravelplan.domain.card.enums.CardStatus;
import travel.mytravelplan.domain.deck.entity.Deck;
import travel.mytravelplan.domain.deck.repository.DeckRepository;
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

@DisplayName("카드 레포지토리 테스트")
public class CardRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("카드를 저장한다")
    void saveCard() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card = createCard("앞면", "뒷면", deck);

        // when
        Card savedCard = cardRepository.save(card);
        em.flush();
        em.clear();

        // then
        assertThat(savedCard.getId()).isNotNull();
        assertThat(savedCard.getFront()).isEqualTo("앞면");
        assertThat(savedCard.getBack()).isEqualTo("뒷면");
        assertThat(savedCard.getCardStatus()).isEqualTo(CardStatus.NONE);
        assertThat(savedCard.getDeck().getId()).isEqualTo(deck.getId());
    }

    @Test
    @DisplayName("카드를 ID로 조회한다")
    void findCardById() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card = createAndSaveCard("앞면", "뒷면", deck);
        em.flush();
        em.clear();

        // when
        Card foundCard = cardRepository.findById(card.getId()).orElse(null);

        // then
        assertThat(foundCard).isNotNull();
        assertThat(foundCard.getId()).isEqualTo(card.getId());
        assertThat(foundCard.getFront()).isEqualTo("앞면");
        assertThat(foundCard.getBack()).isEqualTo("뒷면");
    }

    @Test
    @DisplayName("카드를 수정한다")
    void updateCard() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card = createAndSaveCard("앞면", "뒷면", deck);
        em.flush();
        em.clear();

        // when
        Card foundCard = cardRepository.findById(card.getId()).orElseThrow();
        foundCard.update("수정된 앞면", "수정된 뒷면", CardStatus.GOT_IT);
        em.flush();
        em.clear();

        // then
        Card updatedCard = cardRepository.findById(card.getId()).orElse(null);
        assertThat(updatedCard).isNotNull();
        assertThat(updatedCard.getFront()).isEqualTo("수정된 앞면");
        assertThat(updatedCard.getBack()).isEqualTo("수정된 뒷면");
        assertThat(updatedCard.getCardStatus()).isEqualTo(CardStatus.GOT_IT);
    }

    @Test
    @DisplayName("카드를 삭제한다")
    void deleteCard() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);
        Card card = createAndSaveCard("앞면", "뒷면", deck);
        em.flush();
        em.clear();

        // when
        cardRepository.deleteById(card.getId());
        em.flush();
        em.clear();

        // then
        Card deletedCard = cardRepository.findById(card.getId()).orElse(null);
        assertThat(deletedCard).isNull();
    }

    @Test
    @DisplayName("여러 덱 ID로 카드를 조회한다")
    void findAllByDeckIdIn() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck1 = createAndSaveDeck("덱1", user);
        Deck deck2 = createAndSaveDeck("덱2", user);
        Deck deck3 = createAndSaveDeck("덱3", user);

        Card card1 = createAndSaveCard("카드1", "뒷면1", deck1);
        Card card2 = createAndSaveCard("카드2", "뒷면2", deck1);
        Card card3 = createAndSaveCard("카드3", "뒷면3", deck2);
        Card card4 = createAndSaveCard("카드4", "뒷면4", deck3);

        em.flush();
        em.clear();

        // when
        List<Card> cards = cardRepository.findAllByDeckIdIn(List.of(deck1.getId(), deck2.getId()));

        // then
        assertThat(cards).hasSize(3);
        assertThat(cards)
                .extracting(Card::getFront)
                .containsExactlyInAnyOrder("카드1", "카드2", "카드3");
    }

    @Test
    @DisplayName("키워드로 카드를 검색한다")
    void findAllByCursor_byKeyword() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);

        Card card1 = createAndSaveCard("Hello", "안녕하세요", deck);
        Card card2 = createAndSaveCard("World", "세계", deck);
        Card card3 = createAndSaveCard("Hello World", "안녕하세요 세계", deck);

        em.flush();
        em.clear();

        // when
        List<Card> cards = cardRepository.findAllByCursor(
                deck.getId(),
                "Hello",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(cards).hasSize(2);
        assertThat(cards)
                .extracting(Card::getFront)
                .containsExactlyInAnyOrder("Hello", "Hello World");
    }

    @Test
    @DisplayName("생성일 기준 내림차순으로 카드를 조회한다")
    void findAllByCursor_orderByCreatedAtDesc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);

        Card card1 = createAndSaveCard("카드1", "뒷면1", deck);
        Card card2 = createAndSaveCard("카드2", "뒷면2", deck);
        Card card3 = createAndSaveCard("카드3", "뒷면3", deck);

        em.flush();
        em.clear();

        // when
        List<Card> cards = cardRepository.findAllByCursor(
                deck.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(cards).hasSize(3);
        assertThat(cards.get(0).getFront()).isEqualTo("카드3");
        assertThat(cards.get(1).getFront()).isEqualTo("카드2");
        assertThat(cards.get(2).getFront()).isEqualTo("카드1");
    }

    @Test
    @DisplayName("생성일 기준 오름차순으로 카드를 조회한다")
    void findAllByCursor_orderByCreatedAtAsc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);

        Card card1 = createAndSaveCard("카드1", "뒷면1", deck);
        Card card2 = createAndSaveCard("카드2", "뒷면2", deck);
        Card card3 = createAndSaveCard("카드3", "뒷면3", deck);

        em.flush();
        em.clear();

        // when
        List<Card> cards = cardRepository.findAllByCursor(
                deck.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(cards).hasSize(3);
        assertThat(cards.get(0).getFront()).isEqualTo("카드1");
        assertThat(cards.get(1).getFront()).isEqualTo("카드2");
        assertThat(cards.get(2).getFront()).isEqualTo("카드3");
    }

    @Test
    @DisplayName("limit 개수만큼 카드를 조회한다")
    void findAllByCursor_withLimit() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);

        Card card1 = createAndSaveCard("카드1", "뒷면1", deck);
        Card card2 = createAndSaveCard("카드2", "뒷면2", deck);
        Card card3 = createAndSaveCard("카드3", "뒷면3", deck);
        Card card4 = createAndSaveCard("카드4", "뒷면4", deck);
        Card card5 = createAndSaveCard("카드5", "뒷면5", deck);

        em.flush();
        em.clear();

        // when
        List<Card> cards = cardRepository.findAllByCursor(
                deck.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                3
        );

        // then
        assertThat(cards).hasSize(3);
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 카드를 조회한다 - 내림차순")
    void findAllByCursor_withCursor_desc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);

        Card card1 = createAndSaveCard("카드1", "뒷면1", deck);
        Card card2 = createAndSaveCard("카드2", "뒷면2", deck);
        Card card3 = createAndSaveCard("카드3", "뒷면3", deck);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Card> firstPage = cardRepository.findAllByCursor(
                deck.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getFront()).isEqualTo("카드3");
        assertThat(firstPage.get(1).getFront()).isEqualTo("카드2");

        // when - 두 번째 페이지 조회
        Card lastCard = firstPage.get(firstPage.size() - 1);
        List<Card> secondPage = cardRepository.findAllByCursor(
                deck.getId(),
                null,
                "createdAt",
                "desc",
                lastCard.getCreatedAt().toString(),
                lastCard.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getFront()).isEqualTo("카드1");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 카드를 조회한다 - 오름차순")
    void findAllByCursor_withCursor_asc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("테스트 덱", user);

        Card card1 = createAndSaveCard("카드1", "뒷면1", deck);
        Card card2 = createAndSaveCard("카드2", "뒷면2", deck);
        Card card3 = createAndSaveCard("카드3", "뒷면3", deck);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Card> firstPage = cardRepository.findAllByCursor(
                deck.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getFront()).isEqualTo("카드1");
        assertThat(firstPage.get(1).getFront()).isEqualTo("카드2");

        // when - 두 번째 페이지 조회
        Card lastCard = firstPage.get(firstPage.size() - 1);
        List<Card> secondPage = cardRepository.findAllByCursor(
                deck.getId(),
                null,
                "createdAt",
                "asc",
                lastCard.getCreatedAt().toString(),
                lastCard.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getFront()).isEqualTo("카드3");
    }

    @Test
    @DisplayName("특정 덱의 모든 카드를 조회한다")
    void findAllByCursor_byDeckId() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck1 = createAndSaveDeck("덱1", user);
        Deck deck2 = createAndSaveDeck("덱2", user);

        Card card1 = createAndSaveCard("카드1", "뒷면1", deck1);
        Card card2 = createAndSaveCard("카드2", "뒷면2", deck1);
        Card card3 = createAndSaveCard("카드3", "뒷면3", deck2);

        em.flush();
        em.clear();

        // when
        List<Card> cards = cardRepository.findAllByCursor(
                deck1.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(cards).hasSize(2);
        assertThat(cards)
                .extracting(Card::getFront)
                .containsExactlyInAnyOrder("카드1", "카드2");
    }

    @Test
    @DisplayName("커서와 after가 null인 경우 첫 페이지를 조회한다")
    void findAllByCursor_withNullCursorAndAfter() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("커서 테스트", user);

        Card card1 = createAndSaveCard("카드1", "뒷면1", deck);
        Card card2 = createAndSaveCard("카드2", "뒷면2", deck);

        em.flush();
        em.clear();

        // when
        List<Card> cards = cardRepository.findAllByCursor(
                deck.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(cards).hasSize(2);
        assertThat(cards.get(0).getFront()).isEqualTo("카드1");
        assertThat(cards.get(1).getFront()).isEqualTo("카드2");
    }

    @Test
    @DisplayName("deckId가 null인 경우 모든 덱의 카드를 조회한다")
    void findAllByCursor_withNullDeckId() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck1 = createAndSaveDeck("덱A", user);
        Deck deck2 = createAndSaveDeck("덱B", user);

        Card card1 = createAndSaveCard("덱A 카드", "뒷면1", deck1);
        Card card2 = createAndSaveCard("덱B 카드", "뒷면2", deck2);

        em.flush();
        em.clear();

        // when
        List<Card> cards = cardRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(cards).hasSize(2);
    }

    @Test
    @DisplayName("키워드가 빈 문자열인 경우 모든 카드를 조회한다")
    void findAllByCursor_withEmptyKeyword() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("빈 키워드 테스트", user);

        Card card1 = createAndSaveCard("Hello", "안녕", deck);
        Card card2 = createAndSaveCard("World", "세계", deck);

        em.flush();
        em.clear();

        // when
        List<Card> cards = cardRepository.findAllByCursor(
                deck.getId(),
                "",
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(cards).hasSize(2);
    }

    @Test
    @DisplayName("키워드 검색 시 대소문자를 구분하지 않는다 - front")
    void findAllByCursor_withKeywordCaseInsensitive_front() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("대소문자 테스트", user);

        Card card1 = createAndSaveCard("Hello World", "안녕 세계", deck);
        Card card2 = createAndSaveCard("Goodbye", "안녕히", deck);

        em.flush();
        em.clear();

        // when
        List<Card> cards = cardRepository.findAllByCursor(
                deck.getId(),
                "hello",
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(cards).hasSize(1);
        assertThat(cards.get(0).getFront()).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("키워드 검색 시 대소문자를 구분하지 않는다 - back")
    void findAllByCursor_withKeywordCaseInsensitive_back() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("대소문자 테스트", user);

        Card card1 = createAndSaveCard("카드1", "IMPORTANT", deck);
        Card card2 = createAndSaveCard("카드2", "normal", deck);

        em.flush();
        em.clear();

        // when
        List<Card> cards = cardRepository.findAllByCursor(
                deck.getId(),
                "important",
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(cards).hasSize(1);
        assertThat(cards.get(0).getBack()).isEqualTo("IMPORTANT");
    }

    @Test
    @DisplayName("내림차순 정렬 시 커서 기반 페이지네이션이 정상 작동한다")
    void findAllByCursor_withDescOrderAndCursor() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("내림차순 커서 테스트", user);

        Card card1 = createAndSaveCard("카드1", "뒷면1", deck);
        Card card2 = createAndSaveCard("카드2", "뒷면2", deck);
        Card card3 = createAndSaveCard("카드3", "뒷면3", deck);

        em.flush();
        em.clear();

        // when - 첫 번째 페이지
        List<Card> firstPage = cardRepository.findAllByCursor(
                deck.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getFront()).isEqualTo("카드3");
        assertThat(firstPage.get(1).getFront()).isEqualTo("카드2");

        // when - 두 번째 페이지
        Card lastCard = firstPage.get(firstPage.size() - 1);
        List<Card> secondPage = cardRepository.findAllByCursor(
                deck.getId(),
                null,
                "createdAt",
                "desc",
                lastCard.getCreatedAt().toString(),
                lastCard.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getFront()).isEqualTo("카드1");
    }

    @Test
    @DisplayName("동일한 createdAt을 가진 카드들을 ID로 정렬한다 - 오름차순")
    void findAllByCursor_withSameCreatedAtOrderByIdAsc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("동일 시간 테스트", user);

        Card card1 = createAndSaveCard("카드A", "뒷면A", deck);
        Card card2 = createAndSaveCard("카드B", "뒷면B", deck);
        Card card3 = createAndSaveCard("카드C", "뒷면C", deck);

        em.flush();
        em.clear();

        // when
        List<Card> cards = cardRepository.findAllByCursor(
                deck.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(cards).hasSize(3);
        // ID 순으로 정렬되어야 함
        for (int i = 0; i < cards.size() - 1; i++) {
            assertThat(cards.get(i).getId()).isLessThan(cards.get(i + 1).getId());
        }
    }

    @Test
    @DisplayName("동일한 createdAt을 가진 카드들을 ID로 정렬한다 - 내림차순")
    void findAllByCursor_withSameCreatedAtOrderByIdDesc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("동일 시간 내림차순", user);

        Card card1 = createAndSaveCard("카드X", "뒷면X", deck);
        Card card2 = createAndSaveCard("카드Y", "뒷면Y", deck);
        Card card3 = createAndSaveCard("카드Z", "뒷면Z", deck);

        em.flush();
        em.clear();

        // when
        List<Card> cards = cardRepository.findAllByCursor(
                deck.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(cards).hasSize(3);
        // ID 역순으로 정렬되어야 함
        for (int i = 0; i < cards.size() - 1; i++) {
            assertThat(cards.get(i).getId()).isGreaterThan(cards.get(i + 1).getId());
        }
    }

    @Test
    @DisplayName("cursor가 null이지만 after가 있는 경우 - after는 무시된다")
    void findAllByCursor_withNullCursorButAfterExists() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("커서 null 테스트", user);

        Card card1 = createAndSaveCard("카드1", "뒷면1", deck);
        Card card2 = createAndSaveCard("카드2", "뒷면2", deck);

        em.flush();
        em.clear();

        // when
        List<Card> cards = cardRepository.findAllByCursor(
                deck.getId(),
                null,
                "createdAt",
                "asc",
                null,
                999L,
                10
        );

        // then - cursor가 null이므로 모든 데이터 조회
        assertThat(cards).hasSize(2);
    }

    @Test
    @DisplayName("cursor만 있고 after가 null인 경우 - 조건이 적용되지 않는다")
    void findAllByCursor_withCursorButNullAfter() {
        // given
        User user = createUser("testUser", "test@email.com");
        Deck deck = createAndSaveDeck("after null 테스트", user);

        Card card1 = createAndSaveCard("카드1", "뒷면1", deck);
        Card card2 = createAndSaveCard("카드2", "뒷면2", deck);

        em.flush();
        em.clear();

        // when
        List<Card> cards = cardRepository.findAllByCursor(
                deck.getId(),
                null,
                "createdAt",
                "asc",
                card1.getCreatedAt().toString(),
                null,
                10
        );

        // then - after가 null이므로 모든 데이터 조회
        assertThat(cards).hasSize(2);
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

    private Card createCard(String front, String back, Deck deck) {
        return Card.createCard(front, back, deck);
    }

    private Card createAndSaveCard(String front, String back, Deck deck) {
        Card card = Card.createCard(front, back, deck);
        return cardRepository.save(card);
    }
}

