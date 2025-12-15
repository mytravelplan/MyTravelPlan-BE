package travel.mytravelplan.domain.post.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.post.dto.*;
import travel.mytravelplan.domain.post.service.PostService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.annotaion.LoginUser;
import travel.mytravelplan.global.common.response.ApiResponse;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    // 게시글 생성
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostDto>> createPost(@LoginUser User currentUser, @RequestBody @Validated PostCreateRequestDto postCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(postService.createPost(currentUser, postCreateRequestDto)));
    }

    // 게시글 조회
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDto>> getPost(@LoginUser User currentUser, @PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success(postService.getPost(currentUser, postId)));
    }

    // 게시글 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<CursorPageResponseDto<PostDto>>> getPostsByCursor(
            @LoginUser User currentUser,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt", required = false) String orderBy,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(postService.getPosts(currentUser, keyword, orderBy, direction, cursor, after, limit)));
    }

    // 게시물 수정
    @PatchMapping("/{postId}")
    @PreAuthorize("isAuthenticated() and hasPermission(#postId,'Post','post:update')")
    public ResponseEntity<ApiResponse<PostDto>> updatePost(@LoginUser User currentUser, @PathVariable Long postId, @RequestBody @Validated PostUpdateRequestDto postUpdateRequestDto) {
        PostDto updatedPost = postService.updatePost(currentUser, postId, postUpdateRequestDto);
        return ResponseEntity.ok(ApiResponse.success(updatedPost));
    }

    // 게시물 삭제
    @PreAuthorize("isAuthenticated() and hasPermission(#postId,'Post','post:delete')")
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    // 게시물 좋아요
    @PostMapping("/{postId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostLikeDto>> likePost(@LoginUser User currentUser, @PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success(postService.likePost(currentUser, postId)));
    }

    // 게시물 북마크
    @PostMapping("/{postId}/bookmark")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostBookMarkDto>> bookmarkPost(@LoginUser User currentUser, @PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success(postService.bookmarkPost(currentUser, postId)));
    }
}
