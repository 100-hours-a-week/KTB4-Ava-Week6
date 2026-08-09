# 트러블슈팅

## 모임 신청 정원 초과 동시성 문제

### 문제 상황

모임 게시글에는 정원(`capacity`)과 신청 인원(`applicationCount`)이 있습니다. 작성자는 모임 게시글 작성 시점에 신청 인원에 포함되므로 `applicationCount`는 1명부터 시작합니다. 이후 다른 사용자가 `MEETING` 타입 게시글에 댓글을 작성하면 모임 신청으로 처리됩니다.

처음 구현에서는 여러 사용자가 동시에 신청할 때 정원을 초과해 신청이 성공할 수 있었습니다. 예를 들어 정원이 3명인 모임은 작성자를 제외하고 2명만 추가 신청할 수 있어야 하는데, 일부 요청이 모두 같은 신청 인원 값을 기준으로 정원 체크를 통과하는 문제가 생겼습니다.

### 원인

신청 가능 여부를 확인하는 로직과 신청 인원을 증가시키는 로직이 동시 요청 사이에서 안전하게 직렬화되지 않았습니다.

여러 트랜잭션이 동시에 다음 흐름을 실행하면 문제가 발생할 수 있습니다.

```text
1. 현재 신청 인원 조회
2. 정원 초과 여부 확인
3. 신청 댓글 저장
4. 신청 내역 저장
5. 신청 인원 증가
```

각 요청이 같은 시점의 `applicationCount`를 읽으면, 실제로는 정원이 찼는데도 여러 요청이 모두 성공할 수 있습니다.

### 해결

모임 신청 처리 시 `event_post` row에 비관적 락을 걸어 신청 가능 여부 확인과 신청 인원 증가를 하나의 임계 구역에서 처리했습니다.

```java
@Query(value = "select * from event_post where post_id = :postId for update", nativeQuery = true)
Optional<EventPost> findByPostIdForUpdate(@Param("postId") Long postId);
```

또한 같은 사용자가 동시에 여러 번 신청하는 경우를 막기 위해 `eventPost_id`, `user_id` 조합에 유니크 제약을 두었습니다.

```java
@Table(
    uniqueConstraints = {
        @UniqueConstraint(
            name = "EVENT_POST_USER_UNIQUE",
            columnNames = {"eventPost_id", "user_id"}
        )
    }
)
public class EventApplication {
}
```

애플리케이션 레벨의 중복 신청 체크만으로는 충분하지 않기 때문에, DB 유니크 제약을 최종 방어선으로 두고 제약 위반이 발생하면 비즈니스 예외로 변환했습니다.

```java
try {
    eventApplicationRepository.saveAndFlush(newEventApplication);
} catch (DataIntegrityViolationException e) {
    throw new BusinessException(HttpStatus.CONFLICT, "application_already_exists");
}
```

`save()`가 아니라 `saveAndFlush()`를 사용한 이유는 제약 위반을 메서드 내부에서 바로 감지하기 위해서입니다. `save()`만 호출하면 INSERT가 트랜잭션 커밋 시점까지 지연될 수 있고, 그 경우 현재 `catch` 블록에서 예외를 처리하지 못할 수 있습니다.

### 검증

Testcontainers MySQL 환경에서 실제 DB 락과 트랜잭션 격리 수준을 사용해 검증했습니다.

검증한 시나리오는 다음과 같습니다.

- 정원 3명인 모임에 10명이 동시에 신청하면 작성자를 제외한 2명만 성공한다.
- 같은 사용자가 동시에 여러 번 신청해도 한 번만 성공한다.
- 신청 요청 수가 남은 모집 인원과 정확히 같으면 전원 성공한다.
- 신청 요청 수가 남은 모집 인원보다 한 명 많으면 한 명만 실패한다.
- 성공한 신청 수와 `event_post.application_count`, `event_application` row 수, `post.comment_count`가 일치한다.

## 2. LAZY 연관관계 접근으로 비관적 락이 무력화된 문제

### 문제 상황

모임 신청 동시성 문제를 해결하기 위해 `EventPost`를 `SELECT ... FOR UPDATE`로 조회했습니다. 그런데 테스트에서는 여전히 정원 체크가 정상적으로 동작하지 않았습니다.

정원이 3명인 모임에 10명이 동시에 신청했을 때, 작성자를 제외한 2명만 성공해야 하는데 모든 요청이 성공하거나 `applicationCount`가 기대값과 다르게 저장되는 현상이 있었습니다.

### 원인

문제는 DB 락이 아니라 Hibernate의 1차 캐시였습니다.

처음에는 모임 게시글인지 확인하기 위해 `post.getEventPost()`를 먼저 호출했습니다.

```java
if (post.getEventPost() != null) {
    eventPostRepository.findByPostIdForUpdate(post.getId());
}
```

이 코드는 단순한 null 체크처럼 보이지만, 실제로는 `EventPost` LAZY 연관관계에 접근하는 코드입니다. LAZY 필드라도 접근하는 순간 DB 조회가 발생합니다.

그 결과 다음 순서로 문제가 발생했습니다.

```text
1. post.getEventPost() 호출
2. 락 없는 SELECT로 EventPost 조회
3. 조회된 EventPost가 Hibernate 1차 캐시에 저장됨
4. 이후 findByPostIdForUpdate()로 FOR UPDATE 쿼리 실행
5. Hibernate가 이미 1차 캐시에 있는 EventPost 인스턴스를 반환
6. 락 쿼리로 읽은 최신 값이 애플리케이션 로직에 반영되지 않음
```

MySQL은 row lock을 정상적으로 걸었지만, 애플리케이션에서는 락 없는 조회로 먼저 캐시된 엔티티를 사용하고 있었습니다. 이 때문에 여러 트랜잭션이 오래된 `applicationCount`를 기준으로 정원 체크를 통과했습니다.

### 해결

락을 걸기 전에 `EventPost` 엔티티를 로딩하지 않도록 수정했습니다.

모임 게시글 여부는 엔티티를 조회하지 않는 `existsByPostId()`로 확인하고, 실제 신청 처리가 필요한 경우에만 `findByPostIdForUpdate()`를 호출했습니다.

```java
Optional<EventPost> eventPost = eventPostRepository.existsByPostId(post.getId())
        ? eventPostRepository.findByPostIdForUpdate(post.getId())
        : Optional.empty();
```

`existsByPostId()`는 존재 여부만 확인하고 `EventPost` 엔티티를 영속성 컨텍스트에 올리지 않습니다. 따라서 뒤이어 실행되는 `FOR UPDATE` 쿼리를 방해하지 않습니다.

또한 `Post.eventPost` 연관관계는 명시적으로 LAZY로 설정했습니다.

```java
@OneToOne(fetch = FetchType.LAZY, mappedBy = "post")
private EventPost eventPost;
```

`mappedBy` 쪽 `@OneToOne` LAZY 로딩이 실제로 동작하도록 Hibernate 바이트코드 인핸스먼트도 함께 설정했습니다.

```gradle
plugins {
    id 'org.hibernate.orm' version '7.2.12.Final'
}

hibernate {
    enhancement {
        enableLazyInitialization = true
    }
}
```

### 검증

SQL 로그와 동시성 테스트로 확인했습니다.

- 락 없는 `EventPost` 사전 조회가 발생하지 않는지 확인
- `SELECT ... FOR UPDATE` 쿼리가 신청 요청마다 실행되는지 확인
- 정원 초과 신청 테스트에서 성공 수와 실패 수가 기대값과 일치하는지 확인
- 동일 사용자 중복 신청 테스트에서 유니크 제약 위반이 비즈니스 예외로 변환되는지 확인

## 3. 댓글 수 증가 lost update 문제

### 문제 상황

모임 신청 동시성 테스트에서 `applicationCount`와 신청 row 수는 정상인데, 게시글의 `commentCount`가 실제 성공한 댓글 수보다 작게 저장되는 문제가 발견되었습니다.

정원이 3명인 모임에서 작성자를 제외한 2개의 신청 댓글이 정상 저장되었는데도 `post.comment_count`가 1로 남는 식의 문제가 발생했습니다.

### 원인

`EventPost`에는 비관적 락이 걸려 있었지만, `Post`에는 락이 걸려 있지 않았습니다.

여러 트랜잭션이 같은 게시글을 동시에 조회하면 모두 같은 `commentCount` 값을 가진 `Post` 엔티티를 메모리에 올립니다. 이후 각 트랜잭션이 `post.increaseCommentCount()`를 호출하면 모두 자신의 메모리 값 기준으로 `0 -> 1`을 계산합니다.

결국 마지막에 커밋된 트랜잭션의 값이 저장되며 증가분이 유실됩니다.

```text
트랜잭션 A: commentCount 0 조회 -> 1로 증가
트랜잭션 B: commentCount 0 조회 -> 1로 증가
트랜잭션 C: commentCount 0 조회 -> 1로 증가

최종 결과: 3이 아니라 1
```

### 해결

엔티티의 in-memory 값을 증가시키는 방식 대신 DB 원자적 UPDATE로 변경했습니다.

```java
@Modifying
@Query("update Post p set p.commentCount = p.commentCount + 1 where p.id = :postId")
void increaseCommentCount(@Param("postId") Long postId);
```

댓글 작성 로직에서는 `post.increaseCommentCount()` 대신 Repository 메서드를 호출하도록 수정했습니다.

```java
postRepository.increaseCommentCount(post.getId());
```

이 방식은 현재 DB에 저장된 값을 기준으로 증가시키기 때문에 여러 요청이 동시에 실행되어도 증가분이 유실되지 않습니다.

### 검증

동시 신청 테스트에서 다음 값을 모두 함께 검증했습니다.

- 성공한 신청 수
- `event_post.application_count`
- `event_application` row 수
- `post.comment_count`

이전에는 `applicationCount`만 확인했기 때문에 `commentCount` 유실을 놓칠 수 있었습니다. 이후에는 동시성 시나리오에서 함께 변경되는 값을 모두 assertion에 포함했습니다.

## 정리

이번 문제들은 모두 단순히 "락을 걸었다"는 사실만으로 동시성이 보장되지 않는다는 점을 보여줍니다.

특히 다음 내용을 확인할 수 있었습니다.

1. 비관적 락은 실제로 보호해야 하는 row에 걸어야 한다.
2. 락 쿼리보다 먼저 엔티티를 로딩하면 Hibernate 1차 캐시 때문에 오래된 객체를 사용할 수 있다.
3. LAZY 필드 접근은 부작용 없는 체크가 아니라 실제 SELECT를 유발할 수 있다.
4. 애플리케이션 레벨의 중복 체크는 동시성 상황에서 충분하지 않으므로 DB 제약이 필요하다.
5. 카운터 증가는 in-memory 변경보다 DB 원자적 UPDATE가 더 안전하다.
6. 동시성 테스트에서는 성공/실패 수뿐 아니라 함께 변경되는 모든 집계 값을 검증해야 한다.
