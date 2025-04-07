package com.graduationparty.authservice.domain.model;

/**
 * Representa um token de acesso utilizado para autenticação e autorização.
 *
 * @param accessToken o token de acesso em formato de {@code String}
 */
public record AccessToken(String accessToken) {}
