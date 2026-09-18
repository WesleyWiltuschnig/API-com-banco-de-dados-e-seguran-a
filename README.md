# Agência de Viagens - API RESTful (Desafio 2)

API REST para gerenciamento de destinos turísticos, evoluída da versão em memória para persistência em **PostgreSQL** com **Spring Data JPA** e controle de acesso com **Spring Security 6** (autenticação HTTP Basic + BCrypt, autorização por perfil ADMIN/USER).

## Stack

| Item | Versão |
|---|---|
| Java | 17+ |
| Spring Boot | 3.3.4 |
| Spring Data JPA / Hibernate | 6.x (Jakarta Persistence) |
| Spring Security | 6.x |
| PostgreSQL | 14+ |
| Maven | 3.9+ |

## Arquitetura em camadas

```
Controller  ->  Service  ->  Repository  ->  Entity  ->  PostgreSQL
   (DTO)       (regra)      (Spring Data)     (JPA)
```

```
src/main/java/br/senai/agenciaviagens
├── AgenciaViagensApplication.java
├── config
│   ├── SecurityConfig.java        # SecurityFilterChain, BCrypt, DaoAuthenticationProvider
│   └── DataInitializer.java       # carga inicial de perfis, usuários e destinos
├── controller
│   ├── AuthController.java        # GET /api/auth/me
│   ├── DestinoController.java     # CRUD + avaliação de destinos
│   └── UsuarioController.java     # gestão de usuários (ADMIN)
├── dto                            # records de request/response + erro padronizado
├── entity                         # Destino, Usuario, Perfil
├── exception                      # exceções de domínio + @RestControllerAdvice
├── repository                     # interfaces JpaRepository
├── security
│   ├── UsuarioAutenticado.java    # implementação de UserDetails
│   └── UsuarioDetailsService.java # UserDetailsService lendo do PostgreSQL
└── service                        # DestinoService, UsuarioService
```

## Pré-requisitos

- JDK 17 ou superior (`java -version`)
- Maven 3.9+ — opcional, o wrapper `./mvnw` já está incluído
- PostgreSQL 14+ em execução
- Cliente HTTP para testes (cURL, Postman ou Insomnia)

## 1. Configuração do banco de dados

Crie a base (script em `database/01_create_database.sql`):

```bash
psql -U postgres -c "CREATE DATABASE agencia_viagens WITH ENCODING 'UTF8' TEMPLATE template0;"
```

As tabelas (`destinos`, `usuarios`, `perfis`, `usuario_perfis`) são criadas automaticamente pelo Hibernate na primeira execução, via `spring.jpa.hibernate.ddl-auto=update`. Em seguida, `src/main/resources/data.sql` insere perfis, usuários de teste e destinos de exemplo.

A ordem correta depende de `spring.jpa.defer-datasource-initialization=true`: sem ela, o `data.sql` roda antes da criação das tabelas e falha com `relation does not exist`. O script é idempotente (todos os `INSERT` usam `WHERE NOT EXISTS`), então pode ser reexecutado a cada start.

Duas estratégias de carga inicial, mutuamente exclusivas:

| Estratégia | Configuração |
|---|---|
| SQL (padrão) | `spring.sql.init.mode=always` + `app.seed.enabled=false` |
| Programática (`DataInitializer`) | `spring.sql.init.mode=never` + `app.seed.enabled=true` |

Se quiser controlar o schema manualmente, use `database/03_schema_manual_opcional.sql` com `spring.jpa.hibernate.ddl-auto=none`.

## 2. Configuração da aplicação

`src/main/resources/application.properties` já vem pronto com valores padrão e suporte a variáveis de ambiente:

```properties
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:agencia_viagens}
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.orm.jdbc.bind=TRACE
```

Se as credenciais locais forem diferentes, altere `spring.datasource.username` / `password` ou exporte as variáveis:

```bash
export DB_USER=postgres
export DB_PASSWORD=suasenha
```

## 3. Execução

```bash
./mvnw clean install
./mvnw spring-boot:run
```

O Maven Wrapper está versionado no repositório: não é necessário ter Maven instalado, apenas o JDK 17.

Ou pelo JAR:

```bash
./mvnw clean package
java -jar target/agencia-viagens-api-2.0.0.jar
```

A API sobe em `http://localhost:8080`.

Ao final do start, a base já contém os perfis `ROLE_ADMIN` e `ROLE_USER`, os dois usuários de teste e três destinos de exemplo.

## 4. Usuários e perfis de teste

| Username | Senha | Perfis | Permissões |
|---|---|---|---|
| `admin` | `admin123` | ROLE_ADMIN, ROLE_USER | Todas as operações, inclusive POST/PUT/PATCH/DELETE e gestão de usuários |
| `user` | `user123` | ROLE_USER | Consulta pública + avaliação de destinos |

Hashes BCrypt (custo 10) já calculados, para inserção manual via SQL:

```
admin123 -> $2a$10$mCnIXH0Xt0tD.dOE2dJCt.GG/YKQ772u18cv6vETVSHsifGiGQIeG
user123  -> $2a$10$6SNRSq5cu.SIz85jEKkWFeldHUI/V/v5WUCvH2tv7F9PTxxzckYN6
```

```sql
INSERT INTO usuarios (nome, username, email, senha, ativo, criado_em) VALUES
('Administrador', 'admin', 'admin@agencia.com',
 '$2a$10$mCnIXH0Xt0tD.dOE2dJCt.GG/YKQ772u18cv6vETVSHsifGiGQIeG', true, NOW()),
('Usuario Padrao', 'user', 'user@agencia.com',
 '$2a$10$6SNRSq5cu.SIz85jEKkWFeldHUI/V/v5WUCvH2tv7F9PTxxzckYN6', true, NOW());
```

Autenticação: **HTTP Basic**. Em cURL, use `-u username:senha`.

## 5. Endpoints e regras de acesso

| Método | Rota | Acesso |
|---|---|---|
| GET | `/api/destinos` | Público |
| GET | `/api/destinos/{id}` | Público |
| GET | `/api/destinos/pais/{pais}` | Público |
| GET | `/api/destinos/melhores-avaliados` | Público |
| POST | `/api/destinos` | ADMIN |
| PUT | `/api/destinos/{id}` | ADMIN |
| PATCH | `/api/destinos/{id}/status?ativo=` | ADMIN |
| DELETE | `/api/destinos/{id}` | ADMIN |
| POST | `/api/destinos/{id}/avaliacoes` | USER ou ADMIN (autenticado) |
| GET | `/api/auth/me` | Autenticado |
| GET | `/api/usuarios` | ADMIN |
| GET | `/api/usuarios/{id}` | ADMIN |
| POST | `/api/usuarios` | ADMIN |
| PATCH | `/api/usuarios/{id}/status?ativo=` | ADMIN |
| DELETE | `/api/usuarios/{id}` | ADMIN |

Códigos de retorno: `200` OK, `201` Created, `204` No Content, `400` validação, `401` sem credenciais/credenciais inválidas, `403` perfil sem permissão, `404` recurso inexistente, `409` conflito de regra de negócio.

## 6. Exemplos de requisições

### Consulta pública (sem autenticação)

```bash
curl -X GET "http://localhost:8080/api/destinos?page=0&size=10"
curl -X GET "http://localhost:8080/api/destinos/1"
curl -X GET "http://localhost:8080/api/destinos?nome=lisboa"
curl -X GET "http://localhost:8080/api/destinos/pais/Brasil"
curl -X GET "http://localhost:8080/api/destinos/melhores-avaliados?notaMinima=4.0"
```

### Validar credenciais

```bash
curl -X GET "http://localhost:8080/api/auth/me" -u admin:admin123
```

### Cadastrar destino (ADMIN)

```bash
curl -X POST "http://localhost:8080/api/destinos" \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
        "nome": "Gramado",
        "localizacao": "Serra Gaucha, Rio Grande do Sul",
        "pais": "Brasil",
        "descricao": "Destino de serra com arquitetura europeia, gastronomia e eventos de inverno.",
        "precoBase": 2790.00
      }'
```

Resposta `201 Created`:

```json
{
  "id": 4,
  "nome": "Gramado",
  "localizacao": "Serra Gaucha, Rio Grande do Sul",
  "pais": "Brasil",
  "descricao": "Destino de serra com arquitetura europeia, gastronomia e eventos de inverno.",
  "precoBase": 2790.00,
  "avaliacao": 0.0,
  "totalAvaliacoes": 0,
  "ativo": true,
  "criadoEm": "2026-09-18T14:22:10.512",
  "atualizadoEm": "2026-09-18T14:22:10.512"
}
```

### Atualizar destino (ADMIN)

```bash
curl -X PUT "http://localhost:8080/api/destinos/4" \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
        "nome": "Gramado e Canela",
        "localizacao": "Serra Gaucha, Rio Grande do Sul",
        "pais": "Brasil",
        "descricao": "Roteiro combinado de Gramado e Canela com passeios guiados.",
        "precoBase": 3100.00
      }'
```

### Ativar/inativar destino (ADMIN)

```bash
curl -X PATCH "http://localhost:8080/api/destinos/4/status?ativo=false" -u admin:admin123
```

### Avaliar destino (USER ou ADMIN)

```bash
curl -X POST "http://localhost:8080/api/destinos/1/avaliacoes" \
  -u user:user123 \
  -H "Content-Type: application/json" \
  -d '{"nota": 5}'
```

A média (`avaliacao`) e o contador (`totalAvaliacoes`) são recalculados a cada nota registrada.

### Excluir destino (ADMIN)

```bash
curl -X DELETE "http://localhost:8080/api/destinos/4" -u admin:admin123
```

### Cadastrar usuário (ADMIN)

```bash
curl -X POST "http://localhost:8080/api/usuarios" \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
        "nome": "Maria Silva",
        "username": "maria",
        "email": "maria@agencia.com",
        "senha": "maria123",
        "perfis": ["USER"]
      }'
```

A senha é gravada apenas como hash BCrypt; nunca é retornada nas respostas.

### Testes de bloqueio esperados

```bash
# 401 - operação sensível sem credenciais
curl -i -X DELETE "http://localhost:8080/api/destinos/1"

# 403 - USER tentando operação de ADMIN
curl -i -X POST "http://localhost:8080/api/destinos" \
  -u user:user123 \
  -H "Content-Type: application/json" \
  -d '{"nome":"X","localizacao":"Y","pais":"Z","descricao":"Teste","precoBase":100.00}'

# 400 - validação de campos obrigatórios
curl -i -X POST "http://localhost:8080/api/destinos" \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{"nome":"","localizacao":"","pais":"","descricao":""}'
```

## 7. Modelo de dados

**destinos**: `id`, `nome`, `localizacao`, `pais`, `descricao`, `preco_base`, `avaliacao`, `total_avaliacoes`, `ativo`, `criado_em`, `atualizado_em`

**usuarios**: `id`, `nome`, `username` (único), `email` (único), `senha` (BCrypt), `ativo`, `criado_em`

**perfis**: `id`, `nome` (ROLE_ADMIN / ROLE_USER), `descricao`

**usuario_perfis**: `usuario_id`, `perfil_id` (relacionamento ManyToMany)

## 8. Segurança implementada

- `SecurityFilterChain` com regras por método HTTP e rota, sessão `STATELESS` e CSRF desabilitado (API sem estado).
- `UsuarioDetailsService` implementando `UserDetailsService`, buscando usuário e perfis no PostgreSQL via `UsuarioRepository`.
- `DaoAuthenticationProvider` com `BCryptPasswordEncoder` (custo 10) para validação de credenciais.
- `@EnableMethodSecurity` com `@PreAuthorize` nos controllers, reforçando as regras na camada de método.
- Usuários inativos (`ativo = false`) são bloqueados pelo `isEnabled()` do `UserDetails`.

## 9. Entrega

Repositório Git contendo código-fonte completo, scripts SQL em `database/`, `application.properties` configurado e este `README.md`.
