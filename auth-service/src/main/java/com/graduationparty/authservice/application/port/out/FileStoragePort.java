package com.graduationparty.authservice.application.port.out;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

/** Interface que define as operações de armazenamento de arquivos. */
public interface FileStoragePort {

  /**
   * Realiza o upload de um arquivo para o sistema de armazenamento.
   *
   * @param file o arquivo a ser enviado
   * @param keyPrefix o prefixo de chave utilizado para organizar o arquivo no armazenamento
   * @return uma String que representa a URL do arquivo
   * @throws IOException se ocorrer um erro de I/O durante o upload do arquivo
   */
  String upload(MultipartFile file, String keyPrefix) throws IOException;
}
