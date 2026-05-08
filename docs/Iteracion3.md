# Especificacion de Requerimientos - Colitas Felices

Iteracion: 3
Version del documento: 3.0
Fecha: 8 de mayo de 2026

Este documento complementa y evoluciona el documento entregado en la Iteracion 2. La base funcional previa se mantiene y se agrega el caso de uso correspondiente a Persona 2: Ver detalle de animal.

## Historial de Cambios

| Version | Cambios |
| --- | --- |
| 1.0 | Version inicial del backend y modulo de usuarios |
| 2.0 | Integracion frontend, autenticacion y gestion de usuario autenticado |
| 3.0 | Se agrega consulta autenticada de detalle de animal con vista frontend y documentacion del CU |

## Alcance de Persona 2

Persona 2 cubre la consulta del detalle de un animal registrado.

Backend:
- `GET /animals/{id}`
- Ruta compatible: `GET /api/animales/{id}`
- Requiere `Authorization: Bearer <token>`
- Consulta datos reales mediante `AnimalService` y `AnimalRepository`
- Devuelve `AnimalDetailResponse`

Frontend:
- Vista: `src/main/resources/static/animal-detail.html`
- Entrada: `/animal-detail.html?id={animalId}`
- Consumo HTTP real: `GET /animals/{id}`
- Token tomado de `localStorage` o `sessionStorage`

Documento:
- CU detallado: `docs/CU-VerDetalleAnimal.md`

## Requerimientos Funcionales Agregados

| ID | Requerimiento | Criterios de aceptacion |
| --- | --- | --- |
| RF-DET-01 | Ver detalle de animal | El sistema permite consultar un animal por ID desde una vista funcional. |
| RF-DET-02 | Autenticacion obligatoria | El backend rechaza solicitudes sin token o con token invalido. |
| RF-DET-03 | Consulta real de datos | El detalle se obtiene desde backend y base de datos, sin datos simulados en frontend. |
| RF-DET-04 | Permisos por propietario | La respuesta indica si el usuario autenticado es dueno del animal. |
| RF-DET-05 | Proteccion de acciones de dueno | Editar y eliminar solo se habilitan para el dueno en frontend y se validan nuevamente en backend. |

## CU: Ver Detalle de Animal

Actores:
- Usuario autenticado
- Sistema Colitas Felices

Precondiciones:
- El usuario inicio sesion y cuenta con token valido.
- El animal existe en la base de datos para el flujo normal.

Flujo normal:
1. El usuario abre `/animal-detail.html?id={animalId}`.
2. El frontend lee el token almacenado por el inicio de sesion.
3. El frontend solicita `GET /animals/{id}` con `Authorization: Bearer <token>`.
4. El backend valida el token mediante `UsuarioService`.
5. El backend consulta el animal mediante `AnimalService`.
6. `AnimalService` obtiene el registro desde `AnimalRepository`.
7. El backend responde con `AnimalDetailResponse`.
8. El frontend muestra nombre, especie, raza, sexo, fecha de nacimiento, estatus, esterilizacion y descripcion.

Escenarios alternos:
- Usuario dueno: el sistema responde `esDueno=true`, `puedeEditar=true` y `puedeEliminar=true`; la vista muestra las acciones de dueno.
- Usuario no dueno: el sistema responde `esDueno=false`, `puedeEditar=false` y `puedeEliminar=false`; la vista queda en modo consulta.
- Ruta compatible: el backend tambien acepta `GET /api/animales/{id}` para mantener consistencia con endpoints existentes.

Escenarios de error:
- Token ausente: el backend responde `401 Token requerido`.
- Token invalido: el backend responde `401 Token invalido`.
- Animal inexistente: el backend responde `404 Animal no encontrado`.
- ID invalido: el backend responde `400` mediante el manejador global de `IllegalArgumentException`.
- Error de red: el frontend muestra que no fue posible conectar con el servidor.

Postcondiciones:
- El usuario visualiza el detalle del animal si la solicitud fue valida.
- No se modifica informacion en base de datos durante la consulta.

## Arquitectura

La funcionalidad conserva la arquitectura en capas solicitada:

```text
Frontend -> Controller -> Service -> Repository -> Entity -> Base de datos
```

Componentes implementados:
- Controller: `AnimalController.getAnimal`
- Service: `AnimalService.getAnimalById`
- Repository: `AnimalRepository.findById`
- Entity: `AnimalEntity`
- DTO response: `AnimalDetailResponse`
- Mapper: `Animal.toAnimalDetailResponse`

## Seguridad

- La consulta exige header `Authorization`.
- El token se valida contra el usuario persistido.
- La respuesta incluye permisos calculados desde el `usuarioId` del animal y el usuario autenticado.
- El frontend no decide por si solo si un usuario puede editar o eliminar; usa las banderas del backend.
- Las operaciones de editar y eliminar conservan validacion de propietario en backend.

## Integracion End-to-End

El flujo implementado cumple:

```text
animal-detail.html -> fetch GET /animals/{id} -> AnimalController -> AnimalService -> AnimalRepository -> animal
```

La vista no hardcodea datos del animal ni simula respuestas. Solo renderiza la informacion recibida desde la API real.

## Pruebas

Se agrego cobertura para el controller de animales:
- Rechaza consulta sin token.
- Rechaza token invalido.
- Devuelve detalle y permisos cuando el usuario es dueno.
- Devuelve detalle sin permisos cuando el usuario no es dueno.
- Devuelve 404 cuando el animal no existe.

Comando de verificacion:

```sh
./mvnw test
```

## Limitaciones

- La vista de detalle es estatica y servida desde Spring Boot; no forma parte de un proyecto Next.js separado.
- La entidad `AnimalEntity` no incluye una foto principal, por lo que la vista utiliza un espacio visual generico.
- El tag backend `3.0.0` debe crearse despues de confirmar estos cambios en Git para que apunte al commit correcto.
