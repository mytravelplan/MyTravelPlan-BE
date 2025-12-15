package travel.mytravelplan.domain.delivery.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import travel.mytravelplan.domain.delivery.dto.DeliveryDto;
import travel.mytravelplan.domain.delivery.dto.DeliveryUpdateRequestDto;
import travel.mytravelplan.domain.delivery.entity.Delivery;
import travel.mytravelplan.domain.delivery.enums.Address;
import travel.mytravelplan.domain.delivery.enums.DeliveryStatus;
import travel.mytravelplan.domain.delivery.exception.DeliveryException;
import travel.mytravelplan.domain.delivery.mapper.DeliveryMapper;
import travel.mytravelplan.domain.delivery.repository.DeliveryRepository;
import travel.mytravelplan.global.support.ServiceTestSupport;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("배송 서비스 테스트")
class DeliveryServiceTest extends ServiceTestSupport {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private DeliveryMapper deliveryMapper;

    @InjectMocks
    private DeliveryService deliveryService;

    private Delivery delivery;
    private DeliveryDto deliveryDto;
    private Address address;

    @BeforeEach
    void setUp() {
        address = Address.builder()
                .recipient("홍길동")
                .phone("010-1234-5678")
                .zipcode("12345")
                .address("서울시 강남구")
                .detailAddress("101동 101호")
                .build();

        delivery = Delivery.createDelivery(address, "문 앞에 놔주세요");

        deliveryDto = DeliveryDto.builder()
                .id(1L)
                .address(address)
                .deliveryStatus(DeliveryStatus.READY)
                .requirement("문 앞에 놔주세요")
                .build();
    }

    @Test
    @DisplayName("배송 조회 성공")
    void getDelivery_Success() {
        // given
        Long deliveryId = 1L;
        given(deliveryRepository.findById(eq(deliveryId))).willReturn(Optional.of(delivery));
        given(deliveryMapper.toDto(any(Delivery.class))).willReturn(deliveryDto);

        // when
        DeliveryDto result = deliveryService.getDelivery(deliveryId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDeliveryStatus()).isEqualTo(DeliveryStatus.READY);
        assertThat(result.getRequirement()).isEqualTo("문 앞에 놔주세요");
        assertThat(result.getAddress()).isNotNull();
        assertThat(result.getAddress().getRecipient()).isEqualTo("홍길동");

        then(deliveryRepository).should().findById(eq(deliveryId));
        then(deliveryMapper).should().toDto(any(Delivery.class));
    }

    @Test
    @DisplayName("배송 조회 실패 - 존재하지 않는 배송")
    void getDelivery_NotFound() {
        // given
        Long deliveryId = 999L;
        given(deliveryRepository.findById(eq(deliveryId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deliveryService.getDelivery(deliveryId))
                .isInstanceOf(DeliveryException.class);

        then(deliveryRepository).should().findById(eq(deliveryId));
    }

    @Test
    @DisplayName("배송 상태 수정 성공")
    void updateDelivery_Success() {
        // given
        Long deliveryId = 1L;
        DeliveryUpdateRequestDto updateRequestDto = DeliveryUpdateRequestDto.builder()
                .deliveryStatus(DeliveryStatus.DELIVERING)
                .build();

        DeliveryDto updatedDeliveryDto = DeliveryDto.builder()
                .id(1L)
                .address(address)
                .deliveryStatus(DeliveryStatus.DELIVERING)
                .requirement("문 앞에 놔주세요")
                .build();

        given(deliveryRepository.findById(eq(deliveryId))).willReturn(Optional.of(delivery));
        given(deliveryMapper.toDto(any(Delivery.class))).willReturn(updatedDeliveryDto);

        // when
        DeliveryDto result = deliveryService.updateDelivery(deliveryId, updateRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDeliveryStatus()).isEqualTo(DeliveryStatus.DELIVERING);
        assertThat(result.getRequirement()).isEqualTo("문 앞에 놔주세요");

        then(deliveryRepository).should().findById(eq(deliveryId));
        then(deliveryMapper).should().toDto(any(Delivery.class));
    }

    @Test
    @DisplayName("배송 상태 수정 실패 - 존재하지 않는 배송")
    void updateDelivery_NotFound() {
        // given
        Long deliveryId = 999L;
        DeliveryUpdateRequestDto updateRequestDto = DeliveryUpdateRequestDto.builder()
                .deliveryStatus(DeliveryStatus.DELIVERING)
                .build();

        given(deliveryRepository.findById(eq(deliveryId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deliveryService.updateDelivery(deliveryId, updateRequestDto))
                .isInstanceOf(DeliveryException.class);

        then(deliveryRepository).should().findById(eq(deliveryId));
    }

    @Test
    @DisplayName("배송 상태를 완료로 수정 성공")
    void updateDeliveryToCompleted_Success() {
        // given
        Long deliveryId = 1L;
        DeliveryUpdateRequestDto updateRequestDto = DeliveryUpdateRequestDto.builder()
                .deliveryStatus(DeliveryStatus.COMP)
                .build();

        DeliveryDto completedDeliveryDto = DeliveryDto.builder()
                .id(1L)
                .address(address)
                .deliveryStatus(DeliveryStatus.COMP)
                .requirement("문 앞에 놔주세요")
                .build();

        given(deliveryRepository.findById(eq(deliveryId))).willReturn(Optional.of(delivery));
        given(deliveryMapper.toDto(any(Delivery.class))).willReturn(completedDeliveryDto);

        // when
        DeliveryDto result = deliveryService.updateDelivery(deliveryId, updateRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDeliveryStatus()).isEqualTo(DeliveryStatus.COMP);

        then(deliveryRepository).should().findById(eq(deliveryId));
        then(deliveryMapper).should().toDto(any(Delivery.class));
    }
}