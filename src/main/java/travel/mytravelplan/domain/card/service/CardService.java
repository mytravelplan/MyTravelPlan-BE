package travel.mytravelplan.domain.card.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.card.dto.CardCreateRequestDto;
import travel.mytravelplan.domain.card.dto.CardDto;
import travel.mytravelplan.domain.card.dto.CardUpdateRequestDto;
import travel.mytravelplan.domain.card.entity.Card;
import travel.mytravelplan.domain.card.exception.CardException;
import travel.mytravelplan.domain.card.mapper.CardMapper;
import travel.mytravelplan.domain.card.repository.CardRepository;
import travel.mytravelplan.domain.deck.entity.Deck;
import travel.mytravelplan.domain.deck.exception.DeckException;
import travel.mytravelplan.domain.deck.repository.DeckRepository;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.error.code.CardErrorCode;
import travel.mytravelplan.global.error.code.DeckErrorCode;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;
    private final CardMapper cardMapper;

    @Transactional
    public CardDto createCard(Long deckId, CardCreateRequestDto cardCreateRequestDto) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new DeckException(DeckErrorCode.DECK_NOT_FOUND));

        Card card = Card.createCard(cardCreateRequestDto.getFront(), cardCreateRequestDto.getBack(), deck);

        validateCardBelongsToDeck(card, deck);

        cardRepository.save(card);
        return cardMapper.toDto(card);
    }

    public CardDto getCard(Long deckId, Long cardId) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new DeckException(DeckErrorCode.DECK_NOT_FOUND));

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardException(CardErrorCode.CARD_NOT_FOUND));

        validateCardBelongsToDeck(card, deck);

        return cardMapper.toDto(card);
    }

    public CursorPageResponseDto<CardDto> getCards(Long deckId, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new DeckException(DeckErrorCode.DECK_NOT_FOUND));

        List<Card> cards = cardRepository.findAllByCursor(deck.getId(), keyword, orderBy, direction, cursor, after, limit + 1);

        boolean hasNext = cards.size() > limit;

        List<Card> pagedCards = hasNext ? cards.subList(0, limit) : cards;

        List<CardDto> cardDtos = pagedCards.stream()
                .map(cardMapper::toDto)
                .toList();

        String nextCursor = null;
        Long nextAfter = null;

        if (hasNext) {
            Card lastCard = pagedCards.getLast();

            if (orderBy.equals("createdAt")) {
                nextCursor = lastCard.getCreatedAt().toString();
            }

            nextAfter = lastCard.getId();
        }

        return CursorPageResponseDto.<CardDto>builder()
                .content(cardDtos)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(cardDtos.size())
                .hasNext(hasNext)
                .build();
    }

    @Transactional
    public CardDto updateCard(Long deckId, Long cardId, CardUpdateRequestDto cardUpdateRequestDto) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new DeckException(DeckErrorCode.DECK_NOT_FOUND));

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardException(CardErrorCode.CARD_NOT_FOUND));

        validateCardBelongsToDeck(card, deck);

        card.update(cardUpdateRequestDto.getFront(), cardUpdateRequestDto.getBack(), cardUpdateRequestDto.getCardStatus());

        return cardMapper.toDto(card);
    }

    @Transactional
    public void deleteCard(Long deckId, Long cardId) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new DeckException(DeckErrorCode.DECK_NOT_FOUND));

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardException(CardErrorCode.CARD_NOT_FOUND));

        validateCardBelongsToDeck(card, deck);

        cardRepository.delete(card);
    }

    private void validateCardBelongsToDeck(Card card, Deck deck) {
        if (!card.getDeck().equals(deck)) {
            throw new CardException(CardErrorCode.CARD_NOT_BELONG_TO_DECK);
        }
    }
}
