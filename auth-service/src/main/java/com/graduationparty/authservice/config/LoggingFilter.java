package com.graduationparty.authservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * Filtro para log de requisições e respostas HTTP.
 *
 * <p>Esta classe intercepta as requisições HTTP para registrar informações importantes como o
 * método, URI, IP, payload da requisição e detalhes da resposta, incluindo status e usuário
 * autenticado.
 */
@Component
public class LoggingFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

  /**
   * Filtra a requisição e resposta para registrar informações de log.
   *
   * <p>O método envolve a requisição e resposta em wrappers que permitem capturar o payload,
   * registra informações antes e depois do processamento do filtro e finalmente copia o corpo da
   * resposta para o fluxo original.
   *
   * @param request a requisição HTTP
   * @param response a resposta HTTP
   * @param filterChain a cadeia de filtros que processa a requisição e a resposta
   * @throws ServletException se ocorrer um erro durante o processamento do filtro
   * @throws IOException se ocorrer um erro de I/O durante o processamento do filtro
   */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
    ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

    String ip = request.getRemoteAddr();
    String method = request.getMethod();
    String uri = request.getRequestURI();

    logger.info("Requisição recebida: method={}, uri={}, ip={}", method, uri, ip);

    String requestPayload = getRequestPayload(wrappedRequest);
    if (requestPayload != null && !requestPayload.isEmpty()) {
      logger.info("Payload da requisição: {}", requestPayload);
    }

    filterChain.doFilter(wrappedRequest, wrappedResponse);

    String user = getAuthenticatedUser();
    logger.info(
        "Resposta enviada: uri={}, status={}, user={}",
        uri,
        wrappedResponse.getStatus(),
        user != null ? user : "anonymous");

    wrappedResponse.copyBodyToResponse();
  }

  /**
   * Obtém o nome do usuário autenticado na sessão atual.
   *
   * @return o nome do usuário autenticado ou {@code null} se não houver autenticação válida
   */
  private String getAuthenticatedUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.isAuthenticated()
        && !"anonymousUser".equals(authentication.getName())) {
      return authentication.getName();
    }
    return null;
  }

  /**
   * Extrai o payload da requisição HTTP a partir de um {@link ContentCachingRequestWrapper}.
   *
   * @param request o {@link ContentCachingRequestWrapper} que contém a requisição HTTP
   * @return o payload da requisição como uma {@link String}, ou {@code null} se não houver conteúdo
   */
  private String getRequestPayload(ContentCachingRequestWrapper request) {
    try {
      byte[] content = request.getContentAsByteArray();
      if (content.length > 0) {
        return new String(content, StandardCharsets.UTF_8);
      }
    } catch (Exception e) {
      logger.error("Erro ao capturar o payload da requisição", e);
    }
    return null;
  }
}
