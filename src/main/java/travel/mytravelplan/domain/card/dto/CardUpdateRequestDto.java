package travel.mytravelplan.domain.card.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.card.enums.CardStatus;

@Getter
@NoArgsConstructor
public class CardUpdateRequestDto {
    private String front;
    private String back;
    private CardStatus cardStatus;

    @Builder
    private CardUpdateRequestDto(String front, String back, CardStatus cardStatus) {
        this.front = front;
        this.back = back;
        this.cardStatus = cardStatus;
    }
}
