package travel.mytravelplan.domain.inquiry.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import travel.mytravelplan.global.error.code.InquiryErrorCode;
import travel.mytravelplan.global.error.code.ProductErrorCode;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InquiryService {
    private final ProductRepository productRepository;
    private final InquiryRepository inquiryRepository;
    private final InquiryMapper inquiryMapper;

    @Transactional
    public InquiryDto createInquiry(User currentUser, Long productId, InquiryCreateRequestDto inquiryCreateRequestDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        Inquiry inquiry = Inquiry.createInquiry(
                inquiryCreateRequestDto.getTitle(),
                inquiryCreateRequestDto.getContent(),
                inquiryCreateRequestDto.isSecret(),
                product,
                currentUser
        );

        inquiryRepository.save(inquiry);

        return inquiryMapper.toDto(inquiry, currentUser);
    }

    public CursorPageResponseDto<InquiryDto> getInquiries(User currentUser, Long productId, String keyword, boolean secretOnly, boolean answerOnly, String orderBy, String direction, String cursor, Long after, int limit) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        List<Inquiry> inquiries = inquiryRepository.findAllByCursor(product.getId(), keyword, secretOnly, answerOnly, orderBy, direction, cursor, after, limit + 1);

        boolean hasNext = inquiries.size() > limit;

        List<Inquiry> pagedInquiries = hasNext ? inquiries.subList(0, limit) : inquiries;

        List<InquiryDto> inquiryDtos = pagedInquiries.stream()
                .map(inquiry -> inquiryMapper.toDto(inquiry, currentUser))
                .toList();

        String nextCursor = null;
        Long nextAfter = null;

        if (hasNext) {
            Inquiry lastInquiry = pagedInquiries.getLast();

            if (orderBy.equals("createdAt")) {
                nextCursor = lastInquiry.getCreatedAt().toString();
            }

            nextAfter = lastInquiry.getId();
        }

        return CursorPageResponseDto.<InquiryDto>builder()
                .content(inquiryDtos)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(inquiryDtos.size())
                .hasNext(hasNext)
                .build();
    }

    public InquiryDto getInquiry(User currentUser, Long productId, Long inquiryId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        validateInquiryBelongsToProduct(inquiry, product);

        return inquiryMapper.toDto(inquiry, currentUser);
    }

    @Transactional
    public InquiryDto updateInquiry(User currentUser, Long productId, Long inquiryId, InquiryUpdateRequestDto inquiryUpdateRequestDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        validateInquiryBelongsToProduct(inquiry, product);

        inquiry.update(
                inquiryUpdateRequestDto.getTitle(),
                inquiryUpdateRequestDto.getContent(),
                inquiryUpdateRequestDto.isSecret()
        );

        return inquiryMapper.toDto(inquiry, currentUser);
    }

    @Transactional
    public void deleteInquiry(Long productId, Long inquiryId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        validateInquiryBelongsToProduct(inquiry, product);

        inquiryRepository.delete(inquiry);
    }

    private void validateInquiryBelongsToProduct(Inquiry inquiry, Product product) {
        if (!inquiry.getProduct().equals(product)) {
            throw new InquiryException(InquiryErrorCode.INQUIRY_NOT_BELONGS_TO_PRODUCT);
        }
    }
}
