package travel.mytravelplan.domain.deck.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import travel.mytravelplan.domain.deck.entity.Deck;
import travel.mytravelplan.domain.deck.entity.QDeck;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class DeckRepositoryImpl implements DeckRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QDeck deck = QDeck.deck;

    @Override
    public List<Deck> findAllByCursor(String username, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if(StringUtils.hasText(username)) {
            booleanBuilder.and(deck.user.username.eq(username));
        }

        if(StringUtils.hasText(keyword)) {
            booleanBuilder.and(deck.name.containsIgnoreCase(keyword));
        }

        if(cursor != null && after != null) {
            if (orderBy.equals("createdAt")) {
                LocalDateTime createdAt = LocalDateTime.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(deck.createdAt.gt(createdAt)
                            .or(deck.createdAt.eq(createdAt).and(deck.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(deck.createdAt.lt(createdAt)
                            .or(deck.createdAt.eq(createdAt).and(deck.id.lt(after))));
                }
            }
        }

        OrderSpecifier<?> firstOrder = null;

        if (orderBy.equals("createdAt")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? deck.createdAt.asc() : deck.createdAt.desc();
        }

        OrderSpecifier<?> secondOrder = direction.equalsIgnoreCase("asc") ? deck.id.asc() : deck.id.desc();

        return queryFactory
                .selectFrom(deck)
                .where(booleanBuilder)
                .orderBy(firstOrder, secondOrder)
                .limit(limit)
                .fetch();
    }
}
