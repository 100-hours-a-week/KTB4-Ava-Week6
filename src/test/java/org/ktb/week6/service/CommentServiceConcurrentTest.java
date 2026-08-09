package org.ktb.week6.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ktb.week6.dto.CommentRequestDto;
import org.ktb.week6.entity.EventPost;
import org.ktb.week6.entity.Post;
import org.ktb.week6.entity.User;
import org.ktb.week6.exception.BusinessException;
import org.ktb.week6.repository.EventPostRepository;
import org.ktb.week6.repository.PostRepository;
import org.ktb.week6.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
@SpringBootTest(properties = {
        "jwt.secret=test-secret-key-for-jwt-that-is-long-enough-256-bits",
        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
        "spring.jpa.hibernate.ddl-auto=create-only"
})
class CommentServiceConcurrentTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("week6_test")
            .withUsername("week6")
            .withPassword("week6");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    UserRepository userRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    EventPostRepository eventPostRepository;
    @Autowired
    CommentService commentService;
    @Autowired
    JdbcTemplate jdbcTemplate;

    static final AtomicInteger TEST_DATA_SEQUENCE = new AtomicInteger();

    int capacity;
    int requestCount;
    User author;
    Post post;
    EventPost eventPost;
    List<User> applicants = new ArrayList<>();

    @BeforeEach
    void initData() {
        applicants.clear();

        capacity = 3;
        requestCount = 10;
        int dataId = TEST_DATA_SEQUENCE.incrementAndGet();

        author = userRepository.save(new User(
                "author" + dataId + "@author.com",
                "Test1234!!",
                "author" + dataId
        ));

        for (int i = 1; i <= requestCount; i++) {
            applicants.add(userRepository.save(
                    new User("test" + dataId + "-" + i + "@test.com", "Test1234!!", "test" + dataId + "-" + i)));
        }

        post = postRepository.save(new Post("모임", "시간: 10시반, 장소: 홍대 9번 출구", author, null));
        eventPost = eventPostRepository.saveAndFlush(new EventPost(post, capacity, LocalDateTime.now().plusHours(2)));

    }

    @Test
    void 동시에_10명이_모임을_신청해_정원이_초과된다() throws Exception {

        // Given
        CommentRequestDto request = new CommentRequestDto("참여합니다.", null);

        ExecutorService executorService = Executors.newFixedThreadPool(requestCount);

        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(requestCount);

        List<Future<?>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger fullCount = new AtomicInteger();

        try {
            // when
            for (int i = 0; i < requestCount; i++) {

                int index = i;
                futures.add(
                        executorService.submit(() -> {
                            ready.countDown();

                            try {
                                start.await();

                                commentService.createComment(request, post.getId(), applicants.get(index).getId());
                                successCount.incrementAndGet();
                            } catch (BusinessException e) {
                                if ("application_count_full".equals(e.getMessage())) {
                                    fullCount.incrementAndGet();
                                } else {
                                    throw e;
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new RuntimeException(e);
                            } finally {
                                done.countDown();
                            }
                        })
                );
            }

            ready.await();

            start.countDown();

            done.await();

            for (Future<?> future : futures) {
                future.get();
            }

            // then
            Integer applicationCount = jdbcTemplate.queryForObject(
                    "select application_count from event_post where id = ?",
                    Integer.class,
                    eventPost.getId()
            );
            Long applicationRows = jdbcTemplate.queryForObject(
                    "select count(*) from event_application where event_post_id = ?",
                    Long.class,
                    eventPost.getId()
            );
            // 작성자는 application_count에만 포함되고 신청 댓글은 만들지 않음
            // post.comment_count는 실제 성공한 신청 댓글 수만큼만 증가
            Long commentCount = jdbcTemplate.queryForObject(
                    "select comment_count from post where id = ?",
                    Long.class,
                    post.getId()
            );
            int availableApplicantCount = capacity - 1;
            assertThat(applicationCount).isEqualTo(capacity);
            assertThat(applicationRows).isEqualTo((long) availableApplicantCount);
            assertThat(successCount.get()).isEqualTo(availableApplicantCount);
            assertThat(fullCount.get()).isEqualTo(requestCount - availableApplicantCount);
            assertThat(commentCount).isEqualTo((long) availableApplicantCount);
        } finally {
            executorService.shutdown();
        }
    }

    @Test
    void 같은_사용자가_동시에_여러_번_신청해도_한_번만_성공한다() throws Exception {
        // Given
        User applicant = applicants.getFirst();
        int attemptCount = 10;
        List<User> requesters = Collections.nCopies(attemptCount, applicant);

        // When
        ConcurrentApplyResult result = applyConcurrently(requesters);

        // Then
        Long applicationRows = jdbcTemplate.queryForObject(
                "select count(*) from event_application where event_post_id = ? and user_id = ?",
                Long.class,
                eventPost.getId(), applicant.getId()
        );

        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.alreadyAppliedCount()).isEqualTo(attemptCount - 1);
        assertThat(result.fullCount()).isEqualTo(0);
        assertThat(applicationRows).isEqualTo(1L);
    }

    @Test
    void 신청_요청_수가_남은_모집_인원과_정확히_같으면_전원_성공한다() throws Exception {
        // Given
        int availableApplicantCount = capacity - 1;
        List<User> requesters = applicants.subList(0, availableApplicantCount);

        // When
        ConcurrentApplyResult result = applyConcurrently(requesters);

        // Then
        Integer applicationCount = jdbcTemplate.queryForObject(
                "select application_count from event_post where id = ?",
                Integer.class,
                eventPost.getId()
        );

        assertThat(result.successCount()).isEqualTo(availableApplicantCount);
        assertThat(result.fullCount()).isEqualTo(0);
        assertThat(result.alreadyAppliedCount()).isEqualTo(0);
        assertThat(applicationCount).isEqualTo(capacity);
    }

    @Test
    void 신청_요청_수가_남은_모집_인원보다_한_명_많으면_한_명만_실패한다() throws Exception {
        // Given
        int availableApplicantCount = capacity - 1;
        List<User> requesters = applicants.subList(0, availableApplicantCount + 1);

        // When
        ConcurrentApplyResult result = applyConcurrently(requesters);

        // Then
        Integer applicationCount = jdbcTemplate.queryForObject(
                "select application_count from event_post where id = ?",
                Integer.class,
                eventPost.getId()
        );

        assertThat(result.successCount()).isEqualTo(availableApplicantCount);
        assertThat(result.fullCount()).isEqualTo(1);
        assertThat(result.alreadyAppliedCount()).isEqualTo(0);
        assertThat(applicationCount).isEqualTo(capacity);
    }

    // 별도 스레드에서 동시에 신청하고 결과 집계하는 메서드
    private ConcurrentApplyResult applyConcurrently(List<User> requesters) throws Exception {
        CommentRequestDto request = new CommentRequestDto("참여합니다.", null);
        int n = requesters.size();

        ExecutorService executorService = Executors.newFixedThreadPool(n);

        CountDownLatch ready = new CountDownLatch(n);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(n);

        List<Future<?>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger fullCount = new AtomicInteger();
        AtomicInteger alreadyAppliedCount = new AtomicInteger();

        try {
            for (User requester : requesters) {
                futures.add(
                        executorService.submit(() -> {
                            ready.countDown();

                            try {
                                start.await();

                                commentService.createComment(request, post.getId(), requester.getId());
                                successCount.incrementAndGet();
                            } catch (BusinessException e) {
                                switch (e.getMessage()) {
                                    case "application_count_full" -> fullCount.incrementAndGet();
                                    case "application_already_exists" -> alreadyAppliedCount.incrementAndGet();
                                    default -> throw e;
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new RuntimeException(e);
                            } finally {
                                done.countDown();
                            }
                        })
                );
            }

            ready.await();
            start.countDown();
            done.await();

            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            executorService.shutdown();
        }

        return new ConcurrentApplyResult(successCount.get(), fullCount.get(), alreadyAppliedCount.get());
    }

    private record ConcurrentApplyResult(int successCount, int fullCount, int alreadyAppliedCount) {
    }
}
