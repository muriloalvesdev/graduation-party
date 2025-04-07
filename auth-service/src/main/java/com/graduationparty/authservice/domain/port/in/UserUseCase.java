package com.graduationparty.authservice.domain.port.in;

import com.graduationparty.authservice.adapter.out.response.Page;
import com.graduationparty.authservice.adapter.out.response.UserDTO;
import com.graduationparty.authservice.domain.model.AccessToken;
import com.graduationparty.authservice.domain.model.User;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

/**
 * Interface que define os casos de uso relacionados à gestão de usuários. Especifica operações como
 * criação, autenticação, busca, atualização e exclusão de usuários.
 */
public interface UserUseCase {

  /**
   * Cria um novo usuário com os dados fornecidos e uma foto de perfil opcional.
   *
   * @param user O objeto User contendo as informações do usuário a ser criado.
   * @param profilePhoto O arquivo de foto de perfil do usuário (pode ser nulo).
   * @return O objeto User criado com os dados persistidos.
   */
  User createUser(User user, MultipartFile profilePhoto);

  /**
   * Autentica um usuário com base em seu nome de usuário e senha.
   *
   * @param username O nome de usuário fornecido para autenticação.
   * @param password A senha fornecida para autenticação.
   * @return Um objeto AccessToken contendo o token de acesso gerado após autenticação bem-sucedida.
   */
  AccessToken authenticate(String username, String password);

  /**
   * Busca um usuário específico pelo seu identificador único.
   *
   * @param id O UUID do usuário a ser recuperado.
   * @return Um objeto UserDTO contendo os dados do usuário encontrado.
   */
  UserDTO findUserById(UUID id);

  /**
   * Recupera uma lista paginada de todos os usuários.
   *
   * @param page O número da página a ser retornada.
   * @param size O tamanho da página, ou seja, a quantidade de registros por página.
   * @return Um objeto Page contendo a lista paginada de UserDTO.
   */
  Page<UserDTO> findAllUsers(int page, int size);

  /**
   * Atualiza as informações de um usuário existente.
   *
   * @param id O UUID do usuário a ser atualizado.
   * @param user O objeto User contendo as novas informações do usuário.
   * @return O objeto User atualizado com os dados persistidos.
   */
  User updateUser(UUID id, User user);

  /**
   * Exclui um usuário específico pelo seu identificador único.
   *
   * @param id O UUID do usuário a ser excluído.
   */
  void deleteUser(UUID id);
}
