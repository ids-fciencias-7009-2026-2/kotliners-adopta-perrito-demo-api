# Colitas Felices — Backend API

API REST para la plataforma de adopcion de mascotas. Construida con Spring Boot 4, Kotlin y PostgreSQL.

## Requisitos

- Java 21+
- Maven (incluido via `./mvnw`)
- PostgreSQL 14+

## Variables de entorno

Crea un archivo `.env` en la raiz del proyecto (ya existe `.env.example`):

```env
# Base de datos
URL_DB=localhost:5432/colitas_db
USER_DB=tu_usuario
PASSWORD_DB=tu_password

# Correo Gmail SMTP
GMAIL_USERNAME=tu_correo@gmail.com
GMAIL_APP_PASSWORD=tu_app_password
MAIL_FROM=tu_correo@gmail.com

# Cloudinary (para subida de imagenes)
CLOUDINARY_CLOUD_NAME=tu_cloud_name
CLOUDINARY_API_KEY=tu_api_key
CLOUDINARY_API_SECRET=tu_api_secret
```

### Configurar Gmail App Password

1. Activa verificacion en 2 pasos en tu cuenta Google
2. Ve a [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
3. Genera una contrasena de aplicacion para "Mail"
4. Usa esa contrasena en `GMAIL_APP_PASSWORD`

## Base de datos

```bash
# Crear la base de datos
psql -U tu_usuario -c "CREATE DATABASE colitas_db;"

# Aplicar el schema
psql -U tu_usuario -d colitas_db -f database/schema.sql
```

## Ejecucion

```bash
./mvnw spring-boot:run
```

La API estara disponible en `http://localhost:8080`.

## Endpoints principales

| Metodo | URL | Descripcion |
|--------|-----|-------------|
| POST | `/usuarios/register` | Registro de usuario |
| POST | `/usuarios/login` | Login |
| POST | `/usuarios/logout` | Logout |
| GET | `/usuarios/me` | Perfil del usuario autenticado |
| PUT | `/usuarios` | Actualizar perfil |
| GET | `/api/animales` | Listar animales disponibles |
| GET | `/api/animales/me` | Animales del cuidador autenticado |
| GET | `/api/animales/{id}` | Detalle de un animal |
| POST | `/api/animales` | Publicar animal (cuidador) |
| DELETE | `/api/animales` | Eliminar animal (cuidador) |
| POST | `/api/animales/{id}/interes` | Manifestar interes (adoptante) |
| DELETE | `/api/animales/{id}/interes` | Quitar interes (adoptante) |
| GET | `/api/usuarios/me/intereses` | Favoritos del adoptante |
| GET | `/api/animales/me/intereses` | Intereses recibidos (cuidador) |
| POST | `/uploads/foto-perfil` | Subir foto de perfil a Cloudinary |
