package travel.mytravelplan;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.comment.repository.PostCommentRepository;
import travel.mytravelplan.domain.post.entity.HashTag;
import travel.mytravelplan.domain.post.entity.Post;
import travel.mytravelplan.domain.post.entity.PostHashTag;
import travel.mytravelplan.domain.post.repository.HashTagRepository;
import travel.mytravelplan.domain.post.repository.PostHashTagRepository;
import travel.mytravelplan.domain.post.repository.PostLikeRepository;
import travel.mytravelplan.domain.post.repository.PostRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.exception.UserException;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.error.code.UserErrorCode;

import java.util.ArrayList;
import java.util.List;

@Profile("local")
@Component
@Order(2)
@RequiredArgsConstructor
public class PostInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostHashTagRepository postHashTagRepository;
    private final HashTagRepository hashTagRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCommentRepository postCommentRepository;

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {

        User user = userRepository.findByUsername("cksgud0403")
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        for (int i = 0; i < 100; i++) {

            List<HashTag> hashTags = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                HashTag hashTag = HashTag.createHashTag("포스트 " + i + " #태그 " + j);

                hashTagRepository.save(hashTag);
                hashTags.add(hashTag);
            }

            // 각 게시물에 대한 해시태그 생성
            Post post = Post.createPost(
                    "This is the content of post number " + i,
                    List.of("https://example.com/image" + i + ".jpg"),
                    user, hashTags);

            postRepository.save(post);

            for (HashTag hashTag : hashTags) {
                PostHashTag postHashTag = PostHashTag.createPostHashTag(hashTag);
                postHashTagRepository.save(postHashTag);
            }
        }

        /*
        // 필요 시 댓글 추가
        for(int j = 1; j <= 100; j++) {
            PostComment postComment = PostComment.builder()
                    .content("This is a comment " + j + " on post number " + i)
                    .user(user)
                    .post(post)
                    .build();
            postCommentRepository.save(postComment);
        }
        */
        }

    }
