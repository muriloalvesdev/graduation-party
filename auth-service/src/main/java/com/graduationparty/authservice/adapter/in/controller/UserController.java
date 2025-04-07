package com.graduationparty.authservice.adapter.in.controller;

import com.graduationparty.authservice.domain.model.User;
import com.graduationparty.authservice.domain.port.in.UserUseCase;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST responsável por gerenciar operações relacionadas a usuários. Fornece endpoints
 * para buscar, atualizar e excluir usuários.
 */
@RestController
@RequestMapping("/users")
public class UserController {

  private final UserUseCase userUseCase;

  /**
   * Construtor do controlador que injeta a dependência do caso de uso de usuário.
   *
   * @param userUseCase O caso de uso que contém a lógica de negócio para operações de usuário.
   */
  @Autowired
  public UserController(UserUseCase userUseCase) {
    this.userUseCase = userUseCase;
  }

  /**
   * Recupera uma lista paginada de todos os usuários.
   *
   * @param page O número da página a ser retornada (padrão é 0).
   * @param size O tamanho da página, ou seja, a quantidade de registros por página (padrão é 10).
   * @return ResponseEntity contendo a lista de usuários e o status HTTP 200 (OK).
   */
  @GetMapping
  public ResponseEntity<?> getAllUsers(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(userUseCase.findAllUsers(page, size));
  }

  /**
   * Recupera um usuário específico pelo seu identificador único.
   *
   * @param id O UUID do usuário a ser recuperado.
   * @return ResponseEntity contendo o usuário encontrado e o status HTTP 200 (OK).
   */
  @GetMapping("/{id}")
  public ResponseEntity<?> getUserById(@PathVariable UUID id) {
    return ResponseEntity.ok(userUseCase.findUserById(id));
  }

  /**
   * Atualiza as informações de um usuário existente.
   *
   * @param id O UUID do usuário a ser atualizado.
   * @param user O objeto User contendo as novas informações do usuário.
   * @return ResponseEntity contendo o usuário atualizado e o status HTTP 200 (OK).
   */
  @PutMapping("/{id}")
  public ResponseEntity<?> updateUser(@PathVariable UUID id, @RequestBody User user) {
    return ResponseEntity.ok(userUseCase.updateUser(id, user));
  }

  /**
   * Exclui um usuário específico pelo seu identificador único.
   *
   * @param id O UUID do usuário a ser excluído.
   * @return ResponseEntity com status HTTP 204 (No Content) indicando sucesso na exclusão.
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
    userUseCase.deleteUser(id);
    return ResponseEntity.noContent().build();
  }
}
