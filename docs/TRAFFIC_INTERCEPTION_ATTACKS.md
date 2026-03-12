# Análisis de Abuso del Sistema mediante Interceptación de Tráfico

## Resumen

Este documento describe exhaustivamente los vectores de ataque posibles cuando un actor malicioso intercepta el tráfico HTTP/HTTPS del sistema de pagos, utilizando herramientas como Burp Suite, OWASP ZAP, o similares.

**Nota importante:** Este análisis es para fines defensivos - permite entender los riesgos y diseñar contramedidas apropiadas.

---

## 1. Escenario de Ataque

### 1.1 Supuestos del Atacante
- Acceso a red del cliente (WiFi público, red corporativa comprometida, MITM)
- Instalación de certificado CA raíz en el dispositivo de la víctima
- Herramientas: Burp Suite, OWASP ZAP, mitmproxy, etc.

### 1.2 Limitaciones Asumidas
- El servidor usa HTTPS con TLS válido
- JWT está correctamente implementado con firma robusta
- No hay compromiso directo del servidor

---

## 2. Vectores de Ataque por Componente

### 2.1 Interceptación de Credenciales

#### 2.1.1 Robo de JWT Token
```
Ataque: Extraer token JWT de headers o cookies
Impacto: Acceso completo a la cuenta del usuario
```

**Escenario:**
1. Usuario inicia sesión
2. Burp intercepta la respuesta con el JWT
3. Atacante copia el token y lo usa en otras requests

**Protección actual:** ✅ Tokens en cookies HttpOnly

---

#### 2.1.2 Fuerza Bruta al Login
```
Ataque: Múltiples intentos de login con contraseñas comunes
Impacto: Acceso por compromiso de credenciales
```

**Escenario:**
1. Atacante intercepta endpoint `/auth/session`
2. Automatiza múltiples intentos con diccionarios
3. Sin rate limiting, puede probar miles de contraseñas

**Protección actual:** ❌ No hay rate limiting implementado

---

### 2.2 Abuso del Sistema de Pagos

#### 2.2.1 Manipulación de Monto
```
Ataque: Modificar el valor amount antes de enviar al servidor
Impacto: Pago parcial o gratuito de servicios
```

**Escenario:**
```
# Request Original Interceptado
POST /api/payments/checkout
{
  "courseId": 1,
  "amount": 150.00,
  "currency": "USD"
}

# Request Modificado por Atacante
POST /api/payments/checkout
{
  "courseId": 1,
  "amount": 0.01,
  "currency": "USD"
}
```

**Requisitos previos:**
- El servidor valida el monto contra la base de datos
- Si hay discrepancia, es vulnerable

**Contramedida obligatoria:**
```java
// El monto debe venir del servidor, no del cliente
// Correcto:
public PaymentIntent createPaymentIntent(Long courseId) {
    Course course = courseRepository.findById(courseId)
        .orElseThrow();
    // Monto generado desde BD, no desde request
    return PaymentIntent.builder()
        .amount(course.getPrice())  // Del servidor
        .build();
}
```

---

#### 2.2.2 Replay de Transacción
```
Ataque: Reenviar una transacción válida múltiples veces
Impacto: Cargos duplicados o uso múltiple de un pago
```

**Escenario:**
1. Usuario realiza pago válido
2. Atacante intercepta y guarda el request
3. Reenvía el mismo request múltiples veces

```
# Request Original
POST /api/payments/process
Authorization: Bearer eyJhbG...
{
  "transactionId": "txn_123",
  "amount": 150.00
}

# Ataque: Replay 5 veces
POST /api/payments/process
Authorization: Bearer eyJhbG...
{
  "transactionId": "txn_123",
  "amount": 150.00
}
[repetir 5 veces]
```

**Contramedida obligatoria:**
```java
// Usar ID único (nonce) por transacción
@PostMapping("/process")
public ResponseEntity<PaymentResult> processPayment(
    @RequestHeader("Idempotency-Key") String idempotencyKey,
    @RequestBody PaymentRequest request
) {
    // Verificar si ya fue procesada
    if (paymentService.existsByIdempotencyKey(idempotencyKey)) {
        return ResponseEntity.status(409).build(); // Conflict
    }
    // Procesar...
}
```

---

#### 2.2.3 Manipulación de Estado de Pago
```
Ataque: Cambiar status de PENDING a APPROVED
Impacto: Bypass de verificación de pago
```

**Escenario:**
```
# Request Original - Admin marcando pago
PATCH /api/users/1/course-payment/1/status
{
  "status": "PENDING"
}

# Request Modificado
PATCH /api/users/1/course-payment/1/status
{
  "status": "APPROVED"
}
```

**Contramedida:**
- Validar que el pago realmente fue aprobado por el gateway
- No confiar en el valor enviado por el cliente
- Registrar el agente que aprobó el pago

---

#### 2.2.4 Descuadre de Moneda
```
Ataque: Cambiar la moneda para aprovechar tasas de cambio
Impacto: Pago menor al esperado
```

```
POST /api/payments/checkout
{
  "courseId": 1,
  "amount": 100,
  "currency": "USD"  # Cambiar a VES, COP, etc.
}
```

**Contramedida:**
- Moneda hardcodeada en servidor
- No aceptar currency del request

---

### 2.3 Interceptación de Archivos

#### 2.3.1 Robo de Comprobantes
```
Ataque: Acceder a comprobantes de pago de otros usuarios
Impacto: Exposición de datos financieros sensibles
```

**Escenario:**
```
GET /api/users/1/files/payment
GET /api/users/2/files/payment  # Iterando IDs
```

**Respuesta:**
```xml
HTTP/1.1 200 OK
Content-Type: application/pdf
[Contenido del PDF]
```

**Contramedida:**
- Validar que el usuario solicitante es propietario o admin
- No exponerse por ID secuencial

---

#### 2.3.2 Modificación de Archivos
```
Ataque: Reemplazar comprobante de pago legítimo
Impacto: Aprobación de pago falso
```

**Escenario:**
1. Usuario sube comprobante válido
2. Atacante intercepta y reemplaza con PDF falso
3. Servidor almacena archivo modificado

**Contramedida:**
- Generar hash del archivo en cliente antes de subir
- Verificar hash en servidor

---

### 2.4 Enumeración y Reconocimiento

#### 2.4.1 Enumeración de Usuarios
```
Ataque: Identificar usuarios válidos del sistema
Impacto: Preparar ataques dirigidos
```

```
GET /api/users/1      # 404 vs 200
GET /api/users/2      # Diferentes respuestas
GET /api/users/3
```

**Respuestas potenciales:**
- `200 OK` con datos del usuario
- `404 Not Found` para IDs inexistentes
- `403 Forbidden` para usuarios sin acceso

---

#### 2.4.2 Enumeración de Transacciones
```
Ataque: Mapear todas las transacciones del sistema
Impacto: Acceso a historial financiero
```

```
GET /api/summaries/1
GET /api/summaries/2
GET /api/summaries/3
```

---

#### 2.4.3 Fingerprinting del Sistema
```
Ataque: Identificar tecnologías y versiones
Impacto: Buscar exploits específicos
```

```
# Headers de respuesta
Server: Apache/2.4.41
X-Powered-By: Express
X-Framework: Spring Boot 3.5.3
```

---

### 2.5 Manipulation de Requests JSON

#### 2.5.1 Parameter Pollution
```
Ataque: Enviar múltiples valores para un parámetro
Impacto: Comportamiento inesperado
```

```
POST /api/users
{
  "email": "user@test.com",
  "email": "admin@test.com"  # Sobrescribir o concatenar
}
```

---

#### 2.5.2 Type Confusion
```
Ataque: Cambiar tipos de datos
Impacto: Validación saltada o errores
```

```
# Original
POST /api/payments
{
  "amount": 150
}

# Modificado - tipo incorrecto
POST /api/payments
{
  "amount": "150; DROP TABLE payments;--"
}
```

---

### 2.6 Interceptación en Tiempo Real

#### 2.6.1 Man-in-the-Browser (MitB)
```
Ataque: Modificar DOM o requests desde el navegador comprometido
Impacto: Robo de información antes del cifrado
```

**Escenario:**
1. Usuario tiene malware en el navegador
2. Cada request se modifica antes de ser cifrada por TLS

---

#### 2.6.2 SSL Stripping
```
Ataque: Forzar conexión HTTP antes de HTTPS
Impacto: Interceptar tráfico en texto plano
```

**Protección:**
- HSTS (HTTP Strict Transport Security)
- Redirección automática HTTP→HTTPS

---

## 3. Matriz de Impacto por Ataque

| Ataque | Impacto | Probabilidad | Severidad | Contramedida |
|--------|---------|--------------|-----------|--------------|
| Robo de JWT | Critico | Media | Critico | Cookies HttpOnly, rotación |
| Fuerza bruta login | Alto | Alta | Alto | Rate limiting, MFA |
| Manipulación monto | Critico | Baja | Critico | Monto del servidor |
| Replay de transacción | Alto | Media | Alto | Idempotency keys |
| Modificación de estado | Alto | Baja | Alto | Validación server-side |
| Robo de archivos | Alto | Media | Alto | Ownership validation |
| Enumeración usuarios | Medio | Alta | Medio | Respuestas genéricas |
| Parameter pollution | Bajo | Baja | Bajo | Validación estricta |

---

## 4. Contramedidas Recomendadas

### 4.1 Críticas (Implementar Inmediatamente)

#### 4.1.1 Rate Limiting
```java
@Configuration
public class RateLimitingConfig {
    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitFilter() {
        // Limitar intentos de login a 5 por minuto
        // Limitar requests a 100 por minuto por IP
    }
}
```

#### 4.1.2 Idempotency Keys
```java
@PostMapping("/payments")
public ResponseEntity<Payment> pay(
    @RequestHeader("Idempotency-Key") String key,
    @RequestBody PaymentRequest req
) {
    return paymentService.process(key, req);
}
```

#### 4.1.3 Amount from Server
```java
// NUNCA confiar en el monto del cliente
public BigDecimal getCoursePrice(Long courseId) {
    return courseRepository.findById(courseId)
        .map(Course::getPrice)
        .orElseThrow(); // Del servidor
}
```

---

### 4.2 Altas (Implementar Pronto)

#### 4.2.1 Logging de Seguridad
```java
@Aspect
@Component
public class SecurityAuditAspect {
    @Before("@annotation(SensitiveOperation)")
    public void logSensitiveOperation(JoinPoint joinPoint) {
        log.warn("SENSITIVE: {} by {} from {}",
            joinPoint.getSignature().getName(),
            SecurityUtils.getCurrentUser(),
            request.getRemoteAddr()
        );
    }
}
```

#### 4.2.2 Request Signing
```java
// HMAC del request completo
String signature = calculateHmac(request, secretKey);
```

---

### 4.3 Medias (Considerar)

#### 4.3.1 Web Application Firewall
- AWS WAF, CloudFlare, etc.

#### 4.3.2 API Gateway
- Kong, AWS API Gateway, etc.

---

## 5. Checklist de Seguridad para Pagos

- [ ] Rate limiting en TODOS los endpoints
- [ ] Idempotency keys para operaciones de pago
- [ ] Montos generados en servidor, nunca del cliente
- [ ] Validación de ownership en descarga de archivos
- [ ] Logging de todas las operaciones sensibles
- [ ] Timeout de sesión cortos
- [ ] HTTPS forzado (HSTS)
- [ ] Headers de seguridad (X-Frame-Options, etc.)
- [ ] Sanitización de todos los inputs
- [ ] Respuestas genéricas para errores de autenticación

---

## 6. Referencias

- OWASP API Security Top 10
- OWASP WebGoat - HTTP Interception
- PCI DSS Requirements
- NIST SP 800-63B (Digital Identity Guidelines)
