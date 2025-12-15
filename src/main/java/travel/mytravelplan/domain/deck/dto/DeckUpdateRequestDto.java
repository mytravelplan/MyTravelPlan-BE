package travel.mytravelplan.domain.deck.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeckUpdateRequestDto {
    private String name;

    @Builder
    private DeckUpdateRequestDto(String name) {
        this.name = name;
    }
}
