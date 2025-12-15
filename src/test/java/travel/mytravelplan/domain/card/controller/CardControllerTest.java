package travel.mytravelplan.domain.card.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.card.dto.CardCreateRequestDto;
import travel.mytravelplan.domain.card.dto.CardDto;
import travel.mytravelplan.domain.card.dto.CardUpdateRequestDto;
import travel.mytravelplan.domain.card.enums.CardStatus;
import travel.mytravelplan.domain.card.service.CardService;
import travel.mytravelplan.domain.deck.entity.Deck;
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

@WebMvcTest(CardController.class)
@DisplayName("카드 컨트롤러 테스트")
class CardControllerTest extends ControllerTestSupport {

    @MockitoBean
    private CardService cardService;

    private String accessToken;
    private Long deckId;
    private Long cardId;
    private CardCreateRequestDto createRequestDto;
    private CardUpdateRequestDto updateRequestDto;
    private CardDto cardDto;

    @BeforeEach
    void setUp() {
        given(jwtBlacklistService.isTokenBlacklisted(any(String.class))).willReturn(false);

        UserProfile userProfile = UserProfile.createUserProfile(
                "테스트 유저",
                "http://example.com/user.jpg"
        );

        User testUser = User.createUser(
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

        ReflectionTestUtils.setField(testUser, "id", 1L);

        accessToken = jwtUtils.createAccessToken(1L, Set.of(Role.USER));

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        deckId = 1L;
        cardId = 1L;

        Deck testDeck = Deck.createDeck("테스트 덱", testUser);
        given(deckRepository.findById(eq(deckId))).willReturn(Optional.of(testDeck));

        createRequestDto = CardCreateRequestDto.builder()
                .front("앞면 내용")
                .back("뒷면 내용")
                .build();

        updateRequestDto = CardUpdateRequestDto.builder()
                .front("수정된 앞면")
                .back("수정된 뒷면")
                .cardStatus(CardStatus.GOT_IT)
                .build();

        cardDto = CardDto.builder()
                .id(1L)
                .front("앞면 내용")
                .back("뒷면 내용")
                .cardStatus(CardStatus.NONE)
                .build();
    }

    @Test
    @DisplayName("카드 생성 성공")
    void createCard_Success() throws Exception {
        // given
        given(cardService.createCard(eq(deckId), any(CardCreateRequestDto.class))).willReturn(cardDto);

        // when
        mockMvc.perform(post("/api/decks/{deckId}/cards", deckId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.front").value("앞면 내용"))
                .andExpect(jsonPath("$.data.back").value("뒷면 내용"))
                .andExpect(jsonPath("$.data.cardStatus").value("NONE"))
                .andDo(document("card-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("deckId").description("덱 ID")
                        ),
                        requestFields(
                                fieldWithPath("front").description("카드 앞면 내용"),
                                fieldWithPath("back").description("카드 뒷면 내용")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("카드 ID"),
                                fieldWithPath("data.front").description("카드 앞면 내용"),
                                fieldWithPath("data.back").description("카드 뒷면 내용"),
                                fieldWithPath("data.cardStatus").description("카드 상태 (NONE, NOT_SURE, GOT_IT)")
                        )
                ));

        // then
        then(cardService).should().createCard(eq(deckId), any(CardCreateRequestDto.class));
    }

    @Test
    @DisplayName("카드 조회 성공")
    void getCard_Success() throws Exception {
        // given
        given(cardService.getCard(eq(deckId), eq(cardId))).willReturn(cardDto);

        // when
        mockMvc.perform(get("/api/decks/{deckId}/cards/{cardId}", deckId, cardId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.front").value("앞면 내용"))
                .andExpect(jsonPath("$.data.back").value("뒷면 내용"))
                .andExpect(jsonPath("$.data.cardStatus").value("NONE"))
                .andDo(document("card-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("deckId").description("덱 ID"),
                                parameterWithName("cardId").description("카드 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("카드 ID"),
                                fieldWithPath("data.front").description("카드 앞면 내용"),
                                fieldWithPath("data.back").description("카드 뒷면 내용"),
                                fieldWithPath("data.cardStatus").description("카드 상태 (NONE, NOT_SURE, GOT_IT)")
                        )
                ));

        // then
        then(cardService).should().getCard(eq(deckId), eq(cardId));
    }

    @Test
    @DisplayName("카드 목록 조회 성공")
    void getCards_Success() throws Exception {
        // given
        CardDto cardDto1 = CardDto.builder()
                .id(1L)
                .front("앞면 1")
                .back("뒷면 1")
                .cardStatus(CardStatus.NONE)
                .build();

        CardDto cardDto2 = CardDto.builder()
                .id(2L)
                .front("앞면 2")
                .back("뒷면 2")
                .cardStatus(CardStatus.NOT_SURE)
                .build();

        CursorPageResponseDto<CardDto> response = CursorPageResponseDto.<CardDto>builder()
                .content(List.of(cardDto1, cardDto2))
                .nextCursor("nextCursor")
                .nextAfter(2L)
                .size(2)
                .hasNext(true)
                .build();

        given(cardService.getCards(eq(deckId), isNull(), eq("createdAt"), eq("ASC"), isNull(), isNull(), eq(10)))
                .willReturn(response);

        // when
        mockMvc.perform(get("/api/decks/{deckId}/cards", deckId)
                        .header("Authorization", "Bearer " + accessToken)
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.size").value(2))
                .andDo(document("card-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("deckId").description("덱 ID")
                        ),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드").optional(),
                                parameterWithName("orderBy").description("정렬 기준 (기본값: createdAt)").optional(),
                                parameterWithName("direction").description("정렬 방향 (ASC, DESC, 기본값: ASC)").optional(),
                                parameterWithName("cursor").description("커서 값").optional(),
                                parameterWithName("after").description("이후 ID").optional(),
                                parameterWithName("limit").description("페이지 크기 (기본값: 10)").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.content").description("카드 목록"),
                                fieldWithPath("data.content[].id").description("카드 ID"),
                                fieldWithPath("data.content[].front").description("카드 앞면 내용"),
                                fieldWithPath("data.content[].back").description("카드 뒷면 내용"),
                                fieldWithPath("data.content[].cardStatus").description("카드 상태"),
                                fieldWithPath("data.nextCursor").description("다음 커서"),
                                fieldWithPath("data.nextAfter").description("다음 ID"),
                                fieldWithPath("data.size").description("현재 페이지 크기"),
                                fieldWithPath("data.hasNext").description("다음 페이지 존재 여부")
                        )
                ));

        // then
        then(cardService).should().getCards(eq(deckId), isNull(), eq("createdAt"), eq("ASC"), isNull(), isNull(), eq(10));
    }

    @Test
    @DisplayName("카드 수정 성공")
    void updateCard_Success() throws Exception {
        // given
        CardDto updatedCardDto = CardDto.builder()
                .id(1L)
                .front("수정된 앞면")
                .back("수정된 뒷면")
                .cardStatus(CardStatus.GOT_IT)
                .build();

        given(cardService.updateCard(eq(deckId), eq(cardId), any(CardUpdateRequestDto.class)))
                .willReturn(updatedCardDto);

        // when
        mockMvc.perform(patch("/api/decks/{deckId}/cards/{cardId}", deckId, cardId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.front").value("수정된 앞면"))
                .andExpect(jsonPath("$.data.back").value("수정된 뒷면"))
                .andExpect(jsonPath("$.data.cardStatus").value("GOT_IT"))
                .andDo(document("card-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("deckId").description("덱 ID"),
                                parameterWithName("cardId").description("카드 ID")
                        ),
                        requestFields(
                                fieldWithPath("front").description("수정할 카드 앞면 내용").optional(),
                                fieldWithPath("back").description("수정할 카드 뒷면 내용").optional(),
                                fieldWithPath("cardStatus").description("수정할 카드 상태 (NONE, NOT_SURE, GOT_IT)").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("카드 ID"),
                                fieldWithPath("data.front").description("수정된 카드 앞면 내용"),
                                fieldWithPath("data.back").description("수정된 카드 뒷면 내용"),
                                fieldWithPath("data.cardStatus").description("수정된 카드 상태 (NONE, NOT_SURE, GOT_IT)")
                        )
                ));

        // then
        then(cardService).should().updateCard(eq(deckId), eq(cardId), any(CardUpdateRequestDto.class));
    }

    @Test
    @DisplayName("카드 삭제 성공")
    void deleteCard_Success() throws Exception {
        // given
        willDoNothing().given(cardService).deleteCard(eq(deckId), eq(cardId));

        // when
        mockMvc.perform(delete("/api/decks/{deckId}/cards/{cardId}", deckId, cardId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(document("card-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("deckId").description("덱 ID"),
                                parameterWithName("cardId").description("카드 ID")
                        )
                ));

        // then
        then(cardService).should().deleteCard(eq(deckId), eq(cardId));
    }
}
