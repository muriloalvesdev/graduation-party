# Auth-Service

Auth-Service é o microserviço responsável pela autenticação, criação e gerenciamento de usuários, integrado ao Keycloak para gerenciamento de identidade e acesso. Este serviço faz parte do sistema de gerenciamento de festas de formatura, implementado com base em arquitetura hexagonal, garantindo a separação entre lógica de negócio e infraestrutura.

## Descrição do Projeto

O Auth-Service gerencia as seguintes funcionalidades:

- Criação e autenticação de usuários.
- Integração com Keycloak para gerenciamento de identidade.
- Armazenamento das fotos dos usuários via AWS S3.
- Execução de testes unitários e de integração com cobertura de código com jacoco (Sonar).
- Documentação interativa da API disponível via Swagger/OpenAPI.

## Tecnologias Utilizadas

- **Java 17**
- **Spring Boot**
- **Spring Security e OAuth2 Resource Server**
- **Keycloak (Admin Client)**
- **AWS S3 SDK**
- **Resilience4j** para Circuit Breaker
- **JUnit** para testes unitários e de integração
- **Maven** como ferramenta de build
- **Swagger/OpenAPI** (documentação da API)
- **GitHub Actions** para CI/CD
- **SonarQube e JaCoCo** para análise de qualidade de código e cobertura de testes

## Endpoints Principais

- **Cadastro de Usuário**: `POST /api/v1/auth/signup`
- **Login**: `POST /api/v1/auth/login`
- **Swagger UI**: [http://localhost:8081/api/v1/swagger-ui/index.html#/](http://localhost:8081/api/v1/swagger-ui/index.html#/)

## Como Executar

1. Certifique-se de ter o **Java 17** instalado.
2. Clone o repositório:
   ```bash
   git clone <URL_DO_REPOSITORIO>
3. Acesse o diretório do projeto:
   ```bash
   $ cd auth-service
   ```
4. Execute o projeto usando o script `start-services.sh`.
- Dê permissão ao arquivo `chmod u+x start-services.sh`
- Execute-o:
   ```bash
   $ ./start-services.sh
   ```
Esse comando vai:
1. Subir o keycloak.
2. Compilar e subir o projeto auth-service.

### Para ver os logs:
```bash
$ docker logs -f auth-service
```
ou
```bash
docker logs -f keycloak
```