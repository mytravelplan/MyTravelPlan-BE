package travel.mytravelplan.domain.card.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import travel.mytravelplan.domain.card.entity.Card;
import travel.mytravelplan.domain.card.entity.QCard;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class CardRepositoryImpl implements CardRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QCard card = QCard.card;

    @Override
    public List<Card> findAllByCursor(Long deckId, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (deckId != null) {
            booleanBuilder.and(card.deck.id.eq(deckId));
        }

        if(StringUtils.hasText(keyword)) {
            booleanBuilder.and(card.front.containsIgnoreCase(keyword)
                    .or(card.back.containsIgnoreCase(keyword)));
        }

        if (cursor != null && after != null) {
            if (orderBy.equals("createdAt")) {
                LocalDateTime createdAt = LocalDateTime.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(card.createdAt.gt(createdAt)
                            .or(card.createdAt.eq(createdAt).and(card.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(card.createdAt.lt(createdAt)
                            .or(card.createdAt.eq(createdAt).and(card.id.lt(after))));
                }
            }
        }

        OrderSpecifier<?> firstOrder = null;

        if (orderBy.equals("createdAt")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? card.createdAt.asc() : card.createdAt.desc();
        }

        OrderSpecifier<?> secondOrder = direction.equalsIgnoreCase("asc") ? card.id.asc() : card.id.desc();

        return queryFactory
                .selectFrom(card)
                .where(booleanBuilder)
                .orderBy(firstOrder, secondOrder)
                .limit(limit)
                .fetch();
    }
}
