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

# APIs externas de razas (opcionales — si no se configuran, la seccion de info de raza no aparece)
DOG_API_KEY=tu_key_de_thedogapi
CAT_API_KEY=tu_key_de_thecatapi
```

### Obtener API keys de razas

Las keys son **gratuitas** y se obtienen en menos de 1 minuto:

- **The Dog API**: [thedogapi.com](https://thedogapi.com) → Sign Up → recibes la key por correo
- **The Cat API**: [thecatapi.com](https://thecatapi.com) → Sign Up → recibes la key por correo

Si no se configuran, el endpoint `/api/razas/info` devuelve 404 y la seccion de informacion de raza simplemente no aparece en la ficha del animal (fallback silencioso).

### Configurar Gmail App Password

1. Activa verificacion en 2 pasos en tu cuenta Google
2. Ve a [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
3. Genera una contrasena de aplicacion para "Mail"
4. Usa esa contrasena en `GMAIL_APP_PASSWORD`

## Base de datos

```bash
# Recrear la base de datos desde cero (incluye tabla raza con 694 razas)
psql -U tu_usuario -d postgres -f database/schema.sql
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
| GET | `/api/animales` | Listar animales (soporta filtros: `especie`, `sexo`, `esterilizado`, `codigoPostal`, `vacuna`, `sinPadecimientos`, `ordenar`) |
| GET | `/api/animales/me` | Animales del cuidador autenticado |
| GET | `/api/animales/{id}` | Detalle de un animal |
| POST | `/api/animales` | Publicar animal (cuidador) |
| DELETE | `/api/animales` | Eliminar animal (cuidador) |
| POST | `/api/animales/{id}/interes` | Manifestar interes (adoptante) |
| DELETE | `/api/animales/{id}/interes` | Quitar interes (adoptante) |
| GET | `/api/usuarios/me/intereses` | Favoritos del adoptante |
| GET | `/api/animales/me/intereses` | Intereses recibidos (cuidador) |
| GET | `/api/razas` | Catalogo de razas por especie (`?especie=PERRO` o `?especie=GATO`) |
| GET | `/api/razas/info` | Info detallada de una raza desde API externa (`?razaId=UUID&especie=PERRO`) |
| POST | `/uploads/foto-perfil` | Subir foto de perfil a Cloudinary |
