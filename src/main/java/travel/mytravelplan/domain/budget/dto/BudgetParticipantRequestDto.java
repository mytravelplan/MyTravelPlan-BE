package travel.mytravelplan.domain.budget.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class BudgetParticipantRequestDto {
    private Long id; // 참여자 ID
    private BigDecimal amount; // 참여자가 부담해야 할 금액

    @Builder
    private BudgetParticipantRequestDto(Long id, BigDecimal amount) {
        this.id = id;
        this.amount = amount;
    }
}
