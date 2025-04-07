package com.graduationparty.authservice.adapter.out.response;

import java.util.List;

/**
 * Representa uma página de resultados paginados.
 *
 * @param <T> o tipo dos elementos contidos na página
 */
public class Page<T> {
  private final List<T> content;
  private final int page;
  private final int size;
  private final long totalElements;
  private final int totalPages;

  /**
   * Constrói uma instância de {@code Page} com o conteúdo e informações de paginação.
   *
   * @param content a lista de elementos que compõem o conteúdo da página
   * @param page o número da página atual (baseado em zero ou um, conforme a convenção adotada)
   * @param size o número de elementos por página
   * @param totalElements o número total de elementos disponíveis
   */
  public Page(List<T> content, int page, int size, long totalElements) {
    this.content = content;
    this.page = page;
    this.size = size;
    this.totalElements = totalElements;
    this.totalPages = (int) Math.ceil((double) totalElements / size);
  }

  /**
   * Retorna o número da página atual.
   *
   * @return o número da página atual
   */
  public int getPage() {
    return page;
  }

  /**
   * Retorna o tamanho da página, ou seja, o número de elementos por página.
   *
   * @return o tamanho da página
   */
  public int getSize() {
    return size;
  }

  /**
   * Retorna o total de páginas calculado com base no número total de elementos e no tamanho da
   * página.
   *
   * @return o total de páginas
   */
  public int getTotalPages() {
    return totalPages;
  }

  /**
   * Retorna o conteúdo da página.
   *
   * @return uma lista contendo os elementos da página
   */
  public List<T> getContent() {
    return content;
  }

  /**
   * Retorna o número total de elementos disponíveis.
   *
   * @return o total de elementos
   */
  public long getTotalElements() {
    return totalElements;
  }
}
