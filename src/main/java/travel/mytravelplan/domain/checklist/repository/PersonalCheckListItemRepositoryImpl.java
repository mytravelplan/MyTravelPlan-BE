package travel.mytravelplan.domain.checklist.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import travel.mytravelplan.domain.checklist.entity.*;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class PersonalCheckListItemRepositoryImpl implements PersonalCheckListItemRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<PersonalCheckListItem> findAllByCursor(Long checkListId, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        QPersonalCheckListItem personalCheckListItem = QPersonalCheckListItem.personalCheckListItem;

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if(checkListId != null) {
            booleanBuilder.and(personalCheckListItem.personalCheckList.id.eq(checkListId));
        }

        if(StringUtils.hasText(keyword)) {
            booleanBuilder.and(personalCheckListItem.text.containsIgnoreCase(keyword));
        }

        if(cursor != null && after != null) {
            if(orderBy.equals("createdAt")) {
                LocalDateTime createdAt = LocalDateTime.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(personalCheckListItem.createdAt.gt(createdAt)
                            .or(personalCheckListItem.createdAt.eq(createdAt).and(personalCheckListItem.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(personalCheckListItem.createdAt.lt(createdAt)
                            .or(personalCheckListItem.createdAt.eq(createdAt).and(personalCheckListItem.id.lt(after))));
                }
            }
        }

        OrderSpecifier<?> firstOrder = null;

        if (orderBy.equals("createdAt")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? personalCheckListItem.createdAt.asc() : personalCheckListItem.createdAt.desc();
        }

        OrderSpecifier<?> secondOrder = direction.equalsIgnoreCase("asc") ? personalCheckListItem.id.asc() : personalCheckListItem.id.desc();

        return queryFactory
                .selectFrom(personalCheckListItem)
                .where(booleanBuilder)
                .orderBy(firstOrder, secondOrder)
                .limit(limit)
                .fetch();
    }
}
