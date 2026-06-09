# Colitas Felices - Backend

Backend del proyecto Colitas Felices. Spring Boot 4, Kotlin, PostgreSQL.

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

# APIs externas de razas (opcionales, si no se configuran la seccion de info de raza no aparece)
DOG_API_KEY=tu_key_de_thedogapi
CAT_API_KEY=tu_key_de_thecatapi
```

### Obtener credenciales de Cloudinary

1. Crear cuenta gratuita en [cloudinary.com](https://cloudinary.com)
2. Ir a Dashboard
3. Copiar Cloud Name, API Key y API Secret

### Obtener API keys de razas

Las keys son **gratuitas** y se obtienen en menos de 1 minuto:

- **The Dog API**: Registrarse en [thedogapi.com](https://thedogapi.com), la key llega por correo
- **The Cat API**: Registrarse en [thecatapi.com](https://thecatapi.com), la key llega por correo

Si no se configuran, el endpoint `/api/razas/info` devuelve 404 y la seccion de informacion de raza simplemente no aparece en la ficha del animal (fallback silencioso).

### Configurar Gmail App Password

1. Activa verificacion en 2 pasos en tu cuenta Google
2. Ve a [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
3. Genera una contrasena de aplicacion para "Mail"
4. Usa esa contrasena en `GMAIL_APP_PASSWORD`

## Base de datos

```bash
# Crear/recrear la base de datos
psql -U tu_usuario -d postgres -c "DROP DATABASE IF EXISTS colitas_db; CREATE DATABASE colitas_db;"
psql -U tu_usuario -d colitas_db -f database/schema.sql
```

El schema crea un usuario administrador por defecto:
- Email: `colitasfeliceskotliners@gmail.com` | Password: `Admin123!` (hash BCrypt)

## Ejecución

```bash
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:8080`. Las variables del `.env` se cargan automáticamente con `dotenv-kotlin`.

## Endpoints principales

| Método | URL | Descripción |
|--------|-----|-------------|
| **Auth** | | |
| POST | `/usuarios/register` | Registro (envía correo de verificación) |
| GET | `/usuarios/verificar-correo?token=X` | Verificar correo electrónico |
| POST | `/usuarios/login` | Login (envía código 2FA por correo) |
| POST | `/usuarios/verificar-2fa` | Validar código 2FA y obtener token |
| POST | `/usuarios/recuperar` | Solicitar recuperación de contraseña |
| POST | `/usuarios/restablecer` | Restablecer contraseña con token |
| POST | `/usuarios/logout` | Logout |
| GET | `/usuarios/me` | Perfil del usuario autenticado |
| PUT | `/usuarios` | Actualizar perfil |
| DELETE | `/usuarios/me` | Eliminar cuenta (soft delete) |
| **Animales** | | |
| GET | `/api/animales` | Listar animales (filtros: especie, sexo, raza, edad, distancia, etc.) |
| GET | `/api/animales/me` | Animales del cuidador autenticado |
| GET | `/api/animales/{id}` | Detalle de un animal |
| POST | `/api/animales` | Publicar animal (cuidador) |
| PUT | `/api/animales/{id}` | Editar animal (cuidador) |
| DELETE | `/api/animales` | Eliminar animal (cuidador) |
| GET | `/api/animales/historial-adoptados` | Historial de adoptados (cuidador) |
| GET | `/api/animales/{id}/interesados` | Lista de interesados en un animal (cuidador) |
| **Interés** | | |
| POST | `/api/animales/{id}/interes` | Manifestar interés (adoptante) |
| DELETE | `/api/animales/{id}/interes` | Retirar interés (adoptante) |
| GET | `/api/usuarios/me/intereses` | Favoritos del adoptante |
| GET | `/api/animales/me/intereses` | Intereses recibidos (cuidador) |
| **Reportes (admin)** | | |
| POST | `/api/reportes` | Crear reporte con motivo |
| GET | `/api/reportes/check/{animalId}` | Verificar si ya reporté un animal |
| DELETE | `/api/reportes/{animalId}` | Retirar mi reporte |
| GET | `/api/reportes/pendientes` | Listar reportes pendientes (admin) |
| POST | `/api/reportes/{id}/resolver` | Eliminar publicación (admin) |
| POST | `/api/reportes/{id}/desestimar` | Desestimar reporte (admin) |
| PATCH | `/api/animales/{id}/inapropiado` | Reportar animal con motivo |
| **Razas** | | |
| GET | `/api/razas` | Catálogo de razas (`?especie=PERRO\|GATO`) |
| GET | `/api/razas/info` | Info de raza desde API externa |
| **Vacunas y Padecimientos** | | |
| GET | `/api/vacunas` | Listar vacunas disponibles |
| POST | `/api/vacunas` | Crear vacuna (cuidador) |
| GET | `/api/padecimientos` | Listar padecimientos disponibles |
| POST | `/api/padecimientos` | Crear padecimiento (cuidador) |
| PUT | `/api/animales/{id}/vacunas` | Actualizar vacunas de un animal |
| PUT | `/api/animales/{id}/padecimientos` | Actualizar padecimientos de un animal |
| **Imágenes** | | |
| POST | `/uploads/foto-perfil` | Subir foto de perfil |
| POST | `/api/animales/{id}/fotos` | Subir foto de animal |
| DELETE | `/api/animales/{id}/fotos` | Eliminar foto de animal |

## Tests

```bash
./mvnw test
```

Incluye tests unitarios para `AnimalService`, `MailAdapter` e `InteresService`.

## Colección Postman

En `postman/Kotliners-Iteracion5.postman_collection.json` está la colección completa con todos los endpoints del sistema. Importar en Postman y configurar las variables `token`, `token_cuidador` y `adminToken` tras autenticarse.
