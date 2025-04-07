package com.graduationparty.authservice.domain.model;

/**
 * Representa um usuário do sistema.
 *
 * <p>Este registro contém informações essenciais sobre o usuário, como identificador, nome de
 * usuário, e-mail, senha, papel e foto de perfil.
 *
 * @param id o identificador único do usuário
 * @param username o username do usuário, utilizado no login.
 * @param email o endereço de e-mail do usuário
 * @param password a senha do usuário
 * @param role a papel do usuário no sistema (ADMIN ou USER)
 * @param profilePhoto a URL para a foto de perfil do usuário armazenada no s3
 */
public record User(
    String id, String username, String email, String password, RoleUser role, String profilePhoto) {

  /** Enumeração que define os papéis (roles) que um usuário pode ter no sistema. */
  public enum RoleUser {
    ADMIN,
    USER
  }
}
