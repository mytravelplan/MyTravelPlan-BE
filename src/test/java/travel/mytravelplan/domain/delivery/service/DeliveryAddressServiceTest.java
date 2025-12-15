package travel.mytravelplan.domain.delivery.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import travel.mytravelplan.domain.delivery.dto.DeliveryAddressCreateRequestDto;
import travel.mytravelplan.domain.delivery.dto.DeliveryAddressDto;
import travel.mytravelplan.domain.delivery.entity.DeliveryAddress;
import travel.mytravelplan.domain.delivery.enums.Address;
import travel.mytravelplan.domain.delivery.mapper.DeliveryAddressMapper;
import travel.mytravelplan.domain.delivery.repository.DeliveryAddressRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.support.ServiceTestSupport;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("배송지 서비스 테스트")
class DeliveryAddressServiceTest extends ServiceTestSupport {

    @Mock
    private DeliveryAddressRepository deliveryAddressRepository;

    @Mock
    private DeliveryAddressMapper deliveryAddressMapper;

    @InjectMocks
    private DeliveryAddressService deliveryAddressService;

    private User user;
    private DeliveryAddress deliveryAddress;
    private DeliveryAddressDto deliveryAddressDto;
    private DeliveryAddressCreateRequestDto createRequestDto;
    private Address address;

    @BeforeEach
    void setUp() {
        user = User.createUser("testuser", "password", "test@test.com", null, null, null);

        address = Address.builder()
                .recipient("홍길동")
                .phone("010-1234-5678")
                .zipcode("12345")
                .address("서울시 강남구")
                .detailAddress("101동 202호")
                .build();

        createRequestDto = DeliveryAddressCreateRequestDto.builder()
                .address(address)
                .defaultDeliveryAddress(true)
                .build();

        deliveryAddress = DeliveryAddress.createDeliveryAddress(address, true, user);

        deliveryAddressDto = DeliveryAddressDto.builder()
                .id(1L)
                .address(address)
                .build();
    }

    @Test
    @DisplayName("배송지 생성 성공")
    void createDeliveryAddress_Success() {
        // given
        given(deliveryAddressRepository.save(any(DeliveryAddress.class))).willReturn(deliveryAddress);
        given(deliveryAddressMapper.toDto(any(DeliveryAddress.class))).willReturn(deliveryAddressDto);

        // when
        DeliveryAddressDto result = deliveryAddressService.createDeliveryAddress(user, createRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAddress()).isEqualTo(address);
        assertThat(result.getAddress().getRecipient()).isEqualTo("홍길동");
        assertThat(result.getAddress().getPhone()).isEqualTo("010-1234-5678");
        assertThat(result.getAddress().getZipcode()).isEqualTo("12345");
        assertThat(result.getAddress().getAddress()).isEqualTo("서울시 강남구");
        assertThat(result.getAddress().getDetailAddress()).isEqualTo("101동 202호");

        then(deliveryAddressRepository).should().save(any(DeliveryAddress.class));
        then(deliveryAddressMapper).should().toDto(any(DeliveryAddress.class));
    }

    @Test
    @DisplayName("전체 배송지 목록 조회 성공")
    void getAllDeliveryAddresses_Success() {
        // given
        Address address2 = Address.builder()
                .recipient("김철수")
                .phone("010-9876-5432")
                .zipcode("54321")
                .address("부산시 해운대구")
                .detailAddress("201동 303호")
                .build();

        DeliveryAddress deliveryAddress2 = DeliveryAddress.createDeliveryAddress(address2, false, user);

        DeliveryAddressDto deliveryAddressDto2 = DeliveryAddressDto.builder()
                .id(2L)
                .address(address2)
                .build();

        List<DeliveryAddress> deliveryAddresses = Arrays.asList(deliveryAddress, deliveryAddress2);

        given(deliveryAddressRepository.findAllByUser(eq(user))).willReturn(deliveryAddresses);
        given(deliveryAddressMapper.toDto(eq(deliveryAddress))).willReturn(deliveryAddressDto);
        given(deliveryAddressMapper.toDto(eq(deliveryAddress2))).willReturn(deliveryAddressDto2);

        // when
        List<DeliveryAddressDto> result = deliveryAddressService.getAllDeliveryAddresses(user);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getAddress().getRecipient()).isEqualTo("홍길동");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getAddress().getRecipient()).isEqualTo("김철수");

        then(deliveryAddressRepository).should().findAllByUser(eq(user));
        then(deliveryAddressMapper).should().toDto(eq(deliveryAddress));
        then(deliveryAddressMapper).should().toDto(eq(deliveryAddress2));
    }

    @Test
    @DisplayName("배송지 목록 조회 성공 - 빈 목록")
    void getAllDeliveryAddresses_Success_EmptyList() {
        // given
        given(deliveryAddressRepository.findAllByUser(eq(user))).willReturn(List.of());

        // when
        List<DeliveryAddressDto> result = deliveryAddressService.getAllDeliveryAddresses(user);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        then(deliveryAddressRepository).should().findAllByUser(eq(user));
    }
}