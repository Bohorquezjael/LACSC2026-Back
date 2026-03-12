# Modelo de Amenazas - LACSC2026 Backend

## 1. Descripción del Sistema

### 1.1 Arquitectura General
- **Tipo**: REST API construida con Spring Boot 3.5
- **Autenticación**: OAuth2 con Keycloak (JWT)
- **Base de datos**: PostgreSQL
- **Almacenamiento**: Sistema de archivos local
- **Roles**: ADMIN_GENERAL, ADMIN_SESSION, ADMIN_PAGOS, ADMIN_REVISION

### 1.2 Endpoints Principales
```
/api/users/*     - Gestión de usuarios
/api/summaries/* - Gestión de resúmenes/conferencias
/api/auth/*      - Autenticación
```

---

## 2. Identificación de Actores

| Actor | Descripción | Privilegios |
|-------|-------------|-------------|
| Usuario Registrado | Participante del congreso | Ver/editar propio perfil, subir resúmenes |
| Admin General | Administrador completo | Acceso total al sistema |
| Admin Sesión | Admin de sesiones especiales | Gestionar resúmenes de sus sesiones |
| Admin Pagos | Admin de pagos | Revisar pagos de congreso y cursos |
| Admin Revisión | Admin de revisión | Revisar resúmenes |
| Anónimo | Usuario no autenticado | Solo endpoints públicos |

---

## 3. Análisis de Amenazas (STRIDE)

### 3.1 Spoofing (Suplantación)

| ID | Amenaza | Descripción | Riesgo |
|----|---------|-------------|--------|
| S-01 | Suplantación de JWT | Atacante usa token robado o modificado | ALTO |
| S-02 | Creación de usuario falso | Registro con email de otra persona | MEDIO |
| S-03 | Sesión hijacking | Robo de cookie de sesión | ALTO |

**Mitigaciones:**
- JWT con expiración corta (15 min)
- Cookies HttpOnly, Secure, SameSite
- Validación de firma JWT contra Keycloak

---

### 3.2 Tampering (Manipulación)

| ID | Amenaza | Descripción | Riesgo |
|----|---------|-------------|--------|
| T-01 | Manipulación de datos en tránsito | MITM modifica requests | BAJO |
| T-02 | Manipulación de archivos subidos | Upload de malware en archivos | ALTO |
| T-03 | Modificación de estado via IDOR | Cambiar recursos de otros usuarios | ALTO |

**Mitigaciones:**
- HTTPS/TLS
- Validación de tipo de archivo (PDF only)
- Verificación de ownership en todos los endpoints

---

### 3.3 Repudiation (Repudio)

| ID | Amenaza | Descripción | Riesgo |
|----|---------|-------------|--------|
| R-01 | Eliminación sin evidencia | Admin borra datos sin rastro | MEDIO |
| R-02 | Modificación no auditada | Cambios en resúmenes sin logs | MEDIO |
| R-03 | Acceso no autorizado no detectado | Intentos de acceso fallidos | BAJO |

**Mitigaciones:**
- Sistema de Audit Logging implementado
- Logs de autenticación JWT
- Registro de operaciones sensibles

---

### 3.4 Information Disclosure (Divulgación)

| ID | Amenaza | Descripción | Riesgo |
|----|---------|-------------|--------|
| I-01 | Exposición de datos sensibles | Datos de usuarios expuestos | ALTO |
| I-02 | Información en URLs | Datos sensibles en query params | BAJO |
| I-03 | Stack traces en producción | Errores exponiendo código | ALTO |
| I-04 | Archivos accesibles públicamente | Comprobantes de pago expuestos | CRÍTICO |

**Mitigaciones:**
- Filtrado de campos sensibles en DTOs
- Manejo global de excepciones
- Almacenamiento fuera de webroot
- Validación de permisos antes de servir archivos

---

### 3.5 Denial of Service (Denegación de Servicio)

| ID | Amenaza | Descripción | Riesgo |
|----|---------|-------------|--------|
| D-01 | Flood de uploads | Subida masiva de archivos | MEDIO |
| D-02 | Query injection | Consultas muy pesadas (N+1) | MEDIO |
| D-03 | Login brute force | Múltiples intentos de login | MEDIO |

**Mitigaciones:**
- Límites de tamaño de archivo
- Optimización de queries (N+1 fix)
- Rate limiting en autenticación

---

### 3.6 Elevation of Privilege (Elevación de Privilegios)

| ID | Amenaza | Descripción | Riesgo |
|----|---------|-------------|--------|
| E-01 | Bypass de authorization | Acceder a endpoints sin permisos | CRÍTICO |
| E-02 | Horizontal privilege escalation | Ver datos de otros usuarios | ALTO |
| E-03 | Manipulación de roles | Cambiar rol propio en JWT | CRÍTICO |

**Mitigaciones:**
- @PreAuthorize en todos los endpoints sensibles
- Validación de permisos en Service layer
- Roles definidos en Keycloak (no modificables por usuario)

---

## 4. Matriz de Riesgo

```
                    IMPACTO
              Bajo    Medio    Alto    Crítico
           ┌────────┬────────┬────────┬────────┐
    Bajo   │  R-03  │        │        │        │
MEDIO     │        │ R-01   │ S-02   │        │
          │        │ R-02   │ D-01   │        │
          │        │ D-02   │ D-03   │        │
ALTO      │        │  I-02  │ S-01   │  I-01  │
          │        │        │ S-03   │  E-02  │
          │        │        │ T-01   │  I-04  │
          │        │        │ T-02   │        │
CRÍTICO   │        │        │  T-03  │ E-01   │
          │        │        │        │ E-03   │
          └────────┴────────┴────────┴────────┘
```

---

## 5. Controles de Seguridad Existentes

### 5.1 Autenticación
- OAuth2 Resource Server con Keycloak
- JWT con firma RS256
- Tokens con expiración

### 5.2 Autorización
- @PreAuthorize en endpoints
- Roles: ADMIN_GENERAL, ADMIN_SESSION, ADMIN_PAGOS, ADMIN_REVISION
- Validación de ownership en Service layer

### 5.3 Validación de Entrada
- Spring Validation en DTOs
- Sanitización de inputs
- Tipo de archivos restringidos (PDF)

### 5.4 Logging & Audit
- AuditAspect para registrar operaciones
- Logs de autenticación JWT
- Manejo global de excepciones

---

## 6. Gaps de Seguridad Identificados

| Gap | Descripción | Prioridad | Recomendación |
|-----|-------------|-----------|---------------|
| G-01 | No hay rate limiting | ALTA | Implementar Bucket4j o similar |
| G-02 | Archivos en sistema local | MEDIA | Migrar a S3 con signed URLs |
| G-03 | N+1 query resuelto | MEDIA | Ya mitigado en este PR |
| G-04 | No hay WAF | MEDIA | Considerar CloudFlare/AWS WAF |
| G-05 | Sin HTTPS forzado | BAJA | Configurar redirect HTTP→HTTPS |

---

## 7. Conclusiones

### 7.1 Estado Actual
El proyecto cuenta con controles de seguridad básicos correctamente implementados:
- Autenticación robusta con Keycloak
- Autorización basada en roles
- Logging de operaciones sensibles

### 7.2 Riesgos Principales
1. **E-01/E-03**: Elevación de privilegios es el riesgo más crítico
2. **I-04**: Archivos de pago accesibles públicamente
3. **S-01/S-03**: Suplantación de identidad

### 7.3 Recomendaciones Inmediatas
1. Implementar rate limiting
2. Revocar acceso a archivos mediante signed URLs
3. Añadir pruebas de seguridad automatizadas

---

## 8. Referencias

- OWASP Top 10 2021
- NIST SP 800-53 Security Controls
- Spring Security Documentation
