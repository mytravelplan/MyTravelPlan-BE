package travel.mytravelplan;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.product.repository.ProductRepository;
import travel.mytravelplan.domain.user.repository.UserRepository;

@Profile("local")
@Component
@Order(1)
@RequiredArgsConstructor
public class ProductInitializer implements ApplicationRunner {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {

    }
}
