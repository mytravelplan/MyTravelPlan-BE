package travel.mytravelplan;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.delivery.entity.DeliveryAddress;
import travel.mytravelplan.domain.delivery.enums.Address;
import travel.mytravelplan.domain.delivery.repository.DeliveryAddressRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.exception.UserException;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.error.code.UserErrorCode;

@Profile("local")
@Component
@Order(2)
@RequiredArgsConstructor
public class DeliveryAddressInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {
        User user1 = userRepository.findByUsername("cksgud0403")
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        DeliveryAddress deliveryAddress = DeliveryAddress.createDeliveryAddress(
                Address.builder()
                        .recipient("홍길동")
                        .phone("010-1234-5678")
                        .zipcode("12345")
                        .address("서울특별시 강남구 테헤란로 123")
                        .detailAddress("101동 202호")
                        .build(),
                true,
                user1);

        deliveryAddressRepository.save(deliveryAddress);
    }
}
