# Diagrama C4 de Componentes - Sistema Bancario

Este documento presenta el diagrama C4 de componentes del sistema bancario desarrollado con Spring Boot, mostrando los principales módulos del backend y sus interacciones.

## 🏗️ Diagrama de Componentes

```mermaid
C4Component
    title Component Diagram - Banking Accounting System

    Container_Boundary(api, "Banking API Application") {
        Component(authController, "AuthController", "Spring MVC Controller", "Maneja autenticación y registro de usuarios")
        Component(usuarioController, "UsuarioController", "Spring MVC Controller", "Gestiona operaciones CRUD de usuarios")
        Component(cuentaController, "CuentaBancariaController", "Spring MVC Controller", "Gestiona operaciones CRUD de cuentas bancarias")
        
        Component(jwtUtil, "JwtUtil", "JWT Service", "Genera y valida tokens JWT para autenticación")
        Component(usuarioService, "UsuarioService", "Business Logic", "Lógica de negocio para gestión de usuarios")
        Component(cuentaService, "CuentaBancariaService", "Business Logic", "Lógica de negocio para cuentas bancarias")
        
        Component(usuarioRepo, "UsuarioRepository", "Spring Data JPA", "Acceso a datos de usuarios con consultas personalizadas")
        Component(cuentaRepo, "CuentaBancariaRepository", "Spring Data JPA", "Acceso a datos de cuentas bancarias")
        
        Component(securityConfig, "SecurityConfig", "Spring Security", "Configuración de seguridad y filtros JWT")
        Component(swaggerConfig, "SwaggerConfig", "API Documentation", "Configuración de documentación OpenAPI")
    }

    ContainerDb(h2db, "H2 Database", "In-Memory Database", "Almacena usuarios, cuentas bancarias y relaciones")

    Container_Boundary(external, "External Systems") {
        Container(client, "Client Application", "Web/Mobile", "Aplicación cliente que consume la API")
    }

    %% Client to Controllers
    Rel(client, authController, "Invoca", "POST /api/auth/login, /api/auth/register")
    Rel(client, usuarioController, "Invoca", "GET, POST, PUT, DELETE /api/usuarios")
    Rel(client, cuentaController, "Invoca", "GET, POST, PUT, DELETE /api/cuentas")

    %% Controllers to Services
    Rel(authController, jwtUtil, "Utiliza", "Genera tokens JWT")
    Rel(authController, usuarioRepo, "Consulta", "Busca usuario por correo")
    Rel(usuarioController, usuarioService, "Invoca", "Lógica de negocio")
    Rel(cuentaController, cuentaService, "Invoca", "Lógica de negocio")

    %% Services to Repositories
    Rel(usuarioService, usuarioRepo, "Consulta", "Operaciones CRUD")
    Rel(cuentaService, cuentaRepo, "Consulta", "Operaciones CRUD")

    %% Repositories to Database
    Rel(usuarioRepo, h2db, "Consulta", "SQL queries via JPA/Hibernate")
    Rel(cuentaRepo, h2db, "Consulta", "SQL queries via JPA/Hibernate")

    %% Security interactions
    Rel(securityConfig, authController, "Protege", "Configura filtros de seguridad")
    Rel(securityConfig, usuarioController, "Protege", "Autorización por roles")
    Rel(securityConfig, cuentaController, "Protege", "Validación JWT")

    %% Cross-cutting concerns
    Rel(jwtUtil, securityConfig, "Integra", "Validación de tokens")
```

## 📱 Descripción de Componentes

### **Controllers (Capa de Presentación)**
Los controladores manejan las peticiones HTTP y coordinan las respuestas:

- **AuthController**: 
  - Responsabilidad: Autenticación y registro de usuarios
  - Endpoints: `/api/auth/login`, `/api/auth/register`
  - Tecnología: Spring MVC

- **UsuarioController**: 
  - Responsabilidad: Gestión CRUD de usuarios
  - Endpoints: `/api/usuarios/*`
  - Funciones: Crear, leer, actualizar, eliminar usuarios

- **CuentaBancariaController**: 
  - Responsabilidad: Gestión CRUD de cuentas bancarias
  - Endpoints: `/api/cuentas/*`
  - Funciones: Operaciones bancarias básicas

### **🧠 Services (Capa de Negocio)**
Los servicios implementan la lógica de negocio y orquestan las operaciones:

- **UsuarioService**: 
  - Lógica de negocio para usuarios
  - Validaciones de datos
  - Transformaciones y reglas de negocio

- **CuentaBancariaService**: 
  - Reglas de negocio para cuentas
  - Validaciones de saldo y transacciones
  - Coordinación de operaciones complejas

- **JwtUtil**: 
  - Generación y validación de tokens JWT
  - Manejo de claims y expiración
  - Integración con Spring Security

### **🗄️ Repositories (Capa de Acceso a Datos)**
Los repositorios proporcionan abstracción para el acceso a datos:

- **UsuarioRepository**: 
  - Extiende `JpaRepository<Usuario, Long>`
  - Consulta personalizada: `findByCorreo(String correo)`
  - Operaciones CRUD estándar

- **CuentaBancariaRepository**: 
  - Extiende `JpaRepository<CuentaBancaria, Long>`
  - Operaciones CRUD para cuentas bancarias
  - Consultas automáticas generadas por Spring Data

### **🔐 Security & Configuration**
Componentes transversales de configuración y seguridad:

- **SecurityConfig**: 
  - Configuración de Spring Security
  - Definición de rutas públicas y protegidas
  - Filtros JWT y políticas de autorización

- **SwaggerConfig**: 
  - Configuración de documentación OpenAPI
  - Definición de esquemas de API
  - Interfaz de pruebas interactiva

### **🗃️ Base de Datos**
- **H2 Database**: 
  - Base de datos en memoria para desarrollo
  - Configuración simple y rápida
  - Ideal para pruebas y desarrollo local

## 🔄 Flujo Típico de una Operación

1. **Cliente** → Envía petición HTTP al **Controller**
2. **Controller** → Valida entrada y delega al **Service**
3. **Service** → Ejecuta lógica de negocio y consulta **Repository**
4. **Repository** → Ejecuta operaciones en la **Base de Datos**
5. **Base de Datos** → Retorna datos al **Repository**
6. **Repository** → Convierte a entidades y retorna al **Service**
7. **Service** → Procesa datos y retorna al **Controller**
8. **Controller** → Formatea respuesta HTTP y retorna al **Cliente**

## 🔐 Flujo de Autenticación

1. **Cliente** → POST `/api/auth/login` con credenciales
2. **AuthController** → Valida credenciales via **UsuarioRepository**
3. **JwtUtil** → Genera token JWT con claims del usuario
4. **Cliente** → Recibe token y lo incluye en headers subsecuentes
5. **SecurityConfig** → Valida token en cada petición protegida
6. **Controllers** → Procesan peticiones autenticadas

## 🎯 Beneficios de esta Arquitectura

- **Separación de Responsabilidades**: Cada capa tiene una función específica
- **Testabilidad**: Componentes independientes fáciles de probar
- **Mantenibilidad**: Cambios aislados en cada capa
- **Escalabilidad**: Estructura clara para agregar nuevas funcionalidades
- **Reutilización**: Servicios y repositorios reutilizables