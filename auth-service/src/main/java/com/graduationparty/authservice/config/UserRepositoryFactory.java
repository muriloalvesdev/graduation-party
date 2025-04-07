package com.graduationparty.authservice.config;

import com.graduationparty.authservice.adapter.out.keycloak.KeycloakUserRepository;
import com.graduationparty.authservice.application.port.out.FileStoragePort;
import com.graduationparty.authservice.domain.port.out.UserRepository;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Factory para criação de implementações de {@link UserRepository} baseada no provedor de
 * persistência configurado.
 *
 * <p>Esta classe possibilita a abstração da fonte de dados utilizada para gerenciar os usuários.
 * Atualmente, o provedor suportado é o Keycloak, mas a ideia é permitir a integração com outros
 * provedores (por exemplo, PostgreSQL) no futuro, bastando alterar a propriedade de configuração.
 */
@Configuration
public class UserRepositoryFactory {

  @Value("${auth.persistence.provider:keycloak}")
  private String persistenceProvider;

  @Value("${keycloak.realm}")
  private String realm;

  @Value("${keycloak.server-url}")
  private String serverUrl;

  @Value("${keycloak.resource}")
  private String clientId;

  @Value("${keycloak.credentials.secret}")
  private String clientSecret;

  /**
   * Cria e retorna uma instância de {@link UserRepository} com base no provedor de persistência
   * configurado.
   *
   * <p>Se o valor da propriedade <code>auth.persistence.provider</code> for "keycloak"
   * (independentemente de maiúsculas/minúsculas), será retornada uma instância de {@link
   * KeycloakUserRepository}. Caso contrário, uma exceção será lançada informando que o provedor de
   * persistência é desconhecido.
   *
   * @param keycloak o cliente Keycloak injetado para ser utilizado pelo repositório de usuários
   * @param storage o serviço de armazenamento de arquivos utilizado pelo repositório de usuários
   * @return uma implementação de {@link UserRepository} de acordo com o provedor configurado
   * @throws IllegalArgumentException se o provedor de persistência configurado não for suportado
   */
  @Bean
  public UserRepository userRepository(Keycloak keycloak, FileStoragePort storage) {
    switch (persistenceProvider.toLowerCase()) {
      case "keycloak":
        return new KeycloakUserRepository(
            keycloak, realm, serverUrl, clientId, clientSecret, storage);
      default:
        throw new IllegalArgumentException(
            "Provedor de persistência desconhecido: " + persistenceProvider);
    }
  }
}
