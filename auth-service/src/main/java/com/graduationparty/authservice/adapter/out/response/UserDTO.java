package com.graduationparty.authservice.adapter.out.response;

import com.graduationparty.authservice.domain.model.User;

/**
 * DTO (Data Transfer Object) que representa os dados de um usuário.
 *
 * <p>Esta classe é utilizada para transferir informações de usuário entre diferentes camadas da
 * aplicação, incluindo o identificador, nome de usuário, e-mail, papel no sistema e foto de perfil.
 *
 * @param id Identificador único do usuário.
 * @param username Nome de usuário.
 * @param email Endereço de e-mail do usuário.
 * @param role Papel do usuário no sistema.
 * @param profilePhoto URL com o caminho da foto de perfil do usuário.
 */
public record UserDTO(
    String id, String username, String email, User.RoleUser role, String profilePhoto) {}
