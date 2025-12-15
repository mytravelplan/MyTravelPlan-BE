package travel.mytravelplan.domain.card.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.card.dto.CardCreateRequestDto;
import travel.mytravelplan.domain.card.dto.CardDto;
import travel.mytravelplan.domain.card.dto.CardUpdateRequestDto;
import travel.mytravelplan.domain.card.entity.Card;
import travel.mytravelplan.domain.card.enums.CardStatus;
import travel.mytravelplan.domain.card.exception.CardException;
import travel.mytravelplan.domain.card.mapper.CardMapper;
import travel.mytravelplan.domain.card.repository.CardRepository;
import travel.mytravelplan.domain.deck.entity.Deck;
import travel.mytravelplan.domain.deck.exception.DeckException;
import travel.mytravelplan.domain.deck.repository.DeckRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.support.ServiceTestSupport;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@DisplayName("카드 서비스 테스트")
class CardServiceTest extends ServiceTestSupport {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CardService cardService;

    private User user;
    private Deck deck;
    private Card card;
    private CardDto cardDto;
    private CardCreateRequestDto createRequestDto;
    private CardUpdateRequestDto updateRequestDto;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        deck = Deck.createDeck("테스트 덱", user);
        ReflectionTestUtils.setField(deck, "id", 1L);

        createRequestDto = CardCreateRequestDto.builder()
                .front("앞면")
                .back("뒷면")
                .build();

        updateRequestDto = CardUpdateRequestDto.builder()
                .front("수정된 앞면")
                .back("수정된 뒷면")
                .cardStatus(CardStatus.GOT_IT)
                .build();

        card = Card.createCard("앞면", "뒷면", deck);
        ReflectionTestUtils.setField(card, "id", 1L);

        cardDto = CardDto.builder()
                .id(1L)
                .front("앞면")
                .back("뒷면")
                .cardStatus(CardStatus.NONE)
                .build();
    }

    @Test
    @DisplayName("카드 생성 성공")
    void createCard_Success() {
        // given
        Long deckId = 1L;
        given(deckRepository.findById(eq(deckId))).willReturn(Optional.of(deck));
        given(cardRepository.save(any(Card.class))).willReturn(card);
        given(cardMapper.toDto(any(Card.class))).willReturn(cardDto);

        // when
        CardDto result = cardService.createCard(deckId, createRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(cardDto);

        then(deckRepository).should().findById(eq(deckId));
        then(cardRepository).should().save(any(Card.class));
        then(cardMapper).should().toDto(any(Card.class));
    }

    @Test
    @DisplayName("카드 생성 실패 - 존재하지 않는 덱")
    void createCard_DeckNotFound() {
        // given
        Long deckId = 999L;
        given(deckRepository.findById(eq(deckId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cardService.createCard(deckId, createRequestDto))
                .isInstanceOf(DeckException.class);

        then(deckRepository).should().findById(eq(deckId));
    }

    @Test
    @DisplayName("카드 조회 성공")
    void getCard_Success() {
        // given
        Long deckId = 1L;
        Long cardId = 1L;
        given(deckRepository.findById(eq(deckId))).willReturn(Optional.of(deck));
        given(cardRepository.findById(eq(cardId))).willReturn(Optional.of(card));
        given(cardMapper.toDto(eq(card))).willReturn(cardDto);

        // when
        CardDto result = cardService.getCard(deckId, cardId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(cardDto);

        then(deckRepository).should().findById(eq(deckId));
        then(cardRepository).should().findById(eq(cardId));
        then(cardMapper).should().toDto(eq(card));
    }

    @Test
    @DisplayName("카드 조회 실패 - 존재하지 않는 덱")
    void getCard_DeckNotFound() {
        // given
        Long deckId = 999L;
        Long cardId = 1L;
        given(deckRepository.findById(eq(deckId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cardService.getCard(deckId, cardId))
                .isInstanceOf(DeckException.class);

        then(deckRepository).should().findById(eq(deckId));
    }

    @Test
    @DisplayName("카드 조회 실패 - 존재하지 않는 카드")
    void getCard_CardNotFound() {
        // given
        Long deckId = 1L;
        Long cardId = 999L;
        given(deckRepository.findById(eq(deckId))).willReturn(Optional.of(deck));
        given(cardRepository.findById(eq(cardId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cardService.getCard(deckId, cardId))
                .isInstanceOf(CardException.class);

        then(deckRepository).should().findById(eq(deckId));
        then(cardRepository).should().findById(eq(cardId));
    }

    @Test
    @DisplayName("카드 조회 실패 - 카드가 덱에 속하지 않음")
    void getCard_CardNotBelongToDeck() {
        // given
        Long deckId = 1L;
        Long cardId = 1L;
        Deck anotherDeck = Deck.createDeck("다른 덱", user);
        ReflectionTestUtils.setField(anotherDeck, "id", 2L);
        Card anotherCard = Card.createCard("앞면", "뒷면", anotherDeck);

        given(deckRepository.findById(eq(deckId))).willReturn(Optional.of(deck));
        given(cardRepository.findById(eq(cardId))).willReturn(Optional.of(anotherCard));

        // when & then
        assertThatThrownBy(() -> cardService.getCard(deckId, cardId))
                .isInstanceOf(CardException.class);

        then(deckRepository).should().findById(eq(deckId));
        then(cardRepository).should().findById(eq(cardId));
    }

    @Test
    @DisplayName("카드 목록 조회 성공")
    void getCards_Success() {
        // given
        Long deckId = 1L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        Card card2 = Card.createCard("앞면2", "뒷면2", deck);
        ReflectionTestUtils.setField(card2, "id", 2L);

        List<Card> cards = Arrays.asList(card, card2);

        CardDto cardDto2 = CardDto.builder()
                .id(2L)
                .front("앞면2")
                .back("뒷면2")
                .cardStatus(CardStatus.NONE)
                .build();

        given(deckRepository.findById(eq(deckId))).willReturn(Optional.of(deck));
        given(cardRepository.findAllByCursor(eq(deckId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(cards);
        given(cardMapper.toDto(eq(card))).willReturn(cardDto);
        given(cardMapper.toDto(eq(card2))).willReturn(cardDto2);

        // when
        CursorPageResponseDto<CardDto> result = cardService.getCards(deckId, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isFalse();

        then(deckRepository).should().findById(eq(deckId));
        then(cardRepository).should().findAllByCursor(eq(deckId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(cardMapper).should().toDto(eq(card));
        then(cardMapper).should().toDto(eq(card2));
    }

    @Test
    @DisplayName("카드 목록 조회 성공 - hasNext true")
    void getCards_HasNext() {
        // given
        Long deckId = 1L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 2;

        Card testCard1 = Card.createCard("앞면1", "뒷면1", deck);
        ReflectionTestUtils.setField(testCard1, "id", 1L);
        ReflectionTestUtils.setField(testCard1, "createdAt", LocalDateTime.of(2024, 1, 1, 12, 0, 0));

        Card testCard2 = Card.createCard("앞면2", "뒷면2", deck);
        ReflectionTestUtils.setField(testCard2, "id", 2L);
        ReflectionTestUtils.setField(testCard2, "createdAt", LocalDateTime.of(2024, 1, 2, 12, 0, 0));

        Card testCard3 = Card.createCard("앞면3", "뒷면3", deck);
        ReflectionTestUtils.setField(testCard3, "id", 3L);
        ReflectionTestUtils.setField(testCard3, "createdAt", LocalDateTime.of(2024, 1, 3, 12, 0, 0));

        List<Card> cards = Arrays.asList(testCard1, testCard2, testCard3);

        CardDto cardDto1 = CardDto.builder().build();
        CardDto cardDto2 = CardDto.builder().build();

        given(deckRepository.findById(eq(deckId))).willReturn(Optional.of(deck));
        given(cardRepository.findAllByCursor(eq(deckId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(cards);
        given(cardMapper.toDto(eq(testCard1))).willReturn(cardDto1);
        given(cardMapper.toDto(eq(testCard2))).willReturn(cardDto2);

        // when
        CursorPageResponseDto<CardDto> result = cardService.getCards(deckId, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isNotNull();
        assertThat(result.getNextAfter()).isNotNull();

        then(deckRepository).should().findById(eq(deckId));
        then(cardRepository).should().findAllByCursor(eq(deckId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(cardMapper).should().toDto(eq(testCard1));
        then(cardMapper).should().toDto(eq(testCard2));
    }

    @Test
    @DisplayName("카드 목록 조회 성공 - 빈 리스트")
    void getCards_EmptyList() {
        // given
        Long deckId = 1L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        List<Card> emptyCards = List.of();

        given(deckRepository.findById(eq(deckId))).willReturn(Optional.of(deck));
        given(cardRepository.findAllByCursor(eq(deckId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(emptyCards);

        // when
        CursorPageResponseDto<CardDto> result = cardService.getCards(deckId, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getSize()).isEqualTo(0);
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();

        then(deckRepository).should().findById(eq(deckId));
        then(cardRepository).should().findAllByCursor(eq(deckId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("카드 목록 조회 실패 - 존재하지 않는 덱")
    void getCards_DeckNotFound() {
        // given
        Long deckId = 999L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        given(deckRepository.findById(eq(deckId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cardService.getCards(deckId, keyword, orderBy, direction, cursor, after, limit))
                .isInstanceOf(DeckException.class);

        then(deckRepository).should().findById(eq(deckId));
    }

    @Test
    @DisplayName("카드 수정 성공")
    void updateCard_Success() {
        // given
        Long deckId = 1L;
        Long cardId = 1L;
        CardDto updatedCardDto = CardDto.builder()
                .id(1L)
                .front("수정된 앞면")
                .back("수정된 뒷면")
                .cardStatus(CardStatus.GOT_IT)
                .build();

        given(deckRepository.findById(eq(deckId))).willReturn(Optional.of(deck));
        given(cardRepository.findById(eq(cardId))).willReturn(Optional.of(card));
        given(cardMapper.toDto(eq(card))).willReturn(updatedCardDto);

        // when
        CardDto result = cardService.updateCard(deckId, cardId, updateRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(updatedCardDto);

        then(deckRepository).should().findById(eq(deckId));
        then(cardRepository).should().findById(eq(cardId));
        then(cardMapper).should().toDto(eq(card));
    }

    @Test
    @DisplayName("카드 수정 실패 - 존재하지 않는 덱")
    void updateCard_DeckNotFound() {
        // given
        Long deckId = 999L;
        Long cardId = 1L;
        given(deckRepository.findById(eq(deckId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cardService.updateCard(deckId, cardId, updateRequestDto))
                .isInstanceOf(DeckException.class);

        then(deckRepository).should().findById(eq(deckId));
    }

    @Test
    @DisplayName("카드 수정 실패 - 존재하지 않는 카드")
    void updateCard_CardNotFound() {
        // given
        Long deckId = 1L;
        Long cardId = 999L;
        given(deckRepository.findById(eq(deckId))).willReturn(Optional.of(deck));
        given(cardRepository.findById(eq(cardId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cardService.updateCard(deckId, cardId, updateRequestDto))
                .isInstanceOf(CardException.class);

        then(deckRepository).should().findById(eq(deckId));
        then(cardRepository).should().findById(eq(cardId));
    }

    @Test
    @DisplayName("카드 수정 실패 - 카드가 덱에 속하지 않음")
    void updateCard_CardNotBelongToDeck() {
        // given
        Long deckId = 1L;
        Long cardId = 1L;
        Deck anotherDeck = Deck.createDeck("다른 덱", user);
        ReflectionTestUtils.setField(anotherDeck, "id", 2L);
        Card anotherCard = Card.createCard("앞면", "뒷면", anotherDeck);

        given(deckRepository.findById(eq(deckId))).willReturn(Optional.of(deck));
        given(cardRepository.findById(eq(cardId))).willReturn(Optional.of(anotherCard));

        // when & then
        assertThatThrownBy(() -> cardService.updateCard(deckId, cardId, updateRequestDto))
                .isInstanceOf(CardException.class);

        then(deckRepository).should().findById(eq(deckId));
        then(cardRepository).should().findById(eq(cardId));
    }

    @Test
    @DisplayName("카드 삭제 성공")
    void deleteCard_Success() {
        // given
        Long deckId = 1L;
        Long cardId = 1L;
        given(deckRepository.findById(eq(deckId))).willReturn(Optional.of(deck));
        given(cardRepository.findById(eq(cardId))).willReturn(Optional.of(card));

        // when
        cardService.deleteCard(deckId, cardId);

        // then
        then(deckRepository).should().findById(eq(deckId));
        then(cardRepository).should().findById(eq(cardId));
        then(cardRepository).should().delete(eq(card));
    }

    @Test
    @DisplayName("카드 삭제 실패 - 존재하지 않는 덱")
    void deleteCard_DeckNotFound() {
        // given
        Long deckId = 999L;
        Long cardId = 1L;
        given(deckRepository.findById(eq(deckId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cardService.deleteCard(deckId, cardId))
                .isInstanceOf(DeckException.class);

        then(deckRepository).should().findById(eq(deckId));
    }

    @Test
    @DisplayName("카드 삭제 실패 - 존재하지 않는 카드")
    void deleteCard_CardNotFound() {
        // given
        Long deckId = 1L;
        Long cardId = 999L;
        given(deckRepository.findById(eq(deckId))).willReturn(Optional.of(deck));
        given(cardRepository.findById(eq(cardId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cardService.deleteCard(deckId, cardId))
                .isInstanceOf(CardException.class);

        then(deckRepository).should().findById(eq(deckId));
        then(cardRepository).should().findById(eq(cardId));
    }

    @Test
    @DisplayName("카드 삭제 실패 - 카드가 덱에 속하지 않음")
    void deleteCard_CardNotBelongToDeck() {
        // given
        Long deckId = 1L;
        Long cardId = 1L;
        Deck anotherDeck = Deck.createDeck("다른 덱", user);
        ReflectionTestUtils.setField(anotherDeck, "id", 2L);
        Card anotherCard = Card.createCard("앞면", "뒷면", anotherDeck);

        given(deckRepository.findById(eq(deckId))).willReturn(Optional.of(deck));
        given(cardRepository.findById(eq(cardId))).willReturn(Optional.of(anotherCard));

        // when & then
        assertThatThrownBy(() -> cardService.deleteCard(deckId, cardId))
                .isInstanceOf(CardException.class);

        then(deckRepository).should().findById(eq(deckId));
        then(cardRepository).should().findById(eq(cardId));
    }
}