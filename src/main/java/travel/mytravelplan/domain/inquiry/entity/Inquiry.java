package travel.mytravelplan.domain.inquiry.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.product.entity.Product;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    private boolean secret;

    @OneToMany(mappedBy = "inquiry")
    private List<InquiryReply> inquiryReplies = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder(access = AccessLevel.PRIVATE)
    private Inquiry(String title, String content, boolean secret, Product product, User user) {
        this.title = title;
        this.content = content;
        this.secret = secret;
        this.product = product;
        this.user = user;
    }

    public static Inquiry createInquiry(String title, String content, boolean secret, Product product, User user) {
        return Inquiry.builder()
                .title(title)
                .content(content)
                .secret(secret)
                .product(product)
                .user(user)
                .build();
    }

    public void addInquiryReply(InquiryReply inquiryReply) {
        this.inquiryReplies.add(inquiryReply);
        inquiryReply.setInquiry(this);
    }

    public void update(String title, String content, boolean secret) {
        this.title = title;
        this.content = content;
        this.secret = secret;
    }
}
