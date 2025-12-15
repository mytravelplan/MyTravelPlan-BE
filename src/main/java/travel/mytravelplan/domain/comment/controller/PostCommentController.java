package travel.mytravelplan.domain.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.comment.dto.PostCommentCreateRequestDto;
import travel.mytravelplan.domain.comment.dto.PostCommentDto;
import travel.mytravelplan.domain.comment.dto.PostCommentUpdateRequestDto;
import travel.mytravelplan.domain.comment.service.PostCommentService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.annotaion.LoginUser;
import travel.mytravelplan.global.common.response.ApiResponse;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;

@RestController
@RequestMapping("/api/posts/{postId}/post-comments")
@RequiredArgsConstructor
public class PostCommentController {
    private final PostCommentService postCommentService;

    // 게시물 댓글 생성
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostCommentDto>> createPostComment(
            @LoginUser User currentUser,
            @PathVariable Long postId,
            @RequestBody @Validated PostCommentCreateRequestDto postCommentCreateRequestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(postCommentService.createPostComment(currentUser, postId, postCommentCreateRequestDto)));
    }

    // 게시물 댓글 조회
    @GetMapping("/{postCommentId}")
    public ResponseEntity<ApiResponse<PostCommentDto>> getPostComment(@PathVariable Long postId, @PathVariable Long postCommentId) {
        return ResponseEntity.ok(ApiResponse.success(postCommentService.getPostComment(postId, postCommentId)));
    }

    // 게시물 댓글 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<CursorPageResponseDto<PostCommentDto>>> getPostComments(
            @PathVariable Long postId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt", required = false) String orderBy,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(postCommentService.getPostComments(postId, keyword, orderBy, direction, cursor, after, limit)));
    }

    // 게시물 댓글 수정
    @PatchMapping("/{postCommentId}")
    @PreAuthorize("isAuthenticated() and hasPermission(#postCommentId, 'PostComment', 'postComment:update')")
    public ResponseEntity<ApiResponse<PostCommentDto>> updateComment(@PathVariable Long postId, @PathVariable Long postCommentId, @RequestBody @Validated PostCommentUpdateRequestDto postCommentUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(postCommentService.updatePostComment(postId, postCommentId, postCommentUpdateRequestDto)));
    }

    // 게시물 댓글 삭제
    @DeleteMapping("/{postCommentId}")
    @PreAuthorize("isAuthenticated() and hasPermission(#postCommentId, 'PostComment', 'postComment:delete')")
    public ResponseEntity<Void> deletePostComment(@PathVariable Long postId, @PathVariable Long postCommentId) {
        postCommentService.deletePostComment(postId, postCommentId);
        return ResponseEntity.noContent().build();
    }
}
