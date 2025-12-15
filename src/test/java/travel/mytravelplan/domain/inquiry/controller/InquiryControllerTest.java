package travel.mytravelplan.domain.inquiry.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.inquiry.dto.InquiryCreateRequestDto;
import travel.mytravelplan.domain.inquiry.dto.InquiryDto;
import travel.mytravelplan.domain.inquiry.dto.InquiryUpdateRequestDto;
import travel.mytravelplan.domain.inquiry.entity.Inquiry;
import travel.mytravelplan.domain.inquiry.service.InquiryService;
import travel.mytravelplan.domain.product.entity.Product;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.entity.UserProfile;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.support.ControllerTestSupport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InquiryController.class)
@DisplayName("문의 컨트롤러 테스트")
class InquiryControllerTest extends ControllerTestSupport {

    @MockitoBean
    private InquiryService inquiryService;

    private String accessToken;
    private Long userId;
    private Long productId;
    private Long inquiryId;
    private User testUser;
    private Product testProduct;
    private Inquiry testInquiry;
    private InquiryCreateRequestDto createRequestDto;
    private InquiryUpdateRequestDto updateRequestDto;
    private InquiryDto inquiryDto;

    @BeforeEach
    void setUp() {
        given(jwtBlacklistService.isTokenBlacklisted(any(String.class))).willReturn(false);

        UserProfile userProfile = UserProfile.createUserProfile(
                "테스트 유저",
                "http://example.com/user.jpg"
        );

        testUser = User.createUser(
                "testUser",
                "password",
                "test@test.com",
                SocialType.LOCAL,
                null,
                LocalDate.of(1990, 1, 1),
                "010-1234-5678",
                Gender.MALE,
                Set.of(Role.USER)
        );

        testUser.setUserProfile(userProfile);

        userId = 1L;
        ReflectionTestUtils.setField(testUser, "id", userId);

        accessToken = jwtUtils.createAccessToken(userId, Set.of(Role.USER));

        given(userRepository.findById(eq(userId))).willReturn(Optional.of(testUser));

        productId = 1L;

        testProduct = Product.createProduct(
                "테스트 상품",
                "http://example.com/product.jpg",
                10000,
                100,
                List.of(),
                testUser
        );
        ReflectionTestUtils.setField(testProduct, "id", productId);

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(testProduct));

        inquiryId = 1L;

        testInquiry = Inquiry.createInquiry(
                "문의 제목",
                "문의 내용입니다.",
                false,
                testProduct,
                testUser
        );
        ReflectionTestUtils.setField(testInquiry, "id", inquiryId);

        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(testInquiry));

        createRequestDto = InquiryCreateRequestDto.builder()
                .title("문의 제목")
                .content("문의 내용입니다.")
                .secret(false)
                .build();

        updateRequestDto = InquiryUpdateRequestDto.builder()
                .title("수정된 문의 제목")
                .content("수정된 문의 내용입니다.")
                .secret(true)
                .build();

        inquiryDto = InquiryDto.builder()
                .id(inquiryId)
                .title("문의 제목")
                .content("문의 내용입니다.")
                .answered(false)
                .secret(false)
                .createdAt(LocalDateTime.of(2025, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 1, 10, 0))
                .build();
    }

    @Test
    @DisplayName("상품 문의 등록 성공")
    void createProductInquiry_Success() throws Exception {
        // given
        given(inquiryService.createInquiry(any(User.class), eq(productId), any(InquiryCreateRequestDto.class)))
                .willReturn(inquiryDto);

        // when & then
        mockMvc.perform(post("/api/products/{productId}/inquiries", productId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(inquiryId))
                .andExpect(jsonPath("$.data.title").value("문의 제목"))
                .andExpect(jsonPath("$.data.content").value("문의 내용입니다."))
                .andExpect(jsonPath("$.data.answered").value(false))
                .andExpect(jsonPath("$.data.secret").value(false))
                .andDo(document("inquiry-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("productId").description("상품 ID")
                        ),
                        requestFields(
                                fieldWithPath("title").description("문의 제목"),
                                fieldWithPath("content").description("문의 내용"),
                                fieldWithPath("secret").description("비밀글 여부")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("문의 ID"),
                                fieldWithPath("data.title").description("문의 제목"),
                                fieldWithPath("data.content").description("문의 내용"),
                                fieldWithPath("data.answered").description("답변 완료 여부"),
                                fieldWithPath("data.secret").description("비밀글 여부"),
                                fieldWithPath("data.createdAt").description("생성 일시"),
                                fieldWithPath("data.updatedAt").description("수정 일시")
                        )
                ));

        // then
        then(inquiryService).should().createInquiry(any(User.class), eq(productId), any(InquiryCreateRequestDto.class));
    }

    @Test
    @DisplayName("상품 문의 조회 성공")
    void getInquiry_Success() throws Exception {
        // given
        given(inquiryService.getInquiry(any(User.class), eq(productId), eq(inquiryId)))
                .willReturn(inquiryDto);

        // when & then
        mockMvc.perform(get("/api/products/{productId}/inquiries/{inquiryId}", productId, inquiryId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(inquiryId))
                .andExpect(jsonPath("$.data.title").value("문의 제목"))
                .andExpect(jsonPath("$.data.content").value("문의 내용입니다."))
                .andDo(document("inquiry-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("productId").description("상품 ID"),
                                parameterWithName("inquiryId").description("문의 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("문의 ID"),
                                fieldWithPath("data.title").description("문의 제목"),
                                fieldWithPath("data.content").description("문의 내용"),
                                fieldWithPath("data.answered").description("답변 완료 여부"),
                                fieldWithPath("data.secret").description("비밀글 여부"),
                                fieldWithPath("data.createdAt").description("생성 일시"),
                                fieldWithPath("data.updatedAt").description("수정 일시")
                        )
                ));

        // then
        then(inquiryService).should().getInquiry(any(User.class), eq(productId), eq(inquiryId));
    }

    @Test
    @DisplayName("상품 문의 목록 조회 성공")
    void getProductInquiries_Success() throws Exception {
        // given
        InquiryDto inquiry1 = InquiryDto.builder()
                .id(1L)
                .title("문의 1")
                .content("문의 내용 1")
                .answered(false)
                .secret(false)
                .createdAt(LocalDateTime.of(2025, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 1, 10, 0))
                .build();

        InquiryDto inquiry2 = InquiryDto.builder()
                .id(2L)
                .title("문의 2")
                .content("문의 내용 2")
                .answered(true)
                .secret(false)
                .createdAt(LocalDateTime.of(2025, 1, 2, 10, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 2, 10, 0))
                .build();

        CursorPageResponseDto<InquiryDto> pageResponse = CursorPageResponseDto.<InquiryDto>builder()
                .content(List.of(inquiry1, inquiry2))
                .nextCursor("2025-01-02T10:00:00")
                .nextAfter(2L)
                .size(2)
                .hasNext(false)
                .build();

        given(inquiryService.getInquiries(
                any(User.class),
                eq(productId),
                eq("문의"),
                eq(false),
                eq(false),
                eq("createdAt"),
                eq("ASC"),
                isNull(),
                isNull(),
                eq(10)
        )).willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/products/{productId}/inquiries", productId)
                        .header("Authorization", "Bearer " + accessToken)
                        .param("keyword", "문의")
                        .param("secretOnly", "false")
                        .param("answerOnly", "false")
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andDo(document("inquiry-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("productId").description("상품 ID")
                        ),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드").optional(),
                                parameterWithName("secretOnly").description("비밀글만 조회").optional(),
                                parameterWithName("answerOnly").description("답변 완료된 글만 조회").optional(),
                                parameterWithName("orderBy").description("정렬 기준").optional(),
                                parameterWithName("direction").description("정렬 방향").optional(),
                                parameterWithName("limit").description("조회 개수").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.content").description("문의 목록"),
                                fieldWithPath("data.content[].id").description("문의 ID"),
                                fieldWithPath("data.content[].title").description("문의 제목"),
                                fieldWithPath("data.content[].content").description("문의 내용"),
                                fieldWithPath("data.content[].answered").description("답변 완료 여부"),
                                fieldWithPath("data.content[].secret").description("비밀글 여부"),
                                fieldWithPath("data.content[].createdAt").description("생성 일시"),
                                fieldWithPath("data.content[].updatedAt").description("수정 일시"),
                                fieldWithPath("data.nextCursor").description("다음 커서"),
                                fieldWithPath("data.nextAfter").description("다음 after ID"),
                                fieldWithPath("data.size").description("조회된 개수"),
                                fieldWithPath("data.hasNext").description("다음 페이지 존재 여부")
                        )
                ));

        // then
        then(inquiryService).should().getInquiries(
                any(User.class),
                eq(productId),
                eq("문의"),
                eq(false),
                eq(false),
                eq("createdAt"),
                eq("ASC"),
                isNull(),
                isNull(),
                eq(10)
        );
    }

    @Test
    @DisplayName("상품 문의 수정 성공")
    void updateInquiry_Success() throws Exception {
        // given
        InquiryDto updatedDto = InquiryDto.builder()
                .id(inquiryId)
                .title("수정된 문의 제목")
                .content("수정된 문의 내용입니다.")
                .answered(false)
                .secret(true)
                .createdAt(LocalDateTime.of(2025, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 1, 11, 0))
                .build();

        given(inquiryService.updateInquiry(any(User.class), eq(productId), eq(inquiryId), any(InquiryUpdateRequestDto.class)))
                .willReturn(updatedDto);

        // when & then
        mockMvc.perform(patch("/api/products/{productId}/inquiries/{inquiryId}", productId, inquiryId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(inquiryId))
                .andExpect(jsonPath("$.data.title").value("수정된 문의 제목"))
                .andExpect(jsonPath("$.data.content").value("수정된 문의 내용입니다."))
                .andExpect(jsonPath("$.data.secret").value(true))
                .andDo(document("inquiry-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("productId").description("상품 ID"),
                                parameterWithName("inquiryId").description("문의 ID")
                        ),
                        requestFields(
                                fieldWithPath("title").description("수정할 문의 제목"),
                                fieldWithPath("content").description("수정할 문의 내용"),
                                fieldWithPath("secret").description("비밀글 여부")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("문의 ID"),
                                fieldWithPath("data.title").description("문의 제목"),
                                fieldWithPath("data.content").description("문의 내용"),
                                fieldWithPath("data.answered").description("답변 완료 여부"),
                                fieldWithPath("data.secret").description("비밀글 여부"),
                                fieldWithPath("data.createdAt").description("생성 일시"),
                                fieldWithPath("data.updatedAt").description("수정 일시")
                        )
                ));

        // then
        then(inquiryService).should().updateInquiry(any(User.class), eq(productId), eq(inquiryId), any(InquiryUpdateRequestDto.class));
    }

    @Test
    @DisplayName("상품 문의 삭제 성공")
    void deleteInquiry_Success() throws Exception {
        // given
        willDoNothing().given(inquiryService).deleteInquiry(eq(productId), eq(inquiryId));

        // when & then
        mockMvc.perform(delete("/api/products/{productId}/inquiries/{inquiryId}", productId, inquiryId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(document("inquiry-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("productId").description("상품 ID"),
                                parameterWithName("inquiryId").description("문의 ID")
                        )
                ));

        // then
        then(inquiryService).should().deleteInquiry(eq(productId), eq(inquiryId));
    }
}