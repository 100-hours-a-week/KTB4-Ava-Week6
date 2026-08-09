package org.ktb.week6.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.week6.dto.TemporaryPostRequestDto;
import org.ktb.week6.entity.Comment;
import org.ktb.week6.entity.EventApplication;
import org.ktb.week6.entity.EventPost;
import org.ktb.week6.entity.File;
import org.ktb.week6.entity.Like;
import org.ktb.week6.entity.Post;
import org.ktb.week6.entity.Report;
import org.ktb.week6.entity.TemporaryPost;
import org.ktb.week6.entity.User;
import org.ktb.week6.enums.FileCategory;
import org.ktb.week6.enums.PostType;
import org.ktb.week6.enums.ReportReason;
import org.ktb.week6.enums.StatusType;
import org.ktb.week6.repository.CommentRepository;
import org.ktb.week6.repository.EventApplicationRepository;
import org.ktb.week6.repository.EventPostRepository;
import org.ktb.week6.repository.FileRepository;
import org.ktb.week6.repository.LikeRepository;
import org.ktb.week6.repository.PostRepository;
import org.ktb.week6.repository.ReportRepository;
import org.ktb.week6.repository.TemporaryPostRepository;
import org.ktb.week6.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Slf4j
@Component
@Profile("development")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mock-data.enabled", havingValue = "true")
public class MockDataInitializer implements CommandLineRunner {

    private static final String MOCK_EMAIL_PREFIX = "mock-user";
    private static final String MOCK_PASSWORD = "Password1!";
    private static final byte[] MOCK_PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/p9sAAAAASUVORK5CYII="
    );

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final EventPostRepository eventPostRepository;
    private final EventApplicationRepository eventApplicationRepository;
    private final LikeRepository likeRepository;
    private final ReportRepository reportRepository;
    private final TemporaryPostRepository temporaryPostRepository;
    private final FileRepository fileRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileProperties fileProperties;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByEmail(MOCK_EMAIL_PREFIX + "1@example.com")) {
            log.info("Mock data already exists. Skip seeding.");
            return;
        }

        createMockImageFiles();

        List<User> users = createUsers();
        User deletedUser = users.get(8);

        List<Post> generalPosts = createGeneralPosts(users, deletedUser);
        List<EventPost> meetingPosts = createMeetingPosts(users);

        createGeneralComments(users, generalPosts);
        createMeetingApplications(users, meetingPosts);
        createTemporaryPosts(users);

        log.info("Mock data seeded. users={}, generalPosts={}, meetingPosts={}",
                users.size(), generalPosts.size(), meetingPosts.size());
    }

    private List<User> createUsers() {
        String encodedPassword = passwordEncoder.encode(MOCK_PASSWORD);

        File profile1 = fileRepository.save(new File("/public/images/mock-profile-01.png", FileCategory.PROFILE_IMAGE));
        File profile2 = fileRepository.save(new File("/public/images/mock-profile-02.png", FileCategory.PROFILE_IMAGE));
        File profile3 = fileRepository.save(new File("/public/images/mock-profile-03.png", FileCategory.PROFILE_IMAGE));

        List<User> users = List.of(
                new User("mock-user1@example.com", encodedPassword, "하늘", profile1),
                new User("mock-user2@example.com", encodedPassword, "바다", profile2),
                new User("mock-user3@example.com", encodedPassword, "초록", profile3),
                new User("mock-user4@example.com", encodedPassword, "동네책방"),
                new User("mock-user5@example.com", encodedPassword, "러너"),
                new User("mock-user6@example.com", encodedPassword, "커피러버"),
                new User("mock-user7@example.com", encodedPassword, "사진가"),
                new User("mock-user8@example.com", encodedPassword, "집밥러"),
                new User("mock-left@example.com", encodedPassword, "탈퇴회원")
        );

        User deletedUser = users.get(8);
        deletedUser.deleteUser();
        deletedUser.insertDeletedReason("목업 데이터: 탈퇴한 사용자 예시");
        deletedUser.insertDeletedAt(LocalDateTime.now().minusDays(3));

        return userRepository.saveAll(users);
    }

    private List<Post> createGeneralPosts(List<User> users, User deletedUser) {
        List<File> attachments = fileRepository.saveAll(List.of(
                new File("/public/images/mock-post-01.png", FileCategory.POST_ATTACHMENT),
                new File("/public/images/mock-post-02.png", FileCategory.POST_ATTACHMENT),
                new File("/public/images/mock-post-03.png", FileCategory.POST_ATTACHMENT),
                new File("/public/images/mock-post-04.png", FileCategory.POST_ATTACHMENT)
        ));

        List<Post> posts = List.of(
                new Post("동네 카페 추천해주세요", "조용히 작업하기 좋은 카페를 찾고 있어요. 콘센트가 있고 오래 앉아 있어도 괜찮은 곳이면 좋겠습니다.", users.get(0), attachments.get(0)),
                new Post("주말 산책 코스 공유", "요즘 저녁 날씨가 좋아서 산책 코스를 찾고 있습니다. 강변이나 공원 코스 추천 부탁드려요.", users.get(1), null),
                new Post("중고 모니터 나눔합니다", "27인치 모니터를 정리하려고 합니다. 화면은 정상이고 직접 가져가실 분이면 좋겠습니다.", users.get(2), attachments.get(1)),
                new Post("동네 맛집 리스트", "최근에 가본 식당 중 괜찮았던 곳을 정리해봤습니다. 댓글로 추천도 남겨주세요.", users.get(3), null),
                new Post("러닝 같이 하실 분", "평일 저녁 8시에 가볍게 5km 정도 뛰고 있습니다. 초보자도 환영합니다.", users.get(4), null),
                new Post("분실물 찾습니다", "어제 공원 벤치 근처에서 검은색 카드지갑을 잃어버렸습니다. 보신 분은 댓글 부탁드립니다.", users.get(5), attachments.get(2)),
                new Post("사진 스팟 추천", "해 질 무렵 사진 찍기 좋은 장소를 찾고 있습니다. 사람이 너무 붐비지 않는 곳이면 좋겠습니다.", users.get(6), null),
                new Post("삭제된 일반 게시글", "삭제 상태 게시글은 목록에서 제외되는지 확인하기 위한 목업 데이터입니다.", users.get(7), null),
                new Post("탈퇴 유저 작성글", "작성자는 탈퇴했지만 게시글은 커뮤니티 흐름을 위해 남아있는 예시 데이터입니다.", deletedUser, null),
                new Post("신고 누적 블라인드 글", "이 게시글은 신고가 누적되어 블라인드 처리되는 화면을 확인하기 위한 목업 데이터입니다.", users.get(1), attachments.get(3))
        );

        posts.get(7).updateStatus(StatusType.DELETED);

        List<Post> savedPosts = postRepository.saveAll(posts);

        Post blindPost = savedPosts.get(9);
        for (int i = 0; i < 5; i++) {
            reportRepository.save(new Report(null, blindPost, users.get(i + 2), ReportReason.SPAM));
            blindPost.increaseReportCount();
        }
        blindPost.updateStatus(StatusType.BLIND);

        addLikes(savedPosts.get(0), users.get(1), users.get(2), users.get(3));
        addLikes(savedPosts.get(1), users.get(0), users.get(4));
        addLikes(savedPosts.get(2), users.get(5), users.get(6), users.get(7));
        addLikes(savedPosts.get(4), users.get(0), users.get(1), users.get(2), users.get(3));

        return savedPosts;
    }

    private List<EventPost> createMeetingPosts(List<User> users) {
        LocalDateTime now = LocalDateTime.now();

        List<Post> meetingBasePosts = postRepository.saveAll(List.of(
                new Post("마감된 독서 모임", "지난주에 모집이 마감된 독서 모임 예시입니다. 모집 상태가 EXPIRED로 표시되어야 합니다.", users.get(3), null),
                new Post("정원 마감 러닝", "정원이 모두 찬 러닝 모임 예시입니다. 모집 상태가 FULL로 표시되어야 합니다.", users.get(4), null),
                new Post("한 자리 남은 영화", "정원까지 딱 한 명 남은 모임 예시입니다. 신청 버튼 상태 확인용입니다.", users.get(5), null),
                new Post("방금 만든 커피챗", "방금 작성한 모임글 예시입니다. 작성자만 참여 인원에 포함된 OPEN 상태입니다.", users.get(0), null),
                new Post("주말 보드게임", "신청자가 조금 있는 열린 모임 예시입니다. 여유 좌석이 남아 있습니다.", users.get(2), null)
        ));

        return eventPostRepository.saveAll(List.of(
                new EventPost(meetingBasePosts.get(0), 5, now.minusDays(1)),
                new EventPost(meetingBasePosts.get(1), 3, now.plusDays(2)),
                new EventPost(meetingBasePosts.get(2), 4, now.plusDays(5)),
                new EventPost(meetingBasePosts.get(3), 6, now.plusDays(7)),
                new EventPost(meetingBasePosts.get(4), 8, now.plusDays(10))
        ));
    }

    private void createGeneralComments(List<User> users, List<Post> posts) {
        Comment comment1 = saveComment("저도 궁금했어요. 조용한 곳이면 더 좋겠네요.", posts.get(0), users.get(1), null);
        saveComment("역 근처에 늦게까지 하는 곳 하나 있어요.", posts.get(0), users.get(2), comment1);
        saveComment("강변 코스는 야간 조명도 괜찮아서 추천합니다.", posts.get(1), users.get(4), null);
        saveComment("나눔 받고 싶습니다. 오늘 저녁 가능할까요?", posts.get(2), users.get(5), null);
        saveComment("저도 러닝 참여하고 싶어요.", posts.get(4), users.get(6), null);
        saveComment("삭제된 댓글 화면 확인용입니다.", posts.get(5), users.get(7), null).updateStatusDeleted();
    }

    private void createMeetingApplications(List<User> users, List<EventPost> meetings) {
        apply(meetings.get(0), users.get(0), "마감 전 신청했던 사용자 예시입니다.");

        apply(meetings.get(1), users.get(0), "러닝 모임 참여합니다.");
        apply(meetings.get(1), users.get(1), "저도 같이 뛰고 싶어요.");

        apply(meetings.get(2), users.get(0), "영화 모임 신청합니다.");
        apply(meetings.get(2), users.get(1), "시간 맞으면 참여하고 싶어요.");

        apply(meetings.get(4), users.get(0), "보드게임 좋아해서 신청합니다.");
        apply(meetings.get(4), users.get(1), "초보도 가능하면 참여하고 싶습니다.");
    }

    private void createTemporaryPosts(List<User> users) {
        temporaryPostRepository.save(new TemporaryPost(
                new TemporaryPostRequestDto(
                        "임시저장 산책 모임",
                        "아직 장소를 정하지 못해서 임시저장한 모임글입니다.",
                        PostType.MEETING,
                        5,
                        LocalDateTime.now().plusDays(4)
                ),
                users.get(6),
                null
        ));
    }

    private Comment saveComment(String content, Post post, User user, Comment parent) {
        Comment comment = commentRepository.save(new Comment(content, post, user, parent));
        postRepository.increaseCommentCount(post.getId());
        return comment;
    }

    private void apply(EventPost eventPost, User user, String content) {
        Comment comment = commentRepository.save(new Comment(content, eventPost.getPost(), user, null));
        eventApplicationRepository.save(new EventApplication(eventPost, user, comment));
        eventPost.increaseApplicationCount();
        postRepository.increaseCommentCount(eventPost.getPost().getId());
    }

    private void addLikes(Post post, User... users) {
        for (User user : users) {
            likeRepository.save(new Like(post, user));
            post.increaseLikeCount();
        }
    }

    private void createMockImageFiles() {
        Path uploadDir = Path.of(fileProperties.uploadDir()).toAbsolutePath().normalize();

        try {
            Files.createDirectories(uploadDir);
            for (String name : List.of(
                    "mock-profile-01.png",
                    "mock-profile-02.png",
                    "mock-profile-03.png",
                    "mock-post-01.png",
                    "mock-post-02.png",
                    "mock-post-03.png",
                    "mock-post-04.png"
            )) {
                Path path = uploadDir.resolve(name);
                if (!Files.exists(path)) {
                    Files.write(path, MOCK_PNG);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create mock image files", e);
        }
    }
}
