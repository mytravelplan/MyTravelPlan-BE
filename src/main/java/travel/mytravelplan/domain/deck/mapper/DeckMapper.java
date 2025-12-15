package travel.mytravelplan.domain.deck.mapper;

import org.mapstruct.Mapper;
import travel.mytravelplan.domain.deck.dto.DeckDto;
import travel.mytravelplan.domain.deck.entity.Deck;

@Mapper(componentModel = "spring")
public interface DeckMapper {
    DeckDto toDto(Deck deck);
}
