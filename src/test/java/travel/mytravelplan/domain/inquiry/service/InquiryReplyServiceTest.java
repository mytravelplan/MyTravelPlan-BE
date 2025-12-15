package travel.mytravelplan.domain.inquiry.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.inquiry.dto.InquiryReplyCreateRequestDto;
import travel.mytravelplan.domain.inquiry.dto.InquiryReplyDto;
import travel.mytravelplan.domain.inquiry.dto.InquiryReplyUpdateRequestDto;
import travel.mytravelplan.domain.inquiry.entity.Inquiry;
import travel.mytravelplan.domain.inquiry.entity.InquiryReply;
import travel.mytravelplan.domain.inquiry.exception.InquiryException;
import travel.mytravelplan.domain.inquiry.mapper.InquiryReplyMapper;
import travel.mytravelplan.domain.inquiry.repsotiroy.InquiryReplyRepository;
import travel.mytravelplan.domain.inquiry.repsotiroy.InquiryRepository;
import travel.mytravelplan.domain.product.entity.Product;
import travel.mytravelplan.domain.product.exception.ProductException;
import travel.mytravelplan.domain.product.repository.ProductRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.support.ServiceTestSupport;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("문의 답변 서비스 테스트")
class InquiryReplyServiceTest extends ServiceTestSupport {

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InquiryReplyRepository inquiryReplyRepository;

    @Mock
    private InquiryReplyMapper inquiryReplyMapper;

    @InjectMocks
    private InquiryReplyService inquiryReplyService;

    private User currentUser;
    private Product product;
    private Inquiry inquiry;
    private InquiryReply inquiryReply;
    private InquiryReplyDto inquiryReplyDto;
    private InquiryReplyCreateRequestDto createRequestDto;
    private InquiryReplyUpdateRequestDto updateRequestDto;

    @BeforeEach
    void setUp() {
        currentUser = User.createUser(
                "testuser",
                "password123",
                "test@example.com",
                null,
                null,
                null,
                null,
                null,
                null
        );

        product = Product.createProduct(
                "테스트 상품",
                "image.jpg",
                10000,
                100,
                new ArrayList<>(),
                currentUser
        );

        createRequestDto = InquiryReplyCreateRequestDto.builder()
                .content("문의 답변입니다.")
                .build();

        updateRequestDto = InquiryReplyUpdateRequestDto.builder()
                .content("수정된 답변입니다.")
                .build();

        inquiry = Inquiry.createInquiry(
                "문의 제목",
                "문의 내용",
                false,
                product,
                currentUser
        );
        ReflectionTestUtils.setField(inquiry, "id", 1L);

        inquiryReply = InquiryReply.createInquiryReply("문의 답변입니다.", currentUser);

        inquiryReplyDto = InquiryReplyDto.builder()
                .id(1L)
                .content("문의 답변입니다.")
                .build();
    }

    @Test
    @DisplayName("문의 답변 생성 성공")
    void createInquiryReply_Success() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(inquiry));
        given(inquiryReplyRepository.save(any(InquiryReply.class))).willReturn(inquiryReply);
        given(inquiryReplyMapper.toDto(any(InquiryReply.class), eq(currentUser))).willReturn(inquiryReplyDto);

        // when
        InquiryReplyDto result = inquiryReplyService.createInquiryReply(currentUser, productId, inquiryId, createRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(inquiryReplyDto);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
        then(inquiryReplyRepository).should().save(any(InquiryReply.class));
        then(inquiryReplyMapper).should().toDto(any(InquiryReply.class), eq(currentUser));
    }

    @Test
    @DisplayName("문의 답변 생성 실패 - 상품을 찾을 수 없음")
    void createInquiryReply_ProductNotFound() {
        // given
        Long productId = 999L;
        Long inquiryId = 1L;

        given(productRepository.findById(eq(productId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> inquiryReplyService.createInquiryReply(currentUser, productId, inquiryId, createRequestDto))
                .isInstanceOf(ProductException.class);

        then(productRepository).should().findById(eq(productId));
    }

    @Test
    @DisplayName("문의 답변 생성 실패 - 문의를 찾을 수 없음")
    void createInquiryReply_InquiryNotFound() {
        // given
        Long productId = 1L;
        Long inquiryId = 999L;

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> inquiryReplyService.createInquiryReply(currentUser, productId, inquiryId, createRequestDto))
                .isInstanceOf(InquiryException.class);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
    }

    @Test
    @DisplayName("문의 답변 생성 실패 - 문의가 상품에 속하지 않음")
    void createInquiryReply_InquiryNotBelongsToProduct() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;

        Product otherProduct = Product.createProduct(
                "다른 상품",
                "other.jpg",
                20000,
                50,
                new ArrayList<>(),
                currentUser
        );

        Inquiry otherInquiry = Inquiry.createInquiry(
                "다른 문의",
                "다른 문의 내용",
                false,
                otherProduct,
                currentUser
        );

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(otherInquiry));

        // when & then
        assertThatThrownBy(() -> inquiryReplyService.createInquiryReply(currentUser, productId, inquiryId, createRequestDto))
                .isInstanceOf(InquiryException.class);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
    }

    @Test
    @DisplayName("문의 답변 조회 성공")
    void getInquiryReply_Success() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        Long inquiryReplyId = 1L;

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(inquiry));
        given(inquiryReplyRepository.findById(eq(inquiryReplyId))).willReturn(Optional.of(inquiryReply));
        given(inquiryReplyMapper.toDto(eq(inquiryReply), eq(currentUser))).willReturn(inquiryReplyDto);

        // when
        InquiryReplyDto result = inquiryReplyService.getInquiryReply(currentUser, productId, inquiryId, inquiryReplyId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(inquiryReplyDto);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
        then(inquiryReplyRepository).should().findById(eq(inquiryReplyId));
        then(inquiryReplyMapper).should().toDto(eq(inquiryReply), eq(currentUser));
    }

    @Test
    @DisplayName("문의 답변 조회 실패 - 답변을 찾을 수 없음")
    void getInquiryReply_NotFound() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        Long inquiryReplyId = 999L;

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(inquiry));
        given(inquiryReplyRepository.findById(eq(inquiryReplyId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> inquiryReplyService.getInquiryReply(currentUser, productId, inquiryId, inquiryReplyId))
                .isInstanceOf(InquiryException.class);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
        then(inquiryReplyRepository).should().findById(eq(inquiryReplyId));
    }

    @Test
    @DisplayName("문의 답변 목록 조회 성공")
    void getInquiryReplies_Success() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        InquiryReply inquiryReply2 = InquiryReply.createInquiryReply("두번째 답변", currentUser);
        List<InquiryReply> inquiryReplies = Arrays.asList(inquiryReply, inquiryReply2);

        InquiryReplyDto inquiryReplyDto2 = InquiryReplyDto.builder()
                .id(2L)
                .content("두번째 답변")
                .build();

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(inquiry));
        given(inquiryReplyRepository.findAllByCursor(eq(inquiryId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(inquiryReplies);
        given(inquiryReplyMapper.toDto(eq(inquiryReply), eq(currentUser))).willReturn(inquiryReplyDto);
        given(inquiryReplyMapper.toDto(eq(inquiryReply2), eq(currentUser))).willReturn(inquiryReplyDto2);

        // when
        CursorPageResponseDto<InquiryReplyDto> result = inquiryReplyService.getInquiryReplies(currentUser, productId, inquiryId, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isFalse();

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
        then(inquiryReplyRepository).should().findAllByCursor(eq(inquiryId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(inquiryReplyMapper).should().toDto(eq(inquiryReply), eq(currentUser));
        then(inquiryReplyMapper).should().toDto(eq(inquiryReply2), eq(currentUser));
    }

    @Test
    @DisplayName("문의 답변 목록 조회 성공 - hasNext true")
    void getInquiryReplies_HasNext() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 2;

        InquiryReply testReply1 = InquiryReply.createInquiryReply("첫번째 답변", currentUser);
        ReflectionTestUtils.setField(testReply1, "id", 1L);
        ReflectionTestUtils.setField(testReply1, "createdAt", LocalDateTime.of(2024, 1, 1, 12, 0, 0));

        InquiryReply testReply2 = InquiryReply.createInquiryReply("두번째 답변", currentUser);
        ReflectionTestUtils.setField(testReply2, "id", 2L);
        ReflectionTestUtils.setField(testReply2, "createdAt", LocalDateTime.of(2024, 1, 2, 12, 0, 0));

        InquiryReply testReply3 = InquiryReply.createInquiryReply("세번째 답변", currentUser);
        ReflectionTestUtils.setField(testReply3, "id", 3L);
        ReflectionTestUtils.setField(testReply3, "createdAt", LocalDateTime.of(2024, 1, 3, 12, 0, 0));

        List<InquiryReply> inquiryReplies = Arrays.asList(testReply1, testReply2, testReply3);

        InquiryReplyDto replyDto1 = InquiryReplyDto.builder().id(1L).content("첫번째 답변").build();
        InquiryReplyDto replyDto2 = InquiryReplyDto.builder().id(2L).content("두번째 답변").build();

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(inquiry));
        given(inquiryReplyRepository.findAllByCursor(eq(inquiryId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(inquiryReplies);
        given(inquiryReplyMapper.toDto(eq(testReply1), eq(currentUser))).willReturn(replyDto1);
        given(inquiryReplyMapper.toDto(eq(testReply2), eq(currentUser))).willReturn(replyDto2);

        // when
        CursorPageResponseDto<InquiryReplyDto> result = inquiryReplyService.getInquiryReplies(currentUser, productId, inquiryId, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isEqualTo("2024-01-02T12:00");
        assertThat(result.getNextAfter()).isEqualTo(2L);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
        then(inquiryReplyRepository).should().findAllByCursor(eq(inquiryId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(inquiryReplyMapper).should().toDto(eq(testReply1), eq(currentUser));
        then(inquiryReplyMapper).should().toDto(eq(testReply2), eq(currentUser));
    }

    @Test
    @DisplayName("문의 답변 수정 성공")
    void updateInquiryReply_Success() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        Long inquiryReplyId = 1L;

        InquiryReplyDto updatedDto = InquiryReplyDto.builder()
                .id(1L)
                .content("수정된 답변입니다.")
                .build();

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(inquiry));
        given(inquiryReplyRepository.findById(eq(inquiryReplyId))).willReturn(Optional.of(inquiryReply));
        given(inquiryReplyMapper.toDto(eq(inquiryReply), eq(currentUser))).willReturn(updatedDto);

        // when
        InquiryReplyDto result = inquiryReplyService.updateInquiryReply(currentUser, productId, inquiryId, inquiryReplyId, updateRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(updatedDto);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
        then(inquiryReplyRepository).should().findById(eq(inquiryReplyId));
        then(inquiryReplyMapper).should().toDto(eq(inquiryReply), eq(currentUser));
    }

    @Test
    @DisplayName("문의 답변 수정 실패 - 답변을 찾을 수 없음")
    void updateInquiryReply_NotFound() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        Long inquiryReplyId = 999L;

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(inquiry));
        given(inquiryReplyRepository.findById(eq(inquiryReplyId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> inquiryReplyService.updateInquiryReply(currentUser, productId, inquiryId, inquiryReplyId, updateRequestDto))
                .isInstanceOf(InquiryException.class);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
        then(inquiryReplyRepository).should().findById(eq(inquiryReplyId));
    }

    @Test
    @DisplayName("문의 답변 삭제 성공")
    void deleteInquiryReply_Success() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        Long inquiryReplyId = 1L;

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(inquiry));
        given(inquiryReplyRepository.findById(eq(inquiryReplyId))).willReturn(Optional.of(inquiryReply));

        // when
        inquiryReplyService.deleteInquiryReply(productId, inquiryId, inquiryReplyId);

        // then
        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
        then(inquiryReplyRepository).should().findById(eq(inquiryReplyId));
        then(inquiryReplyRepository).should().delete(eq(inquiryReply));
    }

    @Test
    @DisplayName("문의 답변 삭제 실패 - 답변을 찾을 수 없음")
    void deleteInquiryReply_NotFound() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        Long inquiryReplyId = 999L;

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(inquiry));
        given(inquiryReplyRepository.findById(eq(inquiryReplyId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> inquiryReplyService.deleteInquiryReply(productId, inquiryId, inquiryReplyId))
                .isInstanceOf(InquiryException.class);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
        then(inquiryReplyRepository).should().findById(eq(inquiryReplyId));
    }

    @Test
    @DisplayName("문의 답변 조회 실패 - 상품을 찾을 수 없음")
    void getInquiryReply_ProductNotFound() {
        // given
        Long productId = 999L;
        Long inquiryId = 1L;
        Long inquiryReplyId = 1L;

        given(productRepository.findById(eq(productId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> inquiryReplyService.getInquiryReply(currentUser, productId, inquiryId, inquiryReplyId))
                .isInstanceOf(ProductException.class);

        then(productRepository).should().findById(eq(productId));
    }

    @Test
    @DisplayName("문의 답변 조회 실패 - 문의를 찾을 수 없음")
    void getInquiryReply_InquiryNotFound() {
        // given
        Long productId = 1L;
        Long inquiryId = 999L;
        Long inquiryReplyId = 1L;

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> inquiryReplyService.getInquiryReply(currentUser, productId, inquiryId, inquiryReplyId))
                .isInstanceOf(InquiryException.class);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
    }

    @Test
    @DisplayName("문의 답변 조회 실패 - 문의가 상품에 속하지 않음")
    void getInquiryReply_InquiryNotBelongsToProduct() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        Long inquiryReplyId = 1L;

        Product otherProduct = Product.createProduct(
                "다른 상품",
                "other.jpg",
                20000,
                50,
                new ArrayList<>(),
                currentUser
        );

        Inquiry otherInquiry = Inquiry.createInquiry(
                "다른 문의",
                "다른 문의 내용",
                false,
                otherProduct,
                currentUser
        );

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(otherInquiry));

        // when & then
        assertThatThrownBy(() -> inquiryReplyService.getInquiryReply(currentUser, productId, inquiryId, inquiryReplyId))
                .isInstanceOf(InquiryException.class);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
    }

    @Test
    @DisplayName("문의 답변 목록 조회 성공 - 빈 목록")
    void getInquiryReplies_EmptyList() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        List<InquiryReply> emptyList = new ArrayList<>();

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(inquiry));
        given(inquiryReplyRepository.findAllByCursor(eq(inquiryId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(emptyList);

        // when
        CursorPageResponseDto<InquiryReplyDto> result = inquiryReplyService.getInquiryReplies(currentUser, productId, inquiryId, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
        then(inquiryReplyRepository).should().findAllByCursor(eq(inquiryId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("문의 답변 목록 조회 성공 - 키워드 검색")
    void getInquiryReplies_WithKeyword() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        String keyword = "검색어";
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        List<InquiryReply> inquiryReplies = Collections.singletonList(inquiryReply);

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(inquiry));
        given(inquiryReplyRepository.findAllByCursor(eq(inquiryId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(inquiryReplies);
        given(inquiryReplyMapper.toDto(eq(inquiryReply), eq(currentUser))).willReturn(inquiryReplyDto);

        // when
        CursorPageResponseDto<InquiryReplyDto> result = inquiryReplyService.getInquiryReplies(currentUser, productId, inquiryId, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
        then(inquiryReplyRepository).should().findAllByCursor(eq(inquiryId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(inquiryReplyMapper).should().toDto(eq(inquiryReply), eq(currentUser));
    }

    @Test
    @DisplayName("문의 답변 목록 조회 실패 - 상품을 찾을 수 없음")
    void getInquiryReplies_ProductNotFound() {
        // given
        Long productId = 999L;
        Long inquiryId = 1L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        given(productRepository.findById(eq(productId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> inquiryReplyService.getInquiryReplies(currentUser, productId, inquiryId, keyword, orderBy, direction, cursor, after, limit))
                .isInstanceOf(ProductException.class);

        then(productRepository).should().findById(eq(productId));
    }

    @Test
    @DisplayName("문의 답변 목록 조회 실패 - 문의를 찾을 수 없음")
    void getInquiryReplies_InquiryNotFound() {
        // given
        Long productId = 1L;
        Long inquiryId = 999L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> inquiryReplyService.getInquiryReplies(currentUser, productId, inquiryId, keyword, orderBy, direction, cursor, after, limit))
                .isInstanceOf(InquiryException.class);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
    }

    @Test
    @DisplayName("문의 답변 목록 조회 실패 - 문의가 상품에 속하지 않음")
    void getInquiryReplies_InquiryNotBelongsToProduct() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        Product otherProduct = Product.createProduct(
                "다른 상품",
                "other.jpg",
                20000,
                50,
                new ArrayList<>(),
                currentUser
        );

        Inquiry otherInquiry = Inquiry.createInquiry(
                "다른 문의",
                "다른 문의 내용",
                false,
                otherProduct,
                currentUser
        );

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(otherInquiry));

        // when & then
        assertThatThrownBy(() -> inquiryReplyService.getInquiryReplies(currentUser, productId, inquiryId, keyword, orderBy, direction, cursor, after, limit))
                .isInstanceOf(InquiryException.class);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
    }

    @Test
    @DisplayName("문의 답변 수정 실패 - 상품을 찾을 수 없음")
    void updateInquiryReply_ProductNotFound() {
        // given
        Long productId = 999L;
        Long inquiryId = 1L;
        Long inquiryReplyId = 1L;

        given(productRepository.findById(eq(productId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> inquiryReplyService.updateInquiryReply(currentUser, productId, inquiryId, inquiryReplyId, updateRequestDto))
                .isInstanceOf(ProductException.class);

        then(productRepository).should().findById(eq(productId));
    }

    @Test
    @DisplayName("문의 답변 수정 실패 - 문의를 찾을 수 없음")
    void updateInquiryReply_InquiryNotFound() {
        // given
        Long productId = 1L;
        Long inquiryId = 999L;
        Long inquiryReplyId = 1L;

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> inquiryReplyService.updateInquiryReply(currentUser, productId, inquiryId, inquiryReplyId, updateRequestDto))
                .isInstanceOf(InquiryException.class);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
    }

    @Test
    @DisplayName("문의 답변 수정 실패 - 문의가 상품에 속하지 않음")
    void updateInquiryReply_InquiryNotBelongsToProduct() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        Long inquiryReplyId = 1L;

        Product otherProduct = Product.createProduct(
                "다른 상품",
                "other.jpg",
                20000,
                50,
                new ArrayList<>(),
                currentUser
        );

        Inquiry otherInquiry = Inquiry.createInquiry(
                "다른 문의",
                "다른 문의 내용",
                false,
                otherProduct,
                currentUser
        );

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(otherInquiry));

        // when & then
        assertThatThrownBy(() -> inquiryReplyService.updateInquiryReply(currentUser, productId, inquiryId, inquiryReplyId, updateRequestDto))
                .isInstanceOf(InquiryException.class);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
    }

    @Test
    @DisplayName("문의 답변 삭제 실패 - 상품을 찾을 수 없음")
    void deleteInquiryReply_ProductNotFound() {
        // given
        Long productId = 999L;
        Long inquiryId = 1L;
        Long inquiryReplyId = 1L;

        given(productRepository.findById(eq(productId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> inquiryReplyService.deleteInquiryReply(productId, inquiryId, inquiryReplyId))
                .isInstanceOf(ProductException.class);

        then(productRepository).should().findById(eq(productId));
    }

    @Test
    @DisplayName("문의 답변 삭제 실패 - 문의를 찾을 수 없음")
    void deleteInquiryReply_InquiryNotFound() {
        // given
        Long productId = 1L;
        Long inquiryId = 999L;
        Long inquiryReplyId = 1L;

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> inquiryReplyService.deleteInquiryReply(productId, inquiryId, inquiryReplyId))
                .isInstanceOf(InquiryException.class);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
    }

    @Test
    @DisplayName("문의 답변 삭제 실패 - 문의가 상품에 속하지 않음")
    void deleteInquiryReply_InquiryNotBelongsToProduct() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        Long inquiryReplyId = 1L;

        Product otherProduct = Product.createProduct(
                "다른 상품",
                "other.jpg",
                20000,
                50,
                new ArrayList<>(),
                currentUser
        );

        Inquiry otherInquiry = Inquiry.createInquiry(
                "다른 문의",
                "다른 문의 내용",
                false,
                otherProduct,
                currentUser
        );

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(otherInquiry));

        // when & then
        assertThatThrownBy(() -> inquiryReplyService.deleteInquiryReply(productId, inquiryId, inquiryReplyId))
                .isInstanceOf(InquiryException.class);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
    }

    @Test
    @DisplayName("문의 답변 목록 조회 성공 - 커서와 after 파라미터 사용")
    void getInquiryReplies_WithCursorAndAfter() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = "2024-01-01T12:00:00";
        Long after = 5L;
        int limit = 10;

        InquiryReply reply1 = InquiryReply.createInquiryReply("답변 6", currentUser);
        ReflectionTestUtils.setField(reply1, "id", 6L);

        InquiryReply reply2 = InquiryReply.createInquiryReply("답변 7", currentUser);
        ReflectionTestUtils.setField(reply2, "id", 7L);

        List<InquiryReply> inquiryReplies = Arrays.asList(reply1, reply2);

        InquiryReplyDto replyDto1 = InquiryReplyDto.builder().id(6L).content("답변 6").build();
        InquiryReplyDto replyDto2 = InquiryReplyDto.builder().id(7L).content("답변 7").build();

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(inquiry));
        given(inquiryReplyRepository.findAllByCursor(eq(inquiryId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(inquiryReplies);
        given(inquiryReplyMapper.toDto(eq(reply1), eq(currentUser))).willReturn(replyDto1);
        given(inquiryReplyMapper.toDto(eq(reply2), eq(currentUser))).willReturn(replyDto2);

        // when
        CursorPageResponseDto<InquiryReplyDto> result = inquiryReplyService.getInquiryReplies(currentUser, productId, inquiryId, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isFalse();

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
        then(inquiryReplyRepository).should().findAllByCursor(eq(inquiryId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(inquiryReplyMapper).should().toDto(eq(reply1), eq(currentUser));
        then(inquiryReplyMapper).should().toDto(eq(reply2), eq(currentUser));
    }

    @Test
    @DisplayName("문의 답변 목록 조회 성공 - direction asc")
    void getInquiryReplies_DirectionAsc() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "asc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        List<InquiryReply> inquiryReplies = Collections.singletonList(inquiryReply);

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(inquiry));
        given(inquiryReplyRepository.findAllByCursor(eq(inquiryId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(inquiryReplies);
        given(inquiryReplyMapper.toDto(eq(inquiryReply), eq(currentUser))).willReturn(inquiryReplyDto);

        // when
        CursorPageResponseDto<InquiryReplyDto> result = inquiryReplyService.getInquiryReplies(currentUser, productId, inquiryId, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
        then(inquiryReplyRepository).should().findAllByCursor(eq(inquiryId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(inquiryReplyMapper).should().toDto(eq(inquiryReply), eq(currentUser));
    }

    @Test
    @DisplayName("문의 답변 목록 조회 성공 - limit이 1이고 결과가 2개인 경우")
    void getInquiryReplies_LimitOne_HasNext() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 1;

        InquiryReply reply1 = InquiryReply.createInquiryReply("첫번째 답변", currentUser);
        ReflectionTestUtils.setField(reply1, "id", 1L);
        ReflectionTestUtils.setField(reply1, "createdAt", LocalDateTime.of(2024, 1, 1, 12, 0));

        InquiryReply reply2 = InquiryReply.createInquiryReply("두번째 답변", currentUser);
        ReflectionTestUtils.setField(reply2, "id", 2L);
        ReflectionTestUtils.setField(reply2, "createdAt", LocalDateTime.of(2024, 1, 1, 13, 0));

        List<InquiryReply> inquiryReplies = Arrays.asList(reply1, reply2);

        InquiryReplyDto replyDto1 = InquiryReplyDto.builder().id(1L).content("첫번째 답변").build();

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(inquiry));
        given(inquiryReplyRepository.findAllByCursor(eq(inquiryId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(inquiryReplies);
        given(inquiryReplyMapper.toDto(eq(reply1), eq(currentUser))).willReturn(replyDto1);

        // when
        CursorPageResponseDto<InquiryReplyDto> result = inquiryReplyService.getInquiryReplies(currentUser, productId, inquiryId, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isEqualTo("2024-01-01T12:00");
        assertThat(result.getNextAfter()).isEqualTo(1L);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
        then(inquiryReplyRepository).should().findAllByCursor(eq(inquiryId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(inquiryReplyMapper).should().toDto(eq(reply1), eq(currentUser));
    }

    @Test
    @DisplayName("문의 답변 목록 조회 성공 - 정확히 limit 개수만큼 결과")
    void getInquiryReplies_ExactlyLimit() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 3;

        InquiryReply reply1 = InquiryReply.createInquiryReply("첫번째 답변", currentUser);
        ReflectionTestUtils.setField(reply1, "id", 1L);

        InquiryReply reply2 = InquiryReply.createInquiryReply("두번째 답변", currentUser);
        ReflectionTestUtils.setField(reply2, "id", 2L);

        InquiryReply reply3 = InquiryReply.createInquiryReply("세번째 답변", currentUser);
        ReflectionTestUtils.setField(reply3, "id", 3L);

        List<InquiryReply> inquiryReplies = Arrays.asList(reply1, reply2, reply3);

        InquiryReplyDto replyDto1 = InquiryReplyDto.builder().id(1L).content("첫번째 답변").build();
        InquiryReplyDto replyDto2 = InquiryReplyDto.builder().id(2L).content("두번째 답변").build();
        InquiryReplyDto replyDto3 = InquiryReplyDto.builder().id(3L).content("세번째 답변").build();

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(inquiry));
        given(inquiryReplyRepository.findAllByCursor(eq(inquiryId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(inquiryReplies);
        given(inquiryReplyMapper.toDto(eq(reply1), eq(currentUser))).willReturn(replyDto1);
        given(inquiryReplyMapper.toDto(eq(reply2), eq(currentUser))).willReturn(replyDto2);
        given(inquiryReplyMapper.toDto(eq(reply3), eq(currentUser))).willReturn(replyDto3);

        // when
        CursorPageResponseDto<InquiryReplyDto> result = inquiryReplyService.getInquiryReplies(currentUser, productId, inquiryId, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
        then(inquiryReplyRepository).should().findAllByCursor(eq(inquiryId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(inquiryReplyMapper).should().toDto(eq(reply1), eq(currentUser));
        then(inquiryReplyMapper).should().toDto(eq(reply2), eq(currentUser));
        then(inquiryReplyMapper).should().toDto(eq(reply3), eq(currentUser));
    }

    @Test
    @DisplayName("문의 답변 생성 성공 - inquiry에 reply 추가 검증")
    void createInquiryReply_VerifyAddReplyToInquiry() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;

        InquiryReply newReply = InquiryReply.createInquiryReply(createRequestDto.getContent(), currentUser);
        ReflectionTestUtils.setField(newReply, "id", 2L);

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(inquiry));
        given(inquiryReplyRepository.save(any(InquiryReply.class))).willReturn(newReply);
        given(inquiryReplyMapper.toDto(any(InquiryReply.class), eq(currentUser))).willReturn(inquiryReplyDto);

        // when
        InquiryReplyDto result = inquiryReplyService.createInquiryReply(currentUser, productId, inquiryId, createRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(inquiryReplyDto);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
        then(inquiryReplyRepository).should().save(any(InquiryReply.class));
        then(inquiryReplyMapper).should().toDto(any(InquiryReply.class), eq(currentUser));
    }

    @Test
    @DisplayName("문의 답변 조회 성공 - 답변이 문의에 속함 검증")
    void getInquiryReply_VerifyReplyBelongsToInquiry() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        Long inquiryReplyId = 1L;

        InquiryReply reply = InquiryReply.createInquiryReply("답변 내용", currentUser);
        ReflectionTestUtils.setField(reply, "id", 1L);
        inquiry.addInquiryReply(reply);

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(inquiry));
        given(inquiryReplyRepository.findById(eq(inquiryReplyId))).willReturn(Optional.of(reply));
        given(inquiryReplyMapper.toDto(eq(reply), eq(currentUser))).willReturn(inquiryReplyDto);

        // when
        InquiryReplyDto result = inquiryReplyService.getInquiryReply(currentUser, productId, inquiryId, inquiryReplyId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(inquiryReplyDto);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
        then(inquiryReplyRepository).should().findById(eq(inquiryReplyId));
        then(inquiryReplyMapper).should().toDto(eq(reply), eq(currentUser));
    }

    @Test
    @DisplayName("문의 답변 수정 성공 - 답변 내용 업데이트 검증")
    void updateInquiryReply_VerifyContentUpdate() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        Long inquiryReplyId = 1L;

        InquiryReply reply = InquiryReply.createInquiryReply("원본 답변", currentUser);
        ReflectionTestUtils.setField(reply, "id", 1L);
        inquiry.addInquiryReply(reply);

        InquiryReplyDto updatedDto = InquiryReplyDto.builder()
                .id(1L)
                .content("수정된 답변입니다.")
                .build();

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(inquiry));
        given(inquiryReplyRepository.findById(eq(inquiryReplyId))).willReturn(Optional.of(reply));
        given(inquiryReplyMapper.toDto(eq(reply), eq(currentUser))).willReturn(updatedDto);

        // when
        InquiryReplyDto result = inquiryReplyService.updateInquiryReply(currentUser, productId, inquiryId, inquiryReplyId, updateRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(updatedDto);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
        then(inquiryReplyRepository).should().findById(eq(inquiryReplyId));
        then(inquiryReplyMapper).should().toDto(eq(reply), eq(currentUser));
    }

    @Test
    @DisplayName("문의 답변 삭제 성공 - 답변이 문의에 속함 검증")
    void deleteInquiryReply_VerifyReplyBelongsToInquiry() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        Long inquiryReplyId = 1L;

        InquiryReply reply = InquiryReply.createInquiryReply("답변 내용", currentUser);
        ReflectionTestUtils.setField(reply, "id", 1L);
        inquiry.addInquiryReply(reply);

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(inquiry));
        given(inquiryReplyRepository.findById(eq(inquiryReplyId))).willReturn(Optional.of(reply));

        // when
        inquiryReplyService.deleteInquiryReply(productId, inquiryId, inquiryReplyId);

        // then
        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
        then(inquiryReplyRepository).should().findById(eq(inquiryReplyId));
        then(inquiryReplyRepository).should().delete(eq(reply));
    }
}