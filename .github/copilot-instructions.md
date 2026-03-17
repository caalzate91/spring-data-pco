# Copilot Instructions

## Build & Test Commands

```bash
# Build
./gradlew build

# Run application
./gradlew bootRun

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.banking.accounting.AccountingApplicationTests"

# Run a single test method
./gradlew test --tests "com.banking.accounting.AccountingApplicationTests.contextLoads"
```

## Architecture

This is a Spring Boot 3.4.1 banking proof-of-concept with a standard layered architecture. Base package: `com.banking.accounting`.

**Layer flow:** `Controller → Service → Repository → H2 (file-based)`

### Domain Model
- `Usuario` — system user with email (`correo`), name (`nombre`), BCrypt-hashed `password`, and a `Set<String> roles` stored in the `usuario_roles` join table.
- `CuentaBancaria` — bank account with `numero` (unique), `saldo` (Double), `tipoCuenta`, and a `@ManyToOne` to `Usuario` (Java field named `usuario`, DB column `idUsuario`).

### API Endpoints

| Method | Path | Auth | Notes |
|--------|------|------|-------|
| POST | `/api/auth/register` | Public | `@RequestBody Usuario` — hashes password before saving |
| POST | `/api/auth/login` | Public | `@RequestParam correo, password` — returns JWT string |
| GET | `/api/usuarios` | Authenticated + ADMIN role | `@PreAuthorize("hasAuthority('ADMIN')")` |
| GET | `/api/usuarios/{id}` | Authenticated | |
| GET | `/api/cuentas` | Authenticated | |
| GET | `/api/cuentas/{id}` | Authenticated | |
| POST | `/api/cuentas` | Authenticated | |
| PUT | `/api/cuentas/{id}` | Authenticated | |
| DELETE | `/api/cuentas/{id}` | Authenticated | |

### Security
- `SecurityConfig` disables CSRF, Basic auth, and form login.
- Public endpoints: `/api/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `/h2-console/**`.
- All other endpoints require authentication (`.anyRequest().authenticated()`).
- `JwtUtil` generates/validates HS512 JWT tokens using `jwt.secret` and `jwt.expiration` from `application.properties`.
- **Note:** No JWT filter is currently registered in the `SecurityFilterChain` — token validation exists in `JwtUtil` but is not enforced by Spring Security.
- **Note:** `@EnableMethodSecurity` is absent from `SecurityConfig`, so `@PreAuthorize` on `GET /api/usuarios` is not currently enforced at runtime.

### Key Design Decisions
- `AuthController` directly injects `UsuarioRepository` (bypasses `UsuarioService`) — intentional shortcut for auth flows.
- Password hashing only happens in `AuthController.register()`. `UsuarioService.save()` does **not** hash — never call it with a plaintext password.
- No DTOs: entity classes are used directly as request/response bodies.
- Tests use `@SpringBootTest` (full context load against H2).
- `UsuarioRepository` has one custom query method: `findByCorreo(String correo)`.

## Conventions

### Naming
- Domain entity fields and table names use **Spanish** (e.g., `correo`, `nombre`, `saldo`, `tipoCuenta`, `idCuenta`, `idUsuario`).
- Tables named in Spanish: `CuentaBancaria`, `Usuarios`, `usuario_roles`.
- Technical/infrastructure code uses **English**.

### Entities
All JPA entities use `@Data`, `@NoArgsConstructor`, and `@AllArgsConstructor` from Lombok. No `@Builder` — use the all-args constructor when constructing in tests.

### Dependency Injection
Constructor injection only — no `@Autowired` field injection.

### API Documentation
All controller methods must have `@Operation(summary = "...")` and the controller class must have `@Tag(name = "...", description = "...")`. Swagger UI is available at `/swagger-ui.html`.

### Configuration
- Runtime DB is H2 file-based (`testdb.mv.db` in project root); H2 console at `/h2-console`.
- JWT config: `jwt.secret` and `jwt.expiration` (ms) in `application.properties`.
- JPA DDL: `spring.jpa.hibernate.ddl-auto=update` — schema is auto-managed.
