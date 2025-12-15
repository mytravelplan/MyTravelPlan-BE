package travel.mytravelplan.domain.post.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PostDto {
    private Long id;
    private String authorProfileImageUrl;
    private String content;
    private List<String> imageUrls;
    private List<String> hashTags;
    private long numberOfLikes;
    private long numberOfComments;
    private boolean liked;
    private boolean bookmarked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    private PostDto(Long id, String authorProfileImageUrl, String content, List<String> imageUrls,
                    List<String> hashTags, long numberOfLikes, long numberOfComments,
                    boolean liked, boolean bookmarked,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.authorProfileImageUrl = authorProfileImageUrl;
        this.content = content;
        this.imageUrls = imageUrls;
        this.hashTags = hashTags;
        this.numberOfLikes = numberOfLikes;
        this.numberOfComments = numberOfComments;
        this.liked = liked;
        this.bookmarked = bookmarked;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
