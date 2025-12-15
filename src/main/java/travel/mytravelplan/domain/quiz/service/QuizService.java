package travel.mytravelplan.domain.quiz.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.answer.dto.*;
import travel.mytravelplan.domain.answer.entity.Answer;
import travel.mytravelplan.domain.answer.entity.DictationAnswer;
import travel.mytravelplan.domain.answer.entity.MultipleChoiceAnswer;
import travel.mytravelplan.domain.answer.entity.SelfReviewAnswer;
import travel.mytravelplan.domain.answer.enums.GRADE;
import travel.mytravelplan.domain.answer.enums.SelfReviewStatus;
import travel.mytravelplan.domain.card.entity.Card;
import travel.mytravelplan.domain.card.repository.CardRepository;
import travel.mytravelplan.domain.question.dto.ChoiceDto;
import travel.mytravelplan.domain.question.dto.QuestionResultDto;
import travel.mytravelplan.domain.question.entity.*;
import travel.mytravelplan.domain.question.exception.QuestionException;
import travel.mytravelplan.domain.question.mapper.ChoiceRepository;
import travel.mytravelplan.domain.question.repository.QuestionRepository;
import travel.mytravelplan.domain.quiz.dto.*;
import travel.mytravelplan.domain.quiz.entity.Quiz;
import travel.mytravelplan.domain.quiz.enums.QuizType;
import travel.mytravelplan.domain.quiz.exception.QuizException;
import travel.mytravelplan.domain.quiz.mapper.QuizMapper;
import travel.mytravelplan.domain.quiz.repository.QuizRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.error.code.QuestionErrorCode;
import travel.mytravelplan.global.error.code.QuizErrorCode;

import java.time.LocalDateTime;
import java.util.*;

import static travel.mytravelplan.domain.quiz.enums.QuizType.SELF_REVIEW;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final QuizMapper quizMapper;
    private final QuestionRepository questionRepository;
    private final CardRepository cardRepository;
    private final ChoiceRepository choiceRepository;

    @Transactional
    public QuizDto startQuiz(User currentUser, QuizCreateRequestDto quizCreateRequestDto) {
        Quiz quiz = Quiz.createQuiz(quizCreateRequestDto.getQuizType(), currentUser);

        List<Card> cards = cardRepository.findAllByDeckIdIn(quizCreateRequestDto.getDeckIds());

        quizRepository.save(quiz);

        if (quizCreateRequestDto.getQuizType() == SELF_REVIEW) {
            List<SelfReview> selfReview = cards.stream()
                    .map(card -> SelfReview.createSelfReview(quiz, card))
                    .toList();

            selfReview.forEach(quiz::addQuestion);

            questionRepository.saveAll(selfReview);
        } else if (quizCreateRequestDto.getQuizType() == QuizType.DICTATION) {
            List<Dictation> dictation = cards.stream()
                    .map(card -> Dictation.createDictation(quiz, card))
                    .toList();

            dictation.forEach(quiz::addQuestion);

            questionRepository.saveAll(dictation);
        } else if (quizCreateRequestDto.getQuizType() == QuizType.MULTIPLE_CHOICE) {
            List<MultipleChoice> multipleChoices = cards.stream()
                    .map(card -> MultipleChoice.createMultipleChoice(quiz, card))
                    .toList();

            multipleChoices.forEach(quiz::addQuestion);

            questionRepository.saveAll(multipleChoices);

            List<Choice> choices = new ArrayList<>();
            for (MultipleChoice multiChoice : multipleChoices) {
                // 정답 선지 추가
                Card answerCard = multiChoice.getCard();
                Choice answerChoice = Choice.createChoice(answerCard.getBack(),true);
                multiChoice.addChoice(answerChoice);
                choices.add(answerChoice);

                // 오답 선지 추가
                List<Card> wrongCards = cards.stream()
                        .filter(card -> !card.getId().equals(answerCard.getId()))
                        .toList();

                for (Card wrongCard : wrongCards) {
                    Choice choice = Choice.createChoice(wrongCard.getBack(),false);
                    multiChoice.addChoice(choice);
                    choices.add(choice);
                }
            }
            choiceRepository.saveAll(choices);
        }

        return quizMapper.toDto(quiz);
    }

    @Transactional
    public QuizDto finishQuiz(Long quizId, List<AnswerRequestDto> answerRequestDtos) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizException(QuizErrorCode.QUIZ_NOT_FOUND));

        if (quiz.getFinishedAt() != null) {
            throw new QuizException(QuizErrorCode.QUIZ_ALREADY_FINISHED);
        }

        List<Question> questions = quiz.getQuestions();

        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);

            Answer answer;

            if (quiz.getQuizType() == SELF_REVIEW) {
                answer = SelfReviewAnswer.createSelfReview();
            } else if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                MultipleChoiceAnswerRequestDto multipleChoiceAnswerRequestDto =  (MultipleChoiceAnswerRequestDto) answerRequestDtos.get(i);

                if (!multipleChoiceAnswerRequestDto.isSkipped()) {
                    Choice correctChoice = ((MultipleChoice) question).getChoices().stream()
                            .filter(Choice::isCorrectAnswer)
                            .findFirst()
                            .orElseThrow(() -> new QuestionException(QuestionErrorCode.CORRECT_CHOICE_NOT_FOUND));

                    boolean correct = correctChoice.getId().equals(multipleChoiceAnswerRequestDto.getChoiceId());

                    ((MultipleChoice) question).setGrade(correct ? GRADE.CORRECT : GRADE.INCORRECT);

                    answer = MultipleChoiceAnswer.createMultipleChoice(correctChoice);

                } else {
                    answer = null;
                }

            } else {
                DictationAnswerRequestDto dictationAnswerRequestDto =  (DictationAnswerRequestDto) answerRequestDtos.get(i);

                String correctAnswer = question.getCard().getBack();

                if (!dictationAnswerRequestDto.isSkipped()) {
                    boolean correct = correctAnswer.equalsIgnoreCase(dictationAnswerRequestDto.getText().trim());

                    ((Dictation) question).setGrade(correct ? GRADE.CORRECT : GRADE.INCORRECT);

                    answer = DictationAnswer.createDictation(dictationAnswerRequestDto.getText());

                } else {
                    answer = null;
                }
            }
            question.addAnswer(answer);
        }


        quiz.finish(LocalDateTime.now());

        return quizMapper.toDto(quiz);
    }

    public QuizResultDto getQuizResult(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizException(QuizErrorCode.QUIZ_NOT_FOUND));

        if (quiz.getFinishedAt() == null) {
            throw new QuizException(QuizErrorCode.QUIZ_NOT_FINISHED);
        }

        List<QuestionResultDto> questionResultDtos = new ArrayList<>();
        Map<GRADE, Long> gradeStatMap = new HashMap<>();
        Map<SelfReviewStatus, Long> selfReviewStatMap = new HashMap<>();

        for (Question question : quiz.getQuestions()) {
            Card card = question.getCard();
            Answer answer = question.getAnswer();

            QuestionResultDto questionResultDto;

            if (quiz.getQuizType() == SELF_REVIEW) {
                SelfReviewStatus status = ((SelfReview) question).getSelfReviewStatus();
                selfReviewStatMap.merge(status, 1L, Long::sum);
                questionResultDto = QuestionResultDto.builder()
                        .questionId(question.getId())
                        .front(card.getFront())
                        .back(card.getBack())
                        .answer(SelfReviewAnswerDto.builder()
                                .selfReviewStatus(status)
                                .build())
                        .build();
            } else if (quiz.getQuizType() == QuizType.DICTATION) {
                GRADE grade = ((Dictation) question).getGrade();
                gradeStatMap.merge(grade, 1L, Long::sum);
                questionResultDto = QuestionResultDto.builder()
                        .questionId(question.getId())
                        .front(card.getFront())
                        .back(card.getBack())
                        .grade(grade)
                        .answer(DictationAnswerDto.builder()
                                .text(((DictationAnswer) answer).getText())
                                .build())
                        .build();
            } else {
                GRADE grade = ((MultipleChoice) question).getGrade();
                gradeStatMap.merge(grade, 1L, Long::sum);
                questionResultDto = QuestionResultDto.builder()
                        .questionId(question.getId())
                        .front(card.getFront())
                        .back(card.getBack())
                        .grade(grade)
                        .answer(MultipleChoiceAnswerDto.builder()
                                .choice(ChoiceDto.builder()
                                    .id(((MultipleChoiceAnswer) answer).getChoice().getId())
                                    .text(((MultipleChoiceAnswer) answer).getChoice().getText())
                                    .build())
                                .build())
                        .build();
            }

            questionResultDtos.add(questionResultDto);
        }

        List<QuizResultStatisticsDto> statistics = quiz.getQuizType() == SELF_REVIEW
                ? Arrays.stream(SelfReviewStatus.values())
                .map(status -> SelfReviewQuizResultStatisticsDto.builder()
                        .status(status)
                        .count(selfReviewStatMap.getOrDefault(status, 0L))
                        .percentage(String.valueOf((double) selfReviewStatMap.getOrDefault(status, 0L) / quiz.getQuestions().size() * 100))
                        .build())
                .map(QuizResultStatisticsDto.class::cast)
                .toList()
                : Arrays.stream(GRADE.values())
                .map(grade -> quiz.getQuizType() == QuizType.MULTIPLE_CHOICE
                        ? MultipleChoiceQuizResultStatisticsDto.builder()
                        .grade(grade)
                        .count(gradeStatMap.getOrDefault(grade, 0L))
                        .percentage(String.valueOf((double) gradeStatMap.getOrDefault(grade, 0L) / quiz.getQuestions().size() * 100))
                        .build()
                        : DictationQuizResultStatisticsDto.builder()
                        .grade(grade)
                        .count(gradeStatMap.getOrDefault(grade, 0L))
                        .percentage(String.valueOf((double) gradeStatMap.getOrDefault(grade, 0L) / quiz.getQuestions().size() * 100))
                        .build())
                .toList();

        return QuizResultDto.builder()
                .quizId(quiz.getId())
                .quizType(quiz.getQuizType())
                .finishedAt(quiz.getFinishedAt())
                .questions(questionResultDtos)
                .statistics(statistics)
                .build();
    }

    public CursorPageResponseDto<QuizResultDto> getQuizResults(String username, QuizType quizType, String orderBy, String direction, String cursor, Long after, int limit) {
        List<Quiz> quizzes = quizRepository.findAllByCursor(username, quizType, orderBy, direction, cursor, after, limit);

        boolean hasNext = quizzes.size() > limit;

        List<Quiz> pagedQuizzes = hasNext ? quizzes.subList(0, limit) : quizzes;

        List<QuizResultDto> quizResultDtos = new ArrayList<>();

        for (Quiz pagedQuiz : pagedQuizzes) {
            List<QuestionResultDto> questionResultDtos = new ArrayList<>();
            Map<GRADE, Long> gradeStatMap = new HashMap<>();
            Map<SelfReviewStatus, Long> selfReviewStatMap = new HashMap<>();

            for (Question question : pagedQuiz.getQuestions()) {
                Card card = question.getCard();
                Answer answer = question.getAnswer();

                QuestionResultDto questionResultDto;

                if (pagedQuiz.getQuizType() == SELF_REVIEW) {
                    SelfReviewStatus status = ((SelfReview) question).getSelfReviewStatus();
                    selfReviewStatMap.merge(status, 1L, Long::sum);
                    questionResultDto = QuestionResultDto.builder()
                            .questionId(question.getId())
                            .front(card.getFront())
                            .back(card.getBack())
                            .answer(SelfReviewAnswerDto.builder()
                                    .selfReviewStatus(status)
                                    .build())
                            .build();
                } else if (pagedQuiz.getQuizType() == QuizType.DICTATION) {
                    GRADE grade = ((Dictation) question).getGrade();
                    gradeStatMap.merge(grade, 1L, Long::sum);
                    questionResultDto = QuestionResultDto.builder()
                            .questionId(question.getId())
                            .front(card.getFront())
                            .back(card.getBack())
                            .grade(grade)
                            .answer(DictationAnswerDto.builder()
                                    .text(((DictationAnswer) answer).getText())
                                    .build())
                            .build();
                } else {
                    GRADE grade = ((MultipleChoice) question).getGrade();
                    gradeStatMap.merge(grade, 1L, Long::sum);
                    questionResultDto = QuestionResultDto.builder()
                            .questionId(question.getId())
                            .front(card.getFront())
                            .back(card.getBack())
                            .grade(grade)
                            .answer(MultipleChoiceAnswerDto.builder()
                                    .choice(ChoiceDto.builder()
                                            .id(((MultipleChoiceAnswer) answer).getChoice().getId())
                                            .text(((MultipleChoiceAnswer) answer).getChoice().getText())
                                            .build())
                                    .build())
                            .build();
                }

                questionResultDtos.add(questionResultDto);
            }

            List<QuizResultStatisticsDto> statistics = pagedQuiz.getQuizType() == SELF_REVIEW
                    ? Arrays.stream(SelfReviewStatus.values())
                    .map(status -> SelfReviewQuizResultStatisticsDto.builder()
                            .status(status)
                            .count(selfReviewStatMap.getOrDefault(status, 0L))
                            .percentage(String.valueOf((double) selfReviewStatMap.getOrDefault(status, 0L) / pagedQuiz.getQuestions().size() * 100))
                            .build())
                    .map(QuizResultStatisticsDto.class::cast)
                    .toList()
                    : Arrays.stream(GRADE.values())
                    .map(grade -> pagedQuiz.getQuizType() == QuizType.MULTIPLE_CHOICE
                            ? MultipleChoiceQuizResultStatisticsDto.builder()
                            .grade(grade)
                            .count(gradeStatMap.getOrDefault(grade, 0L))
                            .percentage(String.valueOf((double) gradeStatMap.getOrDefault(grade, 0L) / pagedQuiz.getQuestions().size() * 100))
                            .build()
                            : DictationQuizResultStatisticsDto.builder()
                            .grade(grade)
                            .count(gradeStatMap.getOrDefault(grade, 0L))
                            .percentage(String.valueOf((double) gradeStatMap.getOrDefault(grade, 0L) / pagedQuiz.getQuestions().size() * 100))
                            .build())
                    .toList();

            QuizResultDto quizResultDto = QuizResultDto.builder()
                    .quizId(pagedQuiz.getId())
                    .quizType(pagedQuiz.getQuizType())
                    .finishedAt(pagedQuiz.getFinishedAt())
                    .questions(questionResultDtos)
                    .statistics(statistics)
                    .build();

            quizResultDtos.add(quizResultDto);
        }

        String nextCursor = null;
        Long nextAfter = null;

        if (hasNext) {
            Quiz lastQuiz = pagedQuizzes.getLast();

            if (orderBy.equals("createdAt")) {
                nextCursor = lastQuiz.getCreatedAt().toString();
            }

            nextAfter = lastQuiz.getId();
        }

        return CursorPageResponseDto.<QuizResultDto>builder()
                .content(quizResultDtos)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(quizResultDtos.size())
                .hasNext(hasNext)
                .build();
    }

    @Transactional
    public void deleteQuizResult(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizException(QuizErrorCode.QUIZ_NOT_FOUND));

        quizRepository.delete(quiz);
    }
}
