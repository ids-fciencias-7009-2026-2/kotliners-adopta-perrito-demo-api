# CU: Ver detalle de animal

Este caso de uso tambien queda integrado en `docs/Iteracion3.md` como parte de la evolucion del documento del proyecto para la Iteracion 3.

## Version del documento

Version: 3.0

## Historial de cambios

| Version | Cambios |
| --- | --- |
| 1.0 | Version inicial del proyecto |
| 2.0 | Integracion frontend y autenticacion |
| 3.0 | Se agrega el caso de uso Ver detalle de animal |

## Objetivo

Permitir que un usuario autenticado consulte la informacion completa de un animal registrado en el sistema desde una vista funcional conectada al backend y a la base de datos.

## Actores

- Usuario autenticado
- Sistema Colitas Felices

## Precondiciones

- El usuario inicio sesion y cuenta con un token valido.
- El animal existe en la base de datos.

## Flujo normal

1. El usuario abre la vista de detalle con el identificador del animal.
2. El frontend obtiene el token guardado por el flujo de autenticacion.
3. El frontend solicita `GET /animals/{id}` con el header `Authorization: Bearer <token>`.
4. El backend valida el token con `UsuarioService`.
5. El backend consulta el animal con `AnimalService` y `AnimalRepository`.
6. El backend responde con los datos del animal y las banderas `esDueno`, `puedeEditar` y `puedeEliminar`.
7. El frontend muestra la informacion del animal.

## Flujos alternos

- Si el usuario autenticado es dueno del animal, la vista muestra que la publicacion le pertenece y habilita las acciones de dueno.
- Si el usuario autenticado no es dueno del animal, la vista muestra el detalle en modo consulta.

## Flujos de error

- Token ausente: el backend responde `401 Token requerido`.
- Token invalido: el backend responde `401 Token invalido`.
- Animal inexistente: el backend responde `404 Animal no encontrado`.
- Identificador invalido: el backend responde `400` mediante el manejador global de excepciones.
- Error de conexion: el frontend muestra un mensaje indicando que no fue posible conectar con el servidor.

## Requerimientos funcionales actualizados

- RF-DET-01: El sistema debe permitir consultar el detalle de un animal por ID.
- RF-DET-02: La consulta de detalle debe requerir token de autenticacion.
- RF-DET-03: La respuesta debe indicar si el usuario autenticado es dueno del animal.
- RF-DET-04: Las acciones de edicion y eliminacion solo deben estar disponibles para el dueno y seguir validadas en backend.

## Arquitectura

La funcionalidad mantiene la arquitectura en capas:

Controller -> Service -> Repository -> Entity -> Base de datos

La respuesta del caso de uso se expone mediante `AnimalDetailResponse`, evitando datos simulados o hardcodeados en el frontend.

## Endpoint

- Metodo: `GET`
- Ruta principal: `/animals/{id}`
- Ruta compatible: `/api/animales/{id}`
- Header: `Authorization: Bearer <token>`

## Vista frontend

- Archivo: `src/main/resources/static/animal-detail.html`
- Entrada esperada: `/animal-detail.html?id={animalId}`
- Consumo HTTP real: `GET /animals/{id}`

## Limitaciones

- La entidad de animal no incluye aun una foto principal, por lo que la vista utiliza un espacio visual generico.
- La vista depende de que el flujo de login guarde el token en `localStorage` o `sessionStorage`.
