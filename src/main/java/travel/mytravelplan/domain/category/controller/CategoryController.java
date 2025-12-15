package travel.mytravelplan.domain.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.category.dto.CategoryCreateRequestDto;
import travel.mytravelplan.domain.category.dto.CategoryDto;
import travel.mytravelplan.domain.category.dto.CategoryUpdateRequestDto;
import travel.mytravelplan.domain.category.service.CategoryService;
import travel.mytravelplan.global.common.response.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    // 카테고리 생성
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(@RequestBody @Validated CategoryCreateRequestDto categoryCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(categoryService.createCategory(categoryCreateRequestDto)));
    }

    // 카테고리 조회
    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryDto>> getCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategory(categoryId)));
    }

    // 카테고리 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategories()));
    }

    // 카테고리 수정
    @PatchMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(@PathVariable Long categoryId, @RequestBody @Validated CategoryUpdateRequestDto categoryUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.updateCategory(categoryId, categoryUpdateRequestDto)));
    }

    // 카테고리 삭제
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
