package travel.mytravelplan.domain.card.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.card.dto.CardCreateRequestDto;
import travel.mytravelplan.domain.card.dto.CardDto;
import travel.mytravelplan.domain.card.dto.CardUpdateRequestDto;
import travel.mytravelplan.domain.card.service.CardService;
import travel.mytravelplan.global.common.response.ApiResponse;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;

@RestController
@RequestMapping("/api/decks/{deckId}/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    // 카드 생성
    @PostMapping
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#deckId, 'Deck', 'deck:card:create')")
    public ResponseEntity<ApiResponse<CardDto>> createCard(@PathVariable Long deckId, @RequestBody @Validated CardCreateRequestDto cardCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(cardService.createCard(deckId, cardCreateRequestDto)));
    }

    // 카드 조회
    @GetMapping("/{cardId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#deckId, 'Deck', 'deck:card:read')")
    public ResponseEntity<ApiResponse<CardDto>> getCard(@PathVariable Long deckId, @PathVariable Long cardId) {
        return ResponseEntity.ok(ApiResponse.success(cardService.getCard(deckId, cardId)));
    }

    // 카드 목록 조회
    @GetMapping
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#deckId, 'Deck', 'deck:card:read')")
    public ResponseEntity<ApiResponse<CursorPageResponseDto<CardDto>>> getCards(
            @PathVariable Long deckId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt", required = false) String orderBy,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit) {
        return ResponseEntity.ok(ApiResponse.success(cardService.getCards(deckId, keyword, orderBy, direction, cursor, after, limit)));
    }

    // 카드 수정
    @PatchMapping("/{cardId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#deckId, 'Deck', 'deck:card:update')")
    public ResponseEntity<ApiResponse<CardDto>> updateCard(@PathVariable Long deckId, @PathVariable Long cardId, @RequestBody @Validated CardUpdateRequestDto cardUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(cardService.updateCard(deckId, cardId, cardUpdateRequestDto)));
    }

    // 카드 삭제
    @DeleteMapping("/{cardId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#deckId, 'Deck', 'deck:card:delete')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long deckId, @PathVariable Long cardId) {
        cardService.deleteCard(deckId, cardId);
        return ResponseEntity.noContent().build();
    }
}
