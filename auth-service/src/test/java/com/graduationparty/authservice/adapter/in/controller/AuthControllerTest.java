package com.graduationparty.authservice.adapter.in.controller;

import static org.mockito.ArgumentMatchers.any;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graduationparty.authservice.BaseIntegrationTest;
import com.graduationparty.authservice.domain.model.AccessToken;
import com.graduationparty.authservice.domain.model.User;
import com.graduationparty.authservice.domain.model.User.RoleUser;
import com.graduationparty.authservice.domain.port.in.UserUseCase;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class AuthControllerTest extends BaseIntegrationTest {

  @Autowired private ObjectMapper objectMapper;

  @MockBean private UserUseCase userUseCase;

  @Test
  void testSignup() throws Exception {
    User user = new User(null, "test", "test@test.com", "password", RoleUser.USER, null);
    User createdUser = new User("12345", "test", "test@test.com", null, RoleUser.USER, "photo.jpg");

    MockMultipartFile profilePhoto =
        new MockMultipartFile(
            "profilePhoto", "photo.jpg", "image/jpeg", "mock image data".getBytes());

    MockMultipartFile userPart =
        new MockMultipartFile(
            "user", "user.json", "application/json", objectMapper.writeValueAsBytes(user));

    when(userUseCase.createUser(any(), any())).thenReturn(createdUser);

    perform(MockMvcRequestBuilders.multipart("/auth/signup").file(userPart).file(profilePhoto))
        .andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is("12345")))
        .andExpect(MockMvcResultMatchers.jsonPath("$.username", Matchers.is("test")))
        .andExpect(MockMvcResultMatchers.jsonPath("$.email", Matchers.is("test@test.com")))
        .andExpect(MockMvcResultMatchers.jsonPath("$.profilePhoto", Matchers.is("photo.jpg")));
  }

  @Test
  void testLogin() throws Exception {
    String username = "test";
    String password = "password";
    var token = new AccessToken("dummy-token");

    when(userUseCase.authenticate(username, password)).thenReturn(token);

    perform(
            MockMvcRequestBuilders.post("/auth/login")
                .param("username", username)
                .param("password", password))
        .andExpect(MockMvcResultMatchers.status().isOk());
  }
}
