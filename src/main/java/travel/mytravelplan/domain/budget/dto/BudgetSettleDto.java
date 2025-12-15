package travel.mytravelplan.domain.budget.dto;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class BudgetSettleDto {
    private BigDecimal totalCollectedAmount;
    private BigDecimal totalPaidAmount;
    private BigDecimal remainingAmount;

    @Builder
    private BudgetSettleDto(BigDecimal totalCollectedAmount, BigDecimal totalPaidAmount, BigDecimal remainingAmount) {
        this.totalCollectedAmount = totalCollectedAmount;
        this.totalPaidAmount = totalPaidAmount;
        this.remainingAmount = remainingAmount;
    }
}
