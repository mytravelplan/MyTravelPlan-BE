package travel.mytravelplan.domain.question.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.question.dto.QuestionDto;
import travel.mytravelplan.domain.question.entity.Question;
import travel.mytravelplan.domain.question.mapper.QuestionMapper;
import travel.mytravelplan.domain.question.repository.QuestionRepository;
import travel.mytravelplan.domain.quiz.entity.Quiz;
import travel.mytravelplan.domain.quiz.exception.QuizException;
import travel.mytravelplan.domain.quiz.repository.QuizRepository;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.error.code.QuizErrorCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class QuestionService {
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;

    public CursorPageResponseDto<QuestionDto> getQuestions(Long quizId, boolean shuffle, String orderBy, String direction, String cursor, Long after, int limit) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizException(QuizErrorCode.QUIZ_NOT_FOUND));

        List<Question> questions = questionRepository.findAllByCursor(quiz.getId(), orderBy, direction, cursor, after, limit + 1);

        boolean hasNext = questions.size() > limit;

        List<Question> pagedQuestions = hasNext ? questions.subList(0, limit) : questions;

        List<QuestionDto> questionDtos = pagedQuestions.stream()
                .map(questionMapper::toDto)
                .toList();

        String nextCursor = null;
        Long nextAfter = null;

        if (hasNext) {
            Question lastQuestion = pagedQuestions.getLast();

            if (orderBy.equals("createdAt")) {
                nextCursor = lastQuestion.getCreatedAt().toString();
            }

            nextAfter = lastQuestion.getId();
        }

        if (shuffle) {
            Collections.shuffle(new ArrayList<>(questions));
        }

        return CursorPageResponseDto.<QuestionDto>builder()
                .content(questionDtos)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(questionDtos.size())
                .hasNext(hasNext)
                .build();
    }
}