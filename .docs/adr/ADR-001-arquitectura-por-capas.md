# ADR-001: Arquitectura por Capas (Layered Architecture)

| Campo       | Valor                      |
|-------------|----------------------------|
| **Estado**  | Aceptado                   |
| **Fecha**   | 2026-02-25                 |
| **Autores** | Equipo de Arquitectura     |

---

## Contexto

El sistema bancario necesita una estructura organizada que separe responsabilidades, facilite el mantenimiento y permita escalar cada capa de forma independiente. Se requería un patrón que fuera familiar para equipos Java/Spring y que permitiera añadir nuevas entidades (cuentas, transacciones, usuarios) sin romper el resto del sistema.

## Decisión

Adoptar una **arquitectura por capas estricta** con el flujo:

```
Controller → Service → Repository → H2 Database
```

Cada capa solo puede comunicarse con la capa inmediatamente inferior. Los controladores nunca acceden a repositorios directamente —salvo la excepción documentada en `AuthController` (ver ADR-002).

## Consecuencias

- **Positivas:** Separación de responsabilidades clara; cada capa es independientemente testeable; convención conocida por cualquier desarrollador Spring.
- **Negativas:** Puede generar clases de servicio delgadas (thin services) cuando la lógica de negocio es simple.

---

## C4 Nivel 1 — Diagrama de Contexto del Sistema

Muestra el sistema bancario en relación con sus usuarios y sistemas externos.

```mermaid
C4Context
    title Diagrama de Contexto — Sistema Bancario

    Person(usuarioFinal, "Usuario Final", "Persona que consulta y gestiona sus cuentas bancarias")
    Person(admin, "Administrador", "Gestiona usuarios del sistema")

    System(bankingSystem, "Banking Accounting System", "Aplicación Spring Boot que expone una API REST para gestión de usuarios y cuentas bancarias")

    Rel(usuarioFinal, bankingSystem, "Consulta saldo, crea cuentas", "HTTPS / JSON")
    Rel(admin, bankingSystem, "Gestiona usuarios", "HTTPS / JSON")
```

---

## C4 Nivel 2 — Diagrama de Contenedores

Muestra los bloques técnicos de alto nivel que componen el sistema.

```mermaid
C4Container
    title Diagrama de Contenedores — Sistema Bancario

    Person(usuarioFinal, "Usuario Final", "Accede vía cliente HTTP")
    Person(admin, "Administrador", "Accede vía cliente HTTP")

    System_Boundary(sys, "Banking Accounting System") {
        Container(api, "Banking API", "Spring Boot 3.4.1 / Java 17", "Expone endpoints REST. Gestiona autenticación, usuarios y cuentas bancarias.")
        ContainerDb(db, "H2 Database", "H2 File-based (testdb.mv.db)", "Persiste entidades: Usuarios, CuentaBancaria, usuario_roles.")
    }

    Rel(usuarioFinal, api, "POST /api/auth/login, GET /api/cuentas", "HTTPS / JSON")
    Rel(admin, api, "GET /api/usuarios", "HTTPS / JSON")
    Rel(api, db, "Lee y escribe entidades JPA", "JPA / Hibernate / SQL")
```

---

## C4 Nivel 3 — Diagrama de Componentes (Vista General)

Muestra los componentes principales dentro del contenedor API y sus dependencias.

```mermaid
C4Component
    title Diagrama de Componentes — Banking API

    Container_Boundary(api, "Banking API (Spring Boot)") {

        Component(authCtrl, "AuthController", "Spring MVC Controller", "Registro y login. Inyecta UsuarioRepository directamente.")
        Component(usuarioCtrl, "UsuarioController", "Spring MVC Controller", "CRUD de usuarios. Protegido por roles.")
        Component(cuentaCtrl, "CuentaBancariaController", "Spring MVC Controller", "CRUD de cuentas bancarias.")

        Component(usuarioSvc, "UsuarioService", "Spring Service", "Lógica de negocio de usuarios.")
        Component(cuentaSvc, "CuentaBancariaService", "Spring Service", "Lógica de negocio de cuentas.")
        Component(jwtUtil, "JwtUtil", "Spring Service", "Genera y valida tokens JWT HS512.")

        Component(usuarioRepo, "UsuarioRepository", "Spring Data JPA", "CRUD + findByCorreo(String).")
        Component(cuentaRepo, "CuentaBancariaRepository", "Spring Data JPA", "CRUD estándar.")

        Component(secConfig, "SecurityConfig", "Spring Security", "Define rutas públicas/protegidas. Sin filtro JWT activo.")
    }

    ContainerDb(db, "H2 Database", "File-based", "testdb.mv.db")

    Rel(authCtrl, usuarioRepo, "findByCorreo / save", "directamente (sin Service)")
    Rel(authCtrl, jwtUtil, "generateToken")
    Rel(usuarioCtrl, usuarioSvc, "delega")
    Rel(cuentaCtrl, cuentaSvc, "delega")
    Rel(usuarioSvc, usuarioRepo, "CRUD")
    Rel(cuentaSvc, cuentaRepo, "CRUD")
    Rel(usuarioRepo, db, "SQL via JPA")
    Rel(cuentaRepo, db, "SQL via JPA")
```
