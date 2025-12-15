package travel.mytravelplan.domain.budget.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class BudgetParticipantDto {
    private Long userId;
    private BigDecimal amount;

    @Builder
    private BudgetParticipantDto(Long userId, BigDecimal amount) {
        this.userId = userId;
        this.amount = amount;
    }
}
