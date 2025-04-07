package com.graduationparty.authservice.adapter.out.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Configuração AWS para criação do bean S3Client.
 *
 * <p>Esta classe configura e fornece uma instância do {@link S3Client} utilizando as credenciais e
 * a região especificadas nas propriedades do aplicativo. A configuração é ativada para todos os
 * perfis, exceto o perfil "test".
 */
@Configuration
public class AWSConfig {

  @Value("${aws.s3.region}")
  private String region;

  @Value("${aws.s3.access-key}")
  private String accessKey;

  @Value("${aws.s3.secret-key}")
  private String secretKey;

  /**
   * Cria e configura um bean {@link S3Client} para interagir com o serviço AWS S3.
   *
   * <p>O método constrói as credenciais básicas a partir do access key e secret key, define a
   * região apropriada e retorna uma instância do cliente S3 para ser utilizada na aplicação.
   *
   * @return uma instância configurada do {@link S3Client}
   */
  @Bean
  public S3Client s3Client() {
    return S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(
            StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .build();
  }
}
