package travel.mytravelplan.domain.deck.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.deck.dto.DeckCreateRequestDto;
import travel.mytravelplan.domain.deck.dto.DeckDto;
import travel.mytravelplan.domain.deck.dto.DeckUpdateRequestDto;
import travel.mytravelplan.domain.deck.entity.Deck;
import travel.mytravelplan.domain.deck.exception.DeckException;
import travel.mytravelplan.domain.deck.mapper.DeckMapper;
import travel.mytravelplan.domain.deck.repository.DeckRepository;
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
import static org.mockito.Mockito.mock;

@DisplayName("덱 서비스 테스트")
class DeckServiceTest extends ServiceTestSupport {

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private DeckMapper deckMapper;

    @InjectMocks
    private DeckService deckService;

    private User user;
    private Deck deck;
    private DeckDto deckDto;
    private DeckCreateRequestDto createRequestDto;
    private DeckUpdateRequestDto updateRequestDto;

    @BeforeEach
    void setUp() {
        user = mock(User.class);

        createRequestDto = DeckCreateRequestDto.builder()
                .name("여행 덱")
                .build();

        updateRequestDto = DeckUpdateRequestDto.builder()
                .name("수정된 덱")
                .build();

        deck = Deck.createDeck("여행 덱", user);

        deckDto = DeckDto.builder()
                .build();
    }

    @Test
    @DisplayName("덱 생성 성공")
    void createDeck_Success() {
        // given
        given(deckRepository.save(any(Deck.class))).willReturn(deck);
        given(deckMapper.toDto(any(Deck.class))).willReturn(deckDto);

        // when
        DeckDto result = deckService.createDeck(user, createRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(deckDto);

        then(deckRepository).should().save(any(Deck.class));
        then(deckMapper).should().toDto(any(Deck.class));
    }

    @Test
    @DisplayName("덱 조회 성공")
    void getDeck_Success() {
        // given
        Long deckId = 1L;
        given(deckRepository.findById(eq(deckId))).willReturn(Optional.of(deck));
        given(deckMapper.toDto(eq(deck))).willReturn(deckDto);

        // when
        DeckDto result = deckService.getDeck(deckId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(deckDto);

        then(deckRepository).should().findById(eq(deckId));
        then(deckMapper).should().toDto(eq(deck));
    }

    @Test
    @DisplayName("덱 조회 실패 - 존재하지 않는 덱")
    void getDeck_NotFound() {
        // given
        Long deckId = 999L;
        given(deckRepository.findById(eq(deckId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deckService.getDeck(deckId))
                .isInstanceOf(DeckException.class);

        then(deckRepository).should().findById(eq(deckId));
    }

    @Test
    @DisplayName("사용자별 덱 목록 조회 성공")
    void getUserDecks_Success() {
        // given
        String username = "testuser";
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        Deck deck2 = Deck.createDeck("두번째 덱", user);

        List<Deck> decks = Arrays.asList(deck, deck2);

        DeckDto deckDto2 = DeckDto.builder()
                .build();

        given(deckRepository.findAllByCursor(eq(username), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(decks);
        given(deckMapper.toDto(eq(deck))).willReturn(deckDto);
        given(deckMapper.toDto(eq(deck2))).willReturn(deckDto2);

        // when
        CursorPageResponseDto<DeckDto> result = deckService.getUserDecks(username, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isFalse();

        then(deckRepository).should().findAllByCursor(eq(username), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(deckMapper).should().toDto(eq(deck));
        then(deckMapper).should().toDto(eq(deck2));
    }

    @Test
    @DisplayName("사용자별 덱 목록 조회 성공 - hasNext true")
    void getUserDecks_HasNext() {
        // given
        String username = "testuser";
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 2;

        Deck testDeck1 = Deck.createDeck("첫번째 덱", user);

        ReflectionTestUtils.setField(testDeck1, "id", 1L);
        ReflectionTestUtils.setField(testDeck1, "createdAt", LocalDateTime.of(2024, 1, 1, 12, 0, 0));

        Deck testDeck2 = Deck.createDeck("두번째 덱", user);

        ReflectionTestUtils.setField(testDeck2, "id", 2L);
        ReflectionTestUtils.setField(testDeck2, "createdAt", LocalDateTime.of(2024, 1, 2, 12, 0, 0));

        Deck testDeck3 = Deck.createDeck("세번째 덱", user);

        ReflectionTestUtils.setField(testDeck3, "id", 3L);
        ReflectionTestUtils.setField(testDeck3, "createdAt", LocalDateTime.of(2024, 1, 3, 12, 0, 0));

        List<Deck> decks = Arrays.asList(testDeck1, testDeck2, testDeck3);

        DeckDto deckDto1 = DeckDto.builder().build();
        DeckDto deckDto2 = DeckDto.builder().build();

        given(deckRepository.findAllByCursor(eq(username), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(decks);
        given(deckMapper.toDto(eq(testDeck1))).willReturn(deckDto1);
        given(deckMapper.toDto(eq(testDeck2))).willReturn(deckDto2);

        // when
        CursorPageResponseDto<DeckDto> result = deckService.getUserDecks(username, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isNotNull();
        assertThat(result.getNextAfter()).isNotNull();

        then(deckRepository).should().findAllByCursor(eq(username), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(deckMapper).should().toDto(eq(testDeck1));
        then(deckMapper).should().toDto(eq(testDeck2));
    }

    @Test
    @DisplayName("사용자별 덱 목록 조회 성공 - 빈 목록")
    void getUserDecks_EmptyList() {
        // given
        String username = "testuser";
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        given(deckRepository.findAllByCursor(eq(username), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(List.of());

        // when
        CursorPageResponseDto<DeckDto> result = deckService.getUserDecks(username, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();

        then(deckRepository).should().findAllByCursor(eq(username), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("덱 수정 성공")
    void updateDeck_Success() {
        // given
        Long deckId = 1L;
        given(deckRepository.findById(eq(deckId))).willReturn(Optional.of(deck));
        given(deckMapper.toDto(eq(deck))).willReturn(deckDto);

        // when
        DeckDto result = deckService.updateDeck(deckId, updateRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(deckDto);

        then(deckRepository).should().findById(eq(deckId));
        then(deckMapper).should().toDto(eq(deck));
    }

    @Test
    @DisplayName("덱 수정 실패 - 존재하지 않는 덱")
    void updateDeck_NotFound() {
        // given
        Long deckId = 999L;
        given(deckRepository.findById(eq(deckId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deckService.updateDeck(deckId, updateRequestDto))
                .isInstanceOf(DeckException.class);

        then(deckRepository).should().findById(eq(deckId));
    }

    @Test
    @DisplayName("덱 삭제 성공")
    void deleteDeck_Success() {
        // given
        Long deckId = 1L;
        given(deckRepository.findById(eq(deckId))).willReturn(Optional.of(deck));

        // when
        deckService.deleteDeck(deckId);

        // then
        then(deckRepository).should().findById(eq(deckId));
        then(deckRepository).should().delete(eq(deck));
    }

    @Test
    @DisplayName("덱 삭제 실패 - 존재하지 않는 덱")
    void deleteDeck_NotFound() {
        // given
        Long deckId = 999L;
        given(deckRepository.findById(eq(deckId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deckService.deleteDeck(deckId))
                .isInstanceOf(DeckException.class);

        then(deckRepository).should().findById(eq(deckId));
    }
}

