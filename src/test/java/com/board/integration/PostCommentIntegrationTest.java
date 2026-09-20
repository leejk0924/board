package com.board.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.board.comment.adapter.in.web.CommentCreateRequest;
import com.board.member.adapter.in.web.LoginRequest;
import com.board.member.adapter.in.web.LoginResponse;
import com.board.member.adapter.in.web.SignUpRequest;
import com.board.post.adapter.in.web.PostCreateRequest;
import com.board.post.adapter.in.web.PostResponse;
import com.board.post.adapter.in.web.PostUpdateRequest;
import com.board.testSupport.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class PostCommentIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private String ownerToken;
    private String otherToken;

    @BeforeEach
    void setUp() throws Exception {
        ownerToken = signUpAndLogin("owner@example.com", "닉네임_owner");
        otherToken = signUpAndLogin("other@example.com", "닉네임_other");
    }

    @Test
    @DisplayName("로그인 없이 게시글을 작성하면 401을 반환한다")
    void create_withoutToken_returns401() throws Exception {
        String body = objectMapper.writeValueAsString(new PostCreateRequest("제목", "본문"));

        mockMvc.perform(post("/api/posts").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 조회하면 404를 반환한다")
    void getDetail_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/posts/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("게시글 작성, 목록 조회 시 작성자 닉네임과 댓글 수가 함께 내려온다")
    void create_and_list_withCommentCount() throws Exception {
        Long postId = createPost(ownerToken, "제목1", "본문1");
        createComment(ownerToken, postId, "댓글1", null);
        createComment(otherToken, postId, "댓글2", null);

        mockMvc.perform(get("/api/posts").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(postId))
                .andExpect(jsonPath("$.content[0].authorNickname").value("닉네임_owner"))
                .andExpect(jsonPath("$.content[0].commentCount").value(2));
    }

    @Test
    @DisplayName("제목/본문 키워드로 게시글을 검색할 수 있다")
    void search_byKeyword() throws Exception {
        createPost(ownerToken, "스프링 부트 공부", "JPA와 시큐리티");
        createPost(ownerToken, "다른 주제", "전혀 관련 없는 본문");

        mockMvc.perform(get("/api/posts").param("keyword", "스프링"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("스프링 부트 공부"));
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 게시글을 수정하면 403을 반환한다")
    void update_byNonOwner_returns403() throws Exception {
        Long postId = createPost(ownerToken, "제목", "본문");
        String body = objectMapper.writeValueAsString(new PostUpdateRequest("수정 제목", "수정 본문"));

        mockMvc.perform(patch("/api/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("작성자가 게시글을 삭제하면 딸린 댓글도 함께 삭제된다")
    void delete_cascadesComments() throws Exception {
        Long postId = createPost(ownerToken, "삭제될 글", "본문");
        createComment(ownerToken, postId, "댓글", null);

        mockMvc.perform(delete("/api/posts/{postId}", postId).header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/posts/{postId}", postId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("대댓글은 한 단계까지만 허용되며 위반 시 400을 반환한다")
    void reply_deeperThanOneLevel_returns400() throws Exception {
        Long postId = createPost(ownerToken, "제목", "본문");
        Long rootCommentId = createComment(ownerToken, postId, "루트 댓글", null);
        Long replyId = createComment(otherToken, postId, "대댓글", rootCommentId);

        String body = objectMapper.writeValueAsString(new CommentCreateRequest("대대댓글", replyId));
        mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    private String signUpAndLogin(String email, String nickname) throws Exception {
        String signUpBody = objectMapper.writeValueAsString(new SignUpRequest(email, "password123", nickname));
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(signUpBody))
                .andExpect(status().isCreated());

        String loginBody = objectMapper.writeValueAsString(new LoginRequest(email, "password123"));
        MvcResult result = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), LoginResponse.class).accessToken();
    }

    private Long createPost(String token, String title, String content) throws Exception {
        String body = objectMapper.writeValueAsString(new PostCreateRequest(title, content));
        MvcResult result = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), PostResponse.class).id();
    }

    private Long createComment(String token, Long postId, String content, Long parentId) throws Exception {
        String body = objectMapper.writeValueAsString(new CommentCreateRequest(content, parentId));
        MvcResult result = mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}
