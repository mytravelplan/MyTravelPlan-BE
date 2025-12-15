package travel.mytravelplan.domain.deck.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.entity.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Deck extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder(access = AccessLevel.PRIVATE)
    private Deck(String name, User user) {
        this.name = name;
        this.user = user;
    }

    public static Deck createDeck(String name, User user) {
        return Deck.builder()
                .name(name)
                .user(user)
                .build();
    }

    public void update(String name) {
        this.name = name;
    }
}
