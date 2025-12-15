package travel.mytravelplan.domain.post.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class PostCreateRequestDto {
    private String content;
    private List<String> imageUrls;
    private List<String> hashTags;

    @Builder
    private PostCreateRequestDto(String content, List<String> imageUrls, List<String> hashTags) {
        this.content = content;
        this.imageUrls = imageUrls;
        this.hashTags = hashTags;
    }
}
