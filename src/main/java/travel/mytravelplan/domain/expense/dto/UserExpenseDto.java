package travel.mytravelplan.domain.expense.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class UserExpenseDto {
    private BigDecimal paidAmount; // 사용자가 결제한 금액
    private BigDecimal consumedAmount; // 사용자가 소비한 금액
    private Long tripJoinId;
    private Long userId;
    private String username;
    private String nickname;
    private String profileImageUrl;

    @Builder
    private UserExpenseDto(BigDecimal paidAmount, BigDecimal consumedAmount, Long tripJoinId, Long userId, String username, String nickname, String profileImageUrl) {
        this.paidAmount = paidAmount;
        this.consumedAmount = consumedAmount;
        this.tripJoinId = tripJoinId;
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }
}
