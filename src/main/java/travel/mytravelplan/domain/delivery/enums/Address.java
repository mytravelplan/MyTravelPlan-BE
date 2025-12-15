package travel.mytravelplan.domain.delivery.enums;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {
    private String recipient; // 수령인 이름
    private String phone; // 전화번호
    private String zipcode; // 우편번호
    private String address; // 주소
    private String detailAddress; // 상세 주소


    @Builder
    private Address(String recipient, String phone, String zipcode, String address, String detailAddress) {
        this.recipient = recipient;
        this.phone = phone;
        this.zipcode = zipcode;
        this.address = address;
        this.detailAddress = detailAddress;
    }
}
