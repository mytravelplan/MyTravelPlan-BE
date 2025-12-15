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
import travel.mytravelplan.domain.deck.entity.Deck;
import travel.mytravelplan.domain.deck.exception.DeckException;
import travel.mytravelplan.domain.deck.repository.DeckRepository;
import travel.mytravelplan.global.error.code.DeckErrorCode;

@Profile("local")
@Component
@Order(3)
@RequiredArgsConstructor
public class CardInitializer implements ApplicationRunner {
    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {
        Deck deck = deckRepository.findById(1L)
                .orElseThrow(() -> new DeckException(DeckErrorCode.DECK_NOT_FOUND));

        Card card1 = Card.createCard("Apple", "사과", deck);

        Card card2 = Card.createCard("Banana", "바나나", deck);

        Card card3 = Card.createCard("Grape", "포도", deck);

/*
        Card card4 = Card.builder()
                .question("Orange")
                .answer("오렌지")
                .deck(deck)
                .build();

        Card card5 = Card.builder()
                .question("Pineapple")
                .answer("파인애플")
                .deck(deck)
                .build();

        Card card6 = Card.builder()
                .question("Strawberry")
                .answer("딸기")
                .deck(deck)
                .build();
*/

        cardRepository.save(card1);
        cardRepository.save(card2);
        cardRepository.save(card3);
//        cardRepository.save(card4);
//        cardRepository.save(card5);
//        cardRepository.save(card6);
    }
}
