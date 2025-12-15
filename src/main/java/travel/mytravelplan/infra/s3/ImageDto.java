package travel.mytravelplan.infra.s3;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ImageDto {
    private String fileName;
    private Long size;
    private String contentType;

    @Builder
    private ImageDto(String fileName, Long size, String contentType) {
        this.fileName = fileName;
        this.size = size;
        this.contentType = contentType;
    }
}
