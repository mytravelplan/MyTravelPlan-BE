package travel.mytravelplan.domain.delivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.delivery.dto.DeliveryAddressCreateRequestDto;
import travel.mytravelplan.domain.delivery.dto.DeliveryAddressDto;
import travel.mytravelplan.domain.delivery.entity.DeliveryAddress;
import travel.mytravelplan.domain.delivery.enums.Address;
import travel.mytravelplan.domain.delivery.exception.DeliveryAddressException;
import travel.mytravelplan.domain.delivery.mapper.DeliveryAddressMapper;
import travel.mytravelplan.domain.delivery.repository.DeliveryAddressRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.error.code.DeliveryAddressErrorCode;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DeliveryAddressService {
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final DeliveryAddressMapper deliveryAddressMapper;

    @Transactional
    public DeliveryAddressDto createDeliveryAddress(User currentUser, DeliveryAddressCreateRequestDto deliveryAddressCreateRequestDto) {
        DeliveryAddress deliveryAddress = DeliveryAddress.createDeliveryAddress(
                Address.builder()
                        .recipient(deliveryAddressCreateRequestDto.getAddress().getRecipient())
                        .phone(deliveryAddressCreateRequestDto.getAddress().getPhone())
                        .zipcode(deliveryAddressCreateRequestDto.getAddress().getZipcode())
                        .address(deliveryAddressCreateRequestDto.getAddress().getAddress())
                        .detailAddress(deliveryAddressCreateRequestDto.getAddress().getDetailAddress())
                        .build(),
                deliveryAddressCreateRequestDto.isDefaultDeliveryAddress(),
                currentUser
        );

        deliveryAddressRepository.save(deliveryAddress);

        return deliveryAddressMapper.toDto(deliveryAddress);
    }

    public List<DeliveryAddressDto> getAllDeliveryAddresses(User currentUser) {
        List<DeliveryAddress> addresses = deliveryAddressRepository.findAllByUser(currentUser);

        return addresses.stream()
                .map(deliveryAddressMapper::toDto)
                .toList();
    }
}
