package travel.mytravelplan.domain.expense.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class ExpenseParticipantRequestDto {
    private Long id;
    private BigDecimal amount; // 참여자가 부담해야 할 금액

    @Builder
    private ExpenseParticipantRequestDto(Long id, BigDecimal amount) {
        this.id = id;
        this.amount = amount;
    }
}
