# ADR-002: Autenticación Stateless con JWT

| Campo       | Valor                      |
|-------------|----------------------------|
| **Estado**  | Aceptado                   |
| **Fecha**   | 2026-02-25                 |
| **Autores** | Equipo de Arquitectura     |

---

## Contexto

El sistema necesita autenticación sin estado (stateless) que no requiera sesiones en el servidor. Se optó por JWT (JSON Web Tokens) firmados con HS512, configurables vía `application.properties`.

## Decisión

- `JwtUtil` genera y valida tokens usando `io.jsonwebtoken:jjwt:0.9.1`.
- El token se firma con `jwt.secret` y expira en `jwt.expiration` milisegundos.
- `AuthController` bypasea `UsuarioService` e inyecta `UsuarioRepository` directamente para mantener el flujo de auth autocontenido.
- La contraseña se hashea con BCrypt únicamente en `AuthController.register()`. `UsuarioService.save()` **no** hashea.

## Estado actual (deuda técnica conocida)

| Elemento | Estado |
|----------|--------|
| `JwtUtil.generateToken()` | ✅ Funcional |
| `JwtUtil.validateToken()` | ✅ Funcional |
| Filtro JWT en `SecurityFilterChain` | ❌ No implementado |
| `@EnableMethodSecurity` en `SecurityConfig` | ❌ Ausente — `@PreAuthorize` no se aplica en runtime |

## Consecuencias

- Los endpoints marcados como "autenticados" en `SecurityConfig` no validan el JWT porque no hay filtro registrado.
- `@PreAuthorize("hasAuthority('ADMIN')")` en `UsuarioController` no tiene efecto hasta que se añada `@EnableMethodSecurity`.

---

## C4 Nivel 3 — Componentes de Seguridad

Flujo de autenticación y autorización dentro del sistema.

```mermaid
C4Component
    title Componentes de Seguridad — Flujo JWT

    Person(client, "Cliente HTTP", "Consume la API REST")

    Container_Boundary(api, "Banking API") {
        Component(secConfig, "SecurityConfig", "Spring Security Config", "Rutas públicas: /api/auth/**, /swagger-ui/**, /h2-console/**. Resto: authenticated(). Sin filtro JWT registrado.")
        Component(authCtrl, "AuthController", "Spring MVC Controller", "POST /api/auth/register y /api/auth/login")
        Component(jwtUtil, "JwtUtil", "Spring Service", "generateToken(correo): HS512 JWT. validateToken(token, email): verifica firma y expiración.")
        Component(pwdEncoder, "PasswordEncoder", "BCryptPasswordEncoder", "Hashea contraseñas en registro. Verifica en login.")
        Component(usuarioRepo, "UsuarioRepository", "Spring Data JPA", "findByCorreo(String) para lookup en login.")
    }

    Rel(client, secConfig, "Toda petición pasa por", "HTTP")
    Rel(secConfig, authCtrl, "Permite sin auth", "/api/auth/**")
    Rel(client, authCtrl, "POST /register o /login", "JSON / form params")
    Rel(authCtrl, pwdEncoder, "encode() en registro\nmatches() en login")
    Rel(authCtrl, usuarioRepo, "findByCorreo() / save()")
    Rel(authCtrl, jwtUtil, "generateToken(correo) → retorna JWT al cliente")
```

---

## Flujo de Autenticación (Secuencia)

```mermaid
sequenceDiagram
    participant C as Cliente
    participant AC as AuthController
    participant UR as UsuarioRepository
    participant JU as JwtUtil
    participant DB as H2 Database

    C->>AC: POST /api/auth/login?correo=&password=
    AC->>UR: findByCorreo(correo)
    UR->>DB: SELECT * FROM Usuarios WHERE correo = ?
    DB-->>UR: Usuario entity
    UR-->>AC: Usuario
    AC->>AC: passwordEncoder.matches(raw, hashed)
    alt Credenciales válidas
        AC->>JU: generateToken(correo)
        JU-->>AC: JWT string (HS512, exp: jwt.expiration ms)
        AC-->>C: 200 OK — JWT token
    else Credenciales inválidas
        AC-->>C: 401 — "Credenciales inválidas"
    end
```
