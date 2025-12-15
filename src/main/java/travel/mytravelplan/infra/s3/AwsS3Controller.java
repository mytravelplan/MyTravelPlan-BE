package travel.mytravelplan.infra.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.global.common.response.ApiResponse;

import java.time.Instant;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AwsS3Controller {
    private final AmazonS3Client amazonS3Client;

    @PostMapping("/presigned-url")
    public ResponseEntity<ApiResponse<String>> getPresignedUrl(@RequestBody ImageDto imageDto) {
        return ResponseEntity.ok(ApiResponse.success(amazonS3Client.generatePresignedUrl("mytravelplan",
                imageDto.getFileName(),
                imageDto.getContentType(),
                imageDto.getSize(),
                Instant.now().plusSeconds(60 * 60 * 24 * 7)))); // 7 days expiration
    }
}
