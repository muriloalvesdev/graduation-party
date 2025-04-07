package com.graduationparty.authservice.adapter.out.keycloak;

import com.graduationparty.authservice.application.port.out.FileStoragePort;
import com.graduationparty.authservice.domain.model.AccessToken;
import com.graduationparty.authservice.domain.model.User;
import com.graduationparty.authservice.domain.model.User.RoleUser;
import com.graduationparty.authservice.domain.port.out.AbstractUserRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * Implementação do repositório de usuários utilizando o Keycloak para autenticação e fonte de dados
 */
public class KeycloakUserRepository extends AbstractUserRepository {

  private static final Logger LOG = LoggerFactory.getLogger(KeycloakUserRepository.class);

  private final Keycloak keycloak;
  private final RestTemplate restTemplate;
  private final String realm;
  private final String serverUrl;
  private final String clientId;
  private final String clientSecret;

  /**
   * Construtor da classe KeycloakUserRepository.
   *
   * @param keycloak Instância do cliente Keycloak para interação com o servidor.
   * @param realm Nome do realm no Keycloak onde os usuários serão gerenciados.
   * @param serverUrl URL base do servidor Keycloak.
   * @param clientId Identificador do cliente para autenticação no Keycloak.
   * @param clientSecret Segredo do cliente para autenticação no Keycloak.
   * @param storage Porta de armazenamento de arquivos para upload de fotos de perfil.
   */
  public KeycloakUserRepository(
      Keycloak keycloak,
      String realm,
      String serverUrl,
      String clientId,
      String clientSecret,
      FileStoragePort storage) {
    super(storage);
    this.keycloak = keycloak;
    this.realm = realm;
    this.serverUrl = serverUrl;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.restTemplate = new RestTemplate();
  }

  /**
   * Cria um novo usuário no Keycloak com uma foto de perfil associada.
   *
   * @param user O objeto User a ser criado.
   * @param profilePhoto O arquivo de foto de perfil a ser associado ao usuário.
   * @return O objeto User criado, incluindo o ID gerado e a URL da foto de perfil.
   */
  @Override
  public User create(User user, MultipartFile profilePhoto) {
    try {
      validateUserForCreation(user);
      String urlProfilePhoto = uploadProfilePhoto(user.username(), profilePhoto);

      UserRepresentation kcUser = toUserRepresentation(user, urlProfilePhoto);

      LOG.info("Criando usuário {} com realmRoles: {}", user.username(), kcUser.getRealmRoles());
      Response response = keycloak.realm(realm).users().create(kcUser);
      if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
        LOG.error("Erro ao criar usuário: {}", response.getStatusInfo());
        throw new RuntimeException(
            "Erro ao criar usuário no Keycloak: " + response.getStatusInfo());
      }
      String location = response.getHeaderString("Location");
      String id = location.substring(location.lastIndexOf('/') + 1);
      LOG.info("Usuário criado com ID: {}", id);
      RoleMappingResource roleMappingResource = keycloak.realm(realm).users().get(id).roles();
      RoleRepresentation roleRepresentation =
          keycloak.realm(realm).roles().get(user.role().name()).toRepresentation();
      roleMappingResource.realmLevel().add(List.of(roleRepresentation));
      return new User(
          id, user.username(), user.email(), user.password(), user.role(), urlProfilePhoto);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * Autentica um usuário no Keycloak e retorna um token de acesso.
   *
   * @param username O nome de usuário fornecido para autenticação.
   * @param password A senha fornecida para autenticação.
   * @return Um objeto AccessToken contendo o token de acesso, se a autenticação for bem-sucedida.
   */
  @Override
  public AccessToken authenticate(String username, String password) {
    try {

      validateCredentials(username, password);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
      MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
      body.add("client_id", clientId);
      body.add("client_secret", clientSecret);
      body.add("grant_type", "password");
      body.add("username", username);
      body.add("password", password);
      body.add("scope", "profile email roles openid");
      LOG.info("Solicitando token com scope: {}", body.get("scope"));
      HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
      ResponseEntity<Map> response =
          restTemplate.exchange(
              serverUrl + "/realms/" + realm + "/protocol/openid-connect/token",
              HttpMethod.POST,
              request,
              Map.class);
      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        return new AccessToken((String) response.getBody().get("access_token"));
      }
      throw new RuntimeException("Credenciais inválidas");
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * Busca um usuário no Keycloak pelo seu identificador único.
   *
   * @param id O UUID do usuário a ser encontrado.
   * @return O objeto User correspondente ao ID fornecido.
   */
  @Override
  public User findById(UUID id) {
    try {
      validateId(id);
      UserRepresentation kcUser =
          keycloak.realm(realm).users().get(id.toString()).toRepresentation();
      if (kcUser == null) {
        throw new RuntimeException("Usuário não encontrado no Keycloak");
      }
      String profilePhoto =
          Optional.ofNullable(kcUser.getAttributes())
              .map(attrs -> attrs.get("profilePhoto"))
              .filter(photos -> !CollectionUtils.isEmpty(photos))
              .map(photos -> photos.get(0))
              .orElse("");
      RoleUser roleUser =
          keycloak
              .realm(realm)
              .users()
              .get(kcUser.getId())
              .roles()
              .realmLevel()
              .listEffective()
              .stream()
              .map(RoleRepresentation::getName)
              .filter(name -> name.equals("ADMIN") || name.equals("USER"))
              .findFirst()
              .map(RoleUser::valueOf)
              .orElse(RoleUser.USER);
      return new User(
          kcUser.getId(), kcUser.getUsername(), kcUser.getEmail(), null, roleUser, profilePhoto);
    } catch (NotFoundException e) {
      LOG.warn("Usuário {} não existe no Keycloak", id, e);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado", e);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * Recupera uma lista paginada de usuários do Keycloak.
   *
   * @param first O índice do primeiro usuário a ser retornado (baseado em zero).
   * @param max O número máximo de usuários a serem retornados.
   * @return Uma lista de objetos User conforme os parâmetros de paginação.
   */
  @Override
  public List<User> findAll(int first, int max) {
    try {
      List<UserRepresentation> kcUsers = keycloak.realm(realm).users().list(first, max);
      return kcUsers.stream()
          .map(
              kcUser -> {
                String profilePhoto =
                    Optional.ofNullable(kcUser.getAttributes())
                        .map(attrs -> attrs.get("profilePhoto"))
                        .filter(photos -> !CollectionUtils.isEmpty(photos))
                        .map(photos -> photos.get(0))
                        .orElse("");
                RoleUser roleUser =
                    keycloak
                        .realm(realm)
                        .users()
                        .get(kcUser.getId())
                        .roles()
                        .realmLevel()
                        .listEffective()
                        .stream()
                        .map(RoleRepresentation::getName)
                        .filter(name -> name.equals("ADMIN") || name.equals("USER"))
                        .findFirst()
                        .map(RoleUser::valueOf)
                        .orElse(RoleUser.USER);
                return new User(
                    kcUser.getId(),
                    kcUser.getUsername(),
                    kcUser.getEmail(),
                    null,
                    roleUser,
                    profilePhoto);
              })
          .collect(Collectors.toList());
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * Conta o número total de usuários no realm do Keycloak.
   *
   * @return O número total de usuários.
   */
  @Override
  public long count() {
    try {
      return keycloak.realm(realm).users().count();
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * Atualiza as informações de um usuário existente no Keycloak.
   *
   * @param id O UUID do usuário a ser atualizado.
   * @param user O objeto User com os dados atualizados.
   * @return O objeto User atualizado.
   */
  @Override
  public User update(UUID id, User user) {
    try {
      validateId(id);
      validateUserForUpdate(user);
      UserRepresentation kcUser =
          keycloak.realm(realm).users().get(id.toString()).toRepresentation();
      if (kcUser == null) {
        throw new RuntimeException("Usuário não encontrado no Keycloak");
      }
      kcUser.setUsername(user.username());
      kcUser.setEmail(user.email());
      kcUser.setRealmRoles(List.of(user.role().name()));
      if (StringUtils.hasText(user.profilePhoto())
          && user.profilePhoto().contains("https")
          && user.profilePhoto().contains("s3")) {
        kcUser.setAttributes(Map.of("profilePhoto", List.of(user.profilePhoto())));
      }
      keycloak.realm(realm).users().get(id.toString()).update(kcUser);
      RoleMappingResource roleMappingResource =
          keycloak.realm(realm).users().get(id.toString()).roles();
      RoleRepresentation roleRepresentation =
          keycloak.realm(realm).roles().get(user.role().name()).toRepresentation();
      List<RoleRepresentation> rolesToAdd = List.of(roleRepresentation);
      List<RoleRepresentation> rolesToRemove =
          roleMappingResource.realmLevel().listAll().stream()
              .filter(role -> !role.getName().equals(user.role().name()))
              .collect(Collectors.toList());
      roleMappingResource.realmLevel().remove(rolesToRemove);
      roleMappingResource.realmLevel().add(rolesToAdd);
      return new User(
          id.toString(),
          user.username(),
          user.email(),
          user.password(),
          user.role(),
          user.profilePhoto());
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * Remove um usuário do Keycloak pelo seu identificador único.
   *
   * @param id O UUID do usuário a ser deletado.
   */
  @Override
  public void delete(UUID id) {
    try {
      validateId(id);
      keycloak.realm(realm).users().get(id.toString()).remove();
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * Converte um objeto User para uma representação de usuário do Keycloak.
   *
   * @param user O objeto User a ser convertido.
   * @param urlProfilePhoto A URL da foto de perfil do usuário.
   * @return Um objeto UserRepresentation configurado para o Keycloak.
   */
  private UserRepresentation toUserRepresentation(User user, String urlProfilePhoto) {
    UserRepresentation kcUser = new UserRepresentation();
    kcUser.setUsername(user.username());
    kcUser.setEmail(user.email());
    kcUser.setEnabled(true);
    CredentialRepresentation credential = new CredentialRepresentation();
    credential.setTemporary(false);
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(user.password());
    kcUser.setCredentials(List.of(credential));
    kcUser.setRealmRoles(List.of(user.role().name()));
    LOG.info("Atribuindo realmRoles ao usuário {}: {}", user.username(), kcUser.getRealmRoles());
    kcUser.setAttributes(Map.of("profilePhoto", List.of(urlProfilePhoto)));
    return kcUser;
  }

  /**
   * Valida os dados de um usuário antes de criá-lo no Keycloak.
   *
   * @param user O objeto User a ser validado.
   * @throws IllegalArgumentException Se os dados do usuário forem inválidos.
   */
  private void validateUserForCreation(User user) {
    if (user == null) throw new IllegalArgumentException("Usuário não pode ser nulo");
    if (isBlank(user.username())) throw new IllegalArgumentException("Username é obrigatório");
    if (isBlank(user.email())) throw new IllegalArgumentException("Email é obrigatório");
    if (isBlank(user.password())) throw new IllegalArgumentException("Password é obrigatório");
    if (user.role() == null) throw new IllegalArgumentException("Role é obrigatória");
  }

  /**
   * Valida os dados de um usuário antes de atualizá-lo no Keycloak.
   *
   * @param user O objeto User a ser validado.
   * @throws IllegalArgumentException Se os dados do usuário forem inválidos.
   */
  private void validateUserForUpdate(User user) {
    if (user == null) throw new IllegalArgumentException("Usuário não pode ser nulo");
    if (isBlank(user.username())) throw new IllegalArgumentException("Username é obrigatório");
    if (isBlank(user.email())) throw new IllegalArgumentException("Email é obrigatório");
    if (user.role() == null) throw new IllegalArgumentException("Role é obrigatória");
  }

  /**
   * Valida as credenciais fornecidas para autenticação.
   *
   * @param username O nome de usuário a ser validado.
   * @param password A senha a ser validada.
   * @throws IllegalArgumentException Se as credenciais forem inválidas.
   */
  private void validateCredentials(String username, String password) {
    if (isBlank(username)) throw new IllegalArgumentException("Username deve ser informado");
    if (isBlank(password)) throw new IllegalArgumentException("Password deve ser informado");
  }

  /**
   * Valida o identificador único de um usuário.
   *
   * @param id O UUID a ser validado.
   * @throws IllegalArgumentException Se o ID for nulo.
   */
  private void validateId(UUID id) {
    if (id == null) throw new IllegalArgumentException("ID do usuário não pode ser nulo");
  }

  /**
   * Verifica se uma string é nula ou vazia após remover espaços em branco.
   *
   * @param value A string a ser verificada.
   * @return true se a string for nula ou vazia, false caso contrário.
   */
  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
