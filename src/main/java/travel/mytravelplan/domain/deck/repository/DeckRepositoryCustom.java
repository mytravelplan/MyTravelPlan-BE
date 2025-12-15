package travel.mytravelplan.domain.deck.repository;

import travel.mytravelplan.domain.deck.entity.Deck;

import java.util.List;

public interface DeckRepositoryCustom {
    List<Deck> findAllByCursor(String username, String keyword, String orderBy, String direction, String cursor, Long after, int limit);
}
