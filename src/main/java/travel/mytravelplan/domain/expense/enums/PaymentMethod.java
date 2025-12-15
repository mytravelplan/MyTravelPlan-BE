package travel.mytravelplan.domain.expense.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    CASH("현금"),
    CARD("카드");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
