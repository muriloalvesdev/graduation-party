package com.graduationparty.authservice.adapter.in.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graduationparty.authservice.BaseIntegrationTest;
import com.graduationparty.authservice.adapter.out.response.Page;
import com.graduationparty.authservice.adapter.out.response.UserDTO;
import com.graduationparty.authservice.domain.model.User;
import com.graduationparty.authservice.domain.model.User.RoleUser;
import com.graduationparty.authservice.domain.port.in.UserUseCase;
import java.util.List;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WithMockUser(
    username = "testUser",
    roles = {"ADMIN"})
public class UserControllerTest extends BaseIntegrationTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @MockBean private UserUseCase userUseCase;

  @Test
  public void testGetAllUsers() throws Exception {
    var user1 = new UserDTO("1", "user1", "user1@test.com", RoleUser.USER, null);
    var user2 = new UserDTO("2", "user2", "user2@test.com", RoleUser.ADMIN, null);

    when(userUseCase.findAllUsers(0, 10)).thenReturn(new Page<>(List.of(user1, user2), 0, 10, 2));

    perform(MockMvcRequestBuilders.get("/users"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(2)))
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id", Matchers.is("1")))
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].username", Matchers.is("user1")))
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].id", Matchers.is("2")))
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].username", Matchers.is("user2")));
  }

  @Test
  public void testGetAllUsersWhenServiceFails() throws Exception {
    when(userUseCase.findAllUsers(0, 10))
        .thenThrow(new RuntimeException("Serviço indisponível, tente novamente mais tarde"));

    perform(MockMvcRequestBuilders.get("/users"))
        .andExpect(MockMvcResultMatchers.status().is5xxServerError())
        .andExpect(
            MockMvcResultMatchers.content()
                .string(Matchers.containsString("tente novamente mais tarde")));
  }

  @Test
  public void testGetUserById() throws Exception {
    String userId = UUID.randomUUID().toString();
    var user = new UserDTO(userId, "user", "user@test.com", RoleUser.USER, null);

    when(userUseCase.findUserById(any(UUID.class))).thenReturn(user);

    perform(MockMvcRequestBuilders.get("/users/{id}", userId))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(userId)))
        .andExpect(MockMvcResultMatchers.jsonPath("$.username", Matchers.is("user")))
        .andExpect(MockMvcResultMatchers.jsonPath("$.email", Matchers.is("user@test.com")));
  }

  @Test
  public void testGetUserByIdWhenServiceFails() throws Exception {
    String userId = UUID.randomUUID().toString();
    when(userUseCase.findUserById(any(UUID.class)))
        .thenThrow(new RuntimeException("Serviço indisponível, tente novamente mais tarde"));

    perform(MockMvcRequestBuilders.get("/users/{id}", userId))
        .andExpect(MockMvcResultMatchers.status().is5xxServerError())
        .andExpect(
            MockMvcResultMatchers.content()
                .string(Matchers.containsString("tente novamente mais tarde")));
  }

  @Test
  public void testUpdateUser() throws Exception {
    String userId = UUID.randomUUID().toString();
    User inputUser = new User(null, "updatedUser", "updated@test.com", null, RoleUser.ADMIN, null);
    User updatedUser =
        new User(userId, "updatedUser", "updated@test.com", null, RoleUser.ADMIN, null);

    when(userUseCase.updateUser(any(UUID.class), any(User.class))).thenReturn(updatedUser);

    perform(
            MockMvcRequestBuilders.put("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(OBJECT_MAPPER.writeValueAsString(inputUser)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(userId)))
        .andExpect(MockMvcResultMatchers.jsonPath("$.username", Matchers.is("updatedUser")))
        .andExpect(MockMvcResultMatchers.jsonPath("$.email", Matchers.is("updated@test.com")));
  }

  @Test
  public void testUpdateUserWhenServiceFails() throws Exception {
    String userId = UUID.randomUUID().toString();
    User inputUser = new User(null, "updatedUser", "updated@test.com", null, RoleUser.ADMIN, null);

    when(userUseCase.updateUser(any(UUID.class), any(User.class)))
        .thenThrow(new RuntimeException("Serviço indisponível, tente novamente mais tarde"));

    perform(
            MockMvcRequestBuilders.put("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(OBJECT_MAPPER.writeValueAsString(inputUser)))
        .andExpect(MockMvcResultMatchers.status().is5xxServerError())
        .andExpect(
            MockMvcResultMatchers.content()
                .string(Matchers.containsString("tente novamente mais tarde")));
  }

  @Test
  public void testDeleteUser() throws Exception {
    String userId = UUID.randomUUID().toString();
    doNothing().when(userUseCase).deleteUser(any(UUID.class));

    perform(MockMvcRequestBuilders.delete("/users/{id}", userId))
        .andExpect(MockMvcResultMatchers.status().isNoContent());
  }

  @Test
  public void testDeleteUserWhenServiceFails() throws Exception {
    String userId = UUID.randomUUID().toString();
    doThrow(new RuntimeException("Serviço indisponível, tente novamente mais tarde"))
        .when(userUseCase)
        .deleteUser(any(UUID.class));

    perform(MockMvcRequestBuilders.delete("/users/{id}", userId))
        .andExpect(MockMvcResultMatchers.status().is5xxServerError())
        .andExpect(
            MockMvcResultMatchers.content()
                .string(Matchers.containsString("tente novamente mais tarde")));
  }
}
