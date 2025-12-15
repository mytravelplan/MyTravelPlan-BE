package travel.mytravelplan.domain.quiz.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import travel.mytravelplan.domain.quiz.entity.QQuiz;
import travel.mytravelplan.domain.quiz.entity.Quiz;
import travel.mytravelplan.domain.quiz.enums.QuizType;
import travel.mytravelplan.domain.user.entity.QUser;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class QuizRepositoryImpl implements QuizRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QQuiz quiz = QQuiz.quiz;

    @Override
    public List<Quiz> findAllByCursor(String username, QuizType quizType, String orderBy, String direction, String cursor, Long after, int limit) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        
        if (username != null) {
            booleanBuilder.and(quiz.user.username.eq(username));
        }
        
        if (quizType != null) {
            booleanBuilder.and(quiz.quizType.eq(quizType));
        }

        if(cursor != null && after != null) {
            if (orderBy.equals("createdAt")) {
                LocalDateTime createdAt = LocalDateTime.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(quiz.createdAt.gt(createdAt)
                            .or(quiz.createdAt.eq(createdAt).and(quiz.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(quiz.createdAt.lt(createdAt)
                            .or(quiz.createdAt.eq(createdAt).and(quiz.id.lt(after))));
                }
            }
        }

        OrderSpecifier<?> firstOrder = null;

        if (orderBy.equals("createdAt")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? quiz.createdAt.asc() : quiz.createdAt.desc();
        }

        OrderSpecifier<?> secondOrder = direction.equalsIgnoreCase("asc") ? quiz.id.asc() : quiz.id.desc();

        return queryFactory
                .selectFrom(quiz)
                .where(booleanBuilder)
                .orderBy(firstOrder, secondOrder)
                .limit(limit)
                .fetch();
    }
}