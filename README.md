# QuizArena · Servicio de Identidad y Contenido

Microservicio que maneja cuentas (registro/login con **JWT** y verificación
de correo), **bancos de preguntas**, **historial** de partidas y el
**sistema de amigos** (solicitudes, aceptación, listado). Backend REST
clásico con **JPA + PostgreSQL + Spring Security**, corre en una sola
instancia (no usa Redis, a diferencia del Servicio de Juego).

---

## Requisitos previos

- **Java 17** o superior
- **Maven**
- **Docker Desktop** (para PostgreSQL)
- Una cuenta de correo SMTP para enviar los códigos de verificación (en
  desarrollo se puede dejar vacío y el envío simplemente falla sin romper
  el registro; ver `ServicioCorreo`)

---

## Puesta en marcha

1. **Levanta la base de datos** (desde la carpeta del proyecto):

   ```bash
   docker compose up -d
   ```

   Usa el puerto **5433** en tu máquina (para no chocar con el PostgreSQL del
   Servicio de Juego, que usa el 5432). Las tablas se crean solas al arrancar
   (`spring.jpa.hibernate.ddl-auto=update`).

2. **Arranca el servicio** (desde IntelliJ ejecuta `ServicioIdentidadApplication`,
   o por terminal):

   ```bash
   mvn spring-boot:run
   ```

   Queda escuchando en **http://localhost:8082**.

---

## Probar los endpoints (con curl)

### 1. Registrarse

La contraseña debe tener mínimo 6 caracteres e incluir al menos **un
carácter especial**.

```bash
curl -X POST http://localhost:8082/api/auth/registro \
  -H "Content-Type: application/json" \
  -d '{"correo":"juan@mail.com","contrasena":"secreta123!","nombre":"Juan"}'
```

Respuesta: un mensaje indicando que se envió un código de verificación al
correo. Sin verificar el correo, el login no funciona.

### 2. Verificar el correo

```bash
curl -X POST http://localhost:8082/api/auth/verificar \
  -H "Content-Type: application/json" \
  -d '{"correo":"juan@mail.com","codigo":"123456"}'
```

Respuesta: un **token JWT**.

### 3. Iniciar sesión (alternativa, ya con el correo verificado)

```bash
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"juan@mail.com","contrasena":"secreta123!"}'
```

> Tras **5 intentos fallidos en 10 minutos**, el correo queda bloqueado por
> 5 minutos (`LimitadorIntentosLogin`) — protección básica contra fuerza
> bruta.

### 4. Crear un banco (requiere token)

```bash
curl -X POST http://localhost:8082/api/bancos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TU_TOKEN" \
  -d '{"nombre":"Parcial 1","materia":"Arquitectura"}'
```

### 5. Buscar amigos y enviar una solicitud

```bash
curl "http://localhost:8082/api/amigos/buscar?q=juan" -H "Authorization: Bearer TU_TOKEN"

curl -X POST http://localhost:8082/api/amigos/solicitudes \
  -H "Content-Type: application/json" -H "Authorization: Bearer TU_TOKEN" \
  -d '{"idDestinatario":"ID_DEL_OTRO_USUARIO"}'
```

---

## Estructura del proyecto

```
src/main/java/com/quizarena/identidad/
├── ServicioIdentidadApplication.java
├── config/
│   └── SecurityConfig.java          # que endpoints son publicos / protegidos
├── seguridad/
│   ├── ProveedorJwt.java            # genera y valida los tokens JWT
│   ├── FiltroJwt.java               # valida el token en cada peticion (obligatorio)
│   └── LimitadorIntentosLogin.java  # bloqueo temporal tras fuerza bruta (en memoria)
├── modelo/                          # entidades JPA (tablas)
│   ├── Usuario, BancoPreguntas, PreguntaBanco, OpcionBanco, RegistroPartida
│   ├── SolicitudAmistad, EstadoSolicitud
│   └── Rol, TipoPregunta (enums)
├── repositorio/                     # interfaces Spring Data (acceso a datos)
├── dto/                             # objetos de entrada/salida de la API
├── servicio/                        # logica de negocio
│   ├── ServicioAutenticacion, ServicioBancos, ServicioHistorial
│   ├── ServicioAmigos                    # solicitudes, aceptar/rechazar, listar amigos
│   └── ServicioCorreo                    # envio de codigos de verificacion
└── controlador/                     # endpoints REST
    ├── AuthController, BancoController, HistorialController, AmigoController
    └── ManejadorErroresValidacion        # traduce errores de @Valid a {"error": "..."}
```

## Seguridad, en resumen

- `/api/auth/**` (registro, login, verificación) son **públicos**.
- `POST /api/historial` está **abierto** para la comunicación interna del
  Servicio de Juego en desarrollo.
- **Todo lo demás requiere** la cabecera `Authorization: Bearer <token>`.
- Contraseñas hasheadas con **BCrypt**; nunca en texto plano.
- Registro exige contraseña con al menos un carácter especial.
- Login bloquea un correo temporalmente tras varios intentos fallidos
  seguidos (fuerza bruta).
- `quizarena.jwt.secret` **no tiene valor por defecto**: si falta la
  variable de entorno `JWT_SECRET`, el servicio no arranca — evita firmar
  tokens con una clave pública conocida del repositorio. Debe ser
  **idéntica** a la del API Gateway y el Servicio de Juego.

## Cómo se conecta con los demás servicios

- El Servicio de Juego pide los bancos reales a `GET /api/bancos/{id}` y
  guarda resultados con `POST /api/historial`.
- El Servicio de Juego también valida el mismo JWT (de forma permisiva) para
  saber quién crea una sala y así poder invitar amigos vía
  `/api/amigos/**`.
- El Servicio de IA no llama a Identidad directamente: el frontend recibe
  los borradores generados y, si el usuario los aprueba, los guarda con el
  mismo endpoint `POST /api/bancos/{id}/preguntas` que usa la creación
  manual.
