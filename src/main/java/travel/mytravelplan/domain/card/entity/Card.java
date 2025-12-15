package travel.mytravelplan.domain.card.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.card.enums.CardStatus;
import travel.mytravelplan.domain.deck.entity.Deck;
import travel.mytravelplan.global.common.entity.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Card extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String front;

    private String back;

    @Enumerated(EnumType.STRING)
    private CardStatus cardStatus = CardStatus.NONE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id")
    private Deck deck;

    @Builder(access = AccessLevel.PRIVATE)
    private Card(String front, String back, Deck deck) {
        this.front = front;
        this.back = back;
        this.deck = deck;
    }

    public static Card createCard(String front, String back, Deck deck) {
        return Card.builder()
                .front(front)
                .back(back)
                .deck(deck)
                .build();
    }

    public void update(String front, String back, CardStatus cardStatus) {
        this.front = front;
        this.back = back;
        this.cardStatus = cardStatus;
    }
}
