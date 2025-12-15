package travel.mytravelplan.domain.card.mapper;

import org.mapstruct.Mapper;
import travel.mytravelplan.domain.card.dto.CardDto;
import travel.mytravelplan.domain.card.entity.Card;

@Mapper(componentModel = "spring")
public interface CardMapper {
    CardDto toDto(Card card);
}
