package travel.mytravelplan.domain.inquiry.entity;

import jakarta.persistence.*;
import lombok.*;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.entity.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryReply extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id")
    private Inquiry inquiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder(access = AccessLevel.PRIVATE)
    private InquiryReply(String content, User user) {
        this.content = content;
        this.user = user;
    }

    public static InquiryReply createInquiryReply(String content, User user) {
        return InquiryReply.builder()
                .content(content)
                .user(user)
                .build();
    }

    public void update(String content) {
        this.content = content;
    }
}
