package travel.mytravelplan.domain.category.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import travel.mytravelplan.domain.category.dto.CategoryCreateRequestDto;
import travel.mytravelplan.domain.category.dto.CategoryDto;
import travel.mytravelplan.domain.category.dto.CategoryUpdateRequestDto;
import travel.mytravelplan.domain.category.entity.Category;
import travel.mytravelplan.domain.category.exception.CategoryException;
import travel.mytravelplan.domain.category.mapper.CategoryMapper;
import travel.mytravelplan.domain.category.repository.CategoryRepository;
import travel.mytravelplan.global.support.ServiceTestSupport;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("카테고리 서비스 테스트")
class CategoryServiceTest extends ServiceTestSupport {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private Category parentCategory;
    private CategoryDto categoryDto;
    private CategoryCreateRequestDto createRequestDto;
    private CategoryUpdateRequestDto updateRequestDto;

    @BeforeEach
    void setUp() {
        // 부모 카테고리
        parentCategory = Category.createCategory("부모 카테고리", 0, null);

        // 자식 카테고리
        category = Category.createCategory("카테고리", 1, parentCategory);

        // DTO 설정
        categoryDto = CategoryDto.builder()
                .id(1L)
                .name("카테고리")
                .depth(1)
                .build();

        // 생성 요청 DTO (부모 카테고리 없음)
        createRequestDto = CategoryCreateRequestDto.builder()
                .name("새 카테고리")
                .depth(0)
                .build();

        // 수정 요청 DTO
        updateRequestDto = CategoryUpdateRequestDto.builder()
                .name("수정된 카테고리")
                .build();
    }

    @Test
    @DisplayName("카테고리 생성 성공 - 부모 카테고리 없음")
    void createCategory_WithoutParent_Success() {
        // given
        given(categoryRepository.save(any(Category.class))).willReturn(category);
        given(categoryMapper.toDto(any(Category.class))).willReturn(categoryDto);

        // when
        CategoryDto result = categoryService.createCategory(createRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(categoryDto);

        then(categoryRepository).should().save(any(Category.class));
        then(categoryMapper).should().toDto(any(Category.class));
    }

    @Test
    @DisplayName("카테고리 생성 성공 - 부모 카테고리 있음")
    void createCategory_WithParent_Success() {
        // given
        CategoryCreateRequestDto requestWithParent = CategoryCreateRequestDto.builder()
                .name("자식 카테고리")
                .depth(1)
                .parentId(1L)
                .build();

        given(categoryRepository.findById(eq(1L))).willReturn(Optional.of(parentCategory));
        given(categoryRepository.save(any(Category.class))).willReturn(category);
        given(categoryMapper.toDto(any(Category.class))).willReturn(categoryDto);

        // when
        CategoryDto result = categoryService.createCategory(requestWithParent);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(categoryDto);

        then(categoryRepository).should().findById(eq(1L));
        then(categoryRepository).should().save(any(Category.class));
        then(categoryMapper).should().toDto(any(Category.class));
    }

    @Test
    @DisplayName("카테고리 생성 실패 - 부모 카테고리를 찾을 수 없음")
    void createCategory_ParentNotFound() {
        // given
        CategoryCreateRequestDto requestWithParent = CategoryCreateRequestDto.builder()
                .name("자식 카테고리")
                .depth(1)
                .parentId(999L)
                .build();

        given(categoryRepository.findById(eq(999L))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.createCategory(requestWithParent))
                .isInstanceOf(CategoryException.class);

        then(categoryRepository).should().findById(eq(999L));
    }

    @Test
    @DisplayName("최상위 카테고리 목록 조회 성공")
    void getCategories_Success() {
        // given
        Category category2 = Category.createCategory("카테고리2", 0, null);
        List<Category> categories = Arrays.asList(category, category2);

        CategoryDto categoryDto2 = CategoryDto.builder()
                .id(2L)
                .name("카테고리2")
                .depth(0)
                .build();

        given(categoryRepository.findAllByParentIsNull()).willReturn(categories);
        given(categoryMapper.toDto(eq(category))).willReturn(categoryDto);
        given(categoryMapper.toDto(eq(category2))).willReturn(categoryDto2);

        // when
        List<CategoryDto> result = categoryService.getCategories();

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(categoryDto, categoryDto2);

        then(categoryRepository).should().findAllByParentIsNull();
        then(categoryMapper).should().toDto(eq(category));
        then(categoryMapper).should().toDto(eq(category2));
    }

    @Test
    @DisplayName("카테고리 단건 조회 성공")
    void getCategory_Success() {
        // given
        Long categoryId = 1L;
        given(categoryRepository.findById(eq(categoryId))).willReturn(Optional.of(category));
        given(categoryMapper.toDto(eq(category))).willReturn(categoryDto);

        // when
        CategoryDto result = categoryService.getCategory(categoryId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(categoryDto);

        then(categoryRepository).should().findById(eq(categoryId));
        then(categoryMapper).should().toDto(eq(category));
    }

    @Test
    @DisplayName("카테고리 단건 조회 실패 - 존재하지 않는 카테고리")
    void getCategory_NotFound() {
        // given
        Long categoryId = 999L;
        given(categoryRepository.findById(eq(categoryId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.getCategory(categoryId))
                .isInstanceOf(CategoryException.class);

        then(categoryRepository).should().findById(eq(categoryId));
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void updateCategory_Success() {
        // given
        Long categoryId = 1L;
        CategoryDto updatedDto = CategoryDto.builder()
                .id(1L)
                .name("수정된 카테고리")
                .depth(1)
                .build();

        given(categoryRepository.findById(eq(categoryId))).willReturn(Optional.of(category));
        given(categoryMapper.toDto(eq(category))).willReturn(updatedDto);

        // when
        CategoryDto result = categoryService.updateCategory(categoryId, updateRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(updatedDto);

        then(categoryRepository).should().findById(eq(categoryId));
        then(categoryMapper).should().toDto(eq(category));
    }

    @Test
    @DisplayName("카테고리 수정 실패 - 존재하지 않는 카테고리")
    void updateCategory_NotFound() {
        // given
        Long categoryId = 999L;
        given(categoryRepository.findById(eq(categoryId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.updateCategory(categoryId, updateRequestDto))
                .isInstanceOf(CategoryException.class);

        then(categoryRepository).should().findById(eq(categoryId));
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void deleteCategory_Success() {
        // given
        Long categoryId = 1L;
        given(categoryRepository.findById(eq(categoryId))).willReturn(Optional.of(category));

        // when
        categoryService.deleteCategory(categoryId);

        // then
        then(categoryRepository).should().findById(eq(categoryId));
        then(categoryRepository).should().delete(eq(category));
    }

    @Test
    @DisplayName("카테고리 삭제 실패 - 존재하지 않는 카테고리")
    void deleteCategory_NotFound() {
        // given
        Long categoryId = 999L;
        given(categoryRepository.findById(eq(categoryId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
                .isInstanceOf(CategoryException.class);

        then(categoryRepository).should().findById(eq(categoryId));
    }
}