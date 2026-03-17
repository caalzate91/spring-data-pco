# ADR-003: Base de Datos H2 File-based

| Campo       | Valor                      |
|-------------|----------------------------|
| **Estado**  | Aceptado                   |
| **Fecha**   | 2026-02-25                 |
| **Autores** | Equipo de Arquitectura     |

---

## Contexto

Para este proof-of-concept se requiere una base de datos embebida que no necesite instalación externa, sea accesible mediante consola web y persista los datos entre reinicios de la aplicación durante el desarrollo.

## Decisión

Usar **H2 en modo file-based** (`jdbc:h2:file:./testdb`) en lugar de in-memory. Esto garantiza que los datos sobrevivan reinicios del servidor mientras se prueba localmente. El esquema es gestionado automáticamente por Hibernate (`ddl-auto=update`).

## Consecuencias

- El archivo `testdb.mv.db` vive en la raíz del proyecto y está en `.gitignore`.
- La H2 Console está habilitada en `/h2-console` (solo útil en desarrollo).
- Si se migra a PostgreSQL/MySQL, solo se necesita cambiar `spring.datasource.*` y el driver en `build.gradle`.

---

## C4 Nivel 3 — Componentes de Persistencia

Muestra cómo las entidades del dominio se mapean a tablas en H2.

```mermaid
C4Component
    title Componentes de Persistencia — Capa de Datos

    Container_Boundary(api, "Banking API") {
        Component(usuarioSvc, "UsuarioService", "Spring Service", "Orquesta operaciones de usuario.")
        Component(cuentaSvc, "CuentaBancariaService", "Spring Service", "Orquesta operaciones de cuenta bancaria.")

        Component(usuarioRepo, "UsuarioRepository", "JpaRepository<Usuario, Long>", "findByCorreo(String correo) — único método personalizado.")
        Component(cuentaRepo, "CuentaBancariaRepository", "JpaRepository<CuentaBancaria, Long>", "Solo métodos estándar de JpaRepository.")

        Component(usuarioEntity, "Usuario", "JPA Entity (@Table: Usuarios)", "Campos: idUsuario, correo, nombre, password, roles (Set<String> → usuario_roles).")
        Component(cuentaEntity, "CuentaBancaria", "JPA Entity (@Table: CuentaBancaria)", "Campos: idCuenta, numero, saldo, tipoCuenta, usuario (FK → idUsuario).")
    }

    ContainerDb(db, "H2 Database", "File-based (testdb.mv.db)", "Tablas: Usuarios, CuentaBancaria, usuario_roles.")

    Rel(usuarioSvc, usuarioRepo, "findAll, findById, save, deleteById")
    Rel(cuentaSvc, cuentaRepo, "findAll, findById, save, deleteById")
    Rel(usuarioRepo, usuarioEntity, "mapea a")
    Rel(cuentaRepo, cuentaEntity, "mapea a")
    Rel(usuarioEntity, db, "Tabla: Usuarios")
    Rel(cuentaEntity, db, "Tabla: CuentaBancaria\n(FK: idUsuario → Usuarios)")
```

---

## Modelo de Datos

```mermaid
erDiagram
    USUARIOS {
        BIGINT idUsuario PK
        VARCHAR correo UK "NOT NULL"
        VARCHAR nombre "NOT NULL"
        VARCHAR password "NOT NULL"
    }

    USUARIO_ROLES {
        BIGINT usuario_id FK
        VARCHAR rol
    }

    CUENTABANCARIA {
        BIGINT idCuenta PK
        VARCHAR numero UK "NOT NULL"
        DOUBLE saldo "NOT NULL"
        VARCHAR tipoCuenta
        BIGINT idUsuario FK
    }

    USUARIOS ||--o{ USUARIO_ROLES : "tiene"
    USUARIOS ||--o{ CUENTABANCARIA : "posee"
```
