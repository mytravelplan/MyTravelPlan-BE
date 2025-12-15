package travel.mytravelplan.domain.order.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class OrdererDto {
    private String name;
    private String phoneNumber;
    private String email;

    @Builder
    private OrdererDto(String name, String phoneNumber, String email) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }
}
