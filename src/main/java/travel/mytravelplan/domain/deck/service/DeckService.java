package travel.mytravelplan.domain.deck.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.deck.dto.DeckCreateRequestDto;
import travel.mytravelplan.domain.deck.dto.DeckDto;
import travel.mytravelplan.domain.deck.dto.DeckUpdateRequestDto;
import travel.mytravelplan.domain.deck.entity.Deck;
import travel.mytravelplan.domain.deck.exception.DeckException;
import travel.mytravelplan.domain.deck.mapper.DeckMapper;
import travel.mytravelplan.domain.deck.repository.DeckRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.error.code.DeckErrorCode;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DeckService {
    private final DeckRepository deckRepository;
    private final DeckMapper deckMapper;

    @Transactional
    public DeckDto createDeck(User currentUser, DeckCreateRequestDto deckCreateRequestDto) {
        Deck deck = Deck.createDeck(deckCreateRequestDto.getName(), currentUser);

        deckRepository.save(deck);
        return deckMapper.toDto(deck);
    }

    public DeckDto getDeck(Long deckId) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new DeckException(DeckErrorCode.DECK_NOT_FOUND));
        return deckMapper.toDto(deck);
    }

    public CursorPageResponseDto<DeckDto> getUserDecks(String username, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        List<Deck> decks = deckRepository.findAllByCursor(username, keyword, orderBy, direction, cursor, after, limit + 1);

        boolean hasNext = decks.size() > limit;

        List<Deck> pagedDecks = hasNext ? decks.subList(0, limit) : decks;

        List<DeckDto> deckDtos = pagedDecks.stream()
                .map(deckMapper::toDto)
                .toList();

        String nextCursor = null;
        Long nextAfter = null;

        if (hasNext) {
            Deck lastDeck = pagedDecks.getLast();

            if (orderBy.equals("createdAt")) {
                nextCursor = lastDeck.getCreatedAt().toString();
            }

            nextAfter = lastDeck.getId();
        }

        return CursorPageResponseDto.<DeckDto>builder()
                .content(deckDtos)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(deckDtos.size())
                .hasNext(hasNext)
                .build();
    }

    @Transactional
    public DeckDto updateDeck(Long deckId, DeckUpdateRequestDto deckUpdateRequestDto) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new DeckException(DeckErrorCode.DECK_NOT_FOUND));

        deck.update(deckUpdateRequestDto.getName());

        return deckMapper.toDto(deck);
    }

    @Transactional
    public void deleteDeck(Long deckId) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new DeckException(DeckErrorCode.DECK_NOT_FOUND));
        deckRepository.delete(deck);
    }
}
