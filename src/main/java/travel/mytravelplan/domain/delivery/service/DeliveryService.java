package travel.mytravelplan.domain.delivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.delivery.dto.DeliveryDto;
import travel.mytravelplan.domain.delivery.dto.DeliveryUpdateRequestDto;
import travel.mytravelplan.domain.delivery.entity.Delivery;
import travel.mytravelplan.domain.delivery.exception.DeliveryException;
import travel.mytravelplan.domain.delivery.mapper.DeliveryMapper;
import travel.mytravelplan.domain.delivery.repository.DeliveryRepository;
import travel.mytravelplan.global.error.code.DeliveryErrorCode;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final DeliveryMapper deliveryMapper;

    public DeliveryDto getDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryException(DeliveryErrorCode.DELIVERY_NOT_FOUND));

        return deliveryMapper.toDto(delivery);
    }

    @Transactional
    public DeliveryDto updateDelivery(Long deliveryId, DeliveryUpdateRequestDto deliveryUpdateRequestDto) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryException(DeliveryErrorCode.DELIVERY_NOT_FOUND));

        delivery.update(deliveryUpdateRequestDto.getDeliveryStatus());

        return deliveryMapper.toDto(delivery);
    }
}
