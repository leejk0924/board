package com.board.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.board.auth.adapter.in.web.dto.LoginRequest;
import com.board.auth.adapter.in.web.dto.LoginResponse;
import com.board.auth.adapter.in.web.dto.ReissueRequest;
import com.board.member.adapter.in.web.dto.SignUpRequest;
import com.board.testSupport.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class AuthIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @DisplayName("회원가입에 성공하면 201과 비밀번호가 제외된 회원 정보를 반환한다")
    void signUp_success() throws Exception {
        String body = objectMapper.writeValueAsString(new SignUpRequest("user1@example.com", "password123", "닉네임1"));

        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("user1@example.com"))
                .andExpect(jsonPath("$.nickname").value("닉네임1"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 400을 반환한다")
    void signUp_invalidEmail_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(new SignUpRequest("invalid-email", "password123", "닉네임"));

        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/auth/signup"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("비밀번호가 8자 미만이면 400을 반환한다")
    void signUp_shortPassword_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(new SignUpRequest("user2@example.com", "1234", "닉네임"));

        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이미 가입된 이메일이면 400을 반환한다")
    void signUp_duplicateEmail_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(new SignUpRequest("dup@example.com", "password123", "닉네임"));
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("가입한 이메일/비밀번호로 로그인하면 액세스 토큰과 리프레시 토큰을 발급한다")
    void login_success() throws Exception {
        String signUpBody = objectMapper.writeValueAsString(new SignUpRequest("login@example.com", "password123", "로그인유저"));
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(signUpBody))
                .andExpect(status().isCreated());

        String loginBody = objectMapper.writeValueAsString(new LoginRequest("login@example.com", "password123"));
        MvcResult result = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();

        LoginResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), LoginResponse.class);
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
    }

    @Test
    @DisplayName("비밀번호가 틀리면 401을 반환한다")
    void login_wrongPassword_returns401() throws Exception {
        String signUpBody = objectMapper.writeValueAsString(new SignUpRequest("wrongpw@example.com", "password123", "유저"));
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(signUpBody))
                .andExpect(status().isCreated());

        String loginBody = objectMapper.writeValueAsString(new LoginRequest("wrongpw@example.com", "wrong-password"));
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효한 리프레시 토큰으로 재요청하면 새 액세스/리프레시 토큰을 발급한다(로테이션)")
    void reissue_success() throws Exception {
        String signUpBody = objectMapper.writeValueAsString(new SignUpRequest("reissue@example.com", "password123", "재발급유저"));
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(signUpBody))
                .andExpect(status().isCreated());

        String loginBody = objectMapper.writeValueAsString(new LoginRequest("reissue@example.com", "password123"));
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isOk())
                .andReturn();
        LoginResponse loginResponse = objectMapper.readValue(loginResult.getResponse().getContentAsString(), LoginResponse.class);

        String reissueBody = objectMapper.writeValueAsString(new ReissueRequest(loginResponse.refreshToken()));
        MvcResult reissueResult = mockMvc.perform(post("/api/auth/reissue").contentType(MediaType.APPLICATION_JSON).content(reissueBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();
        LoginResponse reissueResponse = objectMapper.readValue(reissueResult.getResponse().getContentAsString(), LoginResponse.class);

        // 리프레시 토큰은 매번 새로 발급되는 랜덤 값이라 로그인 때와 달라야 한다(로테이션).
        // 액세스 토큰은 클레임(iat/exp)이 초 단위라 같은 초 안에 재발급되면 우연히 같을 수 있어 비교하지 않는다.
        assertThat(reissueResponse.refreshToken()).isNotEqualTo(loginResponse.refreshToken());

        // 로테이션되었으므로 예전 리프레시 토큰은 더 이상 사용할 수 없다
        String staleReissueBody = objectMapper.writeValueAsString(new ReissueRequest(loginResponse.refreshToken()));
        mockMvc.perform(post("/api/auth/reissue").contentType(MediaType.APPLICATION_JSON).content(staleReissueBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("존재하지 않는 리프레시 토큰으로 재발급을 요청하면 401을 반환한다")
    void reissue_invalidToken_returns401() throws Exception {
        String body = objectMapper.writeValueAsString(new ReissueRequest("no-such-refresh-token"));

        mockMvc.perform(post("/api/auth/reissue").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
