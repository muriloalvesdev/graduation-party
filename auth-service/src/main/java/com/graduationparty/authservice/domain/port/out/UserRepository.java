package com.graduationparty.authservice.domain.port.out;

import com.graduationparty.authservice.domain.model.AccessToken;
import com.graduationparty.authservice.domain.model.User;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

/** Interface que define as operações de persistência para a entidade User. */
public interface UserRepository {

  /**
   * Cria um novo usuário no repositório com uma foto de perfil associada.
   *
   * @param user O objeto User a ser criado.
   * @param profilePhoto O arquivo de foto de perfil a ser associado ao usuário.
   * @return O objeto User criado, incluindo quaisquer identificadores gerados.
   */
  User create(User user, MultipartFile profilePhoto);

  /**
   * Busca um usuário pelo seu identificador único.
   *
   * @param id O UUID do usuário a ser encontrado.
   * @return O objeto User correspondente ao ID fornecido, ou null se não encontrado.
   */
  User findById(UUID id);

  /**
   * Recupera uma lista paginada de usuários.
   *
   * @param first O índice do primeiro usuário a ser retornado (baseado em zero).
   * @param max O número máximo de usuários a serem retornados.
   * @return Uma lista de objetos User conforme os parâmetros de paginação.
   */
  List<User> findAll(int first, int max);

  /**
   * Conta o número total de usuários no repositório.
   *
   * @return O número total de usuários.
   */
  long count();

  /**
   * Atualiza as informações de um usuário existente.
   *
   * @param id O UUID do usuário a ser atualizado.
   * @param user O objeto User com os dados atualizados.
   * @return O objeto User atualizado.
   */
  User update(UUID id, User user);

  /**
   * Remove um usuário do repositório pelo seu identificador único.
   *
   * @param id O UUID do usuário a ser deletado.
   */
  void delete(UUID id);

  /**
   * Autentica um usuário com base em seu nome de usuário e senha.
   *
   * @param username O nome de usuário fornecido para autenticação.
   * @param password A senha fornecida para autenticação.
   * @return Um objeto AccessToken se a autenticação for bem-sucedida, ou null caso contrário.
   */
  AccessToken authenticate(String username, String password);
}
