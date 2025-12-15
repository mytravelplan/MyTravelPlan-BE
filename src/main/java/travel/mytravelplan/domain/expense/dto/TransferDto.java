package travel.mytravelplan.domain.expense.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class TransferDto {
    private UserDto from; // 보내는 사람 (채무자)
    private UserDto to; // 받는 사람 (채권자)
    private BigDecimal amount; // 이체 금액

    @Builder
    private TransferDto(UserDto from, UserDto to, BigDecimal amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    @Getter
    public static class UserDto {
        private Long userId;
        private String username;
        private String nickname;
        private String profileImageUrl;

        @Builder
        private UserDto(Long userId, String username, String nickname, String profileImageUrl) {
            this.userId = userId;
            this.username = username;
            this.nickname = nickname;
            this.profileImageUrl = profileImageUrl;
        }
    }
}
