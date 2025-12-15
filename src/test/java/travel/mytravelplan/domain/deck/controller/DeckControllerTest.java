package travel.mytravelplan.domain.deck.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.deck.dto.DeckCreateRequestDto;
import travel.mytravelplan.domain.deck.dto.DeckDto;
import travel.mytravelplan.domain.deck.dto.DeckUpdateRequestDto;
import travel.mytravelplan.domain.deck.entity.Deck;
import travel.mytravelplan.domain.deck.service.DeckService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.entity.UserProfile;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.global.support.ControllerTestSupport;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static org.assertj.core.api.Assertions.assertThat;
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

@WebMvcTest(DeckController.class)
@DisplayName("덱 컨트롤러 테스트")
class DeckControllerTest extends ControllerTestSupport {

    @MockitoBean
    private DeckService deckService;

    private String accessToken;
    private Long userId;
    private Long deckId;
    private User testUser;
    private Deck testDeck;
    private DeckCreateRequestDto createRequestDto;
    private DeckUpdateRequestDto updateRequestDto;
    private DeckDto deckDto;

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

        deckId = 1L;

        testDeck = Deck.createDeck("테스트 덱", testUser);
        given(deckRepository.findById(eq(deckId))).willReturn(Optional.of(testDeck));

        createRequestDto = DeckCreateRequestDto.builder()
                .name("테스트 덱")
                .build();

        updateRequestDto = DeckUpdateRequestDto.builder()
                .name("수정된 덱")
                .build();

        deckDto = DeckDto.builder()
                .id(1L)
                .name("테스트 덱")
                .build();
    }

    @Test
    @DisplayName("덱 생성 성공")
    void createDeck_Success() throws Exception {
        // given
        given(deckService.createDeck(any(User.class), any(DeckCreateRequestDto.class))).willReturn(deckDto);

        // when
        mockMvc.perform(post("/api/decks")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("테스트 덱"))
                .andDo(document("deck-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        requestFields(
                                fieldWithPath("name").description("덱 이름")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("덱 ID"),
                                fieldWithPath("data.name").description("덱 이름")
                        )
                ));

        // then
        assertThat(deckDto).isNotNull();
        assertThat(deckDto.getName()).isEqualTo("테스트 덱");
        then(deckService).should().createDeck(any(User.class), any(DeckCreateRequestDto.class));
    }

    @Test
    @DisplayName("덱 조회 성공")
    void getDeck_Success() throws Exception {
        // given
        given(deckService.getDeck(eq(deckId))).willReturn(deckDto);

        // when
        mockMvc.perform(get("/api/decks/{deckId}", deckId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("테스트 덱"))
                .andDo(document("deck-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("deckId").description("덱 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("덱 ID"),
                                fieldWithPath("data.name").description("덱 이름")
                        )
                ));

        // then
        assertThat(deckDto).isNotNull();
        assertThat(deckDto.getId()).isEqualTo(1L);
        then(deckService).should().getDeck(eq(deckId));
    }

    @Test
    @DisplayName("덱 수정 성공")
    void updateDeck_Success() throws Exception {
        // given
        DeckDto updatedDeckDto = DeckDto.builder()
                .id(1L)
                .name("수정된 덱")
                .build();

        given(deckService.updateDeck(eq(deckId), any(DeckUpdateRequestDto.class)))
                .willReturn(updatedDeckDto);

        // when
        mockMvc.perform(patch("/api/decks/{deckId}", deckId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("수정된 덱"))
                .andDo(document("deck-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("deckId").description("덱 ID")
                        ),
                        requestFields(
                                fieldWithPath("name").description("수정할 덱 이름").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("덱 ID"),
                                fieldWithPath("data.name").description("수정된 덱 이름")
                        )
                ));

        // then
        assertThat(updatedDeckDto).isNotNull();
        assertThat(updatedDeckDto.getName()).isEqualTo("수정된 덱");
        then(deckService).should().updateDeck(eq(deckId), any(DeckUpdateRequestDto.class));
    }

    @Test
    @DisplayName("덱 삭제 성공")
    void deleteDeck_Success() throws Exception {
        // given
        willDoNothing().given(deckService).deleteDeck(eq(deckId));

        // when
        mockMvc.perform(delete("/api/decks/{deckId}", deckId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(document("deck-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("deckId").description("덱 ID")
                        )
                ));

        // then
        then(deckService).should().deleteDeck(eq(deckId));
    }
}