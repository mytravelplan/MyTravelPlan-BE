package travel.mytravelplan.domain.card.repository;

import travel.mytravelplan.domain.card.entity.Card;

import java.util.List;

public interface CardRepositoryCustom {
    List<Card> findAllByCursor(Long deckId, String keyword, String orderBy, String direction, String cursor, Long after, int limit);
}
