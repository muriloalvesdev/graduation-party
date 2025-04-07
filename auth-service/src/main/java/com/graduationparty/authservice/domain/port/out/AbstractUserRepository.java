package com.graduationparty.authservice.domain.port.out;

import com.graduationparty.authservice.application.port.out.FileStoragePort;
import com.graduationparty.authservice.domain.model.AccessToken;
import com.graduationparty.authservice.domain.model.User;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * Classe abstrata que implementa a interface UserRepository, fornecendo funcionalidades comuns para
 * repositórios de usuários, como upload de fotos de perfil e métodos de fallback.
 */
public abstract class AbstractUserRepository implements UserRepository {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractUserRepository.class);

  /** Porta de armazenamento de arquivos utilizada para upload de fotos de perfil. */
  protected final FileStoragePort storage;

  /**
   * Construtor da classe abstrata AbstractUserRepository.
   *
   * @param storage A implementação de FileStoragePort para gerenciar o armazenamento de arquivos.
   */
  protected AbstractUserRepository(FileStoragePort storage) {
    this.storage = storage;
  }

  /**
   * Realiza o upload de uma foto de perfil para o armazenamento externo.
   *
   * @param username O nome de usuário associado à foto de perfil.
   * @param file O arquivo MultipartFile contendo a foto de perfil.
   * @return A URL do arquivo armazenado.
   * @throws ResponseStatusException Se ocorrer um erro durante o upload.
   */
  protected String uploadProfilePhoto(String username, MultipartFile file) {
    try {
      String prefix = "profile-photos/" + username;
      return storage.upload(file, prefix);
    } catch (IOException e) {
      LOG.error("Erro no upload de foto de perfil para o storage {}", e.getMessage(), e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao enviar foto de perfil", e);
    }
  }

  /**
   * Cria um novo usuário no repositório com uma foto de perfil associada.
   *
   * @param user O objeto User a ser criado.
   * @param profilePhoto O arquivo de foto de perfil a ser associado ao usuário.
   * @return O objeto User criado.
   */
  @Override
  public abstract User create(User user, MultipartFile profilePhoto);

  /**
   * Autentica um usuário com base em suas credenciais.
   *
   * @param username O nome de usuário fornecido para autenticação.
   * @param password A senha fornecida para autenticação.
   * @return Um objeto AccessToken se a autenticação for bem-sucedida.
   */
  @Override
  public abstract AccessToken authenticate(String username, String password);

  /**
   * Busca um usuário pelo seu identificador único.
   *
   * @param id O UUID do usuário a ser encontrado.
   * @return O objeto User correspondente ao ID fornecido.
   */
  @Override
  public abstract User findById(UUID id);

  /**
   * Recupera uma lista paginada de usuários.
   *
   * @param first O índice do primeiro usuário a ser retornado (baseado em zero).
   * @param max O número máximo de usuários a serem retornados.
   * @return Uma lista de objetos User conforme os parâmetros de paginação.
   */
  @Override
  public abstract List<User> findAll(int first, int max);

  /**
   * Conta o número total de usuários no repositório.
   *
   * @return O número total de usuários.
   */
  @Override
  public abstract long count();

  /**
   * Atualiza as informações de um usuário existente.
   *
   * @param id O UUID do usuário a ser atualizado.
   * @param user O objeto User com os dados atualizados.
   * @return O objeto User atualizado.
   */
  @Override
  public abstract User update(UUID id, User user);

  /**
   * Remove um usuário do repositório pelo seu identificador único.
   *
   * @param id O UUID do usuário a ser deletado.
   */
  @Override
  public abstract void delete(UUID id);

  /**
   * Método de fallback chamado quando a criação de um usuário falha.
   *
   * @param user O objeto User que falhou ao ser criado.
   * @param t A exceção que causou a falha.
   * @return Um objeto User (nunca retornado, pois lança uma exceção).
   * @throws RuntimeException Indica que o serviço está indisponível.
   */
  protected User createUserFallback(User user, Throwable t) {
    LOG.error("Fallback: Falha ao criar usuário", t);
    throw new RuntimeException("Serviço indisponível, tente novamente mais tarde");
  }

  /**
   * Método de fallback chamado quando a autenticação de um usuário falha.
   *
   * @param username O nome de usuário fornecido.
   * @param password A senha fornecida.
   * @param t A exceção que causou a falha.
   * @return Um objeto AccessToken (nunca retornado, pois lança uma exceção).
   * @throws RuntimeException Indica que o serviço de autenticação está indisponível.
   */
  protected AccessToken authenticateFallback(String username, String password, Throwable t) {
    LOG.error("Fallback: Falha ao autenticar usuário", t);
    throw new RuntimeException("Serviço de autenticação indisponível, tente novamente mais tarde");
  }

  /**
   * Método de fallback chamado quando a busca de um usuário por ID falha.
   *
   * @param id O UUID do usuário buscado.
   * @param t A exceção que causou a falha.
   * @return Um objeto User (nunca retornado, pois lança uma exceção).
   * @throws RuntimeException Indica que o serviço está indisponível.
   */
  protected User findUserByIdFallback(UUID id, Throwable t) {
    LOG.error("Fallback: Falha ao buscar usuário por ID", t);
    throw new RuntimeException("Serviço indisponível, tente novamente mais tarde");
  }

  /**
   * Método de fallback chamado quando a listagem de usuários falha.
   *
   * @param first O índice do primeiro usuário a ser retornado.
   * @param max O número máximo de usuários a serem retornados.
   * @param t A exceção que causou a falha.
   * @return Uma lista de objetos User (nunca retornada, pois lança uma exceção).
   * @throws RuntimeException Indica que o serviço está indisponível.
   */
  protected List<User> findAllUsersFallback(int first, int max, Throwable t) {
    LOG.error("Fallback: Falha ao listar usuários", t);
    throw new RuntimeException("Serviço indisponível, tente novamente mais tarde");
  }

  /**
   * Método de fallback chamado quando a contagem de usuários falha.
   *
   * @param t A exceção que causou a falha.
   * @return O número total de usuários (nunca retornado, pois lança uma exceção).
   * @throws RuntimeException Indica que o serviço está indisponível.
   */
  protected long countFallback(Throwable t) {
    LOG.error("Fallback: Falha ao contar usuários", t);
    throw new RuntimeException("Serviço indisponível, tente novamente mais tarde");
  }

  /**
   * Método de fallback chamado quando a atualização de um usuário falha.
   *
   * @param id O UUID do usuário a ser atualizado.
   * @param user O objeto User com os dados atualizados.
   * @param t A exceção que causou a falha.
   * @return Um objeto User atualizado (nunca retornado, pois lança uma exceção).
   * @throws RuntimeException Indica que o serviço está indisponível.
   */
  protected User updateUserFallback(UUID id, User user, Throwable t) {
    LOG.error("Fallback: Falha ao atualizar usuário", t);
    throw new RuntimeException("Serviço indisponível, tente novamente mais tarde");
  }

  /**
   * Método de fallback chamado quando a remoção de um usuário falha.
   *
   * @param id O UUID do usuário a ser deletado.
   * @param t A exceção que causou a falha.
   * @throws RuntimeException Indica que o serviço está indisponível.
   */
  protected void deleteUserFallback(UUID id, Throwable t) {
    LOG.error("Fallback: Falha ao remover usuário", t);
    throw new RuntimeException("Serviço indisponível, tente novamente mais tarde");
  }
}
