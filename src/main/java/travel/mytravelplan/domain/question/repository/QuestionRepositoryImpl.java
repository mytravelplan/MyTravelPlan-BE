package travel.mytravelplan.domain.question.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import travel.mytravelplan.domain.question.entity.QQuestion;
import travel.mytravelplan.domain.question.entity.Question;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class QuestionRepositoryImpl implements QuestionRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QQuestion question = QQuestion.question;

    @Override
    public List<Question> findAllByCursor(Long quizId, String orderBy, String direction, String cursor, Long after, int limit) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if(quizId != null) {
            booleanBuilder.and(question.quiz.id.eq(quizId));
        }

        if (cursor != null && after != null) {
            if (orderBy.equals("createdAt")) {
                LocalDateTime createdAt = LocalDateTime.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(question.createdAt.gt(createdAt)
                            .or(question.createdAt.eq(createdAt).and(question.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(question.createdAt.lt(createdAt)
                            .or(question.createdAt.eq(createdAt).and(question.id.lt(after))));
                }
            }
        }

        OrderSpecifier<?> firstOrder = null;

        if (orderBy.equals("createdAt")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? question.createdAt.asc() : question.createdAt.desc();
        }

        OrderSpecifier<?> secondOrder = direction.equalsIgnoreCase("asc") ? question.id.asc() : question.id.desc();

        return queryFactory.selectFrom(question)
                .where(booleanBuilder)
                .orderBy(firstOrder, secondOrder)
                .limit(limit)
                .fetch();
    }
}
