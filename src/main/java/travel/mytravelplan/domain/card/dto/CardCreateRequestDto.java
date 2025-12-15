package travel.mytravelplan.domain.card.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CardCreateRequestDto {
    private String front;
    private String back;

    @Builder
    private CardCreateRequestDto(String front, String back) {
        this.front = front;
        this.back = back;
    }
}
