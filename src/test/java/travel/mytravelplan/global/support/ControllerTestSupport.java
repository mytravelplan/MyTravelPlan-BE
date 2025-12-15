package travel.mytravelplan.global.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import travel.mytravelplan.domain.cart.repository.CartRepository;
import travel.mytravelplan.domain.checklist.repository.CheckListRepository;
import travel.mytravelplan.domain.comment.repository.PostCommentRepository;
import travel.mytravelplan.domain.comment.repository.ProductReviewCommentRepository;
import travel.mytravelplan.domain.comment.repository.TripPlaceReviewCommentRepository;
import travel.mytravelplan.domain.deck.repository.DeckRepository;
import travel.mytravelplan.domain.delivery.repository.DeliveryRepository;
import travel.mytravelplan.domain.diary.repository.DiaryRepository;
import travel.mytravelplan.domain.inquiry.repsotiroy.InquiryReplyRepository;
import travel.mytravelplan.domain.inquiry.repsotiroy.InquiryRepository;
import travel.mytravelplan.domain.order.repository.OrderRepository;
import travel.mytravelplan.domain.place.repository.CustomPlaceRepository;
import travel.mytravelplan.domain.post.repository.PostRepository;
import travel.mytravelplan.domain.product.repository.ProductRepository;
import travel.mytravelplan.domain.quiz.repository.QuizRepository;
import travel.mytravelplan.domain.review.repository.ProductReviewRepository;
import travel.mytravelplan.domain.review.repository.TripPlaceReviewRepository;
import travel.mytravelplan.domain.schedule.repository.ScheduleRepository;
import travel.mytravelplan.domain.trip.repository.TripJoinRepository;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.config.TestSecurityConfig;
import travel.mytravelplan.global.security.evaluator.MyTravelPlanPermissionEvaluator;
import travel.mytravelplan.global.security.jwt.handler.JwtAccessDeniedHandler;
import travel.mytravelplan.global.security.jwt.handler.JwtAuthenticationEntryPoint;
import travel.mytravelplan.global.security.jwt.handler.JwtAuthenticationFailureHandler;
import travel.mytravelplan.global.security.jwt.handler.JwtAuthenticationSuccessHandler;
import travel.mytravelplan.global.security.repository.RefreshTokenRepository;
import travel.mytravelplan.global.security.service.JwtBlacklistService;
import travel.mytravelplan.global.security.util.JwtUtils;

@ActiveProfiles("test")
@AutoConfigureRestDocs
@Import({TestSecurityConfig.class, JwtAuthenticationSuccessHandler.class, JwtAuthenticationFailureHandler.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class, JwtUtils.class, MyTravelPlanPermissionEvaluator.class})
public abstract class ControllerTestSupport {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected JwtBlacklistService jwtBlacklistService;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    protected JwtUtils jwtUtils;

    @MockitoBean
    protected UserRepository userRepository;

    @MockitoBean
    protected TripJoinRepository tripJoinRepository;

    @MockitoBean
    protected PostRepository postRepository;

    @MockitoBean
    protected PostCommentRepository postCommentRepository;

    @MockitoBean
    protected ProductReviewCommentRepository productReviewCommentRepository;

    @MockitoBean
    protected ScheduleRepository scheduleRepository;

    @MockitoBean
    protected CheckListRepository checkListRepository;

    @MockitoBean
    protected DiaryRepository diaryRepository;

    @MockitoBean
    protected CustomPlaceRepository customPlaceRepository;

    @MockitoBean
    protected TripPlaceReviewRepository tripPlaceReviewRepository;

    @MockitoBean
    protected ProductReviewRepository productReviewRepository;

    @MockitoBean
    protected TripPlaceReviewCommentRepository tripPlaceReviewCommentRepository;

    @MockitoBean
    protected DeckRepository deckRepository;

    @MockitoBean
    protected InquiryRepository inquiryRepository;

    @MockitoBean
    protected InquiryReplyRepository inquiryReplyRepository;

    @MockitoBean
    protected OrderRepository orderRepository;

    @MockitoBean
    protected ProductRepository productRepository;

    @MockitoBean
    protected QuizRepository quizRepository;

    @MockitoBean
    protected CartRepository cartRepository;

    @MockitoBean
    protected DeliveryRepository deliveryRepository;
}
