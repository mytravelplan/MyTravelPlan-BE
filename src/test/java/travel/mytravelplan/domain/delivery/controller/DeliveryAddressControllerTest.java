package travel.mytravelplan.domain.delivery.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.delivery.dto.DeliveryAddressCreateRequestDto;
import travel.mytravelplan.domain.delivery.dto.DeliveryAddressDto;
import travel.mytravelplan.domain.delivery.enums.Address;
import travel.mytravelplan.domain.delivery.service.DeliveryAddressService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.entity.UserProfile;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.global.support.ControllerTestSupport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeliveryAddressController.class)
@DisplayName("배송지 컨트롤러 테스트")
class DeliveryAddressControllerTest extends ControllerTestSupport {

    @MockitoBean
    private DeliveryAddressService deliveryAddressService;

    private String accessToken;
    private User testUser;
    private DeliveryAddressCreateRequestDto createRequestDto;
    private DeliveryAddressDto deliveryAddressDto;
    private Address address;

    @BeforeEach
    void setUp() {
        given(jwtBlacklistService.isTokenBlacklisted(any(String.class))).willReturn(false);

        UserProfile userProfile = UserProfile.createUserProfile(
                "테스트 사용자",
                "http://example.com/user.jpg"
        );

        testUser = User.createUser(
                "testUser",
                "password",
                "user@test.com",
                SocialType.LOCAL,
                null,
                LocalDate.of(1995, 5, 15),
                "010-1234-5678",
                Gender.MALE,
                Set.of(Role.USER)
        );

        testUser.setUserProfile(userProfile);
        ReflectionTestUtils.setField(testUser, "id", 1L);

        accessToken = jwtUtils.createAccessToken(1L, Set.of(Role.USER));

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        address = Address.builder()
                .recipient("홍길동")
                .phone("010-1234-5678")
                .zipcode("12345")
                .address("서울특별시 강남구 테헤란로 123")
                .detailAddress("456호")
                .build();

        createRequestDto = DeliveryAddressCreateRequestDto.builder()
                .address(address)
                .defaultDeliveryAddress(true)
                .build();

        deliveryAddressDto = DeliveryAddressDto.builder()
                .id(1L)
                .address(address)
                .build();
    }

    @Test
    @DisplayName("배송지 생성 성공")
    void createDeliveryAddress_Success() throws Exception {
        // given
        given(deliveryAddressService.createDeliveryAddress(any(User.class), any(DeliveryAddressCreateRequestDto.class)))
                .willReturn(deliveryAddressDto);

        // when
        mockMvc.perform(post("/api/delivery-addresses")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.address.recipient").value("홍길동"))
                .andExpect(jsonPath("$.data.address.phone").value("010-1234-5678"))
                .andExpect(jsonPath("$.data.address.zipcode").value("12345"))
                .andExpect(jsonPath("$.data.address.address").value("서울특별시 강남구 테헤란로 123"))
                .andExpect(jsonPath("$.data.address.detailAddress").value("456호"))
                .andDo(document("delivery-address-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (USER 또는 ADMIN 권한 필요)")
                        ),
                        requestFields(
                                fieldWithPath("address").description("배송지 주소 정보"),
                                fieldWithPath("address.recipient").description("수령인 이름"),
                                fieldWithPath("address.phone").description("전화번호"),
                                fieldWithPath("address.zipcode").description("우편번호"),
                                fieldWithPath("address.address").description("주소"),
                                fieldWithPath("address.detailAddress").description("상세 주소"),
                                fieldWithPath("defaultDeliveryAddress").description("기본 배송지 여부")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("배송지 ID"),
                                fieldWithPath("data.address").description("배송지 주소 정보"),
                                fieldWithPath("data.address.recipient").description("수령인 이름"),
                                fieldWithPath("data.address.phone").description("전화번호"),
                                fieldWithPath("data.address.zipcode").description("우편번호"),
                                fieldWithPath("data.address.address").description("주소"),
                                fieldWithPath("data.address.detailAddress").description("상세 주소")
                        )
                ));

        // then
        then(deliveryAddressService).should().createDeliveryAddress(any(User.class), any(DeliveryAddressCreateRequestDto.class));
    }

    @Test
    @DisplayName("배송지 목록 조회 성공")
    void getAllDeliveryAddresses_Success() throws Exception {
        // given
        Address address1 = Address.builder()
                .recipient("홍길동")
                .phone("010-1234-5678")
                .zipcode("12345")
                .address("서울특별시 강남구 테헤란로 123")
                .detailAddress("456호")
                .build();

        Address address2 = Address.builder()
                .recipient("김철수")
                .phone("010-9876-5432")
                .zipcode("54321")
                .address("서울특별시 서초구 서초대로 456")
                .detailAddress("789호")
                .build();

        DeliveryAddressDto deliveryAddressDto1 = DeliveryAddressDto.builder()
                .id(1L)
                .address(address1)
                .build();

        DeliveryAddressDto deliveryAddressDto2 = DeliveryAddressDto.builder()
                .id(2L)
                .address(address2)
                .build();

        List<DeliveryAddressDto> deliveryAddresses = List.of(deliveryAddressDto1, deliveryAddressDto2);

        given(deliveryAddressService.getAllDeliveryAddresses(any(User.class))).willReturn(deliveryAddresses);

        // when
        mockMvc.perform(get("/api/delivery-addresses")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].address.recipient").value("홍길동"))
                .andExpect(jsonPath("$.data[0].address.phone").value("010-1234-5678"))
                .andExpect(jsonPath("$.data[0].address.zipcode").value("12345"))
                .andExpect(jsonPath("$.data[0].address.address").value("서울특별시 강남구 테헤란로 123"))
                .andExpect(jsonPath("$.data[0].address.detailAddress").value("456호"))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].address.recipient").value("김철수"))
                .andExpect(jsonPath("$.data[1].address.phone").value("010-9876-5432"))
                .andExpect(jsonPath("$.data[1].address.zipcode").value("54321"))
                .andExpect(jsonPath("$.data[1].address.address").value("서울특별시 서초구 서초대로 456"))
                .andExpect(jsonPath("$.data[1].address.detailAddress").value("789호"))
                .andDo(document("delivery-address-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (USER 또는 ADMIN 권한 필요)")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data").description("배송지 목록"),
                                fieldWithPath("data[].id").description("배송지 ID"),
                                fieldWithPath("data[].address").description("배송지 주소 정보"),
                                fieldWithPath("data[].address.recipient").description("수령인 이름"),
                                fieldWithPath("data[].address.phone").description("전화번호"),
                                fieldWithPath("data[].address.zipcode").description("우편번호"),
                                fieldWithPath("data[].address.address").description("주소"),
                                fieldWithPath("data[].address.detailAddress").description("상세 주소")
                        )
                ));

        // then
        then(deliveryAddressService).should().getAllDeliveryAddresses(any(User.class));
    }
}