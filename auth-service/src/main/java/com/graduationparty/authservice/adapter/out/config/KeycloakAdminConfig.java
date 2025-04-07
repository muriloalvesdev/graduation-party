package com.graduationparty.authservice.adapter.out.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração para o client admin do Keycloak.
 *
 * <p>Esta classe configura e expõe um bean {@link Keycloak} para administração do Keycloak,
 * utilizando as propriedades definidas na configuração.
 */
@Configuration
public class KeycloakAdminConfig {

  @Value("${keycloak.server-url}")
  private String serverUrl;

  @Value("${keycloak.realm}")
  private String realm;

  @Value("${keycloak.admin.username}")
  private String adminUsername;

  @Value("${keycloak.admin.password}")
  private String adminPassword;

  @Value("${keycloak.admin.client-id}")
  private String adminClientId;

  @Value("${keycloak.credentials.secret}")
  private String clientSecret;

  private static final Logger logger = LoggerFactory.getLogger(KeycloakAdminConfig.class);

  /**
   * Cria e retorna um bean {@link Keycloak} configurado para administração.
   *
   * <p>Este método tenta conectar ao servidor Keycloak utilizando um número máximo de tentativas. A
   * cada tentativa, se a conexão falhar, aguarda um tempo definido antes de tentar novamente. Caso
   * todas as tentativas falhem, uma {@link RuntimeException} é lançada.
   *
   * @return uma instância configurada do {@link Keycloak} para administração
   * @throws RuntimeException se não for possível conectar ao Keycloak após o número máximo de
   *     tentativas
   */
  @Bean
  public Keycloak keyCloakAdminClient() {
    int maxAttempts = 5;
    int delaySeconds = 5;

    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        logger.info("Tentando conectar ao KeyCloak na URL: {}", serverUrl);
        return KeycloakBuilder.builder()
            .serverUrl(serverUrl)
            .realm("master")
            .username(adminUsername)
            .password(adminPassword)
            .clientId(adminClientId)
            .clientSecret(clientSecret)
            .build();
      } catch (Exception e) {
        logger.warn(
            "Tentativa {}/{} de conexão com KeyCloak falhou: {}",
            attempt,
            maxAttempts,
            e.getMessage());
        if (attempt == maxAttempts) {
          throw new RuntimeException(
              "Falha ao conectar ao KeyCloak após " + maxAttempts + " tentativas", e);
        }
        try {
          Thread.sleep(delaySeconds * 1000);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          throw new RuntimeException("Interrompido durante espera de retry", ie);
        }
      }
    }
    throw new IllegalStateException(
        "Falha inesperada: não foi possível estabelecer a conexão com Keycloak.");
  }
}
