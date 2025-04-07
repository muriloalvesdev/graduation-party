package com.graduationparty.authservice.adapter.in.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graduationparty.authservice.domain.model.AccessToken;
import com.graduationparty.authservice.domain.model.User;
import com.graduationparty.authservice.domain.port.in.UserUseCase;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Controller que gerencia as operações de autenticação e criação de usuários.
 *
 * <p>Fornece endpoints para registro de novos usuários (signup) e autenticação (login).
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final UserUseCase userUseCase;

  /**
   * Construtor para injeção do caso de uso de usuário.
   *
   * @param userUseCase a interface de caso de uso que encapsula a lógica de criação e autenticação
   *     de usuários
   */
  public AuthController(UserUseCase userUseCase) {
    this.userUseCase = userUseCase;
  }

  /**
   * Registra um novo usuário no sistema.
   *
   * <p>Este endpoint aceita uma requisição multipart contendo uma parte com os dados do usuário em
   * JSON e outra parte com a foto de perfil. Após a criação do usuário, retorna uma resposta com o
   * status HTTP 201 (Created) e a localização do novo recurso.
   *
   * @param userJson a representação JSON do usuário a ser criado
   * @param profilePhoto o arquivo contendo a foto de perfil do usuário
   * @return uma resposta HTTP contendo o usuário criado e o URI de localização
   * @throws JsonProcessingException se ocorrer um erro ao converter o JSON para a classe {@link
   *     User}
   */
  @PostMapping(value = "/signup", consumes = "multipart/form-data")
  public ResponseEntity<User> signup(
      @RequestPart("user") String userJson, @RequestPart("profilePhoto") MultipartFile profilePhoto)
      throws JsonProcessingException {

    User user = MAPPER.readValue(userJson, User.class);
    User createdUser = userUseCase.createUser(user, profilePhoto);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(createdUser.id())
            .toUri();
    return ResponseEntity.created(location).body(createdUser);
  }

  /**
   * Autentica um usuário com base no nome de usuário e senha.
   *
   * <p>Se a autenticação for bem sucedida, retorna um token de acesso para o usuário.
   *
   * @param username o nome de usuário utilizado para autenticação
   * @param password a senha do usuário
   * @return uma resposta HTTP contendo o token de acesso
   */
  @PostMapping("/login")
  public ResponseEntity<AccessToken> login(
      @RequestParam(name = "username") String username,
      @RequestParam(name = "password") String password) {
    return ResponseEntity.ok(userUseCase.authenticate(username, password));
  }
}
