# Deportal

Deportal reserva canchas deportivas. Un cliente elige cancha, fecha y hora, ve el precio con los descuentos y confirma. Si el horario del mismo día está ocupado, queda en lista de espera. El admin registra canchas, cambia la membresía y consulta la utilización.

## Cómo ejecutarlo

Hace falta Java 17, MySQL 8 y Node.js 20.19 o superior. El backend y el frontend se levantan por separado.

Primero crea la base con el script. Crea las tablas y deja cargadas las canchas y las cuentas de prueba. La aplicación no crea ni modifica el esquema al arrancar.

```bash
mysql -u USUARIO -p < database/deportal.sql
```

Con la base lista, copia `backend/.env.example` a `backend/.env` y completa el usuario, la contraseña de MySQL y `JWT_SECRET`. El secreto tiene que medir al menos 32 bytes. Ese archivo no se sube a git.

Backend, en el puerto 8080:

```bash
cd backend
./mvnw spring-boot:run
```

Frontend, en el puerto 4200. El proxy envía `/api` a `http://localhost:8080`, así que el backend tiene que estar corriendo antes.

```bash
cd frontend
npm install
npm start
```

Abre `http://localhost:4200`.

## Cuentas de prueba

La contraseña de todas es `123456`.

| Quién | Correo | Rol |
| --- | --- | --- |
| Admin | admin@admin.com | Administrador |
| Juan Pérez | juan.perez@deportal.test | Cliente, miembro |
| María García | maria.garcia@deportal.test | Cliente, no miembro |
| Carlos López | carlos.lopez@deportal.test | Cliente, miembro |

## Arquitectura

### Backend: hexagonal

El dominio está en el centro, en Java puro y sin Spring. Ahí viven el horario, los solapes, la hora de limpieza, los descuentos, el reembolso y la lista de espera. Cada flujo es un caso de uso. El caso de uso no habla con MySQL ni con HTTP: habla con un puerto, una interfaz pequeña. JPA y los controladores son adaptadores: están afuera y solo traducen.

Elegimos hexagonal porque en este proyecto se evalúan mucho más las reglas de cada requerimiento que las pantallas. El horario, el solape, la limpieza, el descuento, el reembolso y la lista de espera tienen que leerse y probarse solas, sin levantar el servidor. Si mañana hay otro descuento, se agrega otra estrategia y el caso de uso que crea la reserva no se toca. El JSON de la API tampoco es la entidad de dominio.

### Frontend: por funcionalidad

Las pantallas están agrupadas por lo que hace la persona: `auth`, `customer`, `admin` y `home`. Lo que se repite (dinero, avisos, lectura del JSON) está en `shared`. Cada ruta carga su pantalla cuando hace falta.

Dentro de una pantalla, la página guarda el estado y llama al API. Los pasos, como elegir cancha u hora, solo muestran datos y avisan lo que eligió el usuario. El precio no se calcula en Angular: se muestra la cotización que devuelve el backend.

Lo separamos así para que un cambio en reservar no arrastre al admin, y para que la regla de negocio no quede duplicada en el navegador.

## Tecnologías

- Backend: Spring Boot 4.1.1 y Java 17
- Base de datos: MySQL
- Frontend: Angular 22 y Angular Material
- Pruebas: JUnit en el backend y Vitest en el frontend

## Endpoints

El login devuelve un JWT. Las rutas de `/api/reservations` piden un cliente. Las de `/api/admin` piden un administrador.

Autenticación:

- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/auth/session`

Reservas:

- `GET /api/reservations/courts`
- `GET /api/reservations/availability`
- `GET /api/reservations/quote`
- `POST /api/reservations`
- `GET /api/reservations`
- `DELETE /api/reservations/{id}`

Admin:

- `GET /api/admin/sport-types`
- `GET /api/admin/courts`
- `POST /api/admin/courts`
- `PUT /api/admin/courts/{id}`
- `DELETE /api/admin/courts/{id}`
- `GET /api/admin/users`
- `PUT /api/admin/users/{id}`
- `GET /api/admin/reports/utilization`

## Pruebas

```bash
cd backend
./mvnw test
```

```bash
cd frontend
npm test
```
