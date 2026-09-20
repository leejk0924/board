package com.board.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.board.member.adapter.in.web.LoginRequest;
import com.board.member.adapter.in.web.LoginResponse;
import com.board.member.adapter.in.web.SignUpRequest;
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
    @DisplayName("가입한 이메일/비밀번호로 로그인하면 액세스 토큰을 발급한다")
    void login_success() throws Exception {
        String signUpBody = objectMapper.writeValueAsString(new SignUpRequest("login@example.com", "password123", "로그인유저"));
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(signUpBody))
                .andExpect(status().isCreated());

        String loginBody = objectMapper.writeValueAsString(new LoginRequest("login@example.com", "password123"));
        MvcResult result = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();

        LoginResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), LoginResponse.class);
        assertThat(response.accessToken()).isNotBlank();
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
}
