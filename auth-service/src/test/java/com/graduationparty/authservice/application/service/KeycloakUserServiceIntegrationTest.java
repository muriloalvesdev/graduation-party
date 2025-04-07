package com.graduationparty.authservice.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.graduationparty.authservice.BaseIntegrationTest;
import com.graduationparty.authservice.domain.model.AccessToken;
import com.graduationparty.authservice.domain.model.User;
import com.graduationparty.authservice.domain.model.User.RoleUser;
import com.graduationparty.authservice.domain.port.out.UserRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

@TestPropertySource(locations = "classpath:application-test.yml")
public class KeycloakUserServiceIntegrationTest extends BaseIntegrationTest {

  @Autowired private KeycloakUserService keycloakUserService;

  @MockBean private UserRepository userRepository;

  @Autowired private CircuitBreakerRegistry circuitBreakerRegistry;

  private CircuitBreaker circuitBreaker;
  private MultipartFile mockProfilePhoto;

  @BeforeEach
  void setup() {
    circuitBreakerRegistry.circuitBreaker("keycloak").reset();
    mockProfilePhoto =
        new MockMultipartFile("profilePhoto", "photo.jpg", "image/jpeg", "mock data".getBytes());
  }

  @Test
  void testCreateUserSuccess() {
    User user = new User(null, "test", "test@test.com", "password", RoleUser.USER, null);
    User createdUser =
        new User(
            UUID.randomUUID().toString(),
            "test",
            "test@test.com",
            null,
            RoleUser.USER,
            "photo.jpg");

    when(userRepository.create(any(User.class), eq(mockProfilePhoto))).thenReturn(createdUser);

    User result = keycloakUserService.createUser(user, mockProfilePhoto);

    assertNotNull(result.id());
    assertEquals("test", result.username());
    assertEquals("test@test.com", result.email());
    assertEquals(RoleUser.USER, result.role());
    assertEquals("photo.jpg", result.profilePhoto());
  }

  @Test
  void testCreateUserFallback() {
    User user = new User(null, "test", "test@test.com", "password", RoleUser.USER, null);
    when(userRepository.create(any(User.class), eq(mockProfilePhoto)))
        .thenThrow(new RuntimeException("Serviço indisponível, tente novamente mais tarde"));

    var exception =
        assertThrows(
            RuntimeException.class, () -> keycloakUserService.createUser(user, mockProfilePhoto));
    assertEquals("Serviço indisponível, tente novamente mais tarde", exception.getMessage());
  }

  @Test
  void testAuthenticateSuccess() {
    AccessToken token = new AccessToken("jwt-token");
    when(userRepository.authenticate("test", "password")).thenReturn(token);

    var result = keycloakUserService.authenticate("test", "password");
    assertEquals("jwt-token", result.accessToken());
  }

  @Test
  void testAuthenticateFallback() {
    when(userRepository.authenticate("test", "password"))
        .thenThrow(
            new RuntimeException(
                "Serviço de autenticação indisponível, tente novamente mais tarde"));

    var exception =
        assertThrows(
            RuntimeException.class, () -> keycloakUserService.authenticate("test", "password"));
    assertEquals(
        "Serviço de autenticação indisponível, tente novamente mais tarde", exception.getMessage());
  }

  @Test
  void testFindUserByIdSuccess() {
    UUID id = UUID.randomUUID();
    User user = new User(id.toString(), "test", "test@test.com", null, RoleUser.USER, null);
    when(userRepository.findById(id)).thenReturn(user);

    var found = keycloakUserService.findUserById(id);
    assertEquals(id.toString(), found.id());
    assertEquals("test", found.username());
    assertEquals("test@test.com", found.email());
    assertEquals(RoleUser.USER, found.role());
  }

  @Test
  void testFindUserByIdFallback() {
    UUID id = UUID.randomUUID();
    when(userRepository.findById(id))
        .thenThrow(new RuntimeException("Serviço indisponível, tente novamente mais tarde"));

    var exception =
        assertThrows(RuntimeException.class, () -> keycloakUserService.findUserById(id));
    assertEquals("Serviço indisponível, tente novamente mais tarde", exception.getMessage());
  }

  @Test
  void testFindAllUsersSuccess() {
    User u1 =
        new User(
            UUID.randomUUID().toString(), "test1", "test1@test.com", null, RoleUser.USER, null);
    User u2 =
        new User(
            UUID.randomUUID().toString(), "test2", "test2@test.com", null, RoleUser.ADMIN, null);
    when(userRepository.findAll(0, 10)).thenReturn(List.of(u1, u2));
    when(userRepository.count()).thenReturn(2L);

    var page = keycloakUserService.findAllUsers(0, 10);
    assertEquals(2, page.getContent().size());
    assertEquals("test1", page.getContent().get(0).username());
    assertEquals("test2", page.getContent().get(1).username());
  }

  @Test
  void testFindAllUsersFallback() {
    doThrow(new RuntimeException("Serviço indisponível, tente novamente mais tarde"))
        .when(userRepository)
        .findAll(0, 10);

    var exception =
        assertThrows(RuntimeException.class, () -> keycloakUserService.findAllUsers(0, 10));
    assertEquals("Serviço indisponível, tente novamente mais tarde", exception.getMessage());
  }

  @Test
  void testUpdateUserSuccess() {
    UUID id = UUID.randomUUID();
    User req = new User(null, "newUser", "new@test.com", null, RoleUser.ADMIN, null);
    User updated = new User(id.toString(), "newUser", "new@test.com", null, RoleUser.ADMIN, null);

    when(userRepository.update(id, req)).thenReturn(updated);

    var result = keycloakUserService.updateUser(id, req);
    assertEquals("newUser", result.username());
    assertEquals("new@test.com", result.email());
    assertEquals(RoleUser.ADMIN, result.role());
  }

  @Test
  void testUpdateUserFallback() {
    UUID id = UUID.randomUUID();
    User req = new User(null, "newUser", "new@test.com", null, RoleUser.ADMIN, null);
    doThrow(new RuntimeException("Serviço indisponível, tente novamente mais tarde"))
        .when(userRepository)
        .update(id, req);

    var exception =
        assertThrows(RuntimeException.class, () -> keycloakUserService.updateUser(id, req));
    assertEquals("Serviço indisponível, tente novamente mais tarde", exception.getMessage());
  }

  @Test
  void testDeleteUserSuccess() {
    UUID id = UUID.randomUUID();
    doNothing().when(userRepository).delete(id);

    keycloakUserService.deleteUser(id);
    verify(userRepository, times(1)).delete(id);
  }

  @Test
  void testDeleteUserFallback() {
    UUID id = UUID.randomUUID();
    doThrow(new RuntimeException("Serviço indisponível, tente novamente mais tarde"))
        .when(userRepository)
        .delete(id);

    var exception = assertThrows(RuntimeException.class, () -> keycloakUserService.deleteUser(id));
    assertEquals("Serviço indisponível, tente novamente mais tarde", exception.getMessage());
  }

  @ParameterizedTest
  @MethodSource("provideInvalidUsersForCreation")
  void testCreateUserFailsWhenValidationFails(User invalidUser, String expectedMessage) {
    var exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> keycloakUserService.createUser(invalidUser, mockProfilePhoto));
    assertEquals(expectedMessage, exception.getMessage());
    verify(userRepository, never()).create(any(), any());
  }

  @ParameterizedTest
  @MethodSource("provideInvalidCredentials")
  void testAuthenticateFailsWhenInvalidCredentials(
      String username, String password, String expectedMessage) {
    var exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> keycloakUserService.authenticate(username, password));
    assertEquals(expectedMessage, exception.getMessage());
  }

  @ParameterizedTest
  @MethodSource("provideInvalidUsersForUpdate")
  void testUpdateUserFailsWhenValidationFails(UUID id, User invalidUser, String expectedMessage) {
    var exception =
        assertThrows(
            IllegalArgumentException.class, () -> keycloakUserService.updateUser(id, invalidUser));
    assertEquals(expectedMessage, exception.getMessage());
  }

  private static Stream<Arguments> provideInvalidUsersForCreation() {
    return Stream.of(
        Arguments.of(
            new User(null, "", "test@test.com", "password", RoleUser.USER, null),
            "Username é obrigatório"),
        Arguments.of(
            new User(null, "test", "", "password", RoleUser.USER, null), "Email é obrigatório"),
        Arguments.of(
            new User(null, "test", "test@test.com", "", RoleUser.USER, null),
            "Password é obrigatório"),
        Arguments.of(
            new User(null, "test", "test@test.com", "password", null, null), "Role é obrigatória"),
        Arguments.of(null, "Usuário não pode ser nulo"));
  }

  private static Stream<Arguments> provideInvalidCredentials() {
    return Stream.of(
        Arguments.of("", "password", "Username é obrigatório"),
        Arguments.of("test", "", "Password é obrigatório"),
        Arguments.of(null, "password", "Username é obrigatório"),
        Arguments.of("test", null, "Password é obrigatório"));
  }

  private static Stream<Arguments> provideInvalidUsersForUpdate() {
    return Stream.of(
        Arguments.of(
            UUID.randomUUID(),
            new User(null, "", "test@test.com", null, RoleUser.USER, null),
            "Username é obrigatório"),
        Arguments.of(
            UUID.randomUUID(),
            new User(null, "test", "", null, RoleUser.USER, null),
            "Email é obrigatório"),
        Arguments.of(
            UUID.randomUUID(),
            new User(null, "test", "test@test.com", null, null, null),
            "Role é obrigatória"),
        Arguments.of(UUID.randomUUID(), null, "Usuário não pode ser nulo"));
  }
}
