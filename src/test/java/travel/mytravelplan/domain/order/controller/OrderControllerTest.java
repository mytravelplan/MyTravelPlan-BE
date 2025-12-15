package travel.mytravelplan.domain.order.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.delivery.dto.DeliveryAddressDto;
import travel.mytravelplan.domain.delivery.entity.Delivery;
import travel.mytravelplan.domain.delivery.enums.Address;
import travel.mytravelplan.domain.order.dto.OrderCreateRequestDto;
import travel.mytravelplan.domain.order.dto.OrderDto;
import travel.mytravelplan.domain.order.dto.OrdererDto;
import travel.mytravelplan.domain.order.entity.Order;
import travel.mytravelplan.domain.order.enums.OrderStatus;
import travel.mytravelplan.domain.order.enums.Orderer;
import travel.mytravelplan.domain.order.service.OrderService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.entity.UserProfile;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.support.ControllerTestSupport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
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

@WebMvcTest(OrderController.class)
@DisplayName("주문 컨트롤러 테스트")
class OrderControllerTest extends ControllerTestSupport {

    @MockitoBean
    private OrderService orderService;

    private String accessToken;
    private Long orderId;
    private OrderCreateRequestDto createRequestDto;
    private OrderDto orderDto;
    private User orderUser;
    private Orderer orderer;
    private Address address;
    private Delivery delivery;
    private Order order;

    @BeforeEach
    void setUp() {
        given(jwtBlacklistService.isTokenBlacklisted(any(String.class))).willReturn(false);

        UserProfile userProfile = UserProfile.createUserProfile(
                "테스트 사용자",
                "http://example.com/user.jpg"
        );

        User testUser = User.createUser(
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

        orderId = 1L;

        Map<Long, Integer> products = new HashMap<>();
        products.put(1L, 2);

        createRequestDto = OrderCreateRequestDto.builder()
                .products(products)
                .orderer(Orderer.builder()
                        .name("테스트 사용자")
                        .phoneNumber("010-1234-5678")
                        .email("user@test.com")
                        .build())
                .deliveryAddressId(1L)
                .requirement("문 앞에 놓아주세요")
                .build();

        OrdererDto ordererDto = OrdererDto.builder()
                .name("테스트 사용자")
                .phoneNumber("010-1234-5678")
                .email("user@test.com")
                .build();

        DeliveryAddressDto deliveryAddressDto = DeliveryAddressDto.builder()
                .id(1L)
                .address(Address.builder()
                        .recipient("수령인")
                        .phone("010-9876-5432")
                        .address("서울시 강남구")
                        .detailAddress("101호")
                        .zipcode("12345")
                        .build())
                .build();

        orderDto = OrderDto.builder()
                .orderId(1L)
                .orderDate(LocalDateTime.now())
                .totalPrice(30000)
                .paymentCompletedAt(LocalDateTime.now())
                .orderer(ordererDto)
                .deliveryAddress(deliveryAddressDto)
                .build();

        // 공통 테스트 데이터 생성
        orderUser = User.createUser(
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
        ReflectionTestUtils.setField(orderUser, "id", 1L);

        orderer = Orderer.builder()
                .name("테스트 사용자")
                .phoneNumber("010-1234-5678")
                .email("user@test.com")
                .build();

        address = Address.builder()
                .recipient("수령인")
                .phone("010-9876-5432")
                .address("서울시 강남구")
                .detailAddress("101호")
                .zipcode("12345")
                .build();

        delivery = Delivery.createDelivery(address, "문 앞에 놓아주세요");

        order = Order.createOrder(orderUser, delivery, orderer, new ArrayList<>());
        ReflectionTestUtils.setField(order, "id", orderId);
    }

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_Success() throws Exception {
        // given
        given(orderService.createOrder(any(User.class), any(OrderCreateRequestDto.class)))
                .willReturn(orderDto);

        // when & then
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value(1))
                .andExpect(jsonPath("$.data.totalPrice").value(30000))
                .andExpect(jsonPath("$.data.orderer.name").value("테스트 사용자"))
                .andExpect(jsonPath("$.data.orderer.phoneNumber").value("010-1234-5678"))
                .andExpect(jsonPath("$.data.orderer.email").value("user@test.com"))
                .andExpect(jsonPath("$.data.deliveryAddress.id").value(1))
                .andDo(document("order-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (USER 또는 ADMIN 권한 필요)")
                        ),
                        requestFields(
                                subsectionWithPath("products").description("주문할 상품 목록 (상품ID를 키로, 수량을 값으로 하는 Map 구조)"),
                                fieldWithPath("orderer.name").description("주문자 이름"),
                                fieldWithPath("orderer.phoneNumber").description("주문자 전화번호"),
                                fieldWithPath("orderer.email").description("주문자 이메일"),
                                fieldWithPath("deliveryAddressId").description("배송지 ID"),
                                fieldWithPath("requirement").description("배송 요청사항")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.orderId").description("주문 번호"),
                                fieldWithPath("data.orderDate").description("주문 날짜"),
                                fieldWithPath("data.totalPrice").description("총 주문 금액"),
                                fieldWithPath("data.paymentCompletedAt").description("결제 완료 시간"),
                                fieldWithPath("data.orderer.name").description("주문자 이름"),
                                fieldWithPath("data.orderer.phoneNumber").description("주문자 전화번호"),
                                fieldWithPath("data.orderer.email").description("주문자 이메일"),
                                fieldWithPath("data.deliveryAddress.id").description("배송지 ID"),
                                fieldWithPath("data.deliveryAddress.address.recipient").description("수령인 이름"),
                                fieldWithPath("data.deliveryAddress.address.phone").description("수령인 전화번호"),
                                fieldWithPath("data.deliveryAddress.address.address").description("주소"),
                                fieldWithPath("data.deliveryAddress.address.detailAddress").description("상세 주소"),
                                fieldWithPath("data.deliveryAddress.address.zipcode").description("우편번호")
                        )
                ));

        // then
        then(orderService).should().createOrder(any(User.class), any(OrderCreateRequestDto.class));
    }

    @Test
    @DisplayName("주문 조회 성공")
    void getOrder_Success() throws Exception {
        // given

        given(orderRepository.findById(eq(orderId))).willReturn(Optional.of(order));
        given(orderService.getOrder(eq(orderId))).willReturn(orderDto);

        // when & then
        mockMvc.perform(get("/api/orders/{orderId}", orderId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value(1))
                .andExpect(jsonPath("$.data.totalPrice").value(30000))
                .andExpect(jsonPath("$.data.orderer.name").value("테스트 사용자"))
                .andDo(document("order-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (USER 또는 ADMIN 권한 필요, 권한 확인 필요)")
                        ),
                        pathParameters(
                                parameterWithName("orderId").description("주문 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.orderId").description("주문 번호"),
                                fieldWithPath("data.orderDate").description("주문 날짜"),
                                fieldWithPath("data.totalPrice").description("총 주문 금액"),
                                fieldWithPath("data.paymentCompletedAt").description("결제 완료 시간"),
                                fieldWithPath("data.orderer.name").description("주문자 이름"),
                                fieldWithPath("data.orderer.phoneNumber").description("주문자 전화번호"),
                                fieldWithPath("data.orderer.email").description("주문자 이메일"),
                                fieldWithPath("data.deliveryAddress.id").description("배송지 ID"),
                                fieldWithPath("data.deliveryAddress.address.recipient").description("수령인 이름"),
                                fieldWithPath("data.deliveryAddress.address.phone").description("수령인 전화번호"),
                                fieldWithPath("data.deliveryAddress.address.address").description("주소"),
                                fieldWithPath("data.deliveryAddress.address.detailAddress").description("상세 주소"),
                                fieldWithPath("data.deliveryAddress.address.zipcode").description("우편번호")
                        )
                ));

        // then
        then(orderService).should().getOrder(eq(orderId));
    }

    @Test
    @DisplayName("주문 목록 조회 성공")
    void getOrders_Success() throws Exception {
        // given
        OrderDto orderDto1 = OrderDto.builder()
                .orderId(1L)
                .orderDate(LocalDateTime.now())
                .totalPrice(30000)
                .paymentCompletedAt(LocalDateTime.now())
                .orderer(OrdererDto.builder()
                        .name("테스트 사용자")
                        .phoneNumber("010-1234-5678")
                        .email("user@test.com")
                        .build())
                .deliveryAddress(DeliveryAddressDto.builder()
                        .id(1L)
                        .address(Address.builder()
                                .recipient("수령인")
                                .phone("010-9876-5432")
                                .address("서울시 강남구")
                                .detailAddress("101호")
                                .zipcode("12345")
                                .build())
                        .build())
                .build();

        OrderDto orderDto2 = OrderDto.builder()
                .orderId(2L)
                .orderDate(LocalDateTime.now())
                .totalPrice(50000)
                .paymentCompletedAt(LocalDateTime.now())
                .orderer(OrdererDto.builder()
                        .name("테스트 사용자")
                        .phoneNumber("010-1234-5678")
                        .email("user@test.com")
                        .build())
                .deliveryAddress(DeliveryAddressDto.builder()
                        .id(2L)
                        .address(Address.builder()
                                .recipient("수령인2")
                                .phone("010-8888-9999")
                                .address("서울시 서초구")
                                .detailAddress("202호")
                                .zipcode("54321")
                                .build())
                        .build())
                .build();

        CursorPageResponseDto<OrderDto> cursorPage = CursorPageResponseDto.<OrderDto>builder()
                .content(List.of(orderDto1, orderDto2))
                .nextCursor("cursor123")
                .nextAfter(2L)
                .size(2)
                .hasNext(false)
                .build();

        given(orderService.getOrders(
                any(User.class),
                eq(OrderStatus.ORDER),
                eq("createdAt"),
                eq("ASC"),
                eq("cursor123"),
                eq(1L),
                eq(10)
        )).willReturn(cursorPage);

        // when & then
        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("orderStatus", "ORDER")
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("cursor", "cursor123")
                        .param("after", "1")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].orderId").value(1))
                .andExpect(jsonPath("$.data.content[0].totalPrice").value(30000))
                .andExpect(jsonPath("$.data.content[1].orderId").value(2))
                .andExpect(jsonPath("$.data.content[1].totalPrice").value(50000))
                .andExpect(jsonPath("$.data.nextCursor").value("cursor123"))
                .andExpect(jsonPath("$.data.nextAfter").value(2))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andDo(document("order-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (USER 또는 ADMIN 권한 필요)")
                        ),
                        queryParameters(
                                parameterWithName("orderStatus").description("주문 상태 (ORDER, CANCEL) - 기본값: ORDER").optional(),
                                parameterWithName("orderBy").description("정렬 기준 - 기본값: createdAt").optional(),
                                parameterWithName("direction").description("정렬 방향 (ASC, DESC) - 기본값: ASC").optional(),
                                parameterWithName("cursor").description("커서 (페이징)").optional(),
                                parameterWithName("after").description("이후 ID (페이징)").optional(),
                                parameterWithName("limit").description("조회할 개수 - 기본값: 10").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.content").description("주문 목록"),
                                fieldWithPath("data.content[].orderId").description("주문 번호"),
                                fieldWithPath("data.content[].orderDate").description("주문 날짜"),
                                fieldWithPath("data.content[].totalPrice").description("총 주문 금액"),
                                fieldWithPath("data.content[].paymentCompletedAt").description("결제 완료 시간"),
                                fieldWithPath("data.content[].orderer.name").description("주문자 이름"),
                                fieldWithPath("data.content[].orderer.phoneNumber").description("주문자 전화번호"),
                                fieldWithPath("data.content[].orderer.email").description("주문자 이메일"),
                                fieldWithPath("data.content[].deliveryAddress.id").description("배송지 ID"),
                                fieldWithPath("data.content[].deliveryAddress.address.recipient").description("수령인 이름"),
                                fieldWithPath("data.content[].deliveryAddress.address.phone").description("수령인 전화번호"),
                                fieldWithPath("data.content[].deliveryAddress.address.address").description("주소"),
                                fieldWithPath("data.content[].deliveryAddress.address.detailAddress").description("상세 주소"),
                                fieldWithPath("data.content[].deliveryAddress.address.zipcode").description("우편번호"),
                                fieldWithPath("data.nextCursor").description("다음 커서"),
                                fieldWithPath("data.nextAfter").description("다음 이후 ID"),
                                fieldWithPath("data.size").description("현재 페이지 크기"),
                                fieldWithPath("data.hasNext").description("다음 페이지 존재 여부")
                        )
                ));

        // then
        then(orderService).should().getOrders(
                any(User.class),
                eq(OrderStatus.ORDER),
                eq("createdAt"),
                eq("ASC"),
                eq("cursor123"),
                eq(1L),
                eq(10)
        );
    }

    @Test
    @DisplayName("주문 취소 성공")
    void cancelOrder_Success() throws Exception {
        // given

        OrderDto cancelledOrderDto = OrderDto.builder()
                .orderId(1L)
                .orderDate(LocalDateTime.now())
                .totalPrice(30000)
                .paymentCompletedAt(LocalDateTime.now())
                .orderer(OrdererDto.builder()
                        .name("테스트 사용자")
                        .phoneNumber("010-1234-5678")
                        .email("user@test.com")
                        .build())
                .deliveryAddress(DeliveryAddressDto.builder()
                        .id(1L)
                        .address(Address.builder()
                                .recipient("수령인")
                                .phone("010-9876-5432")
                                .address("서울시 강남구")
                                .detailAddress("101호")
                                .zipcode("12345")
                                .build())
                        .build())
                .build();

        given(orderRepository.findById(eq(orderId))).willReturn(Optional.of(order));
        given(orderService.cancelOrder(eq(orderId))).willReturn(cancelledOrderDto);

        // when & then
        mockMvc.perform(post("/api/orders/{orderId}/cancel", orderId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value(1))
                .andExpect(jsonPath("$.data.totalPrice").value(30000))
                .andDo(document("order-cancel",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (USER 또는 ADMIN 권한 필요, 권한 확인 필요)")
                        ),
                        pathParameters(
                                parameterWithName("orderId").description("주문 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.orderId").description("주문 번호"),
                                fieldWithPath("data.orderDate").description("주문 날짜"),
                                fieldWithPath("data.totalPrice").description("총 주문 금액"),
                                fieldWithPath("data.paymentCompletedAt").description("결제 완료 시간"),
                                fieldWithPath("data.orderer.name").description("주문자 이름"),
                                fieldWithPath("data.orderer.phoneNumber").description("주문자 전화번호"),
                                fieldWithPath("data.orderer.email").description("주문자 이메일"),
                                fieldWithPath("data.deliveryAddress.id").description("배송지 ID"),
                                fieldWithPath("data.deliveryAddress.address.recipient").description("수령인 이름"),
                                fieldWithPath("data.deliveryAddress.address.phone").description("수령인 전화번호"),
                                fieldWithPath("data.deliveryAddress.address.address").description("주소"),
                                fieldWithPath("data.deliveryAddress.address.detailAddress").description("상세 주소"),
                                fieldWithPath("data.deliveryAddress.address.zipcode").description("우편번호")
                        )
                ));

        // then
        then(orderService).should().cancelOrder(eq(orderId));
    }
}

