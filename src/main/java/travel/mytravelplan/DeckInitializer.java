package travel.mytravelplan;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.deck.entity.Deck;
import travel.mytravelplan.domain.deck.repository.DeckRepository;
import travel.mytravelplan.domain.delivery.entity.DeliveryAddress;
import travel.mytravelplan.domain.delivery.repository.DeliveryAddressRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.exception.UserException;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.error.code.UserErrorCode;

@Profile("local")
@Component
@Order(2)
@RequiredArgsConstructor
public class DeckInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final DeckRepository deckRepository;

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {
        User user = userRepository.findByUsername("cksgud0403")
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Deck deck = Deck.createDeck("영어", user);

        deckRepository.save(deck);
    }
}
