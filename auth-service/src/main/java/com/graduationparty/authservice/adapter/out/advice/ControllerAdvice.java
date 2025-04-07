package com.graduationparty.authservice.adapter.out.advice;

import com.graduationparty.authservice.adapter.out.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.UnknownHostException;
import java.time.Instant;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

/**
 * ControllerAdvice para tratamento global de exceções em controladores REST.
 *
 * <p>Esta classe intercepta exceções lançadas durante a execução dos endpoints REST e retorna uma
 * resposta estruturada contendo informações sobre o erro através de um objeto {@link
 * ErrorResponse}.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ControllerAdvice {

  /**
   * Trata exceções do tipo {@link ResourceAccessException}.
   *
   * <p>Verifica a causa da exceção para identificar se o erro se deve a host não encontrado ou
   * problemas de I/O, retornando o status HTTP correspondente e uma mensagem de erro detalhada.
   *
   * @param ex a exceção {@link ResourceAccessException} capturada
   * @param request o objeto {@link HttpServletRequest} da requisição atual
   * @return um {@link ResponseEntity} contendo o {@link ErrorResponse} com os detalhes do erro
   */
  @ExceptionHandler(ResourceAccessException.class)
  public ResponseEntity<ErrorResponse> handleResourceAccess(
      ResourceAccessException ex, HttpServletRequest request) {

    Throwable cause = ex.getCause();
    String msg;
    HttpStatus status;
    if (cause instanceof UnknownHostException) {
      status = HttpStatus.SERVICE_UNAVAILABLE;
      msg = "Host não encontrado: " + cause.getMessage();
    } else if (cause instanceof IOException) {
      status = HttpStatus.INTERNAL_SERVER_ERROR;
      msg = "Erro de I/O: " + ex.getMessage();
    } else {
      status = HttpStatus.INTERNAL_SERVER_ERROR;
      msg = "Erro Interno: " + ex.getMessage();
    }

    ErrorResponse body =
        new ErrorResponse(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            ex.getClass().getSimpleName(),
            msg,
            request.getRequestURI());
    return ResponseEntity.status(status).body(body);
  }

  /**
   * Trata exceções do tipo {@link RestClientException}.
   *
   * <p>Retorna uma resposta com o status HTTP BAD_GATEWAY e uma mensagem indicando erro de
   * comunicação externa.
   *
   * @param ex a exceção {@link RestClientException} capturada
   * @param request o objeto {@link HttpServletRequest} da requisição atual
   * @return um {@link ResponseEntity} contendo o {@link ErrorResponse} com os detalhes do erro
   */
  @ExceptionHandler(RestClientException.class)
  public ResponseEntity<ErrorResponse> handleRestClient(
      RestClientException ex, HttpServletRequest request) {

    HttpStatus status = HttpStatus.BAD_GATEWAY;
    ErrorResponse body =
        new ErrorResponse(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            ex.getClass().getSimpleName(),
            "Erro de comunicação externa: " + ex.getMessage(),
            request.getRequestURI());
    return ResponseEntity.status(status).body(body);
  }

  /**
   * Trata todas as exceções não tratadas especificamente por outros métodos.
   *
   * <p>Retorna uma resposta com o status HTTP INTERNAL_SERVER_ERROR e uma mensagem de erro
   * genérica.
   *
   * @param ex a exceção {@link Exception} capturada
   * @param request o objeto {@link HttpServletRequest} da requisição atual
   * @return um {@link ResponseEntity} contendo o {@link ErrorResponse} com os detalhes do erro
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest request) {

    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    ErrorResponse body =
        new ErrorResponse(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            ex.getClass().getSimpleName(),
            "Erro interno: " + ex.getMessage(),
            request.getRequestURI());
    return ResponseEntity.status(status).body(body);
  }

  /**
   * Trata exceções do tipo {@link ResponseStatusException}.
   *
   * <p>Retorna uma resposta com o status HTTP NOT_FOUND e uma mensagem de erro indicando que o
   * recurso não foi encontrado.
   *
   * @param ex a exceção {@link ResponseStatusException} capturada
   * @param request o objeto {@link HttpServletRequest} da requisição atual
   * @return um {@link ResponseEntity} contendo o {@link ErrorResponse} com os detalhes do erro
   */
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponse> handleResponseStatusException(
      ResponseStatusException ex, HttpServletRequest request) {

    HttpStatus status = HttpStatus.NOT_FOUND;
    ErrorResponse body =
        new ErrorResponse(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            ex.getClass().getSimpleName(),
            "Erro interno: " + ex.getMessage(),
            request.getRequestURI());
    return ResponseEntity.status(status).body(body);
  }
}
