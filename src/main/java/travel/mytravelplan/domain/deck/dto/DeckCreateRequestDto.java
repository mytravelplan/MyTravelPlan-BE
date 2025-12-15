package travel.mytravelplan.domain.deck.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeckCreateRequestDto {
    private String name;

    @Builder
    private DeckCreateRequestDto(String name) {
        this.name = name;
    }
}
