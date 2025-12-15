package travel.mytravelplan;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.card.entity.Card;
import travel.mytravelplan.domain.card.repository.CardRepository;
import travel.mytravelplan.domain.question.entity.Choice;
import travel.mytravelplan.domain.question.entity.MultipleChoice;
import travel.mytravelplan.domain.question.repository.QuestionRepository;
import travel.mytravelplan.domain.quiz.entity.Quiz;
import travel.mytravelplan.domain.quiz.enums.QuizType;
import travel.mytravelplan.domain.quiz.repository.QuizRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.exception.UserException;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.error.code.UserErrorCode;

import java.util.ArrayList;
import java.util.List;

@Profile("local")
@Component
@Order(4)
@RequiredArgsConstructor
public class QuizInitializer implements ApplicationRunner {
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {
        User user = userRepository.findByUsername("cksgud0403")
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Quiz quiz = Quiz.createQuiz(QuizType.MULTIPLE_CHOICE, user);

        quizRepository.save(quiz);

        List<Card> cards = cardRepository.findAllByDeckIdIn(List.of(1L));

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
    }
}
