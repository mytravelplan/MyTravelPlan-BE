package travel.mytravelplan.domain.deck.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.deck.dto.DeckCreateRequestDto;
import travel.mytravelplan.domain.deck.dto.DeckDto;
import travel.mytravelplan.domain.deck.dto.DeckUpdateRequestDto;
import travel.mytravelplan.domain.deck.service.DeckService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.annotaion.LoginUser;
import travel.mytravelplan.global.common.response.ApiResponse;

@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
public class DeckController {
    private final DeckService deckService;

    // 덱 생성
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DeckDto>> createDeck(@LoginUser User currentUser, @RequestBody @Validated DeckCreateRequestDto deckCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(deckService.createDeck(currentUser, deckCreateRequestDto)));
    }

    // 덱 조회
    @GetMapping("/{deckId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#deckId, 'Deck', 'deck:read')")
    public ResponseEntity<ApiResponse<DeckDto>> getDeck(@PathVariable Long deckId) {
        return ResponseEntity.ok(ApiResponse.success(deckService.getDeck(deckId)));
    }

    // 덱 수정
    @PatchMapping("/{deckId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#deckId, 'Deck', 'deck:update')")
    public ResponseEntity<ApiResponse<DeckDto>> updateDeck(@PathVariable Long deckId, @RequestBody @Validated DeckUpdateRequestDto deckUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(deckService.updateDeck(deckId, deckUpdateRequestDto)));
    }

    // 덱 삭제
    @DeleteMapping("/{deckId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#deckId, 'Deck', 'deck:delete')")
    public ResponseEntity<Void> deleteDeck(@PathVariable Long deckId) {
        deckService.deleteDeck(deckId);
        return ResponseEntity.noContent().build();
    }
}
