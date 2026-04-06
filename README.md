# Coupon API

API Spring Boot para o desafio técnico de cupons, implementada com foco em avaliação de nível Pleno.

## Stack

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Bean Validation
- H2
- OpenAPI / Swagger
- JUnit 5 / Mockito
- Docker / Docker Compose
- Jacoco

## Estrutura do projeto

```text
src/main/java/com/example/couponapi
├── config
├── controller
├── domain
├── dto
├── exception
├── repository
└── service
```

## Decisões técnicas

### 1. Regra de negócio no domínio
A entidade `Coupon` encapsula as regras centrais:
- sanitização do código;
- validação do tamanho final do código;
- validação do desconto mínimo;
- validação da data de expiração;
- soft delete;
- bloqueio de deleção duplicada.

### 2. Soft delete com `deleted` como fonte de verdade

O controle de exclusão lógica é feito exclusivamente pelo campo `deleted`.

O campo `status` continua existindo no contrato da API, mas é derivado no domínio:
- `deleted = false` -> `ACTIVE`
- `deleted = true` -> `DELETED`

### 3. Código sanitizado antes da validação final
A entrada aceita caracteres especiais, mas a persistência e a resposta retornam o valor já limpo.
Após sanitizar, o código precisa ter exatamente 6 caracteres alfanuméricos.

### 4. Endpoint de exclusão
Foi adotado:

```http
DELETE /coupon/{id}
```

Resposta:
- `204 No Content` em sucesso.

## Regras implementadas

### Create
- `code`, `description`, `discountValue` e `expirationDate` são obrigatórios;
- `code` aceita caracteres especiais na entrada, mas é sanitizado antes de salvar e responder;
- o código final precisa ter exatamente 6 caracteres alfanuméricos;
- `discountValue` deve ser maior ou igual a `0.5`;
- `expirationDate` não pode estar no passado;
- o cupom pode ser criado já publicado.

### Delete
- exclusão lógica;
- não remove fisicamente do banco;
- não permite excluir um cupom já deletado.

## Como executar localmente

### Pré-requisitos
- Java 17
- Gradle 8.14+ ou Gradle Wrapper

### Rodando a aplicação

```bash
./gradlew bootRun
```

ou

```bash
gradle bootRun
```

A aplicação sobe em:

```text
http://localhost:8080
```

## Swagger

Interface:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

## H2 Console

```text
http://localhost:8080/h2-console
```

Credenciais padrão:
- JDBC URL: `jdbc:h2:mem:coupondb`
- User: `sa`
- Password: vazio

## Endpoints

### Criar cupom

```http
POST /coupon
Content-Type: application/json
```

Exemplo de request:

```json
{
  "code": "AB-12@CD",
  "description": "Black Friday discount",
  "discountValue": 10.50,
  "expirationDate": "2026-12-31",
  "published": true
}
```

Exemplo de response `201`:

```json
{
  "id": "4f2d1d48-3578-4ea2-9a95-8ff706d95e22",
  "code": "AB12CD",
  "description": "Black Friday discount",
  "discountValue": 10.50,
  "expirationDate": "2026-12-31",
  "status": "ACTIVE",
  "published": true
}
```

### Deletar cupom

```http
DELETE /coupon/{id}
```

Resposta:
- `204 No Content`

## Testes

### Executar testes

```bash
./gradlew test
```

### Cobertura

```bash
./gradlew check
```

Relatório HTML do Jacoco:

```text
build/reports/jacoco/test/html/index.html
```

## Docker

### Build da imagem

```bash
./gradlew build
docker build -t coupon-api .
```

### Subir com Docker Compose

```bash
docker compose up --build
```

## Observação sobre Gradle Wrapper

Este pacote inclui `build.gradle` e `settings.gradle`. Caso você queira rodar diretamente com wrapper no seu ambiente e ele ainda não exista, gere-o com:

```bash
gradle wrapper
```

