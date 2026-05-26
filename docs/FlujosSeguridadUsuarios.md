# Flujos de seguridad de usuarios

Este documento describe los flujos implementados para verificacion de correo,
recuperacion de contrasena, doble factor de autenticacion (2FA) y bloqueo por
intentos fallidos.

## Verificacion de correo

### Como funciona

1. El usuario se registra en `POST /usuarios/register`.
2. El backend guarda el usuario con `emailVerificado = false`.
3. Se genera un token UUID con expiracion de 24 horas.
4. Se envia un correo con asunto `Verifica tu correo - Colitas Felices`.
5. El usuario confirma el token en `POST /usuarios/verify-email`.
6. Si el token existe y no expiro, la cuenta queda verificada.

### Endpoints

```http
POST /usuarios/register
Content-Type: application/json
```

```http
POST /usuarios/verify-email
Content-Type: application/json

{
  "token": "uuid-del-correo"
}
```

### Correos enviados

- Asunto: `Verifica tu correo - Colitas Felices`
- Contenido: saludo, token de verificacion y vigencia de 24 horas.
- Tambien se reenvia un token nuevo si el usuario intenta hacer login sin haber verificado su correo.

## Recuperacion de contrasena

### Como funciona

1. El usuario solicita recuperacion en `POST /usuarios/password-reset/request`.
2. La respuesta es neutra: no revela si el correo existe.
3. Si el correo existe, se genera un token UUID con expiracion de 30 minutos.
4. Se envia un correo con asunto `Recuperacion de contrasena - Colitas Felices`.
5. El usuario confirma el cambio en `POST /usuarios/password-reset/confirm`.
6. Si el token es valido, se actualiza la contrasena, se limpian intentos fallidos, bloqueo y token activo.

### Endpoints

```http
POST /usuarios/password-reset/request
Content-Type: application/json

{
  "email": "usuario@correo.com"
}
```

```http
POST /usuarios/password-reset/confirm
Content-Type: application/json

{
  "token": "uuid-del-correo",
  "newPassword": "nuevaPassword123"
}
```

### Correos enviados

- Asunto: `Recuperacion de contrasena - Colitas Felices`
- Contenido: token de recuperacion y vigencia de 30 minutos.

## Doble factor de autenticacion (2FA)

### Como funciona

1. El usuario inicia sesion normalmente.
2. Si tiene `twoFactorEnabled = true`, el backend no entrega token de sesion todavia.
3. Se genera un codigo numerico de 6 digitos con expiracion de 10 minutos.
4. Se envia un correo con asunto `Codigo de seguridad 2FA - Colitas Felices`.
5. El cliente recibe HTTP `202 Accepted` y debe pedir el codigo al usuario.
6. El usuario confirma en `POST /usuarios/2fa/verify`.
7. Si el codigo coincide y no expiro, se genera el token de sesion.

### Endpoints

```http
POST /usuarios/2fa/enable
Authorization: Bearer <token>
```

```http
POST /usuarios/2fa/disable
Authorization: Bearer <token>
```

```http
POST /usuarios/2fa/verify
Content-Type: application/json

{
  "email": "usuario@correo.com",
  "code": "123456"
}
```

### Respuesta de login cuando 2FA esta activo

```http
HTTP/1.1 202 Accepted
```

```json
{
  "requiere2FA": true,
  "mensaje": "Codigo 2FA enviado al correo registrado"
}
```

### Correos enviados

- Asunto: `Codigo de seguridad 2FA - Colitas Felices`
- Contenido: codigo de 6 digitos y vigencia de 10 minutos.

## Bloqueo por intentos fallidos

### Como funciona

1. Cada login con contrasena incorrecta incrementa `intentosFallidos`.
2. Al tercer intento fallido consecutivo, la cuenta se bloquea por 15 minutos.
3. Durante el bloqueo, el login responde HTTP `423 Locked`.
4. Se envia correo de alerta con asunto `Cuenta bloqueada temporalmente - Colitas Felices`.
5. Un login exitoso, un reset de contrasena o un 2FA exitoso limpian los intentos fallidos.

### Endpoint afectado

```http
POST /usuarios/login
Content-Type: application/json

{
  "email": "usuario@correo.com",
  "password": "password"
}
```

### Posibles respuestas de login

- `200 OK`: credenciales correctas, correo verificado y 2FA apagado. Devuelve token.
- `202 Accepted`: credenciales correctas, correo verificado y 2FA encendido. Envia codigo por correo.
- `401 Unauthorized`: credenciales incorrectas.
- `403 Forbidden`: correo no verificado.
- `423 Locked`: cuenta bloqueada temporalmente.

## Casos de prueba cubiertos

- Login exitoso genera token de sesion.
- Credenciales incorrectas no generan token.
- Tres credenciales incorrectas bloquean la cuenta.
- Login con 2FA habilitado genera codigo y no entrega token inicial.
- Codigo 2FA valido genera token de sesion.
- Regresiones existentes de token, actualizacion de usuario y detalle de animal.

## Tareas para tablero Notion

| Tarea | Estado | Notas |
| --- | --- | --- |
| Agregar campos de seguridad a `usuario` | Hecho | Incluye verificacion, reset, 2FA e intentos fallidos. |
| Implementar verificacion de correo | Hecho | `POST /usuarios/verify-email`. |
| Implementar recuperacion de contrasena | Hecho | Solicitud y confirmacion con token. |
| Implementar 2FA por correo | Hecho | Activar, desactivar y verificar codigo. |
| Implementar bloqueo por intentos | Hecho | 3 intentos, bloqueo de 15 minutos. |
| Documentar endpoints y correos | Hecho | Este documento. |
| Agregar pruebas de login, errores, bloqueo y 2FA | Hecho | Suite Maven en verde. |
| Registrar avance en Notion | Pendiente externo | Requiere acceso/conector al tablero de Notion. |
