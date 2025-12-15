package travel.mytravelplan.domain.card.dto;

import lombok.Builder;
import lombok.Getter;
import travel.mytravelplan.domain.card.enums.CardStatus;

@Getter
public class CardDto {
    private Long id;
    private String front;
    private String back;
    private CardStatus cardStatus;

    @Builder
    private CardDto(Long id, String front, String back, CardStatus cardStatus) {
        this.id = id;
        this.front = front;
        this.back = back;
        this.cardStatus = cardStatus;
    }
}
