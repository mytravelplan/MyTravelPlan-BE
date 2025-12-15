package travel.mytravelplan.domain.inquiry.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.inquiry.dto.InquiryReplyCreateRequestDto;
import travel.mytravelplan.domain.inquiry.dto.InquiryReplyDto;
import travel.mytravelplan.domain.inquiry.dto.InquiryReplyUpdateRequestDto;
import travel.mytravelplan.domain.inquiry.entity.Inquiry;
import travel.mytravelplan.domain.inquiry.entity.InquiryReply;
import travel.mytravelplan.domain.inquiry.service.InquiryReplyService;
import travel.mytravelplan.domain.product.entity.Product;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.entity.UserProfile;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.support.ControllerTestSupport;

import java.time.LocalDate;
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

@WebMvcTest(InquiryReplyController.class)
@DisplayName("문의 답변 컨트롤러 테스트")
class InquiryReplyControllerTest extends ControllerTestSupport {

    @MockitoBean
    private InquiryReplyService inquiryReplyService;

    private String accessToken;
    private Long userId;
    private Long productId;
    private Long inquiryId;
    private Long inquiryReplyId;
    private User testUser;
    private User sellerUser;
    private Product testProduct;
    private Inquiry testInquiry;
    private InquiryReply testInquiryReply;
    private InquiryReplyCreateRequestDto createRequestDto;
    private InquiryReplyUpdateRequestDto updateRequestDto;
    private InquiryReplyDto inquiryReplyDto;

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

        UserProfile sellerProfile = UserProfile.createUserProfile(
                "판매자 유저",
                "http://example.com/seller.jpg"
        );

        sellerUser = User.createUser(
                "sellerUser",
                "password",
                "seller@test.com",
                SocialType.LOCAL,
                null,
                LocalDate.of(1990, 1, 1),
                "010-1234-5678",
                Gender.MALE,
                Set.of(Role.SELLER)
        );
        sellerUser.setUserProfile(sellerProfile);

        Long sellerId = 2L;
        ReflectionTestUtils.setField(sellerUser, "id", sellerId);

        accessToken = jwtUtils.createAccessToken(sellerId, Set.of(Role.SELLER));

        given(userRepository.findById(eq(sellerId))).willReturn(Optional.of(sellerUser));
        given(userRepository.findById(eq(userId))).willReturn(Optional.of(testUser));

        productId = 1L;

        testProduct = Product.createProduct(
                "테스트 상품",
                "http://example.com/product.jpg",
                10000,
                100,
                List.of(),
                sellerUser
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

        inquiryReplyId = 1L;

        testInquiryReply = InquiryReply.createInquiryReply(
                "답변 내용입니다.",
                sellerUser
        );
        testInquiryReply.setInquiry(testInquiry);
        ReflectionTestUtils.setField(testInquiryReply, "id", inquiryReplyId);

        given(inquiryReplyRepository.findById(eq(inquiryReplyId))).willReturn(Optional.of(testInquiryReply));

        createRequestDto = InquiryReplyCreateRequestDto.builder()
                .content("답변 내용입니다.")
                .build();

        updateRequestDto = InquiryReplyUpdateRequestDto.builder()
                .content("수정된 답변 내용입니다.")
                .build();

        inquiryReplyDto = InquiryReplyDto.builder()
                .id(inquiryReplyId)
                .content("답변 내용입니다.")
                .build();
    }

    @Test
    @DisplayName("상품 문의 답변 작성 성공")
    void createInquiryReply_Success() throws Exception {
        // given
        given(inquiryReplyService.createInquiryReply(any(User.class), eq(productId), eq(inquiryId), any(InquiryReplyCreateRequestDto.class)))
                .willReturn(inquiryReplyDto);

        // when & then
        mockMvc.perform(post("/api/products/{productId}/inquiries/{inquiryId}/inquiry-replies", productId, inquiryId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(inquiryReplyId))
                .andExpect(jsonPath("$.data.content").value("답변 내용입니다."))
                .andDo(document("inquiry-reply-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (판매자 또는 관리자)")
                        ),
                        pathParameters(
                                parameterWithName("productId").description("상품 ID"),
                                parameterWithName("inquiryId").description("문의 ID")
                        ),
                        requestFields(
                                fieldWithPath("content").description("답변 내용")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("답변 ID"),
                                fieldWithPath("data.content").description("답변 내용")
                        )
                ));

        // then
        then(inquiryReplyService).should().createInquiryReply(any(User.class), eq(productId), eq(inquiryId), any(InquiryReplyCreateRequestDto.class));
    }

    @Test
    @DisplayName("상품 문의 답변 조회 성공")
    void getInquiryReply_Success() throws Exception {
        // given
        given(inquiryReplyService.getInquiryReply(any(User.class), eq(productId), eq(inquiryId), eq(inquiryReplyId)))
                .willReturn(inquiryReplyDto);

        // when & then
        mockMvc.perform(get("/api/products/{productId}/inquiries/{inquiryId}/inquiry-replies/{inquiryReplyId}", productId, inquiryId, inquiryReplyId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(inquiryReplyId))
                .andExpect(jsonPath("$.data.content").value("답변 내용입니다."))
                .andDo(document("inquiry-reply-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("productId").description("상품 ID"),
                                parameterWithName("inquiryId").description("문의 ID"),
                                parameterWithName("inquiryReplyId").description("답변 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("답변 ID"),
                                fieldWithPath("data.content").description("답변 내용")
                        )
                ));

        // then
        then(inquiryReplyService).should().getInquiryReply(any(User.class), eq(productId), eq(inquiryId), eq(inquiryReplyId));
    }

    @Test
    @DisplayName("상품 문의 답변 목록 조회 성공")
    void getInquiryReplies_Success() throws Exception {
        // given
        InquiryReplyDto reply1 = InquiryReplyDto.builder()
                .id(1L)
                .content("답변 1")
                .build();

        InquiryReplyDto reply2 = InquiryReplyDto.builder()
                .id(2L)
                .content("답변 2")
                .build();

        CursorPageResponseDto<InquiryReplyDto> pageResponse = CursorPageResponseDto.<InquiryReplyDto>builder()
                .content(List.of(reply1, reply2))
                .nextCursor("cursor")
                .nextAfter(2L)
                .size(2)
                .hasNext(false)
                .build();

        given(inquiryReplyService.getInquiryReplies(
                any(User.class),
                eq(productId),
                eq(inquiryId),
                eq("답변"),
                eq("createdAt"),
                eq("ASC"),
                isNull(),
                isNull(),
                eq(10)
        )).willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/products/{productId}/inquiries/{inquiryId}/inquiry-replies", productId, inquiryId)
                        .header("Authorization", "Bearer " + accessToken)
                        .param("keyword", "답변")
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andDo(document("inquiry-reply-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("productId").description("상품 ID"),
                                parameterWithName("inquiryId").description("문의 ID")
                        ),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드").optional(),
                                parameterWithName("orderBy").description("정렬 기준").optional(),
                                parameterWithName("direction").description("정렬 방향").optional(),
                                parameterWithName("limit").description("조회 개수").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.content").description("답변 목록"),
                                fieldWithPath("data.content[].id").description("답변 ID"),
                                fieldWithPath("data.content[].content").description("답변 내용"),
                                fieldWithPath("data.nextCursor").description("다음 커서"),
                                fieldWithPath("data.nextAfter").description("다음 after ID"),
                                fieldWithPath("data.size").description("조회된 개수"),
                                fieldWithPath("data.hasNext").description("다음 페이지 존재 여부")
                        )
                ));

        // then
        then(inquiryReplyService).should().getInquiryReplies(
                any(User.class),
                eq(productId),
                eq(inquiryId),
                eq("답변"),
                eq("createdAt"),
                eq("ASC"),
                isNull(),
                isNull(),
                eq(10)
        );
    }

    @Test
    @DisplayName("상품 문의 답변 수정 성공")
    void updateInquiryReply_Success() throws Exception {
        // given
        InquiryReplyDto updatedDto = InquiryReplyDto.builder()
                .id(inquiryReplyId)
                .content("수정된 답변 내용입니다.")
                .build();

        given(inquiryReplyService.updateInquiryReply(any(User.class), eq(productId), eq(inquiryId), eq(inquiryReplyId), any(InquiryReplyUpdateRequestDto.class)))
                .willReturn(updatedDto);

        // when & then
        mockMvc.perform(patch("/api/products/{productId}/inquiries/{inquiryId}/inquiry-replies/{inquiryReplyId}", productId, inquiryId, inquiryReplyId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(inquiryReplyId))
                .andExpect(jsonPath("$.data.content").value("수정된 답변 내용입니다."))
                .andDo(document("inquiry-reply-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (판매자 또는 관리자)")
                        ),
                        pathParameters(
                                parameterWithName("productId").description("상품 ID"),
                                parameterWithName("inquiryId").description("문의 ID"),
                                parameterWithName("inquiryReplyId").description("답변 ID")
                        ),
                        requestFields(
                                fieldWithPath("content").description("수정할 답변 내용")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("답변 ID"),
                                fieldWithPath("data.content").description("답변 내용")
                        )
                ));

        // then
        then(inquiryReplyService).should().updateInquiryReply(any(User.class), eq(productId), eq(inquiryId), eq(inquiryReplyId), any(InquiryReplyUpdateRequestDto.class));
    }

    @Test
    @DisplayName("상품 문의 답변 삭제 성공")
    void deleteInquiryReply_Success() throws Exception {
        // given
        willDoNothing().given(inquiryReplyService).deleteInquiryReply(eq(productId), eq(inquiryId), eq(inquiryReplyId));

        // when & then
        mockMvc.perform(delete("/api/products/{productId}/inquiries/{inquiryId}/inquiry-replies/{inquiryReplyId}", productId, inquiryId, inquiryReplyId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(document("inquiry-reply-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (판매자 또는 관리자)")
                        ),
                        pathParameters(
                                parameterWithName("productId").description("상품 ID"),
                                parameterWithName("inquiryId").description("문의 ID"),
                                parameterWithName("inquiryReplyId").description("답변 ID")
                        )
                ));

        // then
        then(inquiryReplyService).should().deleteInquiryReply(eq(productId), eq(inquiryId), eq(inquiryReplyId));
    }
}