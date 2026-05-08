# Colitas Felices - Backend
Kotliners

## Estructura del repositorio
```
/src
/postman
/database
    schema.sql
/README.md
```

## Pasos para el levantamiento del proyecto

### 1. Verificar que PostgreSQL esté activo
Asegúrate de tener PostgreSQL corriendo en tu máquina.

### 2. Construir la base de datos con schema.sql
Ejecuta el script principal para crear todas las tablas desde cero:
```sh
psql -U postgres -f database/schema.sql
```
Este archivo contiene toda la estructura de la base de datos y puede ejecutarse en cualquier computadora.

### 3. Configurar variables de entorno
Copia el archivo de ejemplo y completa tus datos:
```sh
cp .env.example .env
```
Edita `.env` con tus credenciales:
```
URL_DB=localhost:5432/colitas_db
USER_DB=postgres
PASSWORD_DB=tu_password
```

### 4. Levantar el proyecto
```sh
./mvnw spring-boot:run
```
Verifica que aparezca `Started AdoptaPerritoApplicationKt` en la consola.

## Iteracion 3 - Ver detalle de animal

### Backend

Endpoint autenticado:
```http
GET /animals/{id}
Authorization: Bearer <token>
```

Tambien se mantiene la ruta compatible:
```http
GET /api/animales/{id}
Authorization: Bearer <token>
```

La respuesta incluye los datos del animal y banderas para saber si el usuario autenticado es dueno de la publicacion:
`esDueno`, `puedeEditar` y `puedeEliminar`.

### Frontend

Vista estatica servida por Spring:
```text
http://localhost:8080/animal-detail.html?id={animalId}
```

La vista consume la API real con token desde `localStorage` o `sessionStorage`; no usa datos simulados.

### Documento

El caso de uso esta documentado en:
```text
docs/CU-VerDetalleAnimal.md
```

La evolucion de la Iteracion 3 para Persona 2 esta integrada en:
```text
docs/Iteracion3.md
```
