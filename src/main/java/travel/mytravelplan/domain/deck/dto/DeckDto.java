package travel.mytravelplan.domain.deck.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class DeckDto {
    private Long id;
    private String name;

    @Builder
    private DeckDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
