package org.ktb.week6.config;

import lombok.RequiredArgsConstructor;
import org.ktb.week6.entity.Comment;
import org.ktb.week6.entity.File;
import org.ktb.week6.entity.Like;
import org.ktb.week6.entity.Post;
import org.ktb.week6.entity.PostHistory;
import org.ktb.week6.entity.PostViewLogs;
import org.ktb.week6.entity.Report;
import org.ktb.week6.entity.TemporaryPost;
import org.ktb.week6.entity.User;
import org.ktb.week6.enums.ActionType;
import org.ktb.week6.enums.FileCategory;
import org.ktb.week6.enums.ReportReason;
import org.ktb.week6.enums.ReportStatusType;
import org.ktb.week6.enums.StatusType;
import org.ktb.week6.repository.CommentRepository;
import org.ktb.week6.repository.FileRepository;
import org.ktb.week6.repository.LikeRepository;
import org.ktb.week6.repository.PostHistoryRepository;
import org.ktb.week6.repository.PostRepository;
import org.ktb.week6.repository.PostViewLogsRepository;
import org.ktb.week6.repository.ReportRepository;
import org.ktb.week6.repository.TemporaryPostRepository;
import org.ktb.week6.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("development")
@RequiredArgsConstructor
public class MockDataInitializer implements CommandLineRunner {

    private static final String MOCK_PASSWORD = "password123!";
    private static final String MOCK_USER_EMAIL = "seed01@example.com";
    private static final String IMAGE_PATH_PREFIX = "uploads/images/";
    private static final String IMAGE_URL_PREFIX = "/public/images/";

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;
    private final TemporaryPostRepository temporaryPostRepository;
    private final PostHistoryRepository postHistoryRepository;
    private final PostViewLogsRepository postViewLogsRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByEmail(MOCK_USER_EMAIL)) {
            return;
        }

        List<File> profileImages = saveFiles(FileCategory.PROFILE_IMAGE, List.of(
                "sample_images_00.png",
                "sample_images_01.png",
                "sample_images_02.png",
                "sample_images_03.png",
                "sample_images_04.png",
                "sample_images_05.png",
                "sample_images_06.png",
                "sample_images_07.png",
                "defaultProfileImage.png",
                "sample_images_08.png",
                "sample_images_09.png",
                "sample_images_10.png"
        ));

        List<File> postImages = saveFiles(FileCategory.POST_ATTACHMENT, List.of(
                "sample_images_11.png",
                "sample_images_12.png",
                "sample_images_00.png",
                "sample_images_01.png",
                "sample_images_02.png",
                "sample_images_03.png",
                "sample_images_04.png",
                "sample_images_05.png",
                "sample_images_06.png",
                "sample_images_07.png",
                "sample_images_08.png",
                "sample_images_09.png",
                "sample_images_10.png"
        ));

        List<User> users = createUsers(profileImages);
        List<Post> posts = createPosts(users, postImages);

        createLikes(users, posts);
        createComments(users, posts);
        createReports(users, posts);
        createTemporaryPosts(users, postImages);
        createPostHistories(users, posts, postImages);
        createPostViewLogs(users, posts);
    }

    private List<File> saveFiles(FileCategory category, List<String> names) {
        return names.stream()
                .map(name -> new File(IMAGE_PATH_PREFIX + name, IMAGE_URL_PREFIX + name, category))
                .map(fileRepository::save)
                .toList();
    }

    private List<User> createUsers(List<File> profileImages) {
        List<User> users = new ArrayList<>();
        users.add(new User(MOCK_USER_EMAIL, MOCK_PASSWORD, "민지", profileImages.get(0)));
        users.add(new User("seed02@example.com", MOCK_PASSWORD, "준호", profileImages.get(1)));
        users.add(new User("seed03@example.com", MOCK_PASSWORD, "서연", profileImages.get(2)));
        users.add(new User("seed04@example.com", MOCK_PASSWORD, "도윤", profileImages.get(3)));
        users.add(new User("seed05@example.com", MOCK_PASSWORD, "하린", profileImages.get(4)));
        users.add(new User("seed06@example.com", MOCK_PASSWORD, "태오", profileImages.get(5)));
        users.add(new User("seed07@example.com", MOCK_PASSWORD, "나은", profileImages.get(6)));
        users.add(new User("seed08@example.com", MOCK_PASSWORD, "우진", profileImages.get(7)));
        users.add(new User("seed09@example.com", MOCK_PASSWORD, "소율", profileImages.get(8)));
        users.add(new User("seed10@example.com", MOCK_PASSWORD, "현우", profileImages.get(9)));

        User withdrawnSummer = new User("withdrawn01@example.com", MOCK_PASSWORD, "탈퇴한여름", profileImages.get(10));
        withdrawnSummer.deleteUser();
        users.add(withdrawnSummer);

        User withdrawnWriter = new User("withdrawn02@example.com", MOCK_PASSWORD, "떠난작성자", profileImages.get(11));
        withdrawnWriter.deleteUser();
        users.add(withdrawnWriter);

        return userRepository.saveAll(users);
    }

    private List<Post> createPosts(List<User> users, List<File> postImages) {
        List<Post> posts = new ArrayList<>();
        posts.add(savePost(users.get(0), "오늘의 작업 로그", "개발 환경을 정리하고 API 응답을 다시 확인했습니다.", postImages.get(0), 41, false, StatusType.ACTIVE));
        posts.add(savePost(users.get(1), "첫 H2 콘솔 점검", "H2 콘솔에서 테이블과 목데이터가 잘 보이는지 점검하는 글입니다.", null, 16, false, StatusType.ACTIVE));
        posts.add(savePost(users.get(2), "이미지 업로드 테스트", "첨부 이미지가 게시글 목록과 상세에서 자연스럽게 표시되는지 확인합니다.", postImages.get(1), 72, true, StatusType.ACTIVE));
        posts.add(savePost(users.get(3), "댓글 플로우 확인", "댓글과 대댓글 흐름을 테스트하기 위한 일반 게시글입니다.", null, 24, false, StatusType.ACTIVE));
        posts.add(savePost(users.get(4), "좋아요 토글 기록", "여러 유저가 좋아요를 누른 상태를 확인할 수 있습니다.", postImages.get(2), 63, false, StatusType.ACTIVE));
        posts.add(savePost(users.get(5), "긴 글 작성 연습", "본문이 조금 길어져도 레이아웃이 깨지지 않는지 확인하기 위한 목데이터입니다.", null, 9, true, StatusType.ACTIVE));
        posts.add(savePost(users.get(6), "임시저장 복구 후기", "임시저장된 글을 다시 불러온 뒤 발행한 상황을 가정했습니다.", postImages.get(3), 30, false, StatusType.ACTIVE));
        posts.add(savePost(users.get(7), "API 응답 정리", "프론트에서 목록, 상세, 댓글 응답을 확인하기 쉽게 만든 글입니다.", null, 54, false, StatusType.ACTIVE));
        posts.add(savePost(users.get(8), "프로필 이미지 변경", "작성자의 프로필 이미지가 함께 보이는지 확인합니다.", postImages.get(4), 22, false, StatusType.ACTIVE));
        posts.add(savePost(users.get(9), "커서 페이지네이션", "게시글이 10개를 넘을 때 다음 커서가 내려오는지 확인합니다.", null, 88, true, StatusType.ACTIVE));
        posts.add(savePost(users.get(0), "모바일 화면 확인", "모바일에서 제목과 본문이 잘 줄바꿈되는지 보는 게시글입니다.", postImages.get(5), 14, false, StatusType.ACTIVE));
        posts.add(savePost(users.get(1), "야간 배포 메모", "늦은 시간에 수정한 내용을 남겨 둔 일반 게시글입니다.", null, 33, false, StatusType.ACTIVE));

        posts.add(savePost(users.get(2), "광고성 글 신고 테스트", "신고가 1회 들어간 게시글입니다. 아직 블라인드 상태는 아닙니다.", postImages.get(6), 45, false, StatusType.ACTIVE));
        posts.add(savePost(users.get(3), "중복 글 신고 테스트", "중복 콘텐츠 사유로 여러 번 신고된 게시글입니다.", null, 51, false, StatusType.ACTIVE));
        posts.add(savePost(users.get(4), "공격적 표현 신고", "누적 신고가 있지만 기준치 미만이라 본문이 노출되는 게시글입니다.", postImages.get(7), 67, true, StatusType.ACTIVE));
        posts.add(savePost(users.get(5), "개인정보 노출 의심", "개인정보 노출 의심 사유로 검토가 필요한 게시글입니다.", null, 29, false, StatusType.ACTIVE));
        posts.add(savePost(users.get(6), "저작권 신고 대기", "첨부 이미지 저작권 신고가 접수된 상태를 확인합니다.", postImages.get(8), 37, false, StatusType.ACTIVE));

        posts.add(savePost(users.get(7), "블라인드 처리 예시", "신고 누적으로 숨김 처리될 게시글 원문입니다.", null, 104, false, StatusType.ACTIVE));
        posts.add(savePost(users.get(8), "누적 신고 숨김 글", "신고가 5회를 넘어 목록과 상세에서 대체 문구가 보여야 합니다.", postImages.get(9), 119, true, StatusType.ACTIVE));
        posts.add(savePost(users.get(9), "민감 콘텐츠 신고글", "민감한 내용 신고가 누적되어 블라인드 처리되는 게시글입니다.", null, 96, false, StatusType.ACTIVE));

        posts.add(savePost(users.get(10), "탈퇴 유저의 기록", "작성자는 탈퇴 상태지만 게시글은 남아 있는 케이스입니다.", postImages.get(10), 58, false, StatusType.ACTIVE));
        posts.add(savePost(users.get(11), "떠난 작성자의 질문", "탈퇴한 작성자의 글에 댓글과 신고가 남아 있는 상황입니다.", null, 43, false, StatusType.ACTIVE));
        posts.add(savePost(users.get(10), "탈퇴 계정 이미지 글", "탈퇴한 계정이 작성한 이미지 포함 게시글입니다.", postImages.get(11), 35, true, StatusType.ACTIVE));

        posts.add(savePost(users.get(0), "작성자가 삭제한 글", "삭제된 게시글은 목록과 상세에서 제외되어야 합니다.", null, 11, false, StatusType.DELETED));
        posts.add(savePost(users.get(2), "신고 후 삭제된 글", "신고가 접수된 뒤 작성자가 삭제한 게시글입니다.", postImages.get(12), 26, false, StatusType.DELETED));
        posts.add(savePost(users.get(10), "탈퇴 유저 삭제 글", "탈퇴한 사용자가 남긴 뒤 삭제된 게시글입니다.", null, 18, false, StatusType.DELETED));

        return posts;
    }

    private Post savePost(User user, String title, String content, File file, long viewCount, boolean isEdited, StatusType status) {
        Post post = new Post(title, content, user, file);
        post.setViewCount(viewCount);
        post.setIsEdited(isEdited);
        if (status != StatusType.ACTIVE) {
            post.updateStatus(status);
        }
        return postRepository.save(post);
    }

    private void createLikes(List<User> users, List<Post> posts) {
        addLikes(posts.get(0), users, 1, 2, 3, 4);
        addLikes(posts.get(2), users, 0, 1, 4, 5, 6);
        addLikes(posts.get(4), users, 0, 2, 3, 5, 7, 8);
        addLikes(posts.get(7), users, 1, 3, 5);
        addLikes(posts.get(9), users, 0, 2, 4, 6, 8, 9);
        addLikes(posts.get(12), users, 0, 1);
        addLikes(posts.get(14), users, 1, 2, 6, 7);
        addLikes(posts.get(17), users, 0, 1, 2);
        addLikes(posts.get(20), users, 0, 1, 3, 5);
        addLikes(posts.get(21), users, 2, 4);
    }

    private void addLikes(Post post, List<User> users, int... userIndexes) {
        for (int index : userIndexes) {
            likeRepository.save(new Like(post, users.get(index)));
            post.increaseLikeCount();
        }
    }

    private void createComments(List<User> users, List<Post> posts) {
        Comment first = addComment(posts.get(0), users.get(1), "정리 방식 좋네요. 저도 참고하겠습니다.", null, false, false);
        addComment(posts.get(0), users.get(0), "고마워요. 다음에는 체크리스트도 같이 올려볼게요.", first, false, false);
        addComment(posts.get(2), users.get(4), "이미지가 잘 보여요. 상세 화면도 확인했습니다.", null, false, false);
        addComment(posts.get(2), users.get(5), "수정된 게시글 표시도 같이 보면 좋겠습니다.", null, true, false);

        Comment thread = addComment(posts.get(3), users.get(2), "대댓글 정렬도 같이 확인해보면 좋겠어요.", null, false, false);
        addComment(posts.get(3), users.get(3), "부모 댓글 기준으로 묶이는지 확인했습니다.", thread, false, false);
        addComment(posts.get(4), users.get(6), "좋아요 수와 실제 좋아요 데이터가 맞는지 보겠습니다.", null, false, false);
        addComment(posts.get(9), users.get(7), "첫 페이지 이후 커서가 잘 내려오네요.", null, false, false);

        addComment(posts.get(13), users.get(8), "중복 신고 테스트용 댓글입니다.", null, false, false);
        addComment(posts.get(14), users.get(9), "표현 수위 검토가 필요해 보여요.", null, false, false);
        addComment(posts.get(17), users.get(0), "블라인드 처리 전 남겨진 댓글입니다.", null, false, false);
        addComment(posts.get(18), users.get(1), "숨김 게시글의 댓글 노출 여부 확인용입니다.", null, false, true);
        addComment(posts.get(20), users.get(2), "작성자가 탈퇴해도 댓글은 남아 있는 케이스네요.", null, false, false);
        addComment(posts.get(21), users.get(3), "탈퇴 유저 게시글 신고 흐름도 확인했습니다.", null, false, false);
        addComment(posts.get(24), users.get(4), "삭제 전 신고가 있었던 게시글의 댓글입니다.", null, false, true);
    }

    private Comment addComment(Post post, User user, String content, Comment parent, boolean edited, boolean deleted) {
        Comment comment = new Comment(content, post, user, parent);
        comment.setIsEdited(edited);
        if (deleted) {
            comment.updateStatus(StatusType.DELETED);
            comment.setDeletedAt(java.time.LocalDateTime.now());
        }
        Comment savedComment = commentRepository.save(comment);
        post.increaseCommentCount();
        return savedComment;
    }

    private void createReports(List<User> users, List<Post> posts) {
        addReports(posts.get(12), users, ReportReason.SPAM, false, 0);
        addReports(posts.get(13), users, ReportReason.DUPLICATE_CONTENT, false, 0, 1, 2);
        addReports(posts.get(14), users, ReportReason.ABUSE, false, 0, 1, 2, 3);
        addReports(posts.get(15), users, ReportReason.PERSONAL_INFO_EXPOSURE, true, 1, 3);
        addReports(posts.get(16), users, ReportReason.COPYRIGHT_INFRINGEMENT, false, 0, 2, 4);

        addReports(posts.get(17), users, ReportReason.ABUSE, true, 0, 1, 2, 3, 4);
        addReports(posts.get(18), users, ReportReason.SEXUAL_CONTENT, true, 0, 1, 2, 3, 4, 5);
        addReports(posts.get(19), users, ReportReason.VIOLENT_CONTENT, true, 0, 1, 2, 3, 4);

        addReports(posts.get(21), users, ReportReason.OFF_TOPIC, false, 0, 1);
        addReports(posts.get(24), users, ReportReason.OTHER, true, 0, 1);
    }

    private void addReports(Post post, List<User> users, ReportReason reason, boolean reviewed, int... reporterIndexes) {
        for (int index : reporterIndexes) {
            Report report = new Report(null, post, users.get(index), reason);
            if (reviewed) {
                report.setStatus(ReportStatusType.REVIEWED);
            }
            reportRepository.save(report);
            post.increaseReportCount();
        }

        if (post.getReportCount() >= 5 && post.getStatus() != StatusType.DELETED) {
            post.updateStatus(StatusType.BLIND);
        }
    }

    private void createTemporaryPosts(List<User> users, List<File> postImages) {
        temporaryPostRepository.save(new TemporaryPost("임시 저장 제목", "아직 발행하지 않은 임시 글 본문입니다.", users.get(0), null));
        temporaryPostRepository.save(new TemporaryPost("이미지 임시 글", "이미지를 첨부한 임시 저장 글입니다.", users.get(5), postImages.get(5)));
        temporaryPostRepository.save(new TemporaryPost(null, "제목 없이 본문만 작성 중인 임시 글입니다.", users.get(8), null));
    }

    private void createPostHistories(List<User> users, List<Post> posts, List<File> postImages) {
        saveHistory(posts.get(2), users.get(2), "이미지 업로드 테스트", "첨부 이미지 설명을 수정하기 전 내용입니다.", postImages.get(1), ActionType.UPDATE, 1L);
        saveHistory(posts.get(9), users.get(9), "커서 페이지네이션", "처음 작성한 페이지네이션 테스트 글입니다.", null, ActionType.UPDATE, 1L);
        saveHistory(posts.get(24), users.get(2), "신고 후 삭제된 글", "삭제되기 전 신고가 접수된 원문입니다.", postImages.get(0), ActionType.DELETE, 2L);
    }

    private void saveHistory(Post post, User user, String title, String content, File file, ActionType action, long version) {
        PostHistory history = new PostHistory(action, title, content, version, post, user, file);
        postHistoryRepository.save(history);
    }

    private void createPostViewLogs(List<User> users, List<Post> posts) {
        postViewLogsRepository.save(new PostViewLogs(posts.get(0), users.get(1)));
        postViewLogsRepository.save(new PostViewLogs(posts.get(0), users.get(2)));
        postViewLogsRepository.save(new PostViewLogs(posts.get(2), users.get(4)));
        postViewLogsRepository.save(new PostViewLogs(posts.get(9), users.get(7)));
        postViewLogsRepository.save(new PostViewLogs(posts.get(17), users.get(0)));
        postViewLogsRepository.save(new PostViewLogs(posts.get(20), users.get(3)));
    }
}
