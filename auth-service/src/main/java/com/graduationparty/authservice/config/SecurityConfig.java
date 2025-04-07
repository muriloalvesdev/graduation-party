package com.graduationparty.authservice.config;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança para a aplicação.
 *
 * <p>Esta classe configura a cadeia de filtros de segurança, o decodificador JWT e o conversor de
 * autenticação baseado em JWT para integração com o Keycloak. Define também as regras de
 * autorização para os endpoints da aplicação.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private static final Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);

  private final LoggingFilter loggingFilter;
  private final String serverUrl;
  private final String realm;

  /**
   * Construtor para injeção de dependências na configuração de segurança.
   *
   * @param loggingFilter o filtro de log que intercepta requisições HTTP
   * @param serverUrl a URL do servidor Keycloak
   * @param realm o nome do realm no Keycloak
   */
  public SecurityConfig(
      LoggingFilter loggingFilter,
      @Value("${keycloak.server-url}") String serverUrl,
      @Value("${keycloak.realm}") String realm) {
    this.loggingFilter = loggingFilter;
    this.serverUrl = serverUrl;
    this.realm = realm;
  }

  /**
   * Configura a cadeia de filtros de segurança para a aplicação.
   *
   * <p>Este método desabilita a proteção CSRF, adiciona o filtro de log antes do filtro de
   * autenticação com token Bearer, e define as regras de autorização para os endpoints da
   * aplicação. Permite acesso anônimo aos endpoints de cadastro (/auth/signup), login (/auth/login)
   * e aos endpoints da documentação Swagger (/v3/api-docs/**, /swagger-ui/**, /swagger-ui.html),
   * exige a autoridade "ROLE_ADMIN" para os endpoints de usuários (/users/**) e autentica todas as
   * demais requisições. Além disso, configura o recurso do servidor OAuth2 com suporte a JWT,
   * definindo o decodificador e o conversor de autenticação apropriados.
   *
   * @param http a instância de {@link HttpSecurity} utilizada para configurar a segurança
   * @return o objeto {@link SecurityFilterChain} configurado
   * @throws Exception se ocorrer algum erro durante a configuração de segurança
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .addFilterBefore(loggingFilter, BearerTokenAuthenticationFilter.class)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/auth/signup", "/auth/login")
                    .permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                    .permitAll()
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .requestMatchers("/users/**")
                    .hasAuthority("ROLE_ADMIN")
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(
                    jwt ->
                        jwt.decoder(jwtDecoder())
                            .jwtAuthenticationConverter(jwtAuthenticationConverter())));

    return http.build();
  }

  /**
   * Cria e configura o decodificador de tokens JWT.
   *
   * <p>O {@link JwtDecoder} é configurado para utilizar o JWK Set URI do Keycloak, que é construído
   * a partir da URL do servidor e do realm informado.
   *
   * @return uma instância configurada de {@link JwtDecoder}
   */
  @Bean
  public JwtDecoder jwtDecoder() {
    String jwkSetUri = serverUrl + "/realms/" + realm + "/protocol/openid-connect/certs";
    return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
  }

  /**
   * Cria e configura o conversor de autenticação baseado em JWT.
   *
   * <p>O {@link JwtAuthenticationConverter} é configurado para extrair as autoridades (roles) do
   * token JWT e convertê-las em objetos {@link GrantedAuthority}.
   *
   * @return uma instância configurada de {@link JwtAuthenticationConverter}
   */
  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
        jwt -> {
          Collection<GrantedAuthority> authorities =
              (Collection<GrantedAuthority>) extractAuthorities(jwt);
          return authorities;
        });
    return jwtAuthenticationConverter;
  }

  /**
   * Extrai as autoridades (roles) do token JWT.
   *
   * <p>Este método obtém o atributo "realm_access" das claims do token JWT e extrai a lista de
   * roles, convertendo cada role em uma {@link SimpleGrantedAuthority} com o prefixo "ROLE_".
   *
   * @param jwt o token JWT a partir do qual as autoridades serão extraídas
   * @return uma coleção de {@link GrantedAuthority} representando as roles extraídas do token, ou
   *     uma coleção vazia caso as roles não sejam encontradas
   */
  private Collection<? extends GrantedAuthority> extractAuthorities(Jwt jwt) {
    Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");
    if (realmAccess == null) {
      LOG.warn("realm_access não encontrado no token");
      return Collections.emptyList();
    }
    List<String> roles = (List<String>) realmAccess.get("roles");
    if (roles == null) {
      LOG.warn("roles não encontrado em realm_access");
      return Collections.emptyList();
    }

    return roles.stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
        .collect(Collectors.toList());
  }
}
