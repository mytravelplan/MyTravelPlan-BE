package travel.mytravelplan.domain.inquiry.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.budget.dto.BudgetDto;
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
import travel.mytravelplan.global.error.code.InquiryErrorCode;
import travel.mytravelplan.global.error.code.ProductErrorCode;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InquiryReplyService {
    private final InquiryRepository inquiryRepository;
    private final ProductRepository productRepository;
    private final InquiryReplyRepository inquiryReplyRepository;
    private final InquiryReplyMapper inquiryReplyMapper;

    @Transactional
    public InquiryReplyDto createInquiryReply(User currentUser, Long productId, Long inquiryId, InquiryReplyCreateRequestDto inquiryReplyCreateRequestDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        validateInquiryBelongsToProduct(inquiry, product);

        InquiryReply inquiryReply = InquiryReply.createInquiryReply(inquiryReplyCreateRequestDto.getContent(), currentUser);

        inquiry.addInquiryReply(inquiryReply);

        inquiryReplyRepository.save(inquiryReply);

        return inquiryReplyMapper.toDto(inquiryReply, currentUser);
    }

    public InquiryReplyDto getInquiryReply(User currentUser, Long productId, Long inquiryId, Long inquiryReplyId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        validateInquiryBelongsToProduct(inquiry, product);

        InquiryReply inquiryReply = inquiryReplyRepository.findById(inquiryReplyId)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        return inquiryReplyMapper.toDto(inquiryReply, currentUser);
    }

    public CursorPageResponseDto<InquiryReplyDto> getInquiryReplies(User currentUser, Long productId, Long inquiryId, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        validateInquiryBelongsToProduct(inquiry, product);

        List<InquiryReply> inquiryReplies = inquiryReplyRepository.findAllByCursor(inquiry.getId(), keyword, orderBy, direction, cursor, after, limit + 1);

        boolean hasNext = inquiryReplies.size() > limit;

        List<InquiryReply> pagedInquiryReplies = hasNext ? inquiryReplies.subList(0, limit) : inquiryReplies;

        List<InquiryReplyDto> inquiryReplyDtos = pagedInquiryReplies.stream()
                .map(inquiryReply -> inquiryReplyMapper.toDto(inquiryReply, currentUser))
                .toList();

        String nextCursor = null;
        Long nextAfter = null;

        if (hasNext) {
            InquiryReply lastInquiryReply = pagedInquiryReplies.getLast();

            if (orderBy.equals("createdAt")) {
                nextCursor = lastInquiryReply.getCreatedAt().toString();
            }

            nextAfter = lastInquiryReply.getId();
        }

        return CursorPageResponseDto.<InquiryReplyDto>builder()
                .content(inquiryReplyDtos)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(inquiryReplyDtos.size())
                .hasNext(hasNext)
                .build();
    }

    @Transactional
    public InquiryReplyDto updateInquiryReply(User currentUser, Long productId, Long inquiryId, Long inquiryReplyId, InquiryReplyUpdateRequestDto inquiryReplyUpdateRequestDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        validateInquiryBelongsToProduct(inquiry, product);

        InquiryReply inquiryReply = inquiryReplyRepository.findById(inquiryReplyId)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        inquiryReply.update(inquiryReplyUpdateRequestDto.getContent());

        return inquiryReplyMapper.toDto(inquiryReply, currentUser);
    }

    @Transactional
    public void deleteInquiryReply(Long productId, Long inquiryId, Long inquiryReplyId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        validateInquiryBelongsToProduct(inquiry, product);

        InquiryReply inquiryReply = inquiryReplyRepository.findById(inquiryReplyId)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        inquiryReplyRepository.delete(inquiryReply);
    }

    private void validateInquiryBelongsToProduct(Inquiry inquiry, Product product) {
        if (!inquiry.getProduct().equals(product)) {
            throw new InquiryException(InquiryErrorCode.INQUIRY_NOT_BELONGS_TO_PRODUCT);
        }
    }
}
