package travel.mytravelplan.domain.inquiry.repsotiroy;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import travel.mytravelplan.domain.inquiry.entity.InquiryReply;
import travel.mytravelplan.domain.inquiry.entity.QInquiryReply;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class InquiryReplyRepositoryImpl implements InquiryReplyRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QInquiryReply inquiryReply = QInquiryReply.inquiryReply;

    @Override
    public List<InquiryReply> findAllByCursor(Long inquiryId, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if(inquiryId != null) {
            booleanBuilder.and(inquiryReply.inquiry.id.eq(inquiryId));
        }

        if(keyword != null && !keyword.isEmpty()) {
            booleanBuilder.and(inquiryReply.content.containsIgnoreCase(keyword)
                    .or(inquiryReply.content.containsIgnoreCase(keyword)));
        }

        if(cursor != null && after != null) {
            if (orderBy.equals("createdAt")) {
                LocalDateTime createdAt = LocalDateTime.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(inquiryReply.createdAt.gt(createdAt)
                            .or(inquiryReply.createdAt.eq(createdAt).and(inquiryReply.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(inquiryReply.createdAt.lt(createdAt)
                            .or(inquiryReply.createdAt.eq(createdAt).and(inquiryReply.id.lt(after))));
                }
            }
        }

        OrderSpecifier<?> firstOrder = null;

        if (orderBy.equalsIgnoreCase("createdAt")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? inquiryReply.createdAt.asc() : inquiryReply.createdAt.desc();
        }

        OrderSpecifier<?> secondOrder = direction.equalsIgnoreCase("asc") ? inquiryReply.id.asc() : inquiryReply.id.desc();

        return queryFactory.selectFrom(inquiryReply)
                .where(booleanBuilder)
                .orderBy(firstOrder, secondOrder)
                .limit(limit)
                .fetch();
    }
}
