package com.graduationparty.authservice.adapter.out.s3;

import com.graduationparty.authservice.application.port.out.FileStoragePort;
import java.io.IOException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Implementação da interface {@link FileStoragePort}.
 *
 * <p>Esta classe é responsável por realizar o upload do profilePhoto do usuário para um bucket do
 * S3, utilizando o cliente AWS S3.
 */
@Component
public class S3StorageAdapter implements FileStoragePort {

  private static final String URL_FORMAT = "https://%s.s3.%s.amazonaws.com/%s";

  private final S3Client s3Client;

  private final String bucket;
  private final String region;

  /**
   * Construtor do S3StorageAdapter.
   *
   * @param s3Client o cliente AWS S3 para realizar operações de armazenamento
   * @param bucket o nome do bucket onde os arquivos serão armazenados
   * @param region a região do bucket do AWS S3
   */
  public S3StorageAdapter(
      S3Client s3Client,
      @Value("${aws.s3.bucket}") String bucket,
      @Value("${aws.s3.region}") String region) {
    this.s3Client = s3Client;
    this.bucket = bucket;
    this.region = region;
  }

  /**
   * Realiza o upload de um arquivo para o AWS S3.
   *
   * <p>O método gera uma chave única para o arquivo utilizando um prefixo informado e um UUID, e
   * realiza o upload do arquivo para o bucket configurado. Ao final, retorna a URL do arquivo
   * armazenado.
   *
   * @param file o arquivo a ser enviado
   * @param keyPrefix o prefixo que será usado para a chave do arquivo no bucket
   * @return a URL que aponta para o arquivo armazenado no S3
   * @throws IOException se ocorrer um erro de I/O durante o processo de upload
   */
  @Override
  public String upload(MultipartFile file, String keyPrefix) throws IOException {
    String original = file.getOriginalFilename();
    String ext =
        (original != null && original.contains("."))
            ? original.substring(original.lastIndexOf('.'))
            : "";
    String key = keyPrefix + "/" + UUID.randomUUID() + ext;

    PutObjectRequest req =
        PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(file.getContentType())
            .build();

    s3Client.putObject(req, RequestBody.fromBytes(file.getBytes()));

    return String.format(URL_FORMAT, bucket, region, key);
  }
}
