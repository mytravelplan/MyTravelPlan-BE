package travel.mytravelplan.domain.card.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.card.entity.Card;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long>, CardRepositoryCustom {
    List<Card> findAllByDeckIdIn(List<Long> deckIds);
}
