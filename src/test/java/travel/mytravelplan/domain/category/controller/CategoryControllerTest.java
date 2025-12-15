package travel.mytravelplan.domain.category.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.category.dto.CategoryCreateRequestDto;
import travel.mytravelplan.domain.category.dto.CategoryDto;
import travel.mytravelplan.domain.category.dto.CategoryUpdateRequestDto;
import travel.mytravelplan.domain.category.service.CategoryService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.entity.UserProfile;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.global.support.ControllerTestSupport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.then;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@DisplayName("카테고리 컨트롤러 테스트")
class CategoryControllerTest extends ControllerTestSupport {
    @MockitoBean
    private CategoryService categoryService;

    private String accessToken;
    private Long categoryId;
    private CategoryCreateRequestDto createRequestDto;
    private CategoryUpdateRequestDto updateRequestDto;
    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        given(jwtBlacklistService.isTokenBlacklisted(any(String.class))).willReturn(false);

        UserProfile userProfile = UserProfile.createUserProfile(
                "테스트 관리자",
                "http://example.com/admin.jpg"
        );

        User testAdmin = User.createUser(
                "adminUser",
                "password",
                "admin@test.com",
                SocialType.LOCAL,
                null,
                LocalDate.of(1990, 1, 1),
                "010-1234-5678",
                Gender.MALE,
                Set.of(Role.ADMIN)
        );

        testAdmin.setUserProfile(userProfile);

        ReflectionTestUtils.setField(testAdmin, "id", 1L);

        accessToken = jwtUtils.createAccessToken(1L, Set.of(Role.ADMIN));

        given(userRepository.findById(1L)).willReturn(Optional.of(testAdmin));

        categoryId = 1L;

        createRequestDto = CategoryCreateRequestDto.builder()
                .name("테스트 카테고리")
                .depth(1)
                .parentId(null)
                .build();

        updateRequestDto = CategoryUpdateRequestDto.builder()
                .name("수정된 카테고리")
                .build();

        categoryDto = CategoryDto.builder()
                .id(1L)
                .name("테스트 카테고리")
                .depth(1)
                .children(List.of())
                .build();
    }

    @Test
    @DisplayName("카테고리 생성 성공")
    void createCategory_Success() throws Exception {
        // given
        given(categoryService.createCategory(any(CategoryCreateRequestDto.class))).willReturn(categoryDto);

        // when
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("테스트 카테고리"))
                .andExpect(jsonPath("$.data.depth").value(1))
                .andDo(document("category-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (ADMIN 권한 필요)")
                        ),
                        requestFields(
                                fieldWithPath("name").description("카테고리 이름"),
                                fieldWithPath("depth").description("카테고리 깊이"),
                                fieldWithPath("parentId").description("부모 카테고리 ID").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("카테고리 ID"),
                                fieldWithPath("data.name").description("카테고리 이름"),
                                fieldWithPath("data.depth").description("카테고리 깊이"),
                                fieldWithPath("data.children").description("하위 카테고리 목록")
                        )
                ));

        // then
        then(categoryService).should().createCategory(any(CategoryCreateRequestDto.class));
    }

    @Test
    @DisplayName("카테고리 조회 성공")
    void getCategory_Success() throws Exception {
        // given
        given(categoryService.getCategory(eq(categoryId))).willReturn(categoryDto);

        // when
        mockMvc.perform(get("/api/categories/{categoryId}", categoryId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("테스트 카테고리"))
                .andExpect(jsonPath("$.data.depth").value(1))
                .andDo(document("category-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("categoryId").description("카테고리 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("카테고리 ID"),
                                fieldWithPath("data.name").description("카테고리 이름"),
                                fieldWithPath("data.depth").description("카테고리 깊이"),
                                fieldWithPath("data.children").description("하위 카테고리 목록")
                        )
                ));

        // then
        then(categoryService).should().getCategory(eq(categoryId));
    }

    @Test
    @DisplayName("카테고리 목록 조회 성공")
    void getCategories_Success() throws Exception {
        // given
        CategoryDto categoryDto1 = CategoryDto.builder()
                .id(1L)
                .name("카테고리 1")
                .depth(1)
                .children(List.of())
                .build();

        CategoryDto categoryDto2 = CategoryDto.builder()
                .id(2L)
                .name("카테고리 2")
                .depth(1)
                .children(List.of())
                .build();

        List<CategoryDto> categories = List.of(categoryDto1, categoryDto2);

        given(categoryService.getCategories()).willReturn(categories);

        // when
        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("카테고리 1"))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].name").value("카테고리 2"))
                .andDo(document("category-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data").description("카테고리 목록"),
                                fieldWithPath("data[].id").description("카테고리 ID"),
                                fieldWithPath("data[].name").description("카테고리 이름"),
                                fieldWithPath("data[].depth").description("카테고리 깊이"),
                                fieldWithPath("data[].children").description("하위 카테고리 목록")
                        )
                ));

        // then
        then(categoryService).should().getCategories();
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void updateCategory_Success() throws Exception {
        // given
        CategoryDto updatedCategoryDto = CategoryDto.builder()
                .id(1L)
                .name("수정된 카테고리")
                .depth(1)
                .children(List.of())
                .build();

        given(categoryService.updateCategory(eq(categoryId), any(CategoryUpdateRequestDto.class)))
                .willReturn(updatedCategoryDto);

        // when
        mockMvc.perform(patch("/api/categories/{categoryId}", categoryId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("수정된 카테고리"))
                .andExpect(jsonPath("$.data.depth").value(1))
                .andDo(document("category-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (ADMIN 권한 필요)")
                        ),
                        pathParameters(
                                parameterWithName("categoryId").description("카테고리 ID")
                        ),
                        requestFields(
                                fieldWithPath("name").description("수정할 카테고리 이름")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("카테고리 ID"),
                                fieldWithPath("data.name").description("수정된 카테고리 이름"),
                                fieldWithPath("data.depth").description("카테고리 깊이"),
                                fieldWithPath("data.children").description("하위 카테고리 목록")
                        )
                ));

        // then
        then(categoryService).should().updateCategory(eq(categoryId), any(CategoryUpdateRequestDto.class));
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void deleteCategory_Success() throws Exception {
        // given
        willDoNothing().given(categoryService).deleteCategory(eq(categoryId));

        // when
        mockMvc.perform(delete("/api/categories/{categoryId}", categoryId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(document("category-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (ADMIN 권한 필요)")
                        ),
                        pathParameters(
                                parameterWithName("categoryId").description("카테고리 ID")
                        )
                ));

        // then
        then(categoryService).should().deleteCategory(eq(categoryId));
    }
}