# QuizArena · Servicio de Identidad y Contenido

Microservicio que maneja cuentas (registro/login con **JWT**), **bancos de
preguntas** e **historial** de partidas. A diferencia del Servicio de Juego,
este es un backend REST clásico con **JPA + PostgreSQL + Spring Security**.

---

## Requisitos previos

- **Java 17** o superior
- **Maven**
- **Docker Desktop** (para PostgreSQL)
- **IntelliJ IDEA** (recomendado)

---

## Puesta en marcha

1. **Levanta la base de datos** (desde la carpeta del proyecto):

   ```bash
   docker compose up -d
   ```

   Usa el puerto **5433** en tu máquina (para no chocar con el PostgreSQL del
   Servicio de Juego, que usa el 5432). Las tablas se crean solas al arrancar
   (gracias a `spring.jpa.hibernate.ddl-auto=update`).

2. **Arranca el servicio** (desde IntelliJ ejecuta `ServicioIdentidadApplication`,
   o por terminal):

   ```bash
   mvn spring-boot:run
   ```

   Queda escuchando en **http://localhost:8082**.

---

## Probar los endpoints (con curl)

### 1. Registrarse

```bash
curl -X POST http://localhost:8082/api/auth/registro \
  -H "Content-Type: application/json" \
  -d '{"correo":"juan@mail.com","contrasena":"secreta123","nombre":"Juan"}'
```

Respuesta: un **token JWT**. Cópialo, lo necesitas para los siguientes pasos.

### 2. Iniciar sesión (alternativa, devuelve otro token válido)

```bash
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"juan@mail.com","contrasena":"secreta123"}'
```

### 3. Crear un banco (requiere token)

Reemplaza `TU_TOKEN` por el token del paso anterior:

```bash
curl -X POST http://localhost:8082/api/bancos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TU_TOKEN" \
  -d '{"nombre":"Parcial 1","materia":"Arquitectura"}'
```

Respuesta: el banco creado, con su `id`.

### 4. Agregar una pregunta al banco

Reemplaza `ID_BANCO` por el id del paso anterior:

```bash
curl -X POST http://localhost:8082/api/bancos/ID_BANCO/preguntas \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TU_TOKEN" \
  -d '{
    "enunciado":"¿Que patron usa QuizArena?",
    "tipo":"OPCION_MULTIPLE",
    "tiempoLimiteSegundos":20,
    "opciones":[
      {"texto":"Monolito","esCorrecta":false},
      {"texto":"Microservicios","esCorrecta":true}
    ]
  }'
```

### 5. Buscar bancos por materia

```bash
curl "http://localhost:8082/api/bancos?materia=Arquitectura" \
  -H "Authorization: Bearer TU_TOKEN"
```

---

## Estructura del proyecto

```
src/main/java/com/quizarena/identidad/
├── ServicioIdentidadApplication.java
├── config/
│   └── SecurityConfig.java         # que endpoints son publicos / protegidos
├── seguridad/
│   ├── ProveedorJwt.java           # genera y valida los tokens JWT
│   └── FiltroJwt.java              # valida el token en cada peticion
├── modelo/                         # entidades JPA (tablas)
│   ├── Usuario, BancoPreguntas, PreguntaBanco, OpcionBanco, RegistroPartida
│   └── Rol, TipoPregunta (enums)
├── repositorio/                    # interfaces Spring Data (acceso a datos)
├── dto/                            # objetos de entrada/salida de la API
├── servicio/                       # logica de negocio
│   ├── ServicioAutenticacion, ServicioBancos, ServicioHistorial
└── controlador/                    # endpoints REST
    ├── AuthController, BancoController, HistorialController
```

## Seguridad, en resumen

- `/api/auth/**` (registro, login) son **públicos**.
- `POST /api/historial` está **abierto** para la comunicación interna del
  Servicio de Juego en desarrollo (en producción se aseguraría servicio-a-servicio).
- **Todo lo demás requiere** la cabecera `Authorization: Bearer <token>`.
- Las contraseñas se guardan **hasheadas con BCrypt**, nunca en texto plano.

---

## Cómo se conecta con el Servicio de Juego (Fase 2)

- El Servicio de Juego pedirá los bancos reales a `GET /api/bancos/{id}` en lugar
  de usar preguntas quemadas.
- Al terminar una partida, el Servicio de Juego llamará a `POST /api/historial`
  para guardar el resultado de cada jugador.
