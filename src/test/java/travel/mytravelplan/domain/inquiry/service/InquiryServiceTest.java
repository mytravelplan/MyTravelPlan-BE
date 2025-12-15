package travel.mytravelplan.domain.inquiry.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.inquiry.dto.InquiryCreateRequestDto;
import travel.mytravelplan.domain.inquiry.dto.InquiryDto;
import travel.mytravelplan.domain.inquiry.dto.InquiryUpdateRequestDto;
import travel.mytravelplan.domain.inquiry.entity.Inquiry;
import travel.mytravelplan.domain.inquiry.exception.InquiryException;
import travel.mytravelplan.domain.inquiry.mapper.InquiryMapper;
import travel.mytravelplan.domain.inquiry.repsotiroy.InquiryRepository;
import travel.mytravelplan.domain.product.entity.Product;
import travel.mytravelplan.domain.product.exception.ProductException;
import travel.mytravelplan.domain.product.repository.ProductRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.support.ServiceTestSupport;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("문의 서비스 테스트")
class InquiryServiceTest extends ServiceTestSupport {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private InquiryMapper inquiryMapper;

    @InjectMocks
    private InquiryService inquiryService;

    private User user;
    private Product product;
    private Inquiry inquiry;
    private InquiryDto inquiryDto;
    private InquiryCreateRequestDto createRequestDto;
    private InquiryUpdateRequestDto updateRequestDto;

    @BeforeEach
    void setUp() {
        user = User.createUser("testuser", "password", "test@test.com", null, null, null);
        product = Product.createProduct("상품명", "imageUrl", 10000, 100, List.of(), null);
        inquiry = Inquiry.createInquiry("문의 제목", "문의 내용", false, product, user);

        ReflectionTestUtils.setField(product, "id", 1L);

        inquiryDto = InquiryDto.builder()
                .id(1L)
                .title("문의 제목")
                .content("문의 내용")
                .answered(false)
                .secret(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequestDto = InquiryCreateRequestDto.builder()
                .title("문의 제목")
                .content("문의 내용")
                .secret(false)
                .build();

        updateRequestDto = InquiryUpdateRequestDto.builder()
                .title("수정된 제목")
                .content("수정된 내용")
                .secret(true)
                .build();
    }

    @Test
    @DisplayName("문의 생성 성공")
    void createInquiry_Success() {
        // given
        Long productId = 1L;
        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.save(any(Inquiry.class))).willReturn(inquiry);
        given(inquiryMapper.toDto(any(Inquiry.class), eq(user))).willReturn(inquiryDto);

        // when
        InquiryDto result = inquiryService.createInquiry(user, productId, createRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(inquiryDto);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().save(any(Inquiry.class));
        then(inquiryMapper).should().toDto(any(Inquiry.class), eq(user));
    }

    @Test
    @DisplayName("문의 생성 실패 - 상품을 찾을 수 없음")
    void createInquiry_ProductNotFound() {
        // given
        Long productId = 999L;
        given(productRepository.findById(eq(productId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> inquiryService.createInquiry(user, productId, createRequestDto))
                .isInstanceOf(ProductException.class);

        then(productRepository).should().findById(eq(productId));
    }

    @Test
    @DisplayName("문의 목록 조회 성공")
    void getInquiries_Success() {
        // given
        Long productId = 1L;
        String keyword = "제목";
        boolean secretOnly = false;
        boolean answerOnly = false;
        String orderBy = "createdAt";
        String direction = "DESC";
        String cursor = null;
        Long after = null;
        int limit = 10;

        Inquiry inquiry1 = Inquiry.createInquiry("문의1", "내용1", false, product, user);
        Inquiry inquiry2 = Inquiry.createInquiry("문의2", "내용2", false, product, user);
        List<Inquiry> inquiries = Arrays.asList(inquiry1, inquiry2);

        InquiryDto inquiryDto1 = InquiryDto.builder()
                .id(1L)
                .title("문의1")
                .content("내용1")
                .build();

        InquiryDto inquiryDto2 = InquiryDto.builder()
                .id(2L)
                .title("문의2")
                .content("내용2")
                .build();

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findAllByCursor(eq(productId), eq(keyword), eq(secretOnly), eq(answerOnly), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(inquiries);
        given(inquiryMapper.toDto(eq(inquiry1), eq(user))).willReturn(inquiryDto1);
        given(inquiryMapper.toDto(eq(inquiry2), eq(user))).willReturn(inquiryDto2);

        // when
        CursorPageResponseDto<InquiryDto> result = inquiryService.getInquiries(user, productId, keyword, secretOnly, answerOnly, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getSize()).isEqualTo(2);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findAllByCursor(eq(productId), eq(keyword), eq(secretOnly), eq(answerOnly), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("문의 목록 조회 실패 - 상품을 찾을 수 없음")
    void getInquiries_ProductNotFound() {
        // given
        Long productId = 999L;
        given(productRepository.findById(eq(productId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> inquiryService.getInquiries(user, productId, null, false, false, "createdAt", "DESC", null, null, 10))
                .isInstanceOf(ProductException.class);

        then(productRepository).should().findById(eq(productId));
    }

    @Test
    @DisplayName("문의 단건 조회 성공")
    void getInquiry_Success() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(inquiry));
        given(inquiryMapper.toDto(any(Inquiry.class), eq(user))).willReturn(inquiryDto);

        // when
        InquiryDto result = inquiryService.getInquiry(user, productId, inquiryId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(inquiryDto);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
        then(inquiryMapper).should().toDto(any(Inquiry.class), eq(user));
    }

    @Test
    @DisplayName("문의 단건 조회 실패 - 상품을 찾을 수 없음")
    void getInquiry_ProductNotFound() {
        // given
        Long productId = 999L;
        Long inquiryId = 1L;
        given(productRepository.findById(eq(productId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> inquiryService.getInquiry(user, productId, inquiryId))
                .isInstanceOf(ProductException.class);

        then(productRepository).should().findById(eq(productId));
    }

    @Test
    @DisplayName("문의 단건 조회 실패 - 문의를 찾을 수 없음")
    void getInquiry_InquiryNotFound() {
        // given
        Long productId = 1L;
        Long inquiryId = 999L;
        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> inquiryService.getInquiry(user, productId, inquiryId))
                .isInstanceOf(InquiryException.class);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
    }

    @Test
    @DisplayName("문의 수정 성공")
    void updateInquiry_Success() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        InquiryDto updatedDto = InquiryDto.builder()
                .id(1L)
                .title("수정된 제목")
                .content("수정된 내용")
                .secret(true)
                .build();

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(inquiry));
        given(inquiryMapper.toDto(any(Inquiry.class), eq(user))).willReturn(updatedDto);

        // when
        InquiryDto result = inquiryService.updateInquiry(user, productId, inquiryId, updateRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(updatedDto);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
        then(inquiryMapper).should().toDto(any(Inquiry.class), eq(user));
    }

    @Test
    @DisplayName("문의 수정 실패 - 상품을 찾을 수 없음")
    void updateInquiry_ProductNotFound() {
        // given
        Long productId = 999L;
        Long inquiryId = 1L;
        given(productRepository.findById(eq(productId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> inquiryService.updateInquiry(user, productId, inquiryId, updateRequestDto))
                .isInstanceOf(ProductException.class);

        then(productRepository).should().findById(eq(productId));
    }

    @Test
    @DisplayName("문의 수정 실패 - 문의를 찾을 수 없음")
    void updateInquiry_InquiryNotFound() {
        // given
        Long productId = 1L;
        Long inquiryId = 999L;
        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> inquiryService.updateInquiry(user, productId, inquiryId, updateRequestDto))
                .isInstanceOf(InquiryException.class);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
    }

    @Test
    @DisplayName("문의 삭제 성공")
    void deleteInquiry_Success() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(inquiry));

        // when
        inquiryService.deleteInquiry(productId, inquiryId);

        // then
        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
        then(inquiryRepository).should().delete(any(Inquiry.class));
    }

    @Test
    @DisplayName("문의 삭제 실패 - 상품을 찾을 수 없음")
    void deleteInquiry_ProductNotFound() {
        // given
        Long productId = 999L;
        Long inquiryId = 1L;
        given(productRepository.findById(eq(productId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> inquiryService.deleteInquiry(productId, inquiryId))
                .isInstanceOf(ProductException.class);

        then(productRepository).should().findById(eq(productId));
    }

    @Test
    @DisplayName("문의 삭제 실패 - 문의를 찾을 수 없음")
    void deleteInquiry_InquiryNotFound() {
        // given
        Long productId = 1L;
        Long inquiryId = 999L;
        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> inquiryService.deleteInquiry(productId, inquiryId))
                .isInstanceOf(InquiryException.class);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
    }

    @Test
    @DisplayName("문의 조회 실패 - 문의가 해당 상품에 속하지 않음")
    void getInquiry_InquiryNotBelongsToProduct() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        Product anotherProduct = Product.createProduct("다른 상품", "imageUrl2", 20000, 50, List.of(), null);
        ReflectionTestUtils.setField(anotherProduct, "id", 2L);
        Inquiry inquiryOfAnotherProduct = Inquiry.createInquiry("문의 제목", "문의 내용", false, anotherProduct, user);

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(inquiryOfAnotherProduct));

        // when & then
        assertThatThrownBy(() -> inquiryService.getInquiry(user, productId, inquiryId))
                .isInstanceOf(InquiryException.class);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
    }

    @Test
    @DisplayName("문의 수정 실패 - 문의가 해당 상품에 속하지 않음")
    void updateInquiry_InquiryNotBelongsToProduct() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        Product anotherProduct = Product.createProduct("다른 상품", "imageUrl2", 20000, 50, List.of(), null);
        ReflectionTestUtils.setField(anotherProduct, "id", 2L);
        Inquiry inquiryOfAnotherProduct = Inquiry.createInquiry("문의 제목", "문의 내용", false, anotherProduct, user);

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(inquiryOfAnotherProduct));

        // when & then
        assertThatThrownBy(() -> inquiryService.updateInquiry(user, productId, inquiryId, updateRequestDto))
                .isInstanceOf(InquiryException.class);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
    }

    @Test
    @DisplayName("문의 삭제 실패 - 문의가 해당 상품에 속하지 않음")
    void deleteInquiry_InquiryNotBelongsToProduct() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;
        Product anotherProduct = Product.createProduct("다른 상품", "imageUrl2", 20000, 50, List.of(), null);
        ReflectionTestUtils.setField(anotherProduct, "id", 2L);
        Inquiry inquiryOfAnotherProduct = Inquiry.createInquiry("문의 제목", "문의 내용", false, anotherProduct, user);

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(inquiryOfAnotherProduct));

        // when & then
        assertThatThrownBy(() -> inquiryService.deleteInquiry(productId, inquiryId))
                .isInstanceOf(InquiryException.class);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
    }

    @Test
    @DisplayName("문의 목록 조회 성공 - 다음 페이지가 있는 경우")
    void getInquiries_WithNextPage() {
        // given
        Long productId = 1L;
        String orderBy = "createdAt";
        String direction = "DESC";
        int limit = 2;

        Inquiry inquiry1 = Inquiry.createInquiry("문의1", "내용1", false, product, user);
        Inquiry inquiry2 = Inquiry.createInquiry("문의2", "내용2", false, product, user);
        Inquiry inquiry3 = Inquiry.createInquiry("문의3", "내용3", false, product, user);

        ReflectionTestUtils.setField(inquiry1, "id", 1L);
        ReflectionTestUtils.setField(inquiry2, "id", 2L);
        ReflectionTestUtils.setField(inquiry3, "id", 3L);
        ReflectionTestUtils.setField(inquiry1, "createdAt", LocalDateTime.of(2025, 12, 1, 10, 0));
        ReflectionTestUtils.setField(inquiry2, "createdAt", LocalDateTime.of(2025, 12, 2, 10, 0));
        ReflectionTestUtils.setField(inquiry3, "createdAt", LocalDateTime.of(2025, 12, 3, 10, 0));

        List<Inquiry> inquiries = Arrays.asList(inquiry1, inquiry2, inquiry3);

        InquiryDto inquiryDto1 = InquiryDto.builder().id(1L).title("문의1").content("내용1").build();
        InquiryDto inquiryDto2 = InquiryDto.builder().id(2L).title("문의2").content("내용2").build();

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findAllByCursor(eq(productId), eq(null), eq(false), eq(false), eq(orderBy), eq(direction), eq(null), eq(null), eq(limit + 1)))
                .willReturn(inquiries);
        given(inquiryMapper.toDto(eq(inquiry1), eq(user))).willReturn(inquiryDto1);
        given(inquiryMapper.toDto(eq(inquiry2), eq(user))).willReturn(inquiryDto2);

        // when
        CursorPageResponseDto<InquiryDto> result = inquiryService.getInquiries(user, productId, null, false, false, orderBy, direction, null, null, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getNextCursor()).isEqualTo("2025-12-02T10:00");
        assertThat(result.getNextAfter()).isEqualTo(2L);

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findAllByCursor(eq(productId), eq(null), eq(false), eq(false), eq(orderBy), eq(direction), eq(null), eq(null), eq(limit + 1));
    }

    @Test
    @DisplayName("문의 목록 조회 성공 - 키워드 검색")
    void getInquiries_WithKeyword() {
        // given
        Long productId = 1L;
        String keyword = "특정";
        Inquiry inquiry1 = Inquiry.createInquiry("특정 문의", "내용", false, product, user);

        InquiryDto inquiryDto1 = InquiryDto.builder().id(1L).title("특정 문의").content("내용").build();

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findAllByCursor(eq(productId), eq(keyword), eq(false), eq(false), eq("createdAt"), eq("DESC"), eq(null), eq(null), eq(11)))
                .willReturn(List.of(inquiry1));
        given(inquiryMapper.toDto(eq(inquiry1), eq(user))).willReturn(inquiryDto1);

        // when
        CursorPageResponseDto<InquiryDto> result = inquiryService.getInquiries(user, productId, keyword, false, false, "createdAt", "DESC", null, null, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).contains("특정");

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findAllByCursor(eq(productId), eq(keyword), eq(false), eq(false), eq("createdAt"), eq("DESC"), eq(null), eq(null), eq(11));
    }

    @Test
    @DisplayName("문의 목록 조회 성공 - 비밀글만 조회")
    void getInquiries_SecretOnly() {
        // given
        Long productId = 1L;
        boolean secretOnly = true;
        Inquiry inquiry1 = Inquiry.createInquiry("비밀 문의", "내용", true, product, user);

        InquiryDto inquiryDto1 = InquiryDto.builder().id(1L).title("비밀 문의").secret(true).build();

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findAllByCursor(eq(productId), eq(null), eq(secretOnly), eq(false), eq("createdAt"), eq("DESC"), eq(null), eq(null), eq(11)))
                .willReturn(List.of(inquiry1));
        given(inquiryMapper.toDto(eq(inquiry1), eq(user))).willReturn(inquiryDto1);

        // when
        CursorPageResponseDto<InquiryDto> result = inquiryService.getInquiries(user, productId, null, secretOnly, false, "createdAt", "DESC", null, null, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).isSecret()).isTrue();

        then(productRepository).should().findById(eq(productId));
    }

    @Test
    @DisplayName("문의 목록 조회 성공 - 답변된 문의만 조회")
    void getInquiries_AnswerOnly() {
        // given
        Long productId = 1L;
        boolean answerOnly = true;
        Inquiry inquiry1 = Inquiry.createInquiry("답변된 문의", "내용", false, product, user);

        InquiryDto inquiryDto1 = InquiryDto.builder().id(1L).title("답변된 문의").answered(true).build();

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findAllByCursor(eq(productId), eq(null), eq(false), eq(answerOnly), eq("createdAt"), eq("DESC"), eq(null), eq(null), eq(11)))
                .willReturn(List.of(inquiry1));
        given(inquiryMapper.toDto(eq(inquiry1), eq(user))).willReturn(inquiryDto1);

        // when
        CursorPageResponseDto<InquiryDto> result = inquiryService.getInquiries(user, productId, null, false, answerOnly, "createdAt", "DESC", null, null, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).isAnswered()).isTrue();

        then(productRepository).should().findById(eq(productId));
    }

    @Test
    @DisplayName("문의 목록 조회 성공 - 커서 기반 페이징")
    void getInquiries_WithCursor() {
        // given
        Long productId = 1L;
        String cursor = "2025-12-01T10:00:00";
        Long after = 5L;
        int limit = 10;

        Inquiry inquiry1 = Inquiry.createInquiry("문의1", "내용1", false, product, user);
        InquiryDto inquiryDto1 = InquiryDto.builder().id(6L).title("문의1").build();

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findAllByCursor(eq(productId), eq(null), eq(false), eq(false), eq("createdAt"), eq("DESC"), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(List.of(inquiry1));
        given(inquiryMapper.toDto(eq(inquiry1), eq(user))).willReturn(inquiryDto1);

        // when
        CursorPageResponseDto<InquiryDto> result = inquiryService.getInquiries(user, productId, null, false, false, "createdAt", "DESC", cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        then(inquiryRepository).should().findAllByCursor(eq(productId), eq(null), eq(false), eq(false), eq("createdAt"), eq("DESC"), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("문의 목록 조회 성공 - 빈 목록 반환")
    void getInquiries_EmptyList() {
        // given
        Long productId = 1L;
        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findAllByCursor(eq(productId), eq(null), eq(false), eq(false), eq("createdAt"), eq("DESC"), eq(null), eq(null), eq(11)))
                .willReturn(List.of());

        // when
        CursorPageResponseDto<InquiryDto> result = inquiryService.getInquiries(user, productId, null, false, false, "createdAt", "DESC", null, null, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getSize()).isEqualTo(0);
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findAllByCursor(eq(productId), eq(null), eq(false), eq(false), eq("createdAt"), eq("DESC"), eq(null), eq(null), eq(11));
    }

    @Test
    @DisplayName("문의 목록 조회 성공 - 모든 필터 조건 활성화")
    void getInquiries_AllFiltersEnabled() {
        // given
        Long productId = 1L;
        String keyword = "검색어";
        boolean secretOnly = true;
        boolean answerOnly = true;
        String orderBy = "createdAt";
        String direction = "ASC";

        Inquiry inquiry1 = Inquiry.createInquiry("검색어 포함", "내용", true, product, user);

        InquiryDto inquiryDto1 = InquiryDto.builder()
                .id(1L)
                .title("검색어 포함")
                .content("내용")
                .secret(true)
                .answered(true)
                .build();

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findAllByCursor(eq(productId), eq(keyword), eq(secretOnly), eq(answerOnly), eq(orderBy), eq(direction), eq(null), eq(null), eq(11)))
                .willReturn(List.of(inquiry1));
        given(inquiryMapper.toDto(eq(inquiry1), eq(user))).willReturn(inquiryDto1);

        // when
        CursorPageResponseDto<InquiryDto> result = inquiryService.getInquiries(user, productId, keyword, secretOnly, answerOnly, orderBy, direction, null, null, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).isSecret()).isTrue();
        assertThat(result.getContent().get(0).isAnswered()).isTrue();

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findAllByCursor(eq(productId), eq(keyword), eq(secretOnly), eq(answerOnly), eq(orderBy), eq(direction), eq(null), eq(null), eq(11));
    }

    @Test
    @DisplayName("문의 목록 조회 성공 - limit 정확히 일치하는 경우 hasNext는 false")
    void getInquiries_ExactlyLimitSize_HasNextIsFalse() {
        // given
        Long productId = 1L;
        int limit = 2;

        Inquiry inquiry1 = Inquiry.createInquiry("문의1", "내용1", false, product, user);
        Inquiry inquiry2 = Inquiry.createInquiry("문의2", "내용2", false, product, user);

        List<Inquiry> inquiries = Arrays.asList(inquiry1, inquiry2);

        InquiryDto inquiryDto1 = InquiryDto.builder().id(1L).title("문의1").build();
        InquiryDto inquiryDto2 = InquiryDto.builder().id(2L).title("문의2").build();

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findAllByCursor(eq(productId), eq(null), eq(false), eq(false), eq("createdAt"), eq("DESC"), eq(null), eq(null), eq(limit + 1)))
                .willReturn(inquiries);
        given(inquiryMapper.toDto(eq(inquiry1), eq(user))).willReturn(inquiryDto1);
        given(inquiryMapper.toDto(eq(inquiry2), eq(user))).willReturn(inquiryDto2);

        // when
        CursorPageResponseDto<InquiryDto> result = inquiryService.getInquiries(user, productId, null, false, false, "createdAt", "DESC", null, null, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();

        then(productRepository).should().findById(eq(productId));
    }

    @Test
    @DisplayName("문의 수정 성공 - 답변이 있는 문의도 수정 가능")
    void updateInquiry_WithAnsweredInquiry() {
        // given
        Long productId = 1L;
        Long inquiryId = 1L;

        Inquiry answeredInquiry = Inquiry.createInquiry("원본 제목", "원본 내용", false, product, user);

        InquiryDto updatedDto = InquiryDto.builder()
                .id(1L)
                .title("수정된 제목")
                .content("수정된 내용")
                .secret(true)
                .answered(true)
                .build();

        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
        given(inquiryRepository.findById(eq(inquiryId))).willReturn(Optional.of(answeredInquiry));
        given(inquiryMapper.toDto(any(Inquiry.class), eq(user))).willReturn(updatedDto);

        // when
        InquiryDto result = inquiryService.updateInquiry(user, productId, inquiryId, updateRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("수정된 제목");
        assertThat(result.getContent()).isEqualTo("수정된 내용");
        assertThat(result.isSecret()).isTrue();

        then(productRepository).should().findById(eq(productId));
        then(inquiryRepository).should().findById(eq(inquiryId));
        then(inquiryMapper).should().toDto(any(Inquiry.class), eq(user));
    }
}