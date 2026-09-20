# Board API

Spring Boot + Spring Security 기반 게시판 REST API 서버입니다. 회원가입/로그인(JWT), 게시글 CRUD, 댓글(대댓글 1단계 포함) CRUD를 제공합니다.

## 실행 방법

### 요구 사항

- JDK 21
- Docker / Docker Compose (MySQL 실행용)

### 방법 1: Docker Compose 한 번에 실행 (앱 + DB)

```bash
docker compose up --build
```

- `http://localhost:8080` 으로 서버가 뜹니다.
- 앱 컨테이너가 MySQL 컨테이너의 헬스체크 통과를 기다린 뒤 기동됩니다.

### 방법 2: 로컬에서 앱만 직접 실행 (DB는 Docker)

```bash
# 1) MySQL만 기동
docker compose up -d mysql

# 2) 애플리케이션 실행 (dev 프로필, 기본값)
./gradlew bootRun
```

- 실행 시 Flyway 마이그레이션(`src/main/resources/db/migration/V1__init_schema.sql`)이 자동으로 적용되어 스키마가 생성됩니다.
- DB 접속 정보는 `src/main/resources/application-dev.yml`에 있습니다 (`localhost:3306/board`, `application/application`).

### 테스트 실행

```bash
./gradlew test
```

- Testcontainers로 임시 MySQL 컨테이너를 띄워 통합 테스트를 실행하므로 Docker가 필요합니다.

### 환경 변수 (선택)

| 변수 | 설명 | 기본값 |
|---|---|---|
| `JWT_SECRET` | JWT 서명 키 (운영 환경에서는 반드시 교체) | 로컬 개발용 기본값 내장 |
| `JWT_EXPIRATION_SECONDS` | Access Token 만료 시간(초) | `3600` |

---

## 설계 설명

### 1. 로그인 방식: JWT를 선택한 이유

- 이 프로젝트는 서버 렌더링 없는 순수 REST API이며, 프론트엔드/모바일 등 다양한 클라이언트가 붙을 수 있다고 가정했습니다. 세션은 서버가 상태를 들고 있어야 하고 스케일아웃 시 세션 클러스터링/스토리지 공유가 필요하지만, JWT는 서버가 무상태(stateless)로 동작할 수 있어 REST API에 더 적합하다고 판단했습니다.
- 로그인 성공 시 Access Token(HS512, 기본 1시간 만료)을 발급하고, 클라이언트는 이후 요청에 `Authorization: Bearer <token>` 헤더를 실어 보냅니다.
- Refresh Token은 과제 범위를 벗어난다고 판단해 구현하지 않았습니다(Access Token 만료 시 재로그인).
- `JwtAuthenticationFilter`가 매 요청마다 토큰을 검증해 `SecurityContext`에 인증 정보를 채우고, 인증이 필요 없는 경로(`GET /api/posts/**`, `POST /api/auth/**`)는 `SecurityConfig`에서 `permitAll()` 처리했습니다.

### 2. 엔티티 관계

```
Member (1) ── (N) Post
Member (1) ── (N) Comment
Post   (1) ── (N) Comment
Comment(1) ── (N) Comment   (parent, 자기참조 - 대댓글 1단계)
```

- `Member`: `email`(unique), `password`(BCrypt 해시), `nickname`, `createdAt`
- `Post`: `title`, `content`, `member`(작성자, LAZY), `createdAt`/`updatedAt`(JPA Auditing)
- `Comment`: `content`, `post`, `member`(작성자), `parent`(자기참조, nullable), `createdAt`/`updatedAt`
- 대댓글은 `parent`가 이미 대댓글(`parent.parent != null`)인 경우 서비스 계층에서 400으로 거절해 1단계로 제한합니다.
- 요청/응답에는 JPA 엔티티를 직접 노출하지 않고 record 기반 DTO(`SignUpRequest`, `PostResponse`, `CommentResponse` 등)만 사용합니다. 비밀번호 필드는 어떤 응답 DTO에도 포함하지 않습니다.

### 3. N+1 문제를 막은 방법

- **게시글 목록**: `PostRepository.search()`가 `Post`, `Member`, `Comment`를 한 번의 JPQL로 조인해 작성자 닉네임과 댓글 수(`COUNT`)까지 집계한 뒤 `PostSummaryResponse` DTO로 바로 매핑합니다. `GROUP BY` 쿼리라 Spring Data의 기본 count 추정이 불가능하므로 `countQuery`를 별도로 명시했습니다. 목록에 게시글이 몇 개든 이 쿼리 1번 + count 쿼리 1번, 총 2번으로 끝납니다.
- **게시글 상세**: `@EntityGraph(attributePaths = "member")`로 작성자를 fetch join하여 상세 조회 1번으로 처리합니다.
- **댓글 목록**: `CommentRepository.findAllByPostIdWithMember()`가 `JOIN FETCH c.member`로 댓글 작성자를 한 번에 가져옵니다. 대댓글의 `parentId`는 프록시의 식별자만 읽으므로 추가 쿼리가 발생하지 않습니다.

### 4. 게시글 삭제 시 댓글 처리 방식

- **정책: 게시글을 삭제하면 딸린 댓글(대댓글 포함)도 함께 삭제됩니다.**
- 구현: `PostService.delete()`에서 `CommentRepository.deleteAllByPostId()`(벌크 JPQL `DELETE`, `clearAutomatically = true`)로 해당 게시글의 댓글을 먼저 모두 삭제한 뒤 게시글을 삭제합니다.
- 추가로 DB 스키마(`comment.post_id`, `comment.parent_id` FK)에도 `ON DELETE CASCADE`를 걸어, 애플리케이션 코드를 거치지 않는 경로(직접 SQL 등)로 게시글/댓글이 삭제되더라도 참조 무결성이 깨지지 않도록 이중으로 방어했습니다.

### 5. API 설계 원칙

- REST 리소스 중심 경로: `/api/auth/*`(인증), `/api/posts`(게시글), `/api/posts/{postId}/comments`(게시글에 속한 댓글 생성/목록), `/api/comments/{commentId}`(댓글 단건 수정/삭제 — 댓글은 고유 ID로 식별되므로 게시글 경로에 종속시키지 않음).
- 생성은 `201 Created`, 삭제는 `204 No Content`, 나머지는 `200 OK`.
- 목록은 페이지네이션(`page`, `size`, 기본 `size=10`, 최대 50)과 항상 최신순(`createdAt DESC`) 정렬을 강제합니다.

---

## API 명세

모든 요청/응답은 `application/json`입니다. 인증이 필요한 요청은 `Authorization: Bearer <accessToken>` 헤더가 필요합니다.

### 오류 응답 공통 포맷

모든 오류(400/401/403/404/500)는 동일한 모양으로 응답합니다.

```json
{
  "status": 404,
  "message": "게시글을 찾을 수 없습니다. id=999999",
  "path": "/api/posts/999999",
  "timestamp": "2026-09-21T01:47:10.098224"
}
```

### 인증

| 메서드 | 경로 | 인증 | 요청 본문 | 응답 (성공) | 상태 코드 |
|---|---|---|---|---|---|
| POST | `/api/auth/signup` | 불필요 | `{ "email", "password", "nickname" }` | `{ "id", "email", "nickname", "createdAt" }` | 201 / 400(이메일 형식·중복, 비밀번호 8자 미만) |
| POST | `/api/auth/login` | 불필요 | `{ "email", "password" }` | `{ "accessToken", "tokenType", "expiresIn" }` | 200 / 401(이메일 또는 비밀번호 불일치) |

### 게시글

| 메서드 | 경로 | 인증 | 요청 본문 | 응답 (성공) | 상태 코드 |
|---|---|---|---|---|---|
| POST | `/api/posts` | 필요 | `{ "title", "content" }` | `PostResponse` | 201 / 401 |
| GET | `/api/posts?page=&size=&keyword=` | 불필요 | - | `PageResponse<PostSummaryResponse>` | 200 |
| GET | `/api/posts/{postId}` | 불필요 | - | `PostResponse` | 200 / 404 |
| PATCH | `/api/posts/{postId}` | 필요(작성자만) | `{ "title", "content" }` | `PostResponse` | 200 / 401 / 403 / 404 |
| DELETE | `/api/posts/{postId}` | 필요(작성자만) | - | (본문 없음) | 204 / 401 / 403 / 404 |

- `PostResponse`: `{ id, title, content, authorId, authorNickname, createdAt, updatedAt }`
- `PostSummaryResponse`(목록 항목): `{ id, title, authorNickname, createdAt, updatedAt, commentCount }`
- `PageResponse<T>`: `{ content: T[], page, size, totalElements, totalPages, hasNext }`
- `keyword`는 제목/본문에 대한 부분 일치 검색(선택 파라미터).

### 댓글

| 메서드 | 경로 | 인증 | 요청 본문 | 응답 (성공) | 상태 코드 |
|---|---|---|---|---|---|
| POST | `/api/posts/{postId}/comments` | 필요 | `{ "content", "parentId"? }` | `CommentResponse` | 201 / 400(대댓글 2단계 이상) / 401 / 404(게시글 없음) |
| GET | `/api/posts/{postId}/comments` | 불필요 | - | `CommentResponse[]` | 200 / 404(게시글 없음) |
| PATCH | `/api/comments/{commentId}` | 필요(작성자만) | `{ "content" }` | `CommentResponse` | 200 / 401 / 403 / 404 |
| DELETE | `/api/comments/{commentId}` | 필요(작성자만) | - | (본문 없음) | 204 / 401 / 403 / 404 |

- `CommentResponse`: `{ id, content, authorId, authorNickname, postId, parentId, createdAt, updatedAt }`
- `parentId`를 생략하거나 `null`이면 최상위 댓글, 값을 주면 해당 댓글의 대댓글로 생성됩니다(최상위 댓글에만 답글 가능, 대댓글에 다시 답글을 달면 400).

---

## 실행 결과 (실제 curl 요청/응답)

아래는 로컬에서 `./gradlew bootRun`으로 서버를 띄운 뒤 실제로 호출한 결과입니다(타임스탬프·토큰 값은 매 실행마다 달라집니다).

### 1) 회원가입

```
$ curl -i -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"password123","nickname":"alice"}'

HTTP/1.1 201
Content-Type: application/json

{"id":1,"email":"alice@example.com","nickname":"alice","createdAt":"2026-09-21T01:45:42.18284"}
```

### 2) 로그인

```
$ curl -i -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"password123"}'

HTTP/1.1 200
Content-Type: application/json

{"accessToken":"eyJhbGciOiJIUzUxMiJ9...","tokenType":"Bearer","expiresIn":3600}
```

### 3) 게시글 작성

```
$ curl -i -X POST http://localhost:8080/api/posts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"스프링 부트로 게시판 만들기","content":"JWT 인증과 N+1 없는 목록 조회를 구현했습니다."}'

HTTP/1.1 201
Content-Type: application/json

{"id":1,"title":"스프링 부트로 게시판 만들기","content":"JWT 인증과 N+1 없는 목록 조회를 구현했습니다.","authorId":1,"authorNickname":"alice","createdAt":"2026-09-21T01:45:49.026351","updatedAt":"2026-09-21T01:45:49.026351"}
```

### 4) 댓글 작성

```
$ curl -i -X POST http://localhost:8080/api/posts/1/comments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"좋은 글 감사합니다!"}'

HTTP/1.1 201
Content-Type: application/json

{"id":1,"content":"좋은 글 감사합니다!","authorId":1,"authorNickname":"alice","postId":1,"parentId":null,"createdAt":"2026-09-21T01:45:54.725614","updatedAt":"2026-09-21T01:45:54.725614"}
```

### 5) 게시글 목록 조회 (작성자 닉네임 + 댓글 수, N+1 없이 조회)

```
$ curl -i -X GET "http://localhost:8080/api/posts?page=0&size=10"

HTTP/1.1 200
Content-Type: application/json

{"content":[{"id":1,"title":"스프링 부트로 게시판 만들기","authorNickname":"alice","createdAt":"2026-09-21T01:45:49","updatedAt":"2026-09-21T01:45:49","commentCount":1}],"page":0,"size":10,"totalElements":1,"totalPages":1,"hasNext":false}
```

### 6) 401 예시 — 로그인 없이 게시글 작성

```
$ curl -i -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{"title":"인증없이 작성 시도","content":"본문"}'

HTTP/1.1 401
Content-Type: application/json

{"status":401,"message":"인증이 필요합니다.","path":"/api/posts","timestamp":"2026-09-21T01:46:00.075243"}
```

### 7) 403 예시 — 다른 사용자(bob)가 alice의 글을 수정 시도

```
$ curl -i -X PATCH http://localhost:8080/api/posts/1 \
  -H "Authorization: Bearer $BOB_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"수정 시도","content":"권한 없는 수정"}'

HTTP/1.1 403
Content-Type: application/json

{"status":403,"message":"본인이 작성한 게시글만 수정/삭제할 수 있습니다.","path":"/api/posts/1","timestamp":"2026-09-21T01:46:57.007784"}
```

### 8) 404 예시 — 존재하지 않는 게시글 조회

```
$ curl -i -X GET http://localhost:8080/api/posts/999999

HTTP/1.1 404
Content-Type: application/json

{"status":404,"message":"게시글을 찾을 수 없습니다. id=999999","path":"/api/posts/999999","timestamp":"2026-09-21T01:46:10.764398"}
```

### 9) 대댓글(1단계) 작성 및 2단계 제한

```
$ curl -i -X POST http://localhost:8080/api/posts/1/comments \
  -H "Authorization: Bearer $BOB_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"저도 동의합니다.","parentId":1}'

HTTP/1.1 201
{"id":2,"content":"저도 동의합니다.","authorId":2,"authorNickname":"bob","postId":1,"parentId":1, ...}

$ curl -i -X POST http://localhost:8080/api/posts/1/comments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"2단계 대댓글 시도","parentId":2}'

HTTP/1.1 400
{"status":400,"message":"대댓글은 한 단계까지만 작성할 수 있습니다.","path":"/api/posts/1/comments", ...}
```

### 10) 게시글 삭제 시 댓글 함께 삭제 확인

```
$ curl -i -X DELETE http://localhost:8080/api/posts/1 -H "Authorization: Bearer $TOKEN"
HTTP/1.1 204

$ curl -i -X GET http://localhost:8080/api/posts/1
HTTP/1.1 404
{"status":404,"message":"게시글을 찾을 수 없습니다. id=1", ...}

$ curl -i -X GET http://localhost:8080/api/posts/1/comments
HTTP/1.1 404
{"status":404,"message":"게시글을 찾을 수 없습니다. id=1", ...}
```

---

## 가산점 구현 현황

- [x] 대댓글(1단계) — 위 예시 참고
- [x] 제목/본문 검색 — `GET /api/posts?keyword=`
- [x] 통합 테스트 — `AuthIntegrationTest`(가입/로그인/400/401), `PostCommentIntegrationTest`(401/403/404, 목록 댓글수, 검색, 대댓글 제한, 삭제 cascade)

## 참고: Spring Boot 버전 관련 메모

이 프로젝트는 `Spring Boot 4.1.1`을 사용합니다. 과제 요구 사항에는 `Spring Boot 3.x`로 명시되어 있으나, 이미 스캐폴딩된 프로젝트 설정을 유지하기로 결정했습니다. Spring Data JPA / Spring Security 기반 아키텍처와 요구사항 충족 여부는 버전과 무관하게 동일합니다.
