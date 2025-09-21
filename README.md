# LACSC 2026 - Backend API

🚀 **Backend API para la plataforma de gestión del Congreso LACSC 2026**

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-4.0.0-blue.svg)](https://maven.apache.org/)
[![H2 Database](https://img.shields.io/badge/H2-2.3.232-blue.svg)](https://www.h2database.com/)

## 📋 Descripción

LACSC2026-Back es la API REST que proporciona los servicios backend para la plataforma de gestión del Congreso Latinoamericano de Ciencias de la Computación (LACSC) 2026. La aplicación permite gestionar usuarios, resúmenes de investigación y proporciona funcionalidades para el registro y administración del congreso.

## 🏗️ Arquitectura

La aplicación está construida con una arquitectura en capas:

- **Controladores**: Exponen los endpoints REST
- **Servicios**: Contienen la lógica de negocio
- **Repositorios**: Manejo de la persistencia de datos
- **Modelos**: Entidades del dominio

## 🛠️ Stack Tecnológico

- **Java 21** - Lenguaje de programación
- **Spring Boot 3.5.3** - Framework principal
- **Spring Data JPA** - Manejo de persistencia
- **Spring Security** - Seguridad y autenticación
- **H2 Database** - Base de datos en memoria (desarrollo)
- **Swagger/OpenAPI** - Documentación de API
- **Lombok** - Reducción de código boilerplate
- **Maven** - Gestión de dependencias
- **Docker** - Containerización
- **Keycloak** - Gestión de identidad (en desarrollo)

## 📁 Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/innovawebJT/lacsc/
│   │   ├── config/          # Configuraciones
│   │   ├── controller/      # Controladores REST
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── enums/          # Enumeraciones
│   │   ├── exception/      # Manejo de excepciones
│   │   ├── model/          # Entidades JPA
│   │   ├── repository/     # Repositorios de datos
│   │   └── service/        # Lógica de negocio
│   └── resources/
│       ├── application*.properties  # Configuraciones por ambiente
│       └── banner.txt      # Banner de la aplicación
```

## 🚀 Instalación y Configuración

### Prerrequisitos

- **Java 21** o superior
- **Maven 3.6+**
- **Git**

### 1. Clonar el repositorio

```bash
git clone https://github.com/Bohorquezjael/LACSC2026-Back.git
cd LACSC2026-Back
```

### 2. Configurar variables de entorno

Crear un archivo `.env` basado en `.env-example`:

```bash
cp .env-example .env
```

Configurar las variables necesarias:

```properties
# Configuración de Base de Datos
DB_URL_DEV=jdbc:h2:mem:devdb
DB_USER_DEV=sa
DB_PASS_DEV=

# Configuración de Spring Security
SPRING_USER=admin
SPRING_PASS=admin123

# Configuración de Keycloak (opcional)
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=admin
```

### 3. Compilar e instalar dependencias

```bash
./mvnw clean install
```

### 4. Ejecutar la aplicación

```bash
./mvnw spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8081`

## 🐳 Docker

### Ejecutar con Docker Compose

El proyecto incluye Keycloak como servicio de autenticación:

```bash
docker-compose up -d
```

Servicios disponibles:
- **Keycloak**: `http://localhost:8088`
- **Aplicación**: `http://localhost:8081` (cuando se descomente en docker-compose.yml)

## 📚 API Endpoints

### 👤 Usuarios (`/users`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/users` | Crear nuevo usuario |
| `GET` | `/users/{id}` | Obtener usuario por ID |
| `GET` | `/users/all` | Listar todos los usuarios (paginado) |
| `GET` | `/users?email={email}` | Buscar usuario por email |
| `DELETE` | `/users/{id}` | Eliminar usuario |

### 📄 Resúmenes (`/summaries`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/summaries/all` | Listar todos los resúmenes (paginado) |
| `GET` | `/summaries/{id}` | Obtener resumen por ID |
| `DELETE` | `/summaries/{id}` | Eliminar resumen |

### 📖 Documentación API

La documentación interactiva de la API está disponible en:
- **Swagger UI**: `http://localhost:8081/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8081/v3/api-docs`

## 💾 Base de Datos

### H2 Console (Desarrollo)

Acceder a la consola H2 en: `http://localhost:8081/h2-console`

**Configuración de conexión:**
- **JDBC URL**: `jdbc:h2:mem:devdb`
- **Usuario**: `sa`
- **Contraseña**: (vacía)

### Modelo de Datos

#### Entidades Principales

- **User**: Usuarios registrados en el congreso
- **Summary**: Resúmenes de investigación
- **Institution**: Instituciones académicas
- **CoAuthor**: Co-autores de resúmenes
- **EmergencyContact**: Contactos de emergencia

#### Categorías de Usuarios

- `STUDENT`: Estudiantes
- `GUEST`: Invitados
- `INVEST`: Investigadores

## 🔧 Desarrollo

### Perfiles de Configuración

- **dev**: Desarrollo (H2 en memoria, datos de prueba)
- **prod**: Producción

### Datos de Prueba

En el perfil `dev`, se generan automáticamente 400 usuarios de prueba usando la librería DataFaker.

### Comandos Útiles

```bash
# Compilar
./mvnw compile

# Ejecutar tests
./mvnw test

# Ejecutar con perfil específico
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# Limpiar y compilar
./mvnw clean compile

# Generar JAR ejecutable
./mvnw clean package
```

## 🔒 Seguridad

- **Spring Security** configurado para permitir todas las requests durante desarrollo
- **Autenticación básica** configurada con usuario/contraseña desde variables de entorno
- **CSRF deshabilitado** para APIs REST
- **Headers de seguridad deshabilitados** para H2 Console

## 🌐 Configuración por Ambientes

### Desarrollo (`application-dev.properties`)
- Puerto: `8081`
- Base de datos: H2 en memoria
- Console H2 habilitada
- Datos de prueba incluidos

### Producción (`application-prod.properties`)
- Configuración específica para producción
- Base de datos externa

## 🤝 Contribución

1. Fork el proyecto
2. Crear rama feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

## 📝 Licencia

Este proyecto está bajo la licencia [MIT](LICENSE).

## 👥 Equipo

- **InnovawebJT** - Desarrollo principal
- **Bohorquezjael** - Mantenedor del repositorio

## 📞 Soporte

Para soporte técnico o preguntas sobre el proyecto:

- 🐛 **Issues**: [GitHub Issues](https://github.com/Bohorquezjael/LACSC2026-Back/issues)
- 📧 **Email**: Contactar a través de GitHub

---

<div align="center">

**LACSC 2026** - Congreso Latinoamericano de Ciencias de la Computación

</div>