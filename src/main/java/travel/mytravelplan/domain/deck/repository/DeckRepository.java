package travel.mytravelplan.domain.deck.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.deck.entity.Deck;

public interface DeckRepository extends JpaRepository<Deck, Long>, DeckRepositoryCustom {
}
