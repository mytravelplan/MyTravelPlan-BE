package travel.mytravelplan.domain.order.enums;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Orderer {
    private String name; // 주문자 이름
    private String phoneNumber; // 주문자 전화번호
    private String email; // 주문자 이메일

    @Builder
    private Orderer(String name, String phoneNumber, String email) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Orderer orderer = (Orderer) o;
        return Objects.equals(name, orderer.name) && Objects.equals(phoneNumber, orderer.phoneNumber) && Objects.equals(email, orderer.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, phoneNumber, email);
    }
}
