package travel.mytravelplan.domain.expense.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ExpenseParticipantDto {
    private Long userId;
    private BigDecimal amount;

    @Builder
    private ExpenseParticipantDto(Long userId, BigDecimal amount) {
        this.userId = userId;
        this.amount = amount;
    }
}
