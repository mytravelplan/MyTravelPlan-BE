package travel.mytravelplan.domain.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.category.dto.CategoryCreateRequestDto;
import travel.mytravelplan.domain.category.dto.CategoryDto;
import travel.mytravelplan.domain.category.dto.CategoryUpdateRequestDto;
import travel.mytravelplan.domain.category.entity.Category;
import travel.mytravelplan.domain.category.exception.CategoryException;
import travel.mytravelplan.domain.category.mapper.CategoryMapper;
import travel.mytravelplan.domain.category.repository.CategoryRepository;
import travel.mytravelplan.global.error.code.CategoryErrorCode;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public CategoryDto createCategory(CategoryCreateRequestDto categoryCreateRequestDto) {
        Category parentCategory = null;

        if (categoryCreateRequestDto.getParentId() != null) {
            parentCategory = categoryRepository.findById(categoryCreateRequestDto.getParentId())
                    .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        }

        Category category = Category.createCategory(
                categoryCreateRequestDto.getName(),
                categoryCreateRequestDto.getDepth(),
                parentCategory
        );

        categoryRepository.save(category);

        return categoryMapper.toDto(category);
    }

    public List<CategoryDto> getCategories() {
        List<Category> categories = categoryRepository.findAllByParentIsNull();
        return categories.stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    public CategoryDto getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .map(categoryMapper::toDto)
                .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND));
    }

    @Transactional
    public CategoryDto updateCategory(Long categoryId, CategoryUpdateRequestDto categoryUpdateRequestDto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND));

         category.update(categoryUpdateRequestDto.getName());

        return categoryMapper.toDto(category);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        categoryRepository.delete(category);
    }
}
