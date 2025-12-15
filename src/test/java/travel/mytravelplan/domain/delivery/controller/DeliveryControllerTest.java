package travel.mytravelplan.domain.delivery.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.delivery.dto.DeliveryDto;
import travel.mytravelplan.domain.delivery.dto.DeliveryUpdateRequestDto;
import travel.mytravelplan.domain.delivery.entity.Delivery;
import travel.mytravelplan.domain.delivery.enums.Address;
import travel.mytravelplan.domain.delivery.enums.DeliveryStatus;
import travel.mytravelplan.domain.delivery.service.DeliveryService;
import travel.mytravelplan.domain.order.entity.Order;
import travel.mytravelplan.domain.order.enums.Orderer;
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
import static org.mockito.BDDMockito.then;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeliveryController.class)
@DisplayName("배송 컨트롤러 테스트")
class DeliveryControllerTest extends ControllerTestSupport {

    @MockitoBean
    private DeliveryService deliveryService;

    private String accessToken;
    private String adminAccessToken;
    private Long userId;
    private Long deliveryId;
    private User testUser;
    private User adminUser;
    private Delivery testDelivery;
    private Order testOrder;
    private Address testAddress;
    private DeliveryDto deliveryDto;
    private DeliveryUpdateRequestDto updateRequestDto;

    @BeforeEach
    void setUp() {
        given(jwtBlacklistService.isTokenBlacklisted(any(String.class))).willReturn(false);

        // 일반 사용자 설정
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

        // 관리자 사용자 설정
        UserProfile adminProfile = UserProfile.createUserProfile(
                "관리자",
                "http://example.com/admin.jpg"
        );

        adminUser = User.createUser(
                "adminUser",
                "password",
                "admin@test.com",
                SocialType.LOCAL,
                null,
                LocalDate.of(1985, 1, 1),
                "010-9999-9999",
                Gender.MALE,
                Set.of(Role.ADMIN)
        );

        adminUser.setUserProfile(adminProfile);

        Long adminId = 2L;
        ReflectionTestUtils.setField(adminUser, "id", adminId);

        adminAccessToken = jwtUtils.createAccessToken(adminId, Set.of(Role.ADMIN));

        given(userRepository.findById(eq(adminId))).willReturn(Optional.of(adminUser));

        // 배송 정보 설정
        deliveryId = 1L;

        testAddress = Address.builder()
                .recipient("홍길동")
                .phone("010-1234-5678")
                .zipcode("12345")
                .address("서울시 강남구")
                .detailAddress("123번지")
                .build();

        testDelivery = Delivery.createDelivery(testAddress, "문 앞에 놓아주세요");
        ReflectionTestUtils.setField(testDelivery, "id", deliveryId);

        Orderer orderer = Orderer.builder()
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .email("test@test.com")
                .build();

        testOrder = Order.createOrder(testUser, testDelivery, orderer, new java.util.ArrayList<>());
        ReflectionTestUtils.setField(testOrder, "id", 1L);

        given(deliveryRepository.findById(eq(deliveryId))).willReturn(Optional.of(testDelivery));

        deliveryDto = DeliveryDto.builder()
                .id(deliveryId)
                .address(testAddress)
                .deliveryStatus(DeliveryStatus.READY)
                .requirement("문 앞에 놓아주세요")
                .build();

        updateRequestDto = DeliveryUpdateRequestDto.builder()
                .deliveryStatus(DeliveryStatus.DELIVERING)
                .build();
    }

    @Test
    @DisplayName("배송 조회 성공")
    void getDelivery_Success() throws Exception {
        // given
        given(deliveryService.getDelivery(eq(deliveryId))).willReturn(deliveryDto);

        // when
        mockMvc.perform(get("/api/deliveries/{deliveryId}", deliveryId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(deliveryId))
                .andExpect(jsonPath("$.data.address.recipient").value("홍길동"))
                .andExpect(jsonPath("$.data.address.phone").value("010-1234-5678"))
                .andExpect(jsonPath("$.data.address.zipcode").value("12345"))
                .andExpect(jsonPath("$.data.address.address").value("서울시 강남구"))
                .andExpect(jsonPath("$.data.address.detailAddress").value("123번지"))
                .andExpect(jsonPath("$.data.deliveryStatus").value("READY"))
                .andExpect(jsonPath("$.data.requirement").value("문 앞에 놓아주세요"))
                .andDo(document("delivery-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("deliveryId").description("배송 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("배송 ID"),
                                fieldWithPath("data.address.recipient").description("수령인 이름"),
                                fieldWithPath("data.address.phone").description("전화번호"),
                                fieldWithPath("data.address.zipcode").description("우편번호"),
                                fieldWithPath("data.address.address").description("주소"),
                                fieldWithPath("data.address.detailAddress").description("상세 주소"),
                                fieldWithPath("data.deliveryStatus").description("배송 상태 (READY, DELIVERING, COMP)"),
                                fieldWithPath("data.requirement").description("배송 요청 사항")
                        )
                ));

        // then
        assertThat(deliveryDto).isNotNull();
        assertThat(deliveryDto.getId()).isEqualTo(deliveryId);
        assertThat(deliveryDto.getAddress().getRecipient()).isEqualTo("홍길동");
        assertThat(deliveryDto.getDeliveryStatus()).isEqualTo(DeliveryStatus.READY);
        then(deliveryService).should().getDelivery(eq(deliveryId));
    }

    @Test
    @DisplayName("배송 정보 수정 성공 - 관리자")
    void updateDelivery_Success() throws Exception {
        // given
        DeliveryDto updatedDeliveryDto = DeliveryDto.builder()
                .id(deliveryId)
                .address(testAddress)
                .deliveryStatus(DeliveryStatus.DELIVERING)
                .requirement("문 앞에 놓아주세요")
                .build();

        given(deliveryService.updateDelivery(eq(deliveryId), any(DeliveryUpdateRequestDto.class)))
                .willReturn(updatedDeliveryDto);

        // when
        mockMvc.perform(patch("/api/deliveries/{deliveryId}", deliveryId)
                        .header("Authorization", "Bearer " + adminAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(deliveryId))
                .andExpect(jsonPath("$.data.deliveryStatus").value("DELIVERING"))
                .andDo(document("delivery-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (관리자 권한 필요)")
                        ),
                        pathParameters(
                                parameterWithName("deliveryId").description("배송 ID")
                        ),
                        requestFields(
                                fieldWithPath("deliveryStatus").description("수정할 배송 상태 (READY, DELIVERING, COMP)")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("배송 ID"),
                                fieldWithPath("data.address.recipient").description("수령인 이름"),
                                fieldWithPath("data.address.phone").description("전화번호"),
                                fieldWithPath("data.address.zipcode").description("우편번호"),
                                fieldWithPath("data.address.address").description("주소"),
                                fieldWithPath("data.address.detailAddress").description("상세 주소"),
                                fieldWithPath("data.deliveryStatus").description("수정된 배송 상태"),
                                fieldWithPath("data.requirement").description("배송 요청 사항")
                        )
                ));

        // then
        assertThat(updatedDeliveryDto).isNotNull();
        assertThat(updatedDeliveryDto.getDeliveryStatus()).isEqualTo(DeliveryStatus.DELIVERING);
        then(deliveryService).should().updateDelivery(eq(deliveryId), any(DeliveryUpdateRequestDto.class));
    }
}