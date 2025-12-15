package travel.mytravelplan.global.common.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class CursorPageResponseDto<T> {
    private List<T> content;
    private String nextCursor;
    private Long nextAfter;
    private int size;
    private Boolean hasNext;

    @Builder
    private CursorPageResponseDto(List<T> content, String nextCursor, Long nextAfter, int size, Boolean hasNext) {
        this.content = content;
        this.nextCursor = nextCursor;
        this.nextAfter = nextAfter;
        this.size = size;
        this.hasNext = hasNext;
    }
}
