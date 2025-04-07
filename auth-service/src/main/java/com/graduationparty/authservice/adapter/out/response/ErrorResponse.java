package com.graduationparty.authservice.adapter.out.response;

import java.time.Instant;

/**
 * Representa a estrutura de resposta de erro utilizada para retornar informações detalhadas sobre
 * uma exceção.
 *
 * @param timestamp o instante em que o erro ocorreu
 * @param status o código de status HTTP associado ao erro
 * @param error a razão do porque o erro ocorreu
 * @param exception o nome da exceção que ocorreu
 * @param message a mensagem detalhada do erro
 * @param path o caminho da requisição onde o erro foi gerado
 */
public record ErrorResponse(
    Instant timestamp, int status, String error, String exception, String message, String path) {}
