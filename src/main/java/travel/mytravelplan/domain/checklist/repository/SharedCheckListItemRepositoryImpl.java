package travel.mytravelplan.domain.checklist.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import travel.mytravelplan.domain.checklist.entity.QSharedCheckListItem;
import travel.mytravelplan.domain.checklist.entity.SharedCheckListItem;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class SharedCheckListItemRepositoryImpl implements SharedCheckListItemRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<SharedCheckListItem> findAllByCursor(Long checklistId, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        QSharedCheckListItem sharedCheckListItem = QSharedCheckListItem.sharedCheckListItem;

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if(checklistId != null) {
            booleanBuilder.and(sharedCheckListItem.sharedCheckList.id.eq(checklistId));
        }

        if(StringUtils.hasText(keyword)) {
            booleanBuilder.and(sharedCheckListItem.text.containsIgnoreCase(keyword));
        }

        if(cursor != null && after != null) {
            if(orderBy.equals("createdAt")) {
                LocalDateTime createdAt = LocalDateTime.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(sharedCheckListItem.createdAt.gt(createdAt)
                            .or(sharedCheckListItem.createdAt.eq(createdAt).and(sharedCheckListItem.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(sharedCheckListItem.createdAt.lt(createdAt)
                            .or(sharedCheckListItem.createdAt.eq(createdAt).and(sharedCheckListItem.id.lt(after))));
                }
            }
        }

        OrderSpecifier<?> firstOrder = null;

        if (orderBy.equals("createdAt")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? sharedCheckListItem.createdAt.asc() : sharedCheckListItem.createdAt.desc();
        }

        OrderSpecifier<?> secondOrder = direction.equalsIgnoreCase("asc") ? sharedCheckListItem.id.asc() : sharedCheckListItem.id.desc();

        return queryFactory
                .selectFrom(sharedCheckListItem)
                .where(booleanBuilder)
                .orderBy(firstOrder, secondOrder)
                .limit(limit)
                .fetch();
    }
}
