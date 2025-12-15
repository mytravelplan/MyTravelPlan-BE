package travel.mytravelplan.domain.currency.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import travel.mytravelplan.domain.currency.dto.CurrencyDto;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.currency.service.CurrencyService;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.global.support.ControllerTestSupport;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CurrencyController.class)
@DisplayName("환율 컨트롤러 테스트")
class CurrencyControllerTest extends ControllerTestSupport {

    @MockitoBean
    private CurrencyService currencyService;

    private String accessToken;
    private CurrencyDto usdCurrencyDto;
    private CurrencyDto eurCurrencyDto;
    private CurrencyDto jpyCurrencyDto;

    @BeforeEach
    void setUp() {
        given(jwtBlacklistService.isTokenBlacklisted(any(String.class))).willReturn(false);

        accessToken = jwtUtils.createAccessToken(1L, Set.of(Role.USER));

        usdCurrencyDto = CurrencyDto.builder()
                .name("미국 달러")
                .currencyType(CurrencyType.USD)
                .exchangeRate(new BigDecimal("1300.50"))
                .build();

        eurCurrencyDto = CurrencyDto.builder()
                .name("유로")
                .currencyType(CurrencyType.EUR)
                .exchangeRate(new BigDecimal("1450.75"))
                .build();

        jpyCurrencyDto = CurrencyDto.builder()
                .name("일본 엔")
                .currencyType(CurrencyType.JPY)
                .exchangeRate(new BigDecimal("9.50"))
                .build();
    }

    @Test
    @DisplayName("특정 통화 정보 조회 성공")
    void getCurrency_Success() throws Exception {
        // given
        given(currencyService.getCurrency(eq(CurrencyType.USD))).willReturn(usdCurrencyDto);

        // when & then
        mockMvc.perform(get("/api/currencies/{currencyType}", CurrencyType.USD)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("미국 달러"))
                .andExpect(jsonPath("$.data.currencyType").value("USD"))
                .andExpect(jsonPath("$.data.exchangeRate").value(1300.50))
                .andDo(document("currency-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("currencyType").description("통화 타입 (예: USD, EUR, JPY)")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.name").description("통화 이름"),
                                fieldWithPath("data.currencyType").description("통화 타입"),
                                fieldWithPath("data.exchangeRate").description("환율")
                        )
                ));

        // then
        then(currencyService).should().getCurrency(eq(CurrencyType.USD));
    }

    @Test
    @DisplayName("전체 통화 정보 목록 조회 성공")
    void getAllCurrencies_Success() throws Exception {
        // given
        List<CurrencyDto> currencies = List.of(usdCurrencyDto, eurCurrencyDto, jpyCurrencyDto);
        given(currencyService.getAllCurrencies()).willReturn(currencies);

        // when & then
        mockMvc.perform(get("/api/currencies")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].name").value("미국 달러"))
                .andExpect(jsonPath("$.data[0].currencyType").value("USD"))
                .andExpect(jsonPath("$.data[0].exchangeRate").value(1300.50))
                .andExpect(jsonPath("$.data[1].name").value("유로"))
                .andExpect(jsonPath("$.data[1].currencyType").value("EUR"))
                .andExpect(jsonPath("$.data[1].exchangeRate").value(1450.75))
                .andExpect(jsonPath("$.data[2].name").value("일본 엔"))
                .andExpect(jsonPath("$.data[2].currencyType").value("JPY"))
                .andExpect(jsonPath("$.data[2].exchangeRate").value(9.50))
                .andDo(document("currency-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data[]").description("통화 정보 목록"),
                                fieldWithPath("data[].name").description("통화 이름"),
                                fieldWithPath("data[].currencyType").description("통화 타입"),
                                fieldWithPath("data[].exchangeRate").description("환율")
                        )
                ));

        // then
        then(currencyService).should().getAllCurrencies();
    }
}