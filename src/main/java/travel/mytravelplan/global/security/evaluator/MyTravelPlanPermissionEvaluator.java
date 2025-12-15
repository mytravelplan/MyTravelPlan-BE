package travel.mytravelplan.global.security.evaluator;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import travel.mytravelplan.domain.cart.entity.Cart;
import travel.mytravelplan.domain.cart.repository.CartRepository;
import travel.mytravelplan.domain.checklist.entity.CheckList;
import travel.mytravelplan.domain.checklist.entity.PersonalCheckList;
import travel.mytravelplan.domain.checklist.entity.SharedCheckList;
import travel.mytravelplan.domain.checklist.repository.CheckListRepository;
import travel.mytravelplan.domain.comment.entity.PostComment;
import travel.mytravelplan.domain.comment.entity.ProductReviewComment;
import travel.mytravelplan.domain.comment.entity.TripPlaceReviewComment;
import travel.mytravelplan.domain.comment.repository.PostCommentRepository;
import travel.mytravelplan.domain.comment.repository.ProductReviewCommentRepository;
import travel.mytravelplan.domain.comment.repository.TripPlaceReviewCommentRepository;
import travel.mytravelplan.domain.deck.entity.Deck;
import travel.mytravelplan.domain.deck.repository.DeckRepository;
import travel.mytravelplan.domain.delivery.entity.Delivery;
import travel.mytravelplan.domain.delivery.repository.DeliveryRepository;
import travel.mytravelplan.domain.diary.entity.Diary;
import travel.mytravelplan.domain.diary.repository.DiaryRepository;
import travel.mytravelplan.domain.inquiry.entity.Inquiry;
import travel.mytravelplan.domain.inquiry.entity.InquiryReply;
import travel.mytravelplan.domain.inquiry.repsotiroy.InquiryReplyRepository;
import travel.mytravelplan.domain.inquiry.repsotiroy.InquiryRepository;
import travel.mytravelplan.domain.order.entity.Order;
import travel.mytravelplan.domain.order.repository.OrderRepository;
import travel.mytravelplan.domain.place.entity.CustomPlace;
import travel.mytravelplan.domain.place.repository.CustomPlaceRepository;
import travel.mytravelplan.domain.post.entity.Post;
import travel.mytravelplan.domain.post.repository.PostRepository;
import travel.mytravelplan.domain.product.entity.Product;
import travel.mytravelplan.domain.product.repository.ProductRepository;
import travel.mytravelplan.domain.quiz.entity.Quiz;
import travel.mytravelplan.domain.quiz.repository.QuizRepository;
import travel.mytravelplan.domain.review.entity.TripPlaceReview;
import travel.mytravelplan.domain.review.entity.ProductReview;
import travel.mytravelplan.domain.review.repository.TripPlaceReviewRepository;
import travel.mytravelplan.domain.review.repository.ProductReviewRepository;
import travel.mytravelplan.domain.schedule.entity.Schedule;
import travel.mytravelplan.domain.schedule.repository.ScheduleRepository;
import travel.mytravelplan.domain.trip.entity.TripJoin;
import travel.mytravelplan.domain.trip.repository.TripJoinRepository;
import travel.mytravelplan.global.security.jwt.CustomUserPrincipal;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class MyTravelPlanPermissionEvaluator implements PermissionEvaluator {
    private final TripJoinRepository tripJoinRepository;
    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final ProductReviewCommentRepository productReviewCommentRepository;
    private final ScheduleRepository scheduleRepository;
    private final CheckListRepository checkListRepository;
    private final DiaryRepository diaryRepository;
    private final CustomPlaceRepository customPlaceRepository;
    private final TripPlaceReviewRepository tripPlaceReviewRepository;
    private final ProductReviewRepository productReviewRepository;
    private final TripPlaceReviewCommentRepository tripPlaceReviewCommentRepository;
    private final DeckRepository deckRepository;
    private final InquiryRepository inquiryRepository;
    private final InquiryReplyRepository inquiryReplyRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final QuizRepository quizRepository;
    private final CartRepository cartRepository;
    private final DeliveryRepository deliveryRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || targetId == null || !(permission instanceof String permissionString)) {
            return false;
        }

        if (targetType.equals("Post")) {
            Post post = postRepository.findById((Long) targetId)
                .orElse(null);

            if(post == null) {
                return false;
            }

            return hasPostPermission(authentication, post, permissionString);
        }

        if(targetType.equals("PostComment")) {
            PostComment postComment = postCommentRepository.findById((Long) targetId)
                    .orElse(null);

            if(postComment == null) {
                return false;
            }

            return hasPostCommentPermission(authentication, postComment, permissionString);
        }

        if (targetType.equals("Trip")) {
            Long tripId = (Long) targetId;
            return hasTripPermission(authentication, tripId, permissionString);
        }

        if(targetType.equals("CustomPlace")) {
            CustomPlace customPlace = customPlaceRepository.findById((Long) targetId)
                    .orElse(null);

            if(customPlace == null) {
                return false;
            }

            return hasCustomPlacePermission(authentication, customPlace, permissionString);
        }

        if(targetType.equals("TripPlaceReview")) {
            TripPlaceReview tripPlaceReview = tripPlaceReviewRepository.findById((Long) targetId)
                    .orElse(null);

            if(tripPlaceReview == null) {
                return false;
            }

            return hasTripPlaceReviewPermission(authentication, tripPlaceReview, permissionString);
        }

        if(targetType.equals("TripPlaceReviewComment")) {
            TripPlaceReviewComment tripPlaceReviewComment = tripPlaceReviewCommentRepository.findById((Long) targetId)
                    .orElse(null);

            if(tripPlaceReviewComment == null) {
                return false;
            }

            return hasTripPlaceReviewCommentPermission(authentication, tripPlaceReviewComment, permissionString);
        }

        if(targetType.equals("ProductReview")) {
            ProductReview productReview = productReviewRepository.findById((Long) targetId)
                    .orElse(null);

            if(productReview == null) {
                return false;
            }

            return hasProductReviewPermission(authentication, productReview, permissionString);
        }

        if(targetType.equals("Schedule")) {
            Schedule schedule = scheduleRepository.findById((Long) targetId)
                    .orElse(null);

            if(schedule == null) {
                return false;
            }

            return hasSchedulePermission(authentication, schedule, permissionString);
        }

        if(targetType.equals("ProductReviewComment")) {
            ProductReviewComment productReviewComment = productReviewCommentRepository.findById((Long) targetId)
                    .orElse(null);

            if(productReviewComment == null) {
                return false;
            }

            return hasProductReviewCommentPermission(authentication, productReviewComment, permissionString);
        }

        if(targetType.equals("CheckList")) {
            CheckList checkList = checkListRepository.findById((Long) targetId)
                    .orElse(null);

            if(checkList == null) {
                return false;
            }

            return hasCheckListPermission(authentication, checkList, permissionString);
        }

        if(targetType.equals("Diary")) {
            Diary diary = diaryRepository.findById((Long) targetId)
                    .orElse(null);

            if(diary == null) {
                return false;
            }
            return hasDiaryPermission(authentication, diary, permissionString);
        }

        if(targetType.equals("Deck")) {
            Deck deck = deckRepository.findById((Long) targetId)
                    .orElse(null);

            if(deck == null) {
                return false;
            }

            return hasDeckPermission(authentication, deck, permissionString);
        }

        if(targetType.equals("Inquiry")) {
            Inquiry inquiry = inquiryRepository.findById((Long) targetId)
                    .orElse(null);

            if(inquiry == null) {
                return false;
            }

            return hasInquiryPermission(authentication, inquiry, permissionString);
        }

        if(targetType.equals("InquiryReply")) {
            InquiryReply inquiryReply = inquiryReplyRepository.findById((Long) targetId)
                    .orElse(null);

            if(inquiryReply == null) {
                return false;
            }

            return hasInquiryReplyPermission(authentication, inquiryReply, permissionString);
        }

        if(targetType.equals("Order")) {
            Order order = orderRepository.findById((Long) targetId)
                    .orElse(null);

            if(order == null) {
                return false;
            }

            return hasOrderPermission(authentication, order, permissionString);
        }

        if(targetType.equals("Product")) {
            Product product = productRepository.findById((Long) targetId)
                    .orElse(null);

            if(product == null) {
                return false;
            }

            return hasProductPermission(authentication, product, permissionString);
        }

        if(targetType.equals("Quiz")) {
            Quiz quiz = quizRepository.findById((Long) targetId)
                    .orElse(null);

            if(quiz == null) {
                return false;
            }

            return hasQuizPermission(authentication, quiz, permissionString);
        }

        if(targetType.equals("Cart")) {
            Cart cart = cartRepository.findById((Long) targetId)
                    .orElse(null);

            if(cart == null) {
                return false;
            }

            return hasCartPermission(authentication, cart, permissionString);
        }

        if(targetType.equals("Delivery")) {
            Delivery delivery = deliveryRepository.findById((Long) targetId)
                    .orElse(null);

            if(delivery == null) {
                return false;
            }

            return hasDeliveryPermission(authentication, delivery, permissionString);
        }

        return false;
    }

    private boolean hasDeliveryPermission(Authentication authentication, Delivery delivery, String permissionString) {
        Long userId =  ((CustomUserPrincipal) authentication.getPrincipal()).getId();

        switch (permissionString) {
            case "delivery:read":
                return delivery.getOrder().getUser().getId().equals(userId);
            default:
                return false;
        }
    }

    private boolean hasCartPermission(Authentication authentication, Cart cart, String permissionString) {
        Long userId =  ((CustomUserPrincipal) authentication.getPrincipal()).getId();

        switch (permissionString) {
            case "cart:update":
            case "cart:delete":
                return cart.getUser().getId().equals(userId);
            default:
                return false;
        }
    }

    private boolean hasQuizPermission(Authentication authentication, Quiz quiz, String permissionString) {
        Long userId =  ((CustomUserPrincipal) authentication.getPrincipal()).getId();

        switch (permissionString) {
            case "quiz:question:read":
            case "quiz:finish":
            case "quiz:result:read":
            case "quiz:result:delete":
                return quiz.getUser().getId().equals(userId);
            default:
                return false;
        }
    }

    private boolean hasProductPermission(Authentication authentication, Product product, String permissionString) {
        Long userId =  ((CustomUserPrincipal) authentication.getPrincipal()).getId();

        switch (permissionString) {
            case "product:update":
            case "product:delete":
                return product.getSeller().getId().equals(userId);
            default:
                return false;
        }
    }

    private boolean hasOrderPermission(Authentication authentication, Order order, String permissionString) {
        Long userId =  ((CustomUserPrincipal) authentication.getPrincipal()).getId();

        switch (permissionString) {
            case "order:read":
            case "order:cancel":
                return order.getUser().getId().equals(userId);
            default:
                return false;
        }
    }

    private boolean hasInquiryReplyPermission(Authentication authentication, InquiryReply inquiryReply, String permissionString) {
        Long userId =  ((CustomUserPrincipal) authentication.getPrincipal()).getId();

        switch (permissionString) {
            case "inquiryReply:read":
                if(inquiryReply.getInquiry().isSecret()) {
                    return inquiryReply.getInquiry().getUser().getId().equals(userId);
                } else {
                    return true;
                }
            case "inquiryReply:update":
            case "inquiryReply:delete":
                return inquiryReply.getUser().getId().equals(userId);
            default:
                return false;
        }
    }

    private boolean hasInquiryPermission(Authentication authentication, Inquiry inquiry, String permissionString) {
        Long userId =  ((CustomUserPrincipal) authentication.getPrincipal()).getId();

        switch (permissionString) {
            case "inquiry:read":
                if(inquiry.isSecret()) {
                    return inquiry.getUser().getId().equals(userId);
                } else {
                    return true;
                }
            case "inquiry:update":
            case "inquiry:delete":
                return inquiry.getUser().getId().equals(userId);
            default:
                return false;
        }
    }

    private boolean hasTripPlaceReviewCommentPermission(Authentication authentication, TripPlaceReviewComment tripPlaceReviewComment, String permissionString) {
        Long userId =  ((CustomUserPrincipal) authentication.getPrincipal()).getId();

        switch (permissionString) {
            case "tripPlaceReviewComment:update":
            case "tripPlaceReviewComment:delete":
                return tripPlaceReviewComment.getUser().getId().equals(userId);
            default:
                return false;
        }
    }

    private boolean hasPostPermission(Authentication authentication, Post post, String permissionString) {
        Long userId =  ((CustomUserPrincipal) authentication.getPrincipal()).getId();

        switch  (permissionString) {
            case "post:update":
            case "post:delete":
                return post.getUser().getId().equals(userId);
            default:
                return false;
        }
    }

    private boolean hasPostCommentPermission(Authentication authentication, PostComment postComment, String permissionString) {
        Long userId =  ((CustomUserPrincipal) authentication.getPrincipal()).getId();

        switch (permissionString) {
            case "postComment:update":
            case "postComment:delete":
                return postComment.getUser().getId().equals(userId);
            default:
                return false;
        }
    }

    private boolean hasDeckPermission(Authentication authentication, Deck deck, String permissionString) {
        Long userId =  ((CustomUserPrincipal) authentication.getPrincipal()).getId();

        switch (permissionString) {
            case "deck:read":
            case "deck:update":
            case "deck:delete":
            case "deck:card:create":
            case "deck:card:read":
            case "deck:card:update":
            case "deck:card:delete":
                return deck.getUser().getId().equals(userId);
            default:
                return false;
        }
    }

    private boolean hasTripPermission(Authentication authentication, Long tripId, String permissionString) {
        Long userId =  ((CustomUserPrincipal) authentication.getPrincipal()).getId();

        switch (permissionString) {
            case "trip:read":
            case "trip:update":
            case "trip:delete":
            case "trip:settle":
            case "trip:invite":
            case "trip:stats":
            case "trip:tripCurrency:create":
            case "trip:tripCurrency:read":
            case "trip:tripCurrency:update":
            case "trip:exportExpenses":
            case "trip:schedule:create":
            case "trip:schedule:read":
            case "trip:schedule:update":
            case "trip:schedule:delete":
            case "trip:expense:create":
            case "trip:expense:read":
            case "trip:expense:update":
            case "trip:expense:delete":
            case "trip:diary:create":
            case "trip:diary:read":
            case "trip:diary:update":
            case "trip:diary:delete":
            case "trip:album:create":
            case "trip:album:read":
            case "trip:album:update":
            case "trip:album:delete":
            case "trip:album:photo:create":
            case "trip:album:photo:read":
            case "trip:album:photo:update":
            case "trip:album:photo:delete":
            case "trip:budget:create":
            case "trip:budget:read":
            case "trip:budget:update":
            case "trip:budget:delete":
            case "trip:checkList:create":
            case "trip:checkList:read":
            case "trip:checkList:update":
            case "trip:checkList:delete":
            case "trip:checkListItem:create":
            case "trip:checkListItem:read":
            case "trip:checkListItem:update":
            case "trip:checkListItem:delete":
                return tripJoinRepository.existsByUserIdAndTripId(userId, tripId);
            default:
                return false;
        }
    }

    private boolean hasTripPlaceReviewPermission(Authentication authentication, TripPlaceReview tripPlaceReview, String permissionString) {
        Long userId =  ((CustomUserPrincipal) authentication.getPrincipal()).getId();

        switch (permissionString) {
            case "tripPlaceReview:update":
            case "tripPlaceReview:delete":
                return tripPlaceReview.getUser().getId().equals(userId);
            default:
                return false;
        }
    }

    private boolean hasProductReviewPermission(Authentication authentication, ProductReview productReview, String permissionString) {
        Long userId =  ((CustomUserPrincipal) authentication.getPrincipal()).getId();

        switch (permissionString) {
            case "productReview:update":
            case "productReview:delete":
                return productReview.getUser().getId().equals(userId);
            default:
                return false;
        }
    }

    private boolean hasProductReviewCommentPermission(Authentication authentication, ProductReviewComment productReviewComment, String permissionString) {
        Long userId =  ((CustomUserPrincipal) authentication.getPrincipal()).getId();

        switch (permissionString) {
            case "productReviewComment:update":
            case "productReviewComment:delete":
                return productReviewComment.getUser().getId().equals(userId);
            default:
                return false;
        }
    }

    private boolean hasSchedulePermission(Authentication authentication, Schedule schedule, String permissionString) {
        Long userId =  ((CustomUserPrincipal) authentication.getPrincipal()).getId();

        switch (permissionString) {
            case "schedule:read":
            case "schedule:update":
            case "schedule:delete":
                return tripJoinRepository.existsByUserIdAndTripId(userId, schedule.getTrip().getId());
            default:
                return false;
        }
    }

    private boolean hasCheckListPermission(Authentication authentication, CheckList checkList, String permissionString) {
        Long userId =  ((CustomUserPrincipal) authentication.getPrincipal()).getId();

        if(checkList instanceof SharedCheckList) {
            return true;
        }else if(checkList instanceof PersonalCheckList) {
            PersonalCheckList personalCheckList = (PersonalCheckList) checkList;

            switch (permissionString) {
                case "checkList:checkListItem:update":
                case "checkList:checkListItem:delete":
                    TripJoin tripJoin = tripJoinRepository.findByUserIdAndTripId(userId, personalCheckList.getTrip().getId()).orElse(null);

                    if(tripJoin == null) {
                        return false;
                    }

                    return personalCheckList.getTripJoin().getId().equals(tripJoin.getId());
                default:
                    return false;
            }
        }

        return false;
    }

    private boolean hasDiaryPermission(Authentication authentication, Diary diary, String permissionString) {
        Long userId =  ((CustomUserPrincipal) authentication.getPrincipal()).getId();

        switch (permissionString) {
            case "diary:update":
            case "diary:delete":
                TripJoin tripJoin = tripJoinRepository.findByUserIdAndTripId(userId, diary.getTrip().getId()).orElse(null);
                if(tripJoin == null) {
                    return false;
                }
                return diary.getTripJoin().getId().equals(tripJoin.getId());
            default:
                return false;
        }
    }

    private boolean hasCustomPlacePermission(Authentication authentication, CustomPlace customPlace, String permissionString) {
        Long userId =  ((CustomUserPrincipal) authentication.getPrincipal()).getId();

        switch (permissionString) {
            case "customPlace:read":
            case "customPlace:update":
            case "customPlace:delete":
                return customPlace.getUser().getId().equals(userId);
            default:
                return false;
        }
    }
}