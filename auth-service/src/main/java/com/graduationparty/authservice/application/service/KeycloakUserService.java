package com.graduationparty.authservice.application.service;

import com.graduationparty.authservice.adapter.out.response.Page;
import com.graduationparty.authservice.adapter.out.response.UserDTO;
import com.graduationparty.authservice.domain.model.AccessToken;
import com.graduationparty.authservice.domain.model.User;
import com.graduationparty.authservice.domain.port.in.UserUseCase;
import com.graduationparty.authservice.domain.port.out.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Serviço que implementa os casos de uso de gerenciamento de usuários utilizando Keycloak. Fornece
 * operações como criação, autenticação, busca, atualização e exclusão de usuários, com resiliência
 * via Circuit Breaker e tratamento de falhas.
 */
@Service
public class KeycloakUserService implements UserUseCase {

  private static final Logger LOG = LoggerFactory.getLogger(KeycloakUserService.class);

  private final UserRepository userRepository;

  /**
   * Construtor do serviço que injeta o repositório de usuários.
   *
   * @param userRepository O repositório responsável pela persistência de dados de usuários.
   */
  public KeycloakUserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Cria um novo usuário com os dados fornecidos e uma foto de perfil opcional.
   *
   * @param user O objeto User contendo as informações do usuário a ser criado.
   * @param profilePhoto O arquivo de foto de perfil do usuário (pode ser nulo).
   * @return O objeto User criado com os dados persistidos.
   * @throws IllegalArgumentException se os dados do usuário forem inválidos.
   */
  @Override
  @CircuitBreaker(name = "keycloak", fallbackMethod = "createUserFallback")
  public User createUser(User user, MultipartFile profilePhoto) {
    validateUserCreation(user);
    return userRepository.create(user, profilePhoto);
  }

  /**
   * Autentica um usuário com base em seu nome de usuário e senha.
   *
   * @param username O nome de usuário fornecido para autenticação.
   * @param password A senha fornecida para autenticação.
   * @return Um objeto AccessToken contendo o token de acesso gerado.
   * @throws IllegalArgumentException se as credenciais forem inválidas.
   */
  @Override
  @CircuitBreaker(name = "keycloak", fallbackMethod = "authenticateFallback")
  public AccessToken authenticate(String username, String password) {
    validateCredentials(username, password);
    return userRepository.authenticate(username, password);
  }

  /**
   * Busca um usuário específico pelo seu identificador único.
   *
   * @param id O UUID do usuário a ser recuperado.
   * @return Um objeto UserDTO contendo os dados do usuário encontrado.
   * @throws IllegalArgumentException se o ID for nulo.
   * @throws RuntimeException se o usuário não for encontrado.
   */
  @Override
  @CircuitBreaker(name = "keycloak", fallbackMethod = "findUserByIdFallback")
  public UserDTO findUserById(UUID id) {
    if (id == null) throw new IllegalArgumentException("ID não pode ser nulo");
    User user = userRepository.findById(id);
    if (user == null) throw new RuntimeException("Usuário não encontrado");
    return new UserDTO(user.id(), user.username(), user.email(), user.role(), user.profilePhoto());
  }

  /**
   * Recupera uma lista paginada de todos os usuários.
   *
   * @param page O número da página a ser retornada.
   * @param size O tamanho da página, ou seja, a quantidade de registros por página.
   * @return Um objeto Page contendo a lista paginada de UserDTO.
   * @throws IllegalArgumentException se os parâmetros de paginação forem inválidos.
   */
  @Override
  @CircuitBreaker(name = "keycloak", fallbackMethod = "findAllUsersFallback")
  public Page<UserDTO> findAllUsers(int page, int size) {
    if (page < 0 || size <= 0)
      throw new IllegalArgumentException("Parâmetros de paginação inválidos");
    int first = page * size;
    var users =
        userRepository.findAll(first, size).stream()
            .map(
                user ->
                    new UserDTO(
                        user.id(), user.username(), user.email(), user.role(), user.profilePhoto()))
            .toList();
    long total = userRepository.count();
    return new Page<>(users, page, size, total);
  }

  /**
   * Atualiza as informações de um usuário existente.
   *
   * @param id O UUID do usuário a ser atualizado.
   * @param user O objeto User contendo as novas informações do usuário.
   * @return O objeto User atualizado com os dados persistidos.
   * @throws IllegalArgumentException se o ID ou os dados do usuário forem inválidos.
   */
  @Override
  @CircuitBreaker(name = "keycloak", fallbackMethod = "updateUserFallback")
  public User updateUser(UUID id, User user) {
    if (id == null) throw new IllegalArgumentException("ID não pode ser nulo");
    validateUserUpdate(user);
    return userRepository.update(id, user);
  }

  /**
   * Exclui um usuário específico pelo seu identificador único.
   *
   * @param id O UUID do usuário a ser excluído.
   * @throws IllegalArgumentException se o ID for nulo.
   */
  @Override
  @CircuitBreaker(name = "keycloak", fallbackMethod = "deleteUserFallback")
  public void deleteUser(UUID id) {
    if (id == null) throw new IllegalArgumentException("ID não pode ser nulo");
    userRepository.delete(id);
  }

  /**
   * Método de fallback chamado quando a criação de usuário falha no Circuit Breaker.
   *
   * @param user O usuário que estava sendo criado.
   * @param t A exceção que causou a falha.
   * @return Nada, pois lança uma exceção.
   * @throws RuntimeException informando que o serviço está indisponível.
   */
  public User createUserFallback(User user, Throwable t) {
    LOG.error("Fallback: Falha ao criar usuário", t);
    throw new RuntimeException("Serviço indisponível, tente novamente mais tarde");
  }

  /**
   * Método de fallback chamado quando a autenticação falha no Circuit Breaker.
   *
   * @param username O nome de usuário fornecido.
   * @param password A senha fornecida.
   * @param t A exceção que causou a falha.
   * @return Nada, pois lança uma exceção.
   * @throws RuntimeException informando que o serviço está indisponível.
   */
  public AccessToken authenticateFallback(String username, String password, Throwable t) {
    LOG.error("Fallback: Falha ao autenticar usuário", t);
    throw new RuntimeException("Serviço de autenticação indisponível, tente novamente mais tarde");
  }

  /**
   * Método de fallback chamado quando a busca por ID falha no Circuit Breaker.
   *
   * @param id O UUID do usuário.
   * @param t A exceção que causou a falha.
   * @return Nada, pois lança uma exceção.
   * @throws RuntimeException informando que o serviço está indisponível.
   */
  public UserDTO findUserByIdFallback(UUID id, Throwable t) {
    LOG.error("Fallback: Falha ao buscar usuário por ID", t);
    throw new RuntimeException("Serviço indisponível, tente novamente mais tarde");
  }

  /**
   * Método de fallback chamado quando a listagem de usuários falha no Circuit Breaker.
   *
   * @param page O número da página.
   * @param size O tamanho da página.
   * @param t A exceção que causou a falha.
   * @return Nada, pois lança uma exceção.
   * @throws RuntimeException informando que o serviço está indisponível.
   */
  public Page<UserDTO> findAllUsersFallback(int page, int size, Throwable t) {
    LOG.error("Fallback: Falha ao listar usuários", t);
    throw new RuntimeException("Serviço indisponível, tente novamente mais tarde");
  }

  /**
   * Método de fallback chamado quando a atualização de usuário falha no Circuit Breaker.
   *
   * @param id O UUID do usuário.
   * @param user O usuário que estava sendo atualizado.
   * @param t A exceção que causou a falha.
   * @return Nada, pois lança uma exceção.
   * @throws RuntimeException informando que o serviço está indisponível.
   */
  public User updateUserFallback(UUID id, User user, Throwable t) {
    LOG.error("Fallback: Falha ao atualizar usuário", t);
    throw new RuntimeException("Serviço indisponível, tente novamente mais tarde");
  }

  /**
   * Método de fallback chamado quando a exclusão de usuário falha no Circuit Breaker.
   *
   * @param id O UUID do usuário.
   * @param t A exceção que causou a falha.
   * @throws RuntimeException informando que o serviço está indisponível.
   */
  public void deleteUserFallback(UUID id, Throwable t) {
    LOG.error("Fallback: Falha ao remover usuário", t);
    throw new RuntimeException("Serviço indisponível, tente novamente mais tarde");
  }

  /**
   * Valida os dados de um usuário para criação.
   *
   * @param user O objeto User a ser validado.
   * @throws IllegalArgumentException se os dados obrigatórios estiverem ausentes ou inválidos.
   */
  private void validateUserCreation(User user) {
    if (user == null) throw new IllegalArgumentException("Usuário não pode ser nulo");
    validateRequiredField(user.username(), "Username");
    validateRequiredField(user.email(), "Email");
    validateRequiredField(user.password(), "Password");
    if (user.role() == null) throw new IllegalArgumentException("Role é obrigatória");
  }

  /**
   * Valida as credenciais fornecidas para autenticação.
   *
   * @param username O nome de usuário a ser validado.
   * @param password A senha a ser validada.
   * @throws IllegalArgumentException se as credenciais estiverem ausentes ou inválidas.
   */
  private void validateCredentials(String username, String password) {
    validateRequiredField(username, "Username");
    validateRequiredField(password, "Password");
  }

  /**
   * Valida os dados de um usuário para atualização.
   *
   * @param user O objeto User a ser validado.
   * @throws IllegalArgumentException se os dados obrigatórios estiverem ausentes ou inválidos.
   */
  private void validateUserUpdate(User user) {
    if (user == null) throw new IllegalArgumentException("Usuário não pode ser nulo");
    validateRequiredField(user.username(), "Username");
    validateRequiredField(user.email(), "Email");
    if (user.role() == null) throw new IllegalArgumentException("Role é obrigatória");
  }

  /**
   * Valida se um campo obrigatório contém texto válido (não nulo e não vazio).
   *
   * @param value O valor do campo a ser validado.
   * @param fieldName O nome do campo para mensagem de erro.
   * @throws IllegalArgumentException se o campo estiver nulo ou vazio.
   */
  private void validateRequiredField(String value, String fieldName) {
    if (!StringUtils.hasText(value)) {
      throw new IllegalArgumentException(fieldName + " é obrigatório");
    }
  }
}
