package travel.mytravelplan.domain.inquiry.repsotiroy;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import travel.mytravelplan.domain.inquiry.entity.Inquiry;
import travel.mytravelplan.domain.inquiry.entity.QInquiry;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class InquiryRepositoryImpl implements InquiryRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QInquiry inquiry = QInquiry.inquiry;

    @Override
    public List<Inquiry> findAllByCursor(Long productId, String keyword, boolean secretOnly, boolean answerOnly, String orderBy, String direction, String cursor, Long after, int limit) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if(productId != null) {
            booleanBuilder.and(inquiry.product.id.eq(productId));
        }

        if(keyword != null && !keyword.isEmpty()) {
            booleanBuilder.and(inquiry.title.containsIgnoreCase(keyword)
                    .or(inquiry.content.containsIgnoreCase(keyword)));
        }

        if(secretOnly) {
            booleanBuilder.and(inquiry.secret.isTrue());
        }

        if(answerOnly) {
            booleanBuilder.and(inquiry.inquiryReplies.isNotEmpty());
        }

        if(cursor != null && after != null) {
            if (orderBy.equals("createdAt")) {
                LocalDateTime createdAt = LocalDateTime.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(inquiry.createdAt.gt(createdAt)
                            .or(inquiry.createdAt.eq(createdAt).and(inquiry.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(inquiry.createdAt.lt(createdAt)
                            .or(inquiry.createdAt.eq(createdAt).and(inquiry.id.lt(after))));
                }
            }
        }

        OrderSpecifier<?> firstOrder = null;

        if (orderBy.equalsIgnoreCase("createdAt")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? inquiry.createdAt.asc() : inquiry.createdAt.desc();
        }

        OrderSpecifier<?> secondOrder = direction.equalsIgnoreCase("asc") ? inquiry.id.asc() : inquiry.id.desc();

        return queryFactory.selectFrom(inquiry)
                .where(booleanBuilder)
                .orderBy(firstOrder, secondOrder)
                .limit(limit)
                .fetch();
    }
}
