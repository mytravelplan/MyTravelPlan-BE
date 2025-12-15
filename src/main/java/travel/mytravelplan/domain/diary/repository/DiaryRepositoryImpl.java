package travel.mytravelplan.domain.diary.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import travel.mytravelplan.domain.diary.entity.Diary;
import travel.mytravelplan.domain.diary.entity.QDiary;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class DiaryRepositoryImpl implements DiaryRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Diary> findAllByCursor(Long tripId, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        QDiary diary = QDiary.diary;

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (tripId != null) {
            booleanBuilder.and(diary.trip.id.eq(tripId));
        }

        if(StringUtils.hasText(keyword)) {
            booleanBuilder.and(diary.title.containsIgnoreCase(keyword)
                    .or(diary.content.containsIgnoreCase(keyword)));
        }

        if(cursor != null && after != null) {
            if(orderBy.equals("createdAt")) {
                LocalDateTime createdAt = LocalDateTime.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(diary.createdAt.gt(createdAt)
                            .or(diary.createdAt.eq(createdAt).and(diary.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(diary.createdAt.lt(createdAt)
                            .or(diary.createdAt.eq(createdAt).and(diary.id.gt(after))));
                }
            }
        }

        OrderSpecifier<?> firstOrder = null;

        if (orderBy.equals("createdAt")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? diary.createdAt.asc() : diary.createdAt.desc();
        }

        OrderSpecifier<?> secondOrder = direction.equalsIgnoreCase("asc") ? diary.id.asc() : diary.id.desc();

        return queryFactory
                .selectFrom(diary)
                .where(booleanBuilder)
                .orderBy(firstOrder, secondOrder)
                .limit(limit)
                .fetch();
    }
}
