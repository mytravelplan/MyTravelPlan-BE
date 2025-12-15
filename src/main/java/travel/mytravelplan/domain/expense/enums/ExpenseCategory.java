package travel.mytravelplan.domain.expense.enums;

import lombok.Getter;

@Getter
public enum ExpenseCategory {
    FOOD("식사"),
    TRANSPORTATION("교통"),
    ACCOMMODATION("숙박"),
    ENTERTAINMENT("오락"),
    SHOPPING("쇼핑"),
    MISCELLANEOUS("기타");

    private final String description;

    ExpenseCategory(String description) {
        this.description = description;
    }

}
