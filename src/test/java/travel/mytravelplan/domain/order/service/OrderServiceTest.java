package travel.mytravelplan.domain.order.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.delivery.entity.Delivery;
import travel.mytravelplan.domain.delivery.entity.DeliveryAddress;
import travel.mytravelplan.domain.delivery.enums.Address;
import travel.mytravelplan.domain.delivery.repository.DeliveryAddressRepository;
import travel.mytravelplan.domain.order.dto.OrderCreateRequestDto;
import travel.mytravelplan.domain.order.dto.OrderDto;
import travel.mytravelplan.domain.order.entity.Order;
import travel.mytravelplan.domain.order.entity.OrderProduct;
import travel.mytravelplan.domain.order.enums.OrderStatus;
import travel.mytravelplan.domain.order.enums.Orderer;
import travel.mytravelplan.domain.order.exception.OrderException;
import travel.mytravelplan.domain.order.mapper.OrderMapper;
import travel.mytravelplan.domain.order.repository.OrderRepository;
import travel.mytravelplan.domain.product.entity.Product;
import travel.mytravelplan.domain.product.repository.ProductRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.support.ServiceTestSupport;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("주문 서비스 테스트")
class OrderServiceTest extends ServiceTestSupport {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private DeliveryAddressRepository deliveryAddressRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product product1;
    private Product product2;
    private DeliveryAddress deliveryAddress;
    private Address address;
    private Orderer orderer;
    private Order order;
    private OrderDto orderDto;
    private OrderCreateRequestDto createRequestDto;

    @BeforeEach
    void setUp() {
        user = User.createUser("testuser", "password", "test@test.com", null, null, null);

        // 주문자 정보
        orderer = Orderer.builder()
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .email("hong@test.com")
                .build();

        // 배송지 정보
        address = Address.builder()
                .recipient("김철수")
                .phone("010-9876-5432")
                .zipcode("12345")
                .address("서울시 강남구")
                .detailAddress("101동 202호")
                .build();

        deliveryAddress = DeliveryAddress.createDeliveryAddress(address, true, user);
        ReflectionTestUtils.setField(deliveryAddress, "id", 1L);

        // 상품 정보
        product1 = Product.createProduct("상품1", "image1.jpg", 10000, 100, new ArrayList<>(), user);
        ReflectionTestUtils.setField(product1, "id", 1L);

        product2 = Product.createProduct("상품2", "image2.jpg", 20000, 50, new ArrayList<>(), user);
        ReflectionTestUtils.setField(product2, "id", 2L);

        // 주문 생성 요청 DTO
        Map<Long, Integer> products = new HashMap<>();
        products.put(1L, 2);
        products.put(2L, 1);

        createRequestDto = OrderCreateRequestDto.builder()
                .products(products)
                .orderer(orderer)
                .deliveryAddressId(1L)
                .requirement("문 앞에 놔주세요")
                .build();

        // 주문 엔티티
        Delivery delivery = Delivery.createDelivery(address, "문 앞에 놔주세요");
        List<OrderProduct> orderProducts = Arrays.asList(
                OrderProduct.createOrderProduct(product1, 10000, 2),
                OrderProduct.createOrderProduct(product2, 20000, 1)
        );
        order = Order.createOrder(user, delivery, orderer, orderProducts);
        ReflectionTestUtils.setField(order, "id", 1L);
        ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.of(2024, 1, 1, 12, 0, 0));

        // 주문 DTO
        orderDto = OrderDto.builder()
                .orderId(1L)
                .orderDate(LocalDateTime.of(2024, 1, 1, 12, 0, 0))
                .totalPrice(40000)
                .build();
    }

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_Success() {
        // given
        List<Product> productList = Arrays.asList(product1, product2);
        given(productRepository.findAllByIds(any())).willReturn(productList);
        given(deliveryAddressRepository.findById(eq(1L))).willReturn(Optional.of(deliveryAddress));
        given(orderRepository.save(any(Order.class))).willReturn(order);
        given(orderMapper.toDto(any(Order.class))).willReturn(orderDto);

        // when
        OrderDto result = orderService.createOrder(user, createRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(1L);
        assertThat(result.getTotalPrice()).isEqualTo(40000);

        then(productRepository).should().findAllByIds(any());
        then(deliveryAddressRepository).should().findById(eq(1L));
        then(orderRepository).should().save(any(Order.class));
        then(orderMapper).should().toDto(any(Order.class));
    }

    @Test
    @DisplayName("주문 조회 성공")
    void getOrder_Success() {
        // given
        Long orderId = 1L;
        given(orderRepository.findWithDeliveryById(eq(orderId))).willReturn(Optional.of(order));
        given(orderMapper.toDto(eq(order))).willReturn(orderDto);

        // when
        OrderDto result = orderService.getOrder(orderId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(1L);

        then(orderRepository).should().findWithDeliveryById(eq(orderId));
        then(orderMapper).should().toDto(eq(order));
    }

    @Test
    @DisplayName("주문 조회 실패 - 존재하지 않는 주문")
    void getOrder_NotFound() {
        // given
        Long orderId = 999L;
        given(orderRepository.findWithDeliveryById(eq(orderId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.getOrder(orderId))
                .isInstanceOf(OrderException.class);

        then(orderRepository).should().findWithDeliveryById(eq(orderId));
    }

    @Test
    @DisplayName("사용자별 주문 목록 조회 성공")
    void getOrders_Success() {
        // given
        OrderStatus orderStatus = OrderStatus.ORDER;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        Order order2 = Order.createOrder(user,
                Delivery.createDelivery(address, "배송 요청"),
                orderer,
                new ArrayList<>());
        ReflectionTestUtils.setField(order2, "id", 2L);
        ReflectionTestUtils.setField(order2, "createdAt", LocalDateTime.of(2024, 1, 2, 12, 0, 0));

        List<Order> orders = Arrays.asList(order, order2);

        OrderDto orderDto2 = OrderDto.builder()
                .orderId(2L)
                .orderDate(LocalDateTime.of(2024, 1, 2, 12, 0, 0))
                .totalPrice(20000)
                .build();

        given(orderRepository.findAllByCursor(eq("testuser"), eq(orderStatus), eq(orderBy),
                eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(orders);
        given(orderMapper.toDto(eq(order))).willReturn(orderDto);
        given(orderMapper.toDto(eq(order2))).willReturn(orderDto2);

        // when
        CursorPageResponseDto<OrderDto> result = orderService.getOrders(user, orderStatus, orderBy,
                direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getSize()).isEqualTo(2);

        then(orderRepository).should().findAllByCursor(eq("testuser"), eq(orderStatus), eq(orderBy),
                eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(orderMapper).should().toDto(eq(order));
        then(orderMapper).should().toDto(eq(order2));
    }

    @Test
    @DisplayName("사용자별 주문 목록 조회 성공 - hasNext true")
    void getOrders_HasNext() {
        // given
        OrderStatus orderStatus = OrderStatus.ORDER;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 2;

        Order order2 = Order.createOrder(user,
                Delivery.createDelivery(address, "배송 요청"),
                orderer,
                new ArrayList<>());
        ReflectionTestUtils.setField(order2, "id", 2L);
        ReflectionTestUtils.setField(order2, "createdAt", LocalDateTime.of(2024, 1, 2, 12, 0, 0));

        Order order3 = Order.createOrder(user,
                Delivery.createDelivery(address, "배송 요청"),
                orderer,
                new ArrayList<>());
        ReflectionTestUtils.setField(order3, "id", 3L);
        ReflectionTestUtils.setField(order3, "createdAt", LocalDateTime.of(2024, 1, 3, 12, 0, 0));

        List<Order> orders = Arrays.asList(order, order2, order3);

        OrderDto orderDto2 = OrderDto.builder()
                .orderId(2L)
                .orderDate(LocalDateTime.of(2024, 1, 2, 12, 0, 0))
                .totalPrice(20000)
                .build();

        given(orderRepository.findAllByCursor(eq("testuser"), eq(orderStatus), eq(orderBy),
                eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(orders);
        given(orderMapper.toDto(eq(order))).willReturn(orderDto);
        given(orderMapper.toDto(eq(order2))).willReturn(orderDto2);

        // when
        CursorPageResponseDto<OrderDto> result = orderService.getOrders(user, orderStatus, orderBy,
                direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isNotNull();
        assertThat(result.getNextAfter()).isEqualTo(2L);

        then(orderRepository).should().findAllByCursor(eq("testuser"), eq(orderStatus), eq(orderBy),
                eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(orderMapper).should().toDto(eq(order));
        then(orderMapper).should().toDto(eq(order2));
    }

    @Test
    @DisplayName("사용자별 주문 목록 조회 성공 - 빈 목록")
    void getOrders_EmptyList() {
        // given
        OrderStatus orderStatus = OrderStatus.ORDER;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        given(orderRepository.findAllByCursor(eq("testuser"), eq(orderStatus), eq(orderBy),
                eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(List.of());

        // when
        CursorPageResponseDto<OrderDto> result = orderService.getOrders(user, orderStatus, orderBy,
                direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();

        then(orderRepository).should().findAllByCursor(eq("testuser"), eq(orderStatus), eq(orderBy),
                eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("주문 취소 성공")
    void cancelOrder_Success() {
        // given
        Long orderId = 1L;
        given(orderRepository.findById(eq(orderId))).willReturn(Optional.of(order));
        given(orderMapper.toDto(eq(order))).willReturn(orderDto);

        // when
        OrderDto result = orderService.cancelOrder(orderId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(1L);

        then(orderRepository).should().findById(eq(orderId));
        then(orderMapper).should().toDto(eq(order));
    }

    @Test
    @DisplayName("주문 취소 실패 - 존재하지 않는 주문")
    void cancelOrder_NotFound() {
        // given
        Long orderId = 999L;
        given(orderRepository.findById(eq(orderId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId))
                .isInstanceOf(OrderException.class);

        then(orderRepository).should().findById(eq(orderId));
    }

    @Test
    @DisplayName("주문 생성 실패 - 존재하지 않는 배송지")
    void createOrder_DeliveryAddressNotFound() {
        // given
        List<Product> productList = Arrays.asList(product1, product2);
        given(productRepository.findAllByIds(any())).willReturn(productList);
        given(deliveryAddressRepository.findById(eq(1L))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(user, createRequestDto))
                .isInstanceOf(travel.mytravelplan.domain.delivery.exception.DeliveryAddressException.class);

        then(productRepository).should().findAllByIds(any());
        then(deliveryAddressRepository).should().findById(eq(1L));
    }

    @Test
    @DisplayName("주문 취소 실패 - 배송 중인 주문")
    void cancelOrder_AlreadyDelivering() {
        // given
        Long orderId = 1L;
        Delivery delivering = Delivery.createDelivery(address, "배송 요청");
        delivering.update(travel.mytravelplan.domain.delivery.enums.DeliveryStatus.DELIVERING);
        List<OrderProduct> orderProducts = Arrays.asList(
                OrderProduct.createOrderProduct(product1, 10000, 2)
        );
        Order deliveringOrder = Order.createOrder(user, delivering, orderer, orderProducts);
        ReflectionTestUtils.setField(deliveringOrder, "id", orderId);

        given(orderRepository.findById(eq(orderId))).willReturn(Optional.of(deliveringOrder));

        // when & then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId))
                .isInstanceOf(OrderException.class);

        then(orderRepository).should().findById(eq(orderId));
    }

    @Test
    @DisplayName("주문 취소 실패 - 배송 완료된 주문")
    void cancelOrder_AlreadyCompleted() {
        // given
        Long orderId = 1L;
        Delivery completed = Delivery.createDelivery(address, "배송 요청");
        completed.update(travel.mytravelplan.domain.delivery.enums.DeliveryStatus.COMP);
        List<OrderProduct> orderProducts = Arrays.asList(
                OrderProduct.createOrderProduct(product1, 10000, 2)
        );
        Order completedOrder = Order.createOrder(user, completed, orderer, orderProducts);
        ReflectionTestUtils.setField(completedOrder, "id", orderId);

        given(orderRepository.findById(eq(orderId))).willReturn(Optional.of(completedOrder));

        // when & then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId))
                .isInstanceOf(OrderException.class);

        then(orderRepository).should().findById(eq(orderId));
    }

    @Test
    @DisplayName("주문 생성 실패 - 재고 부족")
    void createOrder_InsufficientStock() {
        // given
        Product product3 = Product.createProduct("재고부족상품", "image3.jpg", 15000, 1, new ArrayList<>(), user);
        ReflectionTestUtils.setField(product3, "id", 3L);

        Map<Long, Integer> products = new HashMap<>();
        products.put(3L, 10); // 재고는 1개인데 10개 주문

        OrderCreateRequestDto requestDto = OrderCreateRequestDto.builder()
                .products(products)
                .orderer(orderer)
                .deliveryAddressId(1L)
                .requirement("빠른 배송 부탁드려요")
                .build();

        List<Product> productList = List.of(product3);
        given(productRepository.findAllByIds(any())).willReturn(productList);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(user, requestDto))
                .isInstanceOf(travel.mytravelplan.domain.product.exception.ProductException.class);

        then(productRepository).should().findAllByIds(any());
    }

    @Test
    @DisplayName("주문 생성 성공 - 여러 상품 주문")
    void createOrder_MultipleProducts_Success() {
        // given
        List<Product> productList = Arrays.asList(product1, product2);
        given(productRepository.findAllByIds(any())).willReturn(productList);
        given(deliveryAddressRepository.findById(eq(1L))).willReturn(Optional.of(deliveryAddress));
        given(orderRepository.save(any(Order.class))).willReturn(order);
        given(orderMapper.toDto(any(Order.class))).willReturn(orderDto);

        // when
        OrderDto result = orderService.createOrder(user, createRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(1L);
        assertThat(result.getTotalPrice()).isEqualTo(40000);

        then(productRepository).should().findAllByIds(any());
        then(deliveryAddressRepository).should().findById(eq(1L));
        then(orderRepository).should().save(any(Order.class));
    }

    @Test
    @DisplayName("주문 취소 성공 - READY 상태")
    void cancelOrder_ReadyStatus_Success() {
        // given
        Long orderId = 1L;
        Delivery readyDelivery = Delivery.createDelivery(address, "배송 요청");
        List<OrderProduct> orderProducts = List.of(
                OrderProduct.createOrderProduct(product1, 10000, 2)
        );
        Order readyOrder = Order.createOrder(user, readyDelivery, orderer, orderProducts);
        ReflectionTestUtils.setField(readyOrder, "id", orderId);

        given(orderRepository.findById(eq(orderId))).willReturn(Optional.of(readyOrder));
        given(orderMapper.toDto(eq(readyOrder))).willReturn(orderDto);

        // when
        OrderDto result = orderService.cancelOrder(orderId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(1L);

        then(orderRepository).should().findById(eq(orderId));
        then(orderMapper).should().toDto(eq(readyOrder));
    }

    @Test
    @DisplayName("사용자별 주문 목록 조회 - 취소된 주문만 조회")
    void getOrders_CancelStatus() {
        // given
        OrderStatus orderStatus = OrderStatus.CANCEL;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        List<Order> orders = List.of(order);

        given(orderRepository.findAllByCursor(eq("testuser"), eq(orderStatus), eq(orderBy),
                eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(orders);
        given(orderMapper.toDto(eq(order))).willReturn(orderDto);

        // when
        CursorPageResponseDto<OrderDto> result = orderService.getOrders(user, orderStatus, orderBy,
                direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();

        then(orderRepository).should().findAllByCursor(eq("testuser"), eq(orderStatus), eq(orderBy),
                eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("주문 생성 성공 - 단일 상품 주문")
    void createOrder_SingleProduct_Success() {
        // given
        Map<Long, Integer> products = new HashMap<>();
        products.put(1L, 1);

        OrderCreateRequestDto singleProductRequest = OrderCreateRequestDto.builder()
                .products(products)
                .orderer(orderer)
                .deliveryAddressId(1L)
                .requirement("안전하게 배송해주세요")
                .build();

        List<Product> productList = List.of(product1);
        given(productRepository.findAllByIds(any())).willReturn(productList);
        given(deliveryAddressRepository.findById(eq(1L))).willReturn(Optional.of(deliveryAddress));
        given(orderRepository.save(any(Order.class))).willReturn(order);
        given(orderMapper.toDto(any(Order.class))).willReturn(orderDto);

        // when
        OrderDto result = orderService.createOrder(user, singleProductRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(1L);

        then(productRepository).should().findAllByIds(any());
        then(deliveryAddressRepository).should().findById(eq(1L));
        then(orderRepository).should().save(any(Order.class));
    }

    @Test
    @DisplayName("주문 생성 실패 - 존재하지 않는 상품 ID 포함")
    void createOrder_ProductNotFound() {
        // given
        Map<Long, Integer> products = new HashMap<>();
        products.put(999L, 1); // 존재하지 않는 상품

        OrderCreateRequestDto requestDto = OrderCreateRequestDto.builder()
                .products(products)
                .orderer(orderer)
                .deliveryAddressId(1L)
                .requirement("빠른 배송 부탁드려요")
                .build();

        given(productRepository.findAllByIds(any())).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(user, requestDto))
                .isInstanceOf(NullPointerException.class);

        then(productRepository).should().findAllByIds(any());
    }

    @Test
    @DisplayName("사용자별 주문 목록 조회 - 오름차순 정렬")
    void getOrders_AscendingOrder() {
        // given
        OrderStatus orderStatus = OrderStatus.ORDER;
        String orderBy = "createdAt";
        String direction = "asc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        List<Order> orders = List.of(order);

        given(orderRepository.findAllByCursor(eq("testuser"), eq(orderStatus), eq(orderBy),
                eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(orders);
        given(orderMapper.toDto(eq(order))).willReturn(orderDto);

        // when
        CursorPageResponseDto<OrderDto> result = orderService.getOrders(user, orderStatus, orderBy,
                direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();

        then(orderRepository).should().findAllByCursor(eq("testuser"), eq(orderStatus), eq(orderBy),
                eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("주문 생성 성공 - 배송 요청사항 없음")
    void createOrder_NoRequirement_Success() {
        // given
        Map<Long, Integer> products = new HashMap<>();
        products.put(1L, 1);

        OrderCreateRequestDto noRequirementRequest = OrderCreateRequestDto.builder()
                .products(products)
                .orderer(orderer)
                .deliveryAddressId(1L)
                .requirement(null)
                .build();

        List<Product> productList = List.of(product1);
        given(productRepository.findAllByIds(any())).willReturn(productList);
        given(deliveryAddressRepository.findById(eq(1L))).willReturn(Optional.of(deliveryAddress));
        given(orderRepository.save(any(Order.class))).willReturn(order);
        given(orderMapper.toDto(any(Order.class))).willReturn(orderDto);

        // when
        OrderDto result = orderService.createOrder(user, noRequirementRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(1L);

        then(productRepository).should().findAllByIds(any());
        then(deliveryAddressRepository).should().findById(eq(1L));
        then(orderRepository).should().save(any(Order.class));
    }

    @Test
    @DisplayName("사용자별 주문 목록 조회 - limit이 1인 경우")
    void getOrders_LimitOne() {
        // given
        OrderStatus orderStatus = OrderStatus.ORDER;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 1;

        Order order2 = Order.createOrder(user,
                Delivery.createDelivery(address, "배송 요청"),
                orderer,
                new ArrayList<>());
        ReflectionTestUtils.setField(order2, "id", 2L);
        ReflectionTestUtils.setField(order2, "createdAt", LocalDateTime.of(2024, 1, 2, 12, 0, 0));

        List<Order> orders = Arrays.asList(order, order2);

        given(orderRepository.findAllByCursor(eq("testuser"), eq(orderStatus), eq(orderBy),
                eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(orders);
        given(orderMapper.toDto(eq(order))).willReturn(orderDto);

        // when
        CursorPageResponseDto<OrderDto> result = orderService.getOrders(user, orderStatus, orderBy,
                direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isNotNull();

        then(orderRepository).should().findAllByCursor(eq("testuser"), eq(orderStatus), eq(orderBy),
                eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("주문 생성 성공 - 재고가 정확히 일치하는 경우")
    void createOrder_ExactStockMatch_Success() {
        // given
        Product product3 = Product.createProduct("정확재고상품", "image3.jpg", 15000, 5, new ArrayList<>(), user);
        ReflectionTestUtils.setField(product3, "id", 3L);

        Map<Long, Integer> products = new HashMap<>();
        products.put(3L, 5); // 재고와 정확히 일치

        OrderCreateRequestDto requestDto = OrderCreateRequestDto.builder()
                .products(products)
                .orderer(orderer)
                .deliveryAddressId(1L)
                .requirement("배송 요청")
                .build();

        List<Product> productList = List.of(product3);
        given(productRepository.findAllByIds(any())).willReturn(productList);
        given(deliveryAddressRepository.findById(eq(1L))).willReturn(Optional.of(deliveryAddress));
        given(orderRepository.save(any(Order.class))).willReturn(order);
        given(orderMapper.toDto(any(Order.class))).willReturn(orderDto);

        // when
        OrderDto result = orderService.createOrder(user, requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(1L);

        then(productRepository).should().findAllByIds(any());
        then(deliveryAddressRepository).should().findById(eq(1L));
        then(orderRepository).should().save(any(Order.class));
    }
}

