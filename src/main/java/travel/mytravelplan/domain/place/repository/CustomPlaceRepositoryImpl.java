package travel.mytravelplan.domain.place.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import travel.mytravelplan.domain.place.entity.CustomPlace;
import travel.mytravelplan.domain.place.entity.QCustomPlace;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class CustomPlaceRepositoryImpl implements CustomPlaceRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QCustomPlace customPlace = QCustomPlace.customPlace;
    @Override
    public List<CustomPlace> findAllByCursor(String username, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        
        if(username != null) {
            booleanBuilder.and(customPlace.user.username.eq(username));
        }

        if(cursor != null && after != null) {
            if (orderBy.equals("createdAt")) {
                LocalDateTime createdAt = LocalDateTime.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(customPlace.createdAt.gt(createdAt)
                            .or(customPlace.createdAt.eq(createdAt).and(customPlace.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(customPlace.createdAt.lt(createdAt)
                            .or(customPlace.createdAt.eq(createdAt).and(customPlace.id.lt(after))));
                }
            }
        }

        OrderSpecifier<?> firstOrder = null;

        if (orderBy.equals("createdAt")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? customPlace.createdAt.asc() : customPlace.createdAt.desc();
        }

        OrderSpecifier<?> secondOrder = direction.equalsIgnoreCase("asc") ? customPlace.id.asc() : customPlace.id.desc();

        return queryFactory
                .selectFrom(customPlace)
                .where(booleanBuilder)
                .orderBy(firstOrder, secondOrder)
                .limit(limit)
                .fetch();
    }
}
