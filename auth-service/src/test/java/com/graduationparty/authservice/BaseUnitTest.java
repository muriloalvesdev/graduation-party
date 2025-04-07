package com.graduationparty.authservice;

import com.graduationparty.authservice.application.service.KeycloakUserService;
import com.graduationparty.authservice.domain.model.User;
import com.graduationparty.authservice.domain.port.out.UserRepository;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.provider.Arguments;

@Tag("unit")
public abstract class BaseUnitTest extends BaseTest {

  protected UserRepository userRepository;
  protected KeycloakUserService keycloakUserService;

  @BeforeEach
  void setup() {
    userRepository = mock(UserRepository.class);
    keycloakUserService = new KeycloakUserService(userRepository);
  }

  protected static Stream<Arguments> invalidUserCreationProvider() {
    User missingUsername =
        new User(null, null, "test@test.com", "password", User.RoleUser.USER, null);
    User missingEmail = new User(null, "test", null, "password", User.RoleUser.USER, null);
    User missingPassword = new User(null, "test", "test@test.com", null, User.RoleUser.USER, null);
    User missingRole = new User(null, "test", "test@test.com", "password", null, null);

    return Stream.of(
        Arguments.of(missingUsername, "Username é obrigatório"),
        Arguments.of(missingEmail, "Email é obrigatório"),
        Arguments.of(missingPassword, "Password é obrigatório"),
        Arguments.of(missingRole, "Role é obrigatória"));
  }

  protected static Stream<Arguments> invalidCredentialsProvider() {
    return Stream.of(
        Arguments.of("", "password", "Username deve ser informado"),
        Arguments.of("test", "", "Password deve ser informado"),
        Arguments.of(null, "password", "Username deve ser informado"),
        Arguments.of("test", null, "Password deve ser informado"));
  }

  protected static Stream<Arguments> invalidUserUpdateProvider() {
    UUID id = UUID.randomUUID();
    User missingUsername = new User(null, null, "test@test.com", null, User.RoleUser.ADMIN, null);
    User missingEmail = new User(null, "test", null, null, User.RoleUser.ADMIN, null);
    User missingRole = new User(null, "test", "test@test.com", null, null, null);

    return Stream.of(
        Arguments.of(id, missingUsername, "Username é obrigatório"),
        Arguments.of(id, missingEmail, "Email é obrigatório"),
        Arguments.of(id, missingRole, "Role é obrigatória"));
  }
}
