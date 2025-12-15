package travel.mytravelplan.domain.order.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.delivery.entity.Delivery;
import travel.mytravelplan.domain.delivery.enums.Address;
import travel.mytravelplan.domain.delivery.enums.DeliveryStatus;
import travel.mytravelplan.domain.order.entity.Order;
import travel.mytravelplan.domain.order.entity.OrderProduct;
import travel.mytravelplan.domain.order.enums.OrderStatus;
import travel.mytravelplan.domain.order.enums.Orderer;
import travel.mytravelplan.domain.product.entity.Product;
import travel.mytravelplan.domain.product.repository.ProductRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.support.RepositoryTestSupport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("주문 레포지토리 테스트")
class OrderRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("주문을 저장한다")
    void saveOrder() {
        // given
        User user = createAndSaveUser("user1", "user1@email.com");
        Product product = createAndSaveProduct("상품1", "image1.jpg", 10000, 100, user);
        Delivery delivery = createDelivery();
        Orderer orderer = createOrderer("주문자1", "010-1234-5678", "orderer1@email.com");
        OrderProduct orderProduct = createOrderProduct(product, 10000, 2);
        Order order = Order.createOrder(user, delivery, orderer, List.of(orderProduct));

        // when
        Order savedOrder = orderRepository.save(order);
        em.flush();
        em.clear();

        // then
        Order foundOrder = orderRepository.findById(savedOrder.getId()).orElse(null);
        assertThat(foundOrder).isNotNull();
        assertThat(foundOrder.getOrderStatus()).isEqualTo(OrderStatus.ORDER);
        assertThat(foundOrder.getUser().getUsername()).isEqualTo("user1");
        assertThat(foundOrder.getOrderer().getName()).isEqualTo("주문자1");
        assertThat(foundOrder.getOrderProducts()).hasSize(1);
    }

    @Test
    @DisplayName("주문을 ID로 조회한다")
    void findOrderById() {
        // given
        User user = createAndSaveUser("user2", "user2@email.com");
        Product product = createAndSaveProduct("상품2", "image2.jpg", 20000, 50, user);
        Delivery delivery = createDelivery();
        Orderer orderer = createOrderer("주문자2", "010-2345-6789", "orderer2@email.com");
        OrderProduct orderProduct = createOrderProduct(product, 20000, 1);
        Order order = Order.createOrder(user, delivery, orderer, List.of(orderProduct));
        Order savedOrder = orderRepository.save(order);
        em.flush();
        em.clear();

        // when
        Order foundOrder = orderRepository.findById(savedOrder.getId()).orElse(null);

        // then
        assertThat(foundOrder).isNotNull();
        assertThat(foundOrder.getId()).isEqualTo(savedOrder.getId());
        assertThat(foundOrder.getOrderStatus()).isEqualTo(OrderStatus.ORDER);
    }

    @Test
    @DisplayName("주문을 배송 정보와 함께 조회한다")
    void findWithDeliveryById() {
        // given
        User user = createAndSaveUser("user3", "user3@email.com");
        Product product = createAndSaveProduct("상품3", "image3.jpg", 30000, 30, user);
        Delivery delivery = createDelivery();
        Orderer orderer = createOrderer("주문자3", "010-3456-7890", "orderer3@email.com");
        OrderProduct orderProduct = createOrderProduct(product, 30000, 3);
        Order order = Order.createOrder(user, delivery, orderer, List.of(orderProduct));
        Order savedOrder = orderRepository.save(order);
        em.flush();
        em.clear();

        // when
        Optional<Order> foundOrder = orderRepository.findWithDeliveryById(savedOrder.getId());

        // then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getDelivery()).isNotNull();
        assertThat(foundOrder.get().getDelivery().getDeliveryStatus()).isEqualTo(DeliveryStatus.READY);
        assertThat(foundOrder.get().getDelivery().getAddresses().getRecipient()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("주문을 삭제한다")
    void deleteOrder() {
        // given
        User user = createAndSaveUser("user4", "user4@email.com");
        Product product = createAndSaveProduct("상품4", "image4.jpg", 40000, 20, user);
        Delivery delivery = createDelivery();
        Orderer orderer = createOrderer("주문자4", "010-4567-8901", "orderer4@email.com");
        OrderProduct orderProduct = createOrderProduct(product, 40000, 1);
        Order order = Order.createOrder(user, delivery, orderer, List.of(orderProduct));
        Order savedOrder = orderRepository.save(order);
        em.flush();
        em.clear();

        // when
        orderRepository.deleteById(savedOrder.getId());
        em.flush();
        em.clear();

        // then
        Order deletedOrder = orderRepository.findById(savedOrder.getId()).orElse(null);
        assertThat(deletedOrder).isNull();
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 주문을 조회한다 - 생성일 오름차순")
    void findAllByCursor_orderByCreatedAtAsc() {
        // given
        User user = createAndSaveUser("user5", "user5@email.com");
        Product product = createAndSaveProduct("상품5", "image5.jpg", 50000, 10, user);

        Delivery delivery1 = createDelivery();
        Orderer orderer1 = createOrderer("주문자5-1", "010-5678-9012", "orderer5-1@email.com");
        OrderProduct orderProduct1 = createOrderProduct(product, 50000, 1);
        Order order1 = Order.createOrder(user, delivery1, orderer1, List.of(orderProduct1));

        Delivery delivery2 = createDelivery();
        Orderer orderer2 = createOrderer("주문자5-2", "010-5678-9013", "orderer5-2@email.com");
        OrderProduct orderProduct2 = createOrderProduct(product, 50000, 2);
        Order order2 = Order.createOrder(user, delivery2, orderer2, List.of(orderProduct2));

        Delivery delivery3 = createDelivery();
        Orderer orderer3 = createOrderer("주문자5-3", "010-5678-9014", "orderer5-3@email.com");
        OrderProduct orderProduct3 = createOrderProduct(product, 50000, 3);
        Order order3 = Order.createOrder(user, delivery3, orderer3, List.of(orderProduct3));

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);
        em.flush();
        em.clear();

        // when - 첫 번째 페이지 조회
        List<Order> firstPage = orderRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getOrderer().getName()).isEqualTo("주문자5-1");
        assertThat(firstPage.get(1).getOrderer().getName()).isEqualTo("주문자5-2");

        // when - 두 번째 페이지 조회
        Order lastOrder = firstPage.getLast();
        List<Order> secondPage = orderRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "asc",
                lastOrder.getCreatedAt().toString(),
                lastOrder.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.getFirst().getOrderer().getName()).isEqualTo("주문자5-3");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 주문을 조회한다 - 생성일 내림차순")
    void findAllByCursor_orderByCreatedAtDesc() {
        // given
        User user = createAndSaveUser("user6", "user6@email.com");
        Product product = createAndSaveProduct("상품6", "image6.jpg", 60000, 5, user);

        Delivery delivery1 = createDelivery();
        Orderer orderer1 = createOrderer("주문자6-1", "010-6789-0123", "orderer6-1@email.com");
        OrderProduct orderProduct1 = createOrderProduct(product, 60000, 1);
        Order order1 = Order.createOrder(user, delivery1, orderer1, List.of(orderProduct1));

        Delivery delivery2 = createDelivery();
        Orderer orderer2 = createOrderer("주문자6-2", "010-6789-0124", "orderer6-2@email.com");
        OrderProduct orderProduct2 = createOrderProduct(product, 60000, 2);
        Order order2 = Order.createOrder(user, delivery2, orderer2, List.of(orderProduct2));

        Delivery delivery3 = createDelivery();
        Orderer orderer3 = createOrderer("주문자6-3", "010-6789-0125", "orderer6-3@email.com");
        OrderProduct orderProduct3 = createOrderProduct(product, 60000, 3);
        Order order3 = Order.createOrder(user, delivery3, orderer3, List.of(orderProduct3));

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);
        em.flush();
        em.clear();

        // when
        List<Order> orders = orderRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(orders).hasSize(3);
        assertThat(orders.get(0).getOrderer().getName()).isEqualTo("주문자6-3");
        assertThat(orders.get(1).getOrderer().getName()).isEqualTo("주문자6-2");
        assertThat(orders.get(2).getOrderer().getName()).isEqualTo("주문자6-1");
    }

    @Test
    @DisplayName("주문 상태로 주문을 필터링한다")
    void findAllByCursor_filterByOrderStatus() {
        // given
        User user = createAndSaveUser("user7", "user7@email.com");
        Product product = createAndSaveProduct("상품7", "image7.jpg", 70000, 15, user);

        Delivery delivery1 = createDelivery();
        Orderer orderer1 = createOrderer("주문자7-1", "010-7890-1234", "orderer7-1@email.com");
        OrderProduct orderProduct1 = createOrderProduct(product, 70000, 1);
        Order order1 = Order.createOrder(user, delivery1, orderer1, List.of(orderProduct1));

        Delivery delivery2 = createDelivery();
        Orderer orderer2 = createOrderer("주문자7-2", "010-7890-1235", "orderer7-2@email.com");
        OrderProduct orderProduct2 = createOrderProduct(product, 70000, 2);
        Order order2 = Order.createOrder(user, delivery2, orderer2, List.of(orderProduct2));

        orderRepository.save(order1);
        orderRepository.save(order2);
        em.flush();
        em.clear();

        // when
        List<Order> orders = orderRepository.findAllByCursor(
                user.getUsername(),
                OrderStatus.ORDER,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(orders).hasSize(2);
        assertThat(orders).allMatch(order -> order.getOrderStatus() == OrderStatus.ORDER);
    }

    @Test
    @DisplayName("사용자명으로 주문을 필터링한다")
    void findAllByCursor_filterByUsername() {
        // given
        User user1 = createAndSaveUser("user8", "user8@email.com");
        User user2 = createAndSaveUser("user9", "user9@email.com");
        Product product = createAndSaveProduct("상품8", "image8.jpg", 80000, 25, user1);

        Delivery delivery1 = createDelivery();
        Orderer orderer1 = createOrderer("주문자8", "010-8901-2345", "orderer8@email.com");
        OrderProduct orderProduct1 = createOrderProduct(product, 80000, 1);
        Order order1 = Order.createOrder(user1, delivery1, orderer1, List.of(orderProduct1));

        Delivery delivery2 = createDelivery();
        Orderer orderer2 = createOrderer("주문자9", "010-8901-2346", "orderer9@email.com");
        OrderProduct orderProduct2 = createOrderProduct(product, 80000, 2);
        Order order2 = Order.createOrder(user2, delivery2, orderer2, List.of(orderProduct2));

        orderRepository.save(order1);
        orderRepository.save(order2);
        em.flush();
        em.clear();

        // when
        List<Order> orders = orderRepository.findAllByCursor(
                "user8",
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(orders).hasSize(1);
        assertThat(orders.getFirst().getUser().getUsername()).isEqualTo("user8");
    }

    @Test
    @DisplayName("여러 상품을 포함한 주문을 저장한다")
    void saveOrderWithMultipleProducts() {
        // given
        User user = createAndSaveUser("user10", "user10@email.com");
        Product product1 = createAndSaveProduct("상품9", "image9.jpg", 90000, 5, user);
        Product product2 = createAndSaveProduct("상품10", "image10.jpg", 10000, 10, user);
        Delivery delivery = createDelivery();
        Orderer orderer = createOrderer("주문자10", "010-9012-3456", "orderer10@email.com");
        OrderProduct orderProduct1 = createOrderProduct(product1, 90000, 1);
        OrderProduct orderProduct2 = createOrderProduct(product2, 10000, 2);
        Order order = Order.createOrder(user, delivery, orderer, List.of(orderProduct1, orderProduct2));

        // when
        Order savedOrder = orderRepository.save(order);
        em.flush();
        em.clear();

        // then
        Order foundOrder = orderRepository.findById(savedOrder.getId()).orElse(null);
        assertThat(foundOrder).isNotNull();
        assertThat(foundOrder.getOrderProducts()).hasSize(2);
        assertThat(foundOrder.getOrderProducts())
                .extracting(OrderProduct::getOrderPrice)
                .containsExactlyInAnyOrder(90000, 10000);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 주문을 조회하면 빈 결과를 반환한다")
    void findOrderById_notFound() {
        // when
        Optional<Order> foundOrder = orderRepository.findById(999999L);

        // then
        assertThat(foundOrder).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 ID로 배송 정보와 함께 주문을 조회하면 빈 결과를 반환한다")
    void findWithDeliveryById_notFound() {
        // when
        Optional<Order> foundOrder = orderRepository.findWithDeliveryById(999999L);

        // then
        assertThat(foundOrder).isEmpty();
    }

    @Test
    @DisplayName("사용자명이 null일 때 모든 주문을 조회한다")
    void findAllByCursor_withNullUsername() {
        // given
        User user1 = createAndSaveUser("user11", "user11@email.com");
        User user2 = createAndSaveUser("user12", "user12@email.com");
        Product product = createAndSaveProduct("상품11", "image11.jpg", 100000, 5, user1);

        Delivery delivery1 = createDelivery();
        Orderer orderer1 = createOrderer("주문자11", "010-1111-1111", "orderer11@email.com");
        OrderProduct orderProduct1 = createOrderProduct(product, 100000, 1);
        Order order1 = Order.createOrder(user1, delivery1, orderer1, List.of(orderProduct1));

        Delivery delivery2 = createDelivery();
        Orderer orderer2 = createOrderer("주문자12", "010-2222-2222", "orderer12@email.com");
        OrderProduct orderProduct2 = createOrderProduct(product, 100000, 1);
        Order order2 = Order.createOrder(user2, delivery2, orderer2, List.of(orderProduct2));

        orderRepository.save(order1);
        orderRepository.save(order2);
        em.flush();
        em.clear();

        // when
        List<Order> orders = orderRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(orders).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("주문 상태가 null일 때 모든 상태의 주문을 조회한다")
    void findAllByCursor_withNullOrderStatus() {
        // given
        User user = createAndSaveUser("user13", "user13@email.com");
        Product product = createAndSaveProduct("상품12", "image12.jpg", 110000, 3, user);

        Delivery delivery1 = createDelivery();
        Orderer orderer1 = createOrderer("주문자13-1", "010-3333-3333", "orderer13-1@email.com");
        OrderProduct orderProduct1 = createOrderProduct(product, 110000, 1);
        Order order1 = Order.createOrder(user, delivery1, orderer1, List.of(orderProduct1));

        Delivery delivery2 = createDelivery();
        Orderer orderer2 = createOrderer("주문자13-2", "010-3333-3334", "orderer13-2@email.com");
        OrderProduct orderProduct2 = createOrderProduct(product, 110000, 1);
        Order order2 = Order.createOrder(user, delivery2, orderer2, List.of(orderProduct2));

        orderRepository.save(order1);
        orderRepository.save(order2);
        em.flush();
        em.clear();

        // when
        List<Order> orders = orderRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(orders).hasSize(2);
    }

    @Test
    @DisplayName("커서가 null일 때 첫 페이지부터 조회한다")
    void findAllByCursor_withNullCursor() {
        // given
        User user = createAndSaveUser("user14", "user14@email.com");
        Product product = createAndSaveProduct("상품13", "image13.jpg", 120000, 7, user);

        Delivery delivery = createDelivery();
        Orderer orderer = createOrderer("주문자14", "010-4444-4444", "orderer14@email.com");
        OrderProduct orderProduct = createOrderProduct(product, 120000, 1);
        Order order = Order.createOrder(user, delivery, orderer, List.of(orderProduct));

        orderRepository.save(order);
        em.flush();
        em.clear();

        // when
        List<Order> orders = orderRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(orders).hasSize(1);
        assertThat(orders.getFirst().getOrderer().getName()).isEqualTo("주문자14");
    }

    @Test
    @DisplayName("limit이 1일 때 한 개의 주문만 조회한다")
    void findAllByCursor_withLimitOne() {
        // given
        User user = createAndSaveUser("user15", "user15@email.com");
        Product product = createAndSaveProduct("상품14", "image14.jpg", 130000, 2, user);

        Delivery delivery1 = createDelivery();
        Orderer orderer1 = createOrderer("주문자15-1", "010-5555-5555", "orderer15-1@email.com");
        OrderProduct orderProduct1 = createOrderProduct(product, 130000, 1);
        Order order1 = Order.createOrder(user, delivery1, orderer1, List.of(orderProduct1));

        Delivery delivery2 = createDelivery();
        Orderer orderer2 = createOrderer("주문자15-2", "010-5555-5556", "orderer15-2@email.com");
        OrderProduct orderProduct2 = createOrderProduct(product, 130000, 1);
        Order order2 = Order.createOrder(user, delivery2, orderer2, List.of(orderProduct2));

        orderRepository.save(order1);
        orderRepository.save(order2);
        em.flush();
        em.clear();

        // when
        List<Order> orders = orderRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                1
        );

        // then
        assertThat(orders).hasSize(1);
    }

    @Test
    @DisplayName("커서와 after를 모두 제공하여 다음 페이지를 조회한다")
    void findAllByCursor_withCursorAndAfter() {
        // given
        User user = createAndSaveUser("user16", "user16@email.com");
        Product product = createAndSaveProduct("상품15", "image15.jpg", 140000, 8, user);

        Delivery delivery1 = createDelivery();
        Orderer orderer1 = createOrderer("주문자16-1", "010-6666-6666", "orderer16-1@email.com");
        OrderProduct orderProduct1 = createOrderProduct(product, 140000, 1);
        Order order1 = Order.createOrder(user, delivery1, orderer1, List.of(orderProduct1));

        Delivery delivery2 = createDelivery();
        Orderer orderer2 = createOrderer("주문자16-2", "010-6666-6667", "orderer16-2@email.com");
        OrderProduct orderProduct2 = createOrderProduct(product, 140000, 1);
        Order order2 = Order.createOrder(user, delivery2, orderer2, List.of(orderProduct2));

        orderRepository.save(order1);
        orderRepository.save(order2);
        em.flush();
        em.clear();

        // when - 첫 번째 조회
        List<Order> firstPage = orderRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                1
        );

        Order lastOrder = firstPage.getFirst();

        // when - 커서와 after를 사용한 다음 페이지 조회
        List<Order> secondPage = orderRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "asc",
                lastOrder.getCreatedAt().toString(),
                lastOrder.getId(),
                1
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.getFirst().getId()).isNotEqualTo(firstPage.getFirst().getId());
    }

    @Test
    @DisplayName("내림차순으로 커서를 사용하여 이전 페이지를 조회한다")
    void findAllByCursor_descWithCursor() {
        // given
        User user = createAndSaveUser("user17", "user17@email.com");
        Product product = createAndSaveProduct("상품16", "image16.jpg", 150000, 4, user);

        Delivery delivery1 = createDelivery();
        Orderer orderer1 = createOrderer("주문자17-1", "010-7777-7777", "orderer17-1@email.com");
        OrderProduct orderProduct1 = createOrderProduct(product, 150000, 1);
        Order order1 = Order.createOrder(user, delivery1, orderer1, List.of(orderProduct1));

        Delivery delivery2 = createDelivery();
        Orderer orderer2 = createOrderer("주문자17-2", "010-7777-7778", "orderer17-2@email.com");
        OrderProduct orderProduct2 = createOrderProduct(product, 150000, 1);
        Order order2 = Order.createOrder(user, delivery2, orderer2, List.of(orderProduct2));

        Delivery delivery3 = createDelivery();
        Orderer orderer3 = createOrderer("주문자17-3", "010-7777-7779", "orderer17-3@email.com");
        OrderProduct orderProduct3 = createOrderProduct(product, 150000, 1);
        Order order3 = Order.createOrder(user, delivery3, orderer3, List.of(orderProduct3));

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);
        em.flush();
        em.clear();

        // when - 첫 번째 페이지 (내림차순)
        List<Order> firstPage = orderRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getOrderer().getName()).isEqualTo("주문자17-3");
        assertThat(firstPage.get(1).getOrderer().getName()).isEqualTo("주문자17-2");

        // when - 두 번째 페이지
        Order lastOrder = firstPage.getLast();
        List<Order> secondPage = orderRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "desc",
                lastOrder.getCreatedAt().toString(),
                lastOrder.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.getFirst().getOrderer().getName()).isEqualTo("주문자17-1");
    }

    @Test
    @DisplayName("사용자명과 주문 상태를 모두 필터링하여 조회한다")
    void findAllByCursor_filterByUsernameAndOrderStatus() {
        // given
        User user1 = createAndSaveUser("user18", "user18@email.com");
        User user2 = createAndSaveUser("user19", "user19@email.com");
        Product product = createAndSaveProduct("상품17", "image17.jpg", 160000, 6, user1);

        Delivery delivery1 = createDelivery();
        Orderer orderer1 = createOrderer("주문자18", "010-8888-8888", "orderer18@email.com");
        OrderProduct orderProduct1 = createOrderProduct(product, 160000, 1);
        Order order1 = Order.createOrder(user1, delivery1, orderer1, List.of(orderProduct1));

        Delivery delivery2 = createDelivery();
        Orderer orderer2 = createOrderer("주문자19", "010-8888-8889", "orderer19@email.com");
        OrderProduct orderProduct2 = createOrderProduct(product, 160000, 1);
        Order order2 = Order.createOrder(user2, delivery2, orderer2, List.of(orderProduct2));

        orderRepository.save(order1);
        orderRepository.save(order2);
        em.flush();
        em.clear();

        // when
        List<Order> orders = orderRepository.findAllByCursor(
                "user18",
                OrderStatus.ORDER,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(orders).hasSize(1);
        assertThat(orders.getFirst().getUser().getUsername()).isEqualTo("user18");
        assertThat(orders.getFirst().getOrderStatus()).isEqualTo(OrderStatus.ORDER);
    }

    @Test
    @DisplayName("조회 결과가 없을 때 빈 리스트를 반환한다")
    void findAllByCursor_emptyResult() {
        // given
        User user = createAndSaveUser("user20", "user20@email.com");

        // when
        List<Order> orders = orderRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(orders).isEmpty();
    }

    @Test
    @DisplayName("배송 정보와 함께 주문을 조회할 때 연관된 배송 정보가 로드된다")
    void findWithDeliveryById_fetchJoin() {
        // given
        User user = createAndSaveUser("user21", "user21@email.com");
        Product product = createAndSaveProduct("상품18", "image18.jpg", 170000, 9, user);
        Delivery delivery = createDelivery();
        Orderer orderer = createOrderer("주문자21", "010-9999-9999", "orderer21@email.com");
        OrderProduct orderProduct = createOrderProduct(product, 170000, 1);
        Order order = Order.createOrder(user, delivery, orderer, List.of(orderProduct));
        Order savedOrder = orderRepository.save(order);
        em.flush();
        em.clear();

        // when
        Optional<Order> foundOrder = orderRepository.findWithDeliveryById(savedOrder.getId());

        // then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getDelivery()).isNotNull();
        assertThat(foundOrder.get().getDelivery().getRequirement()).isEqualTo("문 앞에 놔주세요");
        assertThat(foundOrder.get().getDelivery().getAddresses().getZipcode()).isEqualTo("12345");
        assertThat(foundOrder.get().getDelivery().getAddresses().getAddress()).isEqualTo("서울시 강남구");
        assertThat(foundOrder.get().getDelivery().getAddresses().getDetailAddress()).isEqualTo("101호");
    }

    @Test
    @DisplayName("cursor가 null이지만 after가 있는 경우 - after는 무시된다")
    void findAllByCursor_withNullCursorButAfterExists() {
        // given
        User user = createAndSaveUser("user26", "user26@email.com");
        Product product = createAndSaveProduct("상품23", "image23.jpg", 210000, 9, user);

        Delivery delivery1 = createDelivery();
        Orderer orderer1 = createOrderer("주문자26-1", "010-3333-3333", "orderer26-1@email.com");
        OrderProduct orderProduct1 = createOrderProduct(product, 210000, 1);
        Order order1 = Order.createOrder(user, delivery1, orderer1, List.of(orderProduct1));

        Delivery delivery2 = createDelivery();
        Orderer orderer2 = createOrderer("주문자26-2", "010-3333-3334", "orderer26-2@email.com");
        OrderProduct orderProduct2 = createOrderProduct(product, 210000, 1);
        Order order2 = Order.createOrder(user, delivery2, orderer2, List.of(orderProduct2));

        orderRepository.save(order1);
        orderRepository.save(order2);
        em.flush();
        em.clear();

        // when - cursor는 null이지만 after는 999L로 설정
        List<Order> orders = orderRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "asc",
                null,
                999L,
                10
        );

        // then - cursor가 null이므로 모든 데이터 조회 (after는 무시됨)
        assertThat(orders).hasSize(2);
    }

    @Test
    @DisplayName("cursor만 있고 after가 null인 경우 - 조건이 적용되지 않는다")
    void findAllByCursor_withCursorButNullAfter() {
        // given
        User user = createAndSaveUser("user27", "user27@email.com");
        Product product = createAndSaveProduct("상품24", "image24.jpg", 220000, 11, user);

        Delivery delivery1 = createDelivery();
        Orderer orderer1 = createOrderer("주문자27-1", "010-4444-4444", "orderer27-1@email.com");
        OrderProduct orderProduct1 = createOrderProduct(product, 220000, 1);
        Order order1 = Order.createOrder(user, delivery1, orderer1, List.of(orderProduct1));

        Delivery delivery2 = createDelivery();
        Orderer orderer2 = createOrderer("주문자27-2", "010-4444-4445", "orderer27-2@email.com");
        OrderProduct orderProduct2 = createOrderProduct(product, 220000, 1);
        Order order2 = Order.createOrder(user, delivery2, orderer2, List.of(orderProduct2));

        orderRepository.save(order1);
        orderRepository.save(order2);
        em.flush();
        em.clear();

        // when - cursor는 있지만 after는 null
        List<Order> orders = orderRepository.findAllByCursor(
                user.getUsername(),
                null,
                "createdAt",
                "asc",
                order1.getCreatedAt().toString(),
                null,
                10
        );

        // then - after가 null이므로 조건이 적용되지 않고 모든 데이터 조회
        assertThat(orders).hasSize(2);
    }

    @Test
    @DisplayName("주문과 연관된 주문 상품이 함께 저장된다")
    void saveOrder_withOrderProducts() {
        // given
        User user = createAndSaveUser("user23", "user23@email.com");
        Product product = createAndSaveProduct("상품20", "image20.jpg", 180000, 11, user);
        Delivery delivery = createDelivery();
        Orderer orderer = createOrderer("주문자23", "010-0000-1111", "orderer23@email.com");
        OrderProduct orderProduct = createOrderProduct(product, 180000, 5);
        Order order = Order.createOrder(user, delivery, orderer, List.of(orderProduct));

        // when
        Order savedOrder = orderRepository.save(order);
        em.flush();
        em.clear();

        // then
        Order foundOrder = orderRepository.findById(savedOrder.getId()).orElse(null);
        assertThat(foundOrder).isNotNull();
        assertThat(foundOrder.getOrderProducts()).hasSize(1);
        assertThat(foundOrder.getOrderProducts().getFirst().getQuantity()).isEqualTo(5);
        assertThat(foundOrder.getOrderProducts().getFirst().getProduct().getName()).isEqualTo("상품20");
    }

    @Test
    @DisplayName("주문 날짜가 자동으로 설정된다")
    void createOrder_autoSetOrderDate() {
        // given
        User user = createAndSaveUser("user24", "user24@email.com");
        Product product = createAndSaveProduct("상품21", "image21.jpg", 190000, 13, user);
        Delivery delivery = createDelivery();
        Orderer orderer = createOrderer("주문자24", "010-1111-1111", "orderer24@email.com");
        OrderProduct orderProduct = createOrderProduct(product, 190000, 1);
        Order order = Order.createOrder(user, delivery, orderer, List.of(orderProduct));

        // when
        Order savedOrder = orderRepository.save(order);
        em.flush();
        em.clear();

        // then
        Order foundOrder = orderRepository.findById(savedOrder.getId()).orElse(null);
        assertThat(foundOrder).isNotNull();
        assertThat(foundOrder.getOrderDate()).isNotNull();
    }

    @Test
    @DisplayName("주문 상태가 ORDER로 초기화된다")
    void createOrder_defaultOrderStatus() {
        // given
        User user = createAndSaveUser("user25", "user25@email.com");
        Product product = createAndSaveProduct("상품22", "image22.jpg", 200000, 14, user);
        Delivery delivery = createDelivery();
        Orderer orderer = createOrderer("주문자25", "010-2222-2222", "orderer25@email.com");
        OrderProduct orderProduct = createOrderProduct(product, 200000, 1);
        Order order = Order.createOrder(user, delivery, orderer, List.of(orderProduct));

        // when
        Order savedOrder = orderRepository.save(order);
        em.flush();
        em.clear();

        // then
        Order foundOrder = orderRepository.findById(savedOrder.getId()).orElse(null);
        assertThat(foundOrder).isNotNull();
        assertThat(foundOrder.getOrderStatus()).isEqualTo(OrderStatus.ORDER);
    }

    // TestFixture 메서드들
    private User createUser(String username, String email) {
        return User.createUser(
                username,
                "password123",
                email,
                SocialType.LOCAL,
                null,
                LocalDate.of(1990, 1, 1),
                "010-1234-5678",
                Gender.MALE,
                Set.of(Role.USER)
        );
    }

    private User createAndSaveUser(String username, String email) {
        User user = createUser(username, email);
        return userRepository.save(user);
    }

    private Product createProduct(String name, String imageUrl, int price, int stockQuantity, User seller) {
        return Product.createProduct(
                name,
                imageUrl,
                price,
                stockQuantity,
                List.of(),
                seller
        );
    }

    private Product createAndSaveProduct(String name, String imageUrl, int price, int stockQuantity, User seller) {
        Product product = createProduct(name, imageUrl, price, stockQuantity, seller);
        return productRepository.save(product);
    }

    private Delivery createDelivery() {
        Address address = Address.builder()
                .recipient("홍길동")
                .phone("010-1111-2222")
                .zipcode("12345")
                .address("서울시 강남구")
                .detailAddress("101호")
                .build();
        return Delivery.createDelivery(address, "문 앞에 놔주세요");
    }

    private Orderer createOrderer(String name, String phoneNumber, String email) {
        return Orderer.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .email(email)
                .build();
    }

    private OrderProduct createOrderProduct(Product product, int orderPrice, int quantity) {
        return OrderProduct.createOrderProduct(product, orderPrice, quantity);
    }
}