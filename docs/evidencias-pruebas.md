# Evidencias de pruebas de los endpoints

Generadas automáticamente por `UsuarioApiIntegrationTest` (`mvn test`) contra un PostgreSQL real (embebido) creado con `db/script_bd.sql`.
Cada caso muestra la solicitud enviada, el código HTTP y la respuesta JSON obtenida.

Resultado de la ejecución: **60 pruebas, 0 fallas** (27 de integración, 27 de validación de DTOs y 6 unitarias del servicio).

| # | Caso | Método y ruta | HTTP |
|---|---|---|---|
| 1 | Crear usuario (exitoso) | `POST /api/v1/usuarios` | 201 |
| 2 | Crear segundo usuario (sin segundo nombre) | `POST /api/v1/usuarios` | 201 |
| 3 | Crear con correo ya registrado (aunque cambien las mayusculas) | `POST /api/v1/usuarios` | 409 |
| 4 | Crear con CURP ya registrada (restriccion UNIQUE de la BD) | `POST /api/v1/usuarios` | 409 |
| 5 | Crear con datos no validos (Bean Validation) | `POST /api/v1/usuarios` | 400 |
| 6 | Crear enviando id, fechaCreacion y activo (el servidor los ignora) | `POST /api/v1/usuarios` | 201 |
| 7 | Crear con un valor fuera del catalogo (enum Sexo) | `POST /api/v1/usuarios` | 400 |
| 8 | Listar usuarios (paginado) | `GET /api/v1/usuarios` | 200 |
| 9 | Busqueda dinamica: nombre parcial + activo | `POST /api/v1/usuarios/busqueda` | 200 |
| 10 | Busqueda dinamica: solo correo | `POST /api/v1/usuarios/busqueda` | 200 |
| 11 | Busqueda dinamica sin criterios | `POST /api/v1/usuarios/busqueda` | 400 |
| 12 | Busqueda dinamica con pagina y tamanio nulos (@NotNull) | `POST /api/v1/usuarios/busqueda` | 400 |
| 13 | Consultar por nombre + correo (exitoso) | `POST /api/v1/usuarios/consulta` | 200 |
| 14 | Consultar cuando nombre y correo no coinciden | `POST /api/v1/usuarios/consulta` | 404 |
| 15 | Consultar con campos vacios | `POST /api/v1/usuarios/consulta` | 400 |
| 16 | Actualizar telefono y activo (segundoNombre omitido no cambia) | `PATCH /api/v1/usuarios/actualizacion` | 200 |
| 17 | Actualizar segundoNombre con null explicito (se borra) | `PATCH /api/v1/usuarios/actualizacion` | 200 |
| 18 | Actualizar con un correo que ya usa otro usuario | `PATCH /api/v1/usuarios/actualizacion` | 409 |
| 19 | Actualizar nombre y correo (cambio de llave) | `PATCH /api/v1/usuarios/actualizacion` | 200 |
| 20 | Actualizar sin campos a modificar | `PATCH /api/v1/usuarios/actualizacion` | 400 |
| 21 | Actualizar usuario inexistente | `PATCH /api/v1/usuarios/actualizacion` | 404 |
| 22 | Eliminar por nombre + correo (exitoso) | `DELETE /api/v1/usuarios/eliminacion` | 200 |
| 23 | Eliminar un usuario que ya no existe | `DELETE /api/v1/usuarios/eliminacion` | 404 |
| 24 | JSON mal formado | `POST /api/v1/usuarios` | 400 |
| 25 | Ruta inexistente | `GET /api/v1/usuarios/1` | 404 |
| 26 | Metodo HTTP no permitido | `PUT /api/v1/usuarios` | 405 |

---

### 1. Crear usuario (exitoso)

`POST /api/v1/usuarios`, resultado: **HTTP 201**

Solicitud:
```json
{
  "nombre": "Juan",
  "segundoNombre": "Carlos",
  "apellidoPaterno": "Perez",
  "apellidoMaterno": "Gomez",
  "fechaNacimiento": "1990-01-01",
  "curp": "PEGJ900101HGTRRN09",
  "rfc": "PEGJ900101AB1",
  "sexo": "HOMBRE",
  "nacionalidad": "MEXICANA",
  "estadoCivil": "SOLTERO",
  "correo": "juan.perez@correo.com",
  "telefono": "4771234567"
}
```

Respuesta:
```json
{
  "mensaje" : "Usuario registrado correctamente.",
  "datos" : {
    "id" : 1,
    "nombre" : "Juan",
    "segundoNombre" : "Carlos",
    "apellidoPaterno" : "Perez",
    "apellidoMaterno" : "Gomez",
    "fechaNacimiento" : "1990-01-01",
    "curp" : "PEGJ900101HGTRRN09",
    "rfc" : "PEGJ900101AB1",
    "sexo" : "HOMBRE",
    "nacionalidad" : "MEXICANA",
    "estadoCivil" : "SOLTERO",
    "correo" : "juan.perez@correo.com",
    "telefono" : "4771234567",
    "activo" : true,
    "fechaCreacion" : "2026-10-22T17:40:35"
  }
}
```

### 2. Crear segundo usuario (sin segundo nombre)

`POST /api/v1/usuarios`, resultado: **HTTP 201**

Solicitud:
```json
{
  "nombre": "Maria",
  "apellidoPaterno": "Lopez",
  "apellidoMaterno": "Diaz",
  "fechaNacimiento": "1995-06-15",
  "curp": "LODM950615MGTPZR01",
  "rfc": "LODM950615CD2",
  "sexo": "MUJER",
  "nacionalidad": "MEXICANA",
  "estadoCivil": "CASADO",
  "correo": "maria.lopez@correo.com",
  "telefono": "4777654321"
}
```

Respuesta:
```json
{
  "mensaje" : "Usuario registrado correctamente.",
  "datos" : {
    "id" : 2,
    "nombre" : "Maria",
    "segundoNombre" : null,
    "apellidoPaterno" : "Lopez",
    "apellidoMaterno" : "Diaz",
    "fechaNacimiento" : "1995-06-15",
    "curp" : "LODM950615MGTPZR01",
    "rfc" : "LODM950615CD2",
    "sexo" : "MUJER",
    "nacionalidad" : "MEXICANA",
    "estadoCivil" : "CASADO",
    "correo" : "maria.lopez@correo.com",
    "telefono" : "4777654321",
    "activo" : true,
    "fechaCreacion" : "2026-10-22T17:40:36"
  }
}
```

### 3. Crear con correo ya registrado (aunque cambien las mayusculas)

`POST /api/v1/usuarios`, resultado: **HTTP 409**

Solicitud:
```json
{
  "nombre": "Maria",
  "apellidoPaterno": "Lopez",
  "apellidoMaterno": "Diaz",
  "fechaNacimiento": "1995-06-15",
  "curp": "LODM950615MGTPZR02",
  "rfc": "LODM950615CD3",
  "sexo": "MUJER",
  "nacionalidad": "MEXICANA",
  "estadoCivil": "CASADO",
  "correo": "Juan.Perez@Correo.com",
  "telefono": "4777654321"
}
```

Respuesta:
```json
{
  "codigo" : "CORREO_DUPLICADO",
  "mensaje" : "El correo ya se encuentra registrado. Intente con otro correo."
}
```

### 4. Crear con CURP ya registrada (restriccion UNIQUE de la BD)

`POST /api/v1/usuarios`, resultado: **HTTP 409**

Solicitud:
```json
{
  "nombre": "Maria",
  "apellidoPaterno": "Lopez",
  "apellidoMaterno": "Diaz",
  "fechaNacimiento": "1995-06-15",
  "curp": "PEGJ900101HGTRRN09",
  "rfc": "LODM950615CD3",
  "sexo": "MUJER",
  "nacionalidad": "MEXICANA",
  "estadoCivil": "CASADO",
  "correo": "otra@correo.com",
  "telefono": "4777654321"
}
```

Respuesta:
```json
{
  "codigo" : "INTEGRIDAD_DATOS",
  "mensaje" : "La CURP ya se encuentra registrada."
}
```

### 5. Crear con datos no validos (Bean Validation)

`POST /api/v1/usuarios`, resultado: **HTTP 400**

Solicitud:
```json
{
  "nombre": "   ",
  "apellidoPaterno": "Perez",
  "apellidoMaterno": "Gomez",
  "fechaNacimiento": "2999-01-01",
  "curp": "",
  "rfc": "XXXX",
  "sexo": "HOMBRE",
  "nacionalidad": "MEXICANA",
  "estadoCivil": "SOLTERO",
  "correo": "correo-sin-arroba",
  "telefono": "12345"
}
```

Respuesta:
```json
{
  "codigo" : "DATOS_INVALIDOS",
  "mensaje" : "Verifique la información capturada e intente nuevamente.",
  "errores" : {
    "curp" : "La CURP es obligatoria.",
    "nombre" : "El nombre es obligatorio y no puede contener solo espacios.",
    "rfc" : "El RFC no tiene un formato valido (13 caracteres de persona fisica).",
    "fechaNacimiento" : "La fecha de nacimiento debe ser una fecha pasada.",
    "correo" : "El correo electrónico no tiene un formato válido.",
    "telefono" : "El teléfono debe contener exactamente 10 dígitos."
  }
}
```

### 6. Crear enviando id, fechaCreacion y activo (el servidor los ignora)

`POST /api/v1/usuarios`, resultado: **HTTP 201**

Solicitud:
```json
{
  "id": 999,
  "fechaCreacion": "2000-01-01T00:00:00",
  "activo": false,
  "nombre": "Pedro",
  "apellidoPaterno": "Ramirez",
  "apellidoMaterno": "Soto",
  "fechaNacimiento": "1988-03-10",
  "curp": "RASP880310HGTMTD05",
  "rfc": "RASP880310EF4",
  "sexo": "HOMBRE",
  "nacionalidad": "EXTRANJERA",
  "estadoCivil": "DIVORCIADO",
  "correo": "pedro.ramirez@correo.com",
  "telefono": "4771112233"
}
```

Respuesta:
```json
{
  "mensaje" : "Usuario registrado correctamente.",
  "datos" : {
    "id" : 4,
    "nombre" : "Pedro",
    "segundoNombre" : null,
    "apellidoPaterno" : "Ramirez",
    "apellidoMaterno" : "Soto",
    "fechaNacimiento" : "1988-03-10",
    "curp" : "RASP880310HGTMTD05",
    "rfc" : "RASP880310EF4",
    "sexo" : "HOMBRE",
    "nacionalidad" : "EXTRANJERA",
    "estadoCivil" : "DIVORCIADO",
    "correo" : "pedro.ramirez@correo.com",
    "telefono" : "4771112233",
    "activo" : true,
    "fechaCreacion" : "2026-10-22T17:40:36"
  }
}
```

### 7. Crear con un valor fuera del catalogo (enum Sexo)

`POST /api/v1/usuarios`, resultado: **HTTP 400**

Solicitud:
```json
{
  "nombre": "Juan",
  "segundoNombre": "Carlos",
  "apellidoPaterno": "Perez",
  "apellidoMaterno": "Gomez",
  "fechaNacimiento": "1990-01-01",
  "curp": "PEGJ900101HGTRRN09",
  "rfc": "PEGJ900101AB1",
  "sexo": "OTRO",
  "nacionalidad": "MEXICANA",
  "estadoCivil": "SOLTERO",
  "correo": "juan.perez@correo.com",
  "telefono": "4771234567"
}
```

Respuesta:
```json
{
  "codigo" : "SOLICITUD_INVALIDA",
  "mensaje" : "El cuerpo de la solicitud no tiene un formato válido.",
  "errores" : {
    "sexo" : "Valor no valido. Valores permitidos: [HOMBRE, MUJER, NO_BINARIO]"
  }
}
```

### 8. Listar usuarios (paginado)

`GET /api/v1/usuarios`, resultado: **HTTP 200**

Respuesta:
```json
{
  "content" : [ {
    "id" : 4,
    "nombre" : "Pedro",
    "segundoNombre" : null,
    "apellidoPaterno" : "Ramirez",
    "apellidoMaterno" : "Soto",
    "fechaNacimiento" : "1988-03-10",
    "curp" : "RASP880310HGTMTD05",
    "rfc" : "RASP880310EF4",
    "sexo" : "HOMBRE",
    "nacionalidad" : "EXTRANJERA",
    "estadoCivil" : "DIVORCIADO",
    "correo" : "pedro.ramirez@correo.com",
    "telefono" : "4771112233",
    "activo" : true,
    "fechaCreacion" : "2026-10-22T17:40:36"
  }, {
    "id" : 2,
    "nombre" : "Maria",
    "segundoNombre" : null,
    "apellidoPaterno" : "Lopez",
    "apellidoMaterno" : "Diaz",
    "fechaNacimiento" : "1995-06-15",
    "curp" : "LODM950615MGTPZR01",
    "rfc" : "LODM950615CD2",
    "sexo" : "MUJER",
    "nacionalidad" : "MEXICANA",
    "estadoCivil" : "CASADO",
    "correo" : "maria.lopez@correo.com",
    "telefono" : "4777654321",
    "activo" : true,
    "fechaCreacion" : "2026-10-22T17:40:36"
  }, {
    "id" : 1,
    "nombre" : "Juan",
    "segundoNombre" : "Carlos",
    "apellidoPaterno" : "Perez",
    "apellidoMaterno" : "Gomez",
    "fechaNacimiento" : "1990-01-01",
    "curp" : "PEGJ900101HGTRRN09",
    "rfc" : "PEGJ900101AB1",
    "sexo" : "HOMBRE",
    "nacionalidad" : "MEXICANA",
    "estadoCivil" : "SOLTERO",
    "correo" : "juan.perez@correo.com",
    "telefono" : "4771234567",
    "activo" : true,
    "fechaCreacion" : "2026-10-22T17:40:35"
  } ],
  "page" : {
    "size" : 10,
    "number" : 0,
    "totalElements" : 3,
    "totalPages" : 1
  }
}
```

### 9. Busqueda dinamica: nombre parcial + activo

`POST /api/v1/usuarios/busqueda`, resultado: **HTTP 200**

Solicitud:
```json
{ "nombre": "jua", "correo": null, "telefono": null, "activo": true }
```

Respuesta:
```json
{
  "content" : [ {
    "id" : 1,
    "nombre" : "Juan",
    "segundoNombre" : "Carlos",
    "apellidoPaterno" : "Perez",
    "apellidoMaterno" : "Gomez",
    "fechaNacimiento" : "1990-01-01",
    "curp" : "PEGJ900101HGTRRN09",
    "rfc" : "PEGJ900101AB1",
    "sexo" : "HOMBRE",
    "nacionalidad" : "MEXICANA",
    "estadoCivil" : "SOLTERO",
    "correo" : "juan.perez@correo.com",
    "telefono" : "4771234567",
    "activo" : true,
    "fechaCreacion" : "2026-10-22T17:40:35"
  } ],
  "page" : {
    "size" : 10,
    "number" : 0,
    "totalElements" : 1,
    "totalPages" : 1
  }
}
```

### 10. Busqueda dinamica: solo correo

`POST /api/v1/usuarios/busqueda`, resultado: **HTTP 200**

Solicitud:
```json
{ "correo": "maria.lopez@correo.com" }
```

Respuesta:
```json
{
  "content" : [ {
    "id" : 2,
    "nombre" : "Maria",
    "segundoNombre" : null,
    "apellidoPaterno" : "Lopez",
    "apellidoMaterno" : "Diaz",
    "fechaNacimiento" : "1995-06-15",
    "curp" : "LODM950615MGTPZR01",
    "rfc" : "LODM950615CD2",
    "sexo" : "MUJER",
    "nacionalidad" : "MEXICANA",
    "estadoCivil" : "CASADO",
    "correo" : "maria.lopez@correo.com",
    "telefono" : "4777654321",
    "activo" : true,
    "fechaCreacion" : "2026-10-22T17:40:36"
  } ],
  "page" : {
    "size" : 10,
    "number" : 0,
    "totalElements" : 1,
    "totalPages" : 1
  }
}
```

### 11. Busqueda dinamica sin criterios

`POST /api/v1/usuarios/busqueda`, resultado: **HTTP 400**

Solicitud:
```json
{ }
```

Respuesta:
```json
{
  "codigo" : "DATOS_INVALIDOS",
  "mensaje" : "Verifique la información capturada e intente nuevamente.",
  "errores" : {
    "algunCriterio" : "Debe enviar al menos un criterio de búsqueda (nombre, correo, telefono o activo)."
  }
}
```

### 12. Busqueda dinamica con pagina y tamanio nulos (@NotNull)

`POST /api/v1/usuarios/busqueda`, resultado: **HTTP 400**

Solicitud:
```json
{ "nombre": "Juan", "pagina": null, "tamanio": null }
```

Respuesta:
```json
{
  "codigo" : "DATOS_INVALIDOS",
  "mensaje" : "Verifique la información capturada e intente nuevamente.",
  "errores" : {
    "tamanio" : "El tamaño de pagina no puede ser nulo.",
    "pagina" : "La pagina no puede ser nula."
  }
}
```

### 13. Consultar por nombre + correo (exitoso)

`POST /api/v1/usuarios/consulta`, resultado: **HTTP 200**

Solicitud:
```json
{ "nombre": "Juan", "correo": "juan.perez@correo.com" }
```

Respuesta:
```json
{
  "id" : 1,
  "nombre" : "Juan",
  "segundoNombre" : "Carlos",
  "apellidoPaterno" : "Perez",
  "apellidoMaterno" : "Gomez",
  "fechaNacimiento" : "1990-01-01",
  "curp" : "PEGJ900101HGTRRN09",
  "rfc" : "PEGJ900101AB1",
  "sexo" : "HOMBRE",
  "nacionalidad" : "MEXICANA",
  "estadoCivil" : "SOLTERO",
  "correo" : "juan.perez@correo.com",
  "telefono" : "4771234567",
  "activo" : true,
  "fechaCreacion" : "2026-10-22T17:40:35"
}
```

### 14. Consultar cuando nombre y correo no coinciden

`POST /api/v1/usuarios/consulta`, resultado: **HTTP 404**

Solicitud:
```json
{ "nombre": "Maria", "correo": "juan.perez@correo.com" }
```

Respuesta:
```json
{
  "codigo" : "USUARIO_NO_ENCONTRADO",
  "mensaje" : "No existe un usuario con el nombre y correo electrónico proporcionados."
}
```

### 15. Consultar con campos vacios

`POST /api/v1/usuarios/consulta`, resultado: **HTTP 400**

Solicitud:
```json
{ "nombre": " ", "correo": "" }
```

Respuesta:
```json
{
  "codigo" : "DATOS_INVALIDOS",
  "mensaje" : "Verifique la información capturada e intente nuevamente.",
  "errores" : {
    "correo" : "El correo electrónico es obligatorio.",
    "nombre" : "El nombre es obligatorio y no puede contener solo espacios."
  }
}
```

### 16. Actualizar telefono y activo (segundoNombre omitido no cambia)

`PATCH /api/v1/usuarios/actualizacion`, resultado: **HTTP 200**

Solicitud:
```json
{
  "nombre": "Juan",
  "correo": "juan.perez@correo.com",
  "telefono": "4779876543",
  "activo": false
}
```

Respuesta:
```json
{
  "mensaje" : "Usuario actualizado correctamente.",
  "datos" : {
    "id" : 1,
    "nombre" : "Juan",
    "segundoNombre" : "Carlos",
    "apellidoPaterno" : "Perez",
    "apellidoMaterno" : "Gomez",
    "fechaNacimiento" : "1990-01-01",
    "curp" : "PEGJ900101HGTRRN09",
    "rfc" : "PEGJ900101AB1",
    "sexo" : "HOMBRE",
    "nacionalidad" : "MEXICANA",
    "estadoCivil" : "SOLTERO",
    "correo" : "juan.perez@correo.com",
    "telefono" : "4779876543",
    "activo" : false,
    "fechaCreacion" : "2026-10-22T17:40:35"
  }
}
```

### 17. Actualizar segundoNombre con null explicito (se borra)

`PATCH /api/v1/usuarios/actualizacion`, resultado: **HTTP 200**

Solicitud:
```json
{ "nombre": "Juan", "correo": "juan.perez@correo.com", "segundoNombre": null }
```

Respuesta:
```json
{
  "mensaje" : "Usuario actualizado correctamente.",
  "datos" : {
    "id" : 1,
    "nombre" : "Juan",
    "segundoNombre" : null,
    "apellidoPaterno" : "Perez",
    "apellidoMaterno" : "Gomez",
    "fechaNacimiento" : "1990-01-01",
    "curp" : "PEGJ900101HGTRRN09",
    "rfc" : "PEGJ900101AB1",
    "sexo" : "HOMBRE",
    "nacionalidad" : "MEXICANA",
    "estadoCivil" : "SOLTERO",
    "correo" : "juan.perez@correo.com",
    "telefono" : "4779876543",
    "activo" : false,
    "fechaCreacion" : "2026-10-22T17:40:35"
  }
}
```

### 18. Actualizar con un correo que ya usa otro usuario

`PATCH /api/v1/usuarios/actualizacion`, resultado: **HTTP 409**

Solicitud:
```json
{ "nombre": "Juan", "correo": "juan.perez@correo.com", "nuevoCorreo": "maria.lopez@correo.com" }
```

Respuesta:
```json
{
  "codigo" : "CORREO_DUPLICADO",
  "mensaje" : "El correo ya se encuentra registrado. Intente con otro correo."
}
```

### 19. Actualizar nombre y correo (cambio de llave)

`PATCH /api/v1/usuarios/actualizacion`, resultado: **HTTP 200**

Solicitud:
```json
{
  "nombre": "Juan",
  "correo": "juan.perez@correo.com",
  "nuevoNombre": "Juan Pablo",
  "nuevoCorreo": "JuanPablo.Perez@correo.com"
}
```

Respuesta:
```json
{
  "mensaje" : "Usuario actualizado correctamente.",
  "datos" : {
    "id" : 1,
    "nombre" : "Juan Pablo",
    "segundoNombre" : null,
    "apellidoPaterno" : "Perez",
    "apellidoMaterno" : "Gomez",
    "fechaNacimiento" : "1990-01-01",
    "curp" : "PEGJ900101HGTRRN09",
    "rfc" : "PEGJ900101AB1",
    "sexo" : "HOMBRE",
    "nacionalidad" : "MEXICANA",
    "estadoCivil" : "SOLTERO",
    "correo" : "juanpablo.perez@correo.com",
    "telefono" : "4779876543",
    "activo" : false,
    "fechaCreacion" : "2026-10-22T17:40:35"
  }
}
```

### 20. Actualizar sin campos a modificar

`PATCH /api/v1/usuarios/actualizacion`, resultado: **HTTP 400**

Solicitud:
```json
{ "nombre": "Juan Pablo", "correo": "juanpablo.perez@correo.com" }
```

Respuesta:
```json
{
  "codigo" : "DATOS_INVALIDOS",
  "mensaje" : "Verifique la información capturada e intente nuevamente.",
  "errores" : {
    "algunCampoParaActualizar" : "Debe enviar al menos un campo para actualizar."
  }
}
```

### 21. Actualizar usuario inexistente

`PATCH /api/v1/usuarios/actualizacion`, resultado: **HTTP 404**

Solicitud:
```json
{ "nombre": "Juan", "correo": "juan.perez@correo.com", "telefono": "4770000000" }
```

Respuesta:
```json
{
  "codigo" : "USUARIO_NO_ENCONTRADO",
  "mensaje" : "No existe un usuario con el nombre y correo electrónico proporcionados."
}
```

### 22. Eliminar por nombre + correo (exitoso)

`DELETE /api/v1/usuarios/eliminacion`, resultado: **HTTP 200**

Solicitud:
```json
{ "nombre": "Maria", "correo": "maria.lopez@correo.com" }
```

Respuesta:
```json
{
  "mensaje" : "Usuario eliminado correctamente."
}
```

### 23. Eliminar un usuario que ya no existe

`DELETE /api/v1/usuarios/eliminacion`, resultado: **HTTP 404**

Solicitud:
```json
{ "nombre": "Maria", "correo": "maria.lopez@correo.com" }
```

Respuesta:
```json
{
  "codigo" : "USUARIO_NO_ENCONTRADO",
  "mensaje" : "No existe un usuario con el nombre y correo electrónico proporcionados."
}
```

### 24. JSON mal formado

`POST /api/v1/usuarios`, resultado: **HTTP 400**

Solicitud:
```json
{ "nombre": 
```

Respuesta:
```json
{
  "codigo" : "SOLICITUD_INVALIDA",
  "mensaje" : "El cuerpo de la solicitud no tiene un formato válido."
}
```

### 25. Ruta inexistente

`GET /api/v1/usuarios/1`, resultado: **HTTP 404**

Respuesta:
```json
{
  "codigo" : "SOLICITUD_INVALIDA",
  "mensaje" : "El recurso solicitado no existe. Consulte la documentacion en /swagger-ui.html."
}
```

### 26. Metodo HTTP no permitido

`PUT /api/v1/usuarios`, resultado: **HTTP 405**

Solicitud:
```json
{
  "nombre": "Juan",
  "segundoNombre": "Carlos",
  "apellidoPaterno": "Perez",
  "apellidoMaterno": "Gomez",
  "fechaNacimiento": "1990-01-01",
  "curp": "PEGJ900101HGTRRN09",
  "rfc": "PEGJ900101AB1",
  "sexo": "HOMBRE",
  "nacionalidad": "MEXICANA",
  "estadoCivil": "SOLTERO",
  "correo": "juan.perez@correo.com",
  "telefono": "4771234567"
}
```

Respuesta:
```json
{
  "codigo" : "SOLICITUD_INVALIDA",
  "mensaje" : "El metodo HTTP no esta permitido para este recurso."
}
```

---

## Pruebas en producción (Railway + Neon)

Ejecutadas el 2026-10-20 04:16 UTC contra la API desplegada en **https://api-usuarios-production-7dde.up.railway.app**, con los datos guardados en la base `usuarios_db` de Neon.
El usuario de prueba se crea al inicio y se elimina al final, así que la tabla queda como estaba.

Estas pruebas se ejecutaron sobre la versión desplegada en esa fecha, anterior a los ajustes de validación de los DTOs (correo estricto, `@NotNull` en la paginación y prioridad del mensaje de campo obligatorio). Esos ajustes están cubiertos por las pruebas automáticas de este documento; el redespliegue en Railway queda pendiente porque la plataforma no está permitiendo despliegues.

| # | Caso | Método y ruta | HTTP |
|---|---|---|---|
| 1 | Crear usuario | `POST /api/v1/usuarios` | 201 |
| 2 | Crear con correo duplicado | `POST /api/v1/usuarios` | 409 |
| 3 | Crear con datos no validos | `POST /api/v1/usuarios` | 400 |
| 4 | Listar usuarios (paginado) | `GET /api/v1/usuarios` | 200 |
| 5 | Busqueda dinamica: nombre parcial + activo | `POST /api/v1/usuarios/busqueda` | 200 |
| 6 | Consultar por nombre + correo | `POST /api/v1/usuarios/consulta` | 200 |
| 7 | Consultar sin coincidencia | `POST /api/v1/usuarios/consulta` | 404 |
| 8 | Actualizar telefono y estado civil | `PATCH /api/v1/usuarios/actualizacion` | 200 |
| 9 | Eliminar por nombre + correo | `DELETE /api/v1/usuarios/eliminacion` | 200 |
| 10 | Eliminar usuario ya eliminado | `DELETE /api/v1/usuarios/eliminacion` | 404 |

### P1. Crear usuario

`POST /api/v1/usuarios`, resultado: **HTTP 201**

Solicitud:
```json
{
  "nombre": "Ana",
  "segundoNombre": "Sofia",
  "apellidoPaterno": "Torres",
  "apellidoMaterno": "Vega",
  "fechaNacimiento": "1998-04-12",
  "curp": "TOVA980412MGTRGN03",
  "rfc": "TOVA980412KL5",
  "sexo": "MUJER",
  "nacionalidad": "MEXICANA",
  "estadoCivil": "SOLTERO",
  "correo": "ana.torres@correo.com",
  "telefono": "4775551234"
}
```

Respuesta:
```json
{
  "mensaje": "Usuario registrado correctamente.",
  "datos": {
    "id": 3,
    "nombre": "Ana",
    "segundoNombre": "Sofia",
    "apellidoPaterno": "Torres",
    "apellidoMaterno": "Vega",
    "fechaNacimiento": "1998-04-12",
    "curp": "TOVA980412MGTRGN03",
    "rfc": "TOVA980412KL5",
    "sexo": "MUJER",
    "nacionalidad": "MEXICANA",
    "estadoCivil": "SOLTERO",
    "correo": "ana.torres@correo.com",
    "telefono": "4775551234",
    "activo": true,
    "fechaCreacion": "2026-10-20T04:16:49"
  }
}
```

### P2. Crear con correo duplicado

`POST /api/v1/usuarios`, resultado: **HTTP 409**

Solicitud:
```json
{
  "nombre": "Ana",
  "segundoNombre": "Sofia",
  "apellidoPaterno": "Torres",
  "apellidoMaterno": "Vega",
  "fechaNacimiento": "1998-04-12",
  "curp": "TOVA980412MGTRGN04",
  "rfc": "TOVA980412KL6",
  "sexo": "MUJER",
  "nacionalidad": "MEXICANA",
  "estadoCivil": "SOLTERO",
  "correo": "ana.torres@correo.com",
  "telefono": "4775551234"
}
```

Respuesta:
```json
{
  "codigo": "CORREO_DUPLICADO",
  "mensaje": "El correo ya se encuentra registrado. Intente con otro correo."
}
```

### P3. Crear con datos no validos

`POST /api/v1/usuarios`, resultado: **HTTP 400**

Solicitud:
```json
{
  "nombre": "  ",
  "correo": "sin-arroba",
  "telefono": "123"
}
```

Respuesta:
```json
{
  "codigo": "DATOS_INVALIDOS",
  "mensaje": "Verifique la información capturada e intente nuevamente.",
  "errores": {
    "curp": "La CURP es obligatoria.",
    "sexo": "El sexo es obligatorio.",
    "apellidoPaterno": "El apellido paterno es obligatorio y no puede contener solo espacios.",
    "telefono": "El teléfono debe contener exactamente 10 dígitos.",
    "estadoCivil": "El estado civil es obligatorio.",
    "fechaNacimiento": "La fecha de nacimiento es obligatoria.",
    "nacionalidad": "La nacionalidad es obligatoria.",
    "apellidoMaterno": "El apellido materno es obligatorio y no puede contener solo espacios.",
    "correo": "El correo electrónico no tiene un formato válido.",
    "rfc": "El RFC es obligatorio.",
    "nombre": "El nombre es obligatorio y no puede contener solo espacios."
  }
}
```

### P4. Listar usuarios (paginado)

`GET /api/v1/usuarios`, resultado: **HTTP 200**

Respuesta:
```json
{
  "content": [
    {
      "id": 3,
      "nombre": "Ana",
      "segundoNombre": "Sofia",
      "apellidoPaterno": "Torres",
      "apellidoMaterno": "Vega",
      "fechaNacimiento": "1998-04-12",
      "curp": "TOVA980412MGTRGN03",
      "rfc": "TOVA980412KL5",
      "sexo": "MUJER",
      "nacionalidad": "MEXICANA",
      "estadoCivil": "SOLTERO",
      "correo": "ana.torres@correo.com",
      "telefono": "4775551234",
      "activo": true,
      "fechaCreacion": "2026-10-20T04:16:49"
    }
  ],
  "page": {
    "size": 10,
    "number": 0,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### P5. Busqueda dinamica: nombre parcial + activo

`POST /api/v1/usuarios/busqueda`, resultado: **HTTP 200**

Solicitud:
```json
{
  "nombre": "an",
  "activo": true
}
```

Respuesta:
```json
{
  "content": [
    {
      "id": 3,
      "nombre": "Ana",
      "segundoNombre": "Sofia",
      "apellidoPaterno": "Torres",
      "apellidoMaterno": "Vega",
      "fechaNacimiento": "1998-04-12",
      "curp": "TOVA980412MGTRGN03",
      "rfc": "TOVA980412KL5",
      "sexo": "MUJER",
      "nacionalidad": "MEXICANA",
      "estadoCivil": "SOLTERO",
      "correo": "ana.torres@correo.com",
      "telefono": "4775551234",
      "activo": true,
      "fechaCreacion": "2026-10-20T04:16:49"
    }
  ],
  "page": {
    "size": 10,
    "number": 0,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### P6. Consultar por nombre + correo

`POST /api/v1/usuarios/consulta`, resultado: **HTTP 200**

Solicitud:
```json
{
  "nombre": "Ana",
  "correo": "ana.torres@correo.com"
}
```

Respuesta:
```json
{
  "id": 3,
  "nombre": "Ana",
  "segundoNombre": "Sofia",
  "apellidoPaterno": "Torres",
  "apellidoMaterno": "Vega",
  "fechaNacimiento": "1998-04-12",
  "curp": "TOVA980412MGTRGN03",
  "rfc": "TOVA980412KL5",
  "sexo": "MUJER",
  "nacionalidad": "MEXICANA",
  "estadoCivil": "SOLTERO",
  "correo": "ana.torres@correo.com",
  "telefono": "4775551234",
  "activo": true,
  "fechaCreacion": "2026-10-20T04:16:49"
}
```

### P7. Consultar sin coincidencia

`POST /api/v1/usuarios/consulta`, resultado: **HTTP 404**

Solicitud:
```json
{
  "nombre": "Otra",
  "correo": "ana.torres@correo.com"
}
```

Respuesta:
```json
{
  "codigo": "USUARIO_NO_ENCONTRADO",
  "mensaje": "No existe un usuario con el nombre y correo electrónico proporcionados."
}
```

### P8. Actualizar telefono y estado civil

`PATCH /api/v1/usuarios/actualizacion`, resultado: **HTTP 200**

Solicitud:
```json
{
  "nombre": "Ana",
  "correo": "ana.torres@correo.com",
  "telefono": "4775559876",
  "estadoCivil": "CASADO"
}
```

Respuesta:
```json
{
  "mensaje": "Usuario actualizado correctamente.",
  "datos": {
    "id": 3,
    "nombre": "Ana",
    "segundoNombre": "Sofia",
    "apellidoPaterno": "Torres",
    "apellidoMaterno": "Vega",
    "fechaNacimiento": "1998-04-12",
    "curp": "TOVA980412MGTRGN03",
    "rfc": "TOVA980412KL5",
    "sexo": "MUJER",
    "nacionalidad": "MEXICANA",
    "estadoCivil": "CASADO",
    "correo": "ana.torres@correo.com",
    "telefono": "4775559876",
    "activo": true,
    "fechaCreacion": "2026-10-20T04:16:49"
  }
}
```

### P9. Eliminar por nombre + correo

`DELETE /api/v1/usuarios/eliminacion`, resultado: **HTTP 200**

Solicitud:
```json
{
  "nombre": "Ana",
  "correo": "ana.torres@correo.com"
}
```

Respuesta:
```json
{
  "mensaje": "Usuario eliminado correctamente."
}
```

### P10. Eliminar usuario ya eliminado

`DELETE /api/v1/usuarios/eliminacion`, resultado: **HTTP 404**

Solicitud:
```json
{
  "nombre": "Ana",
  "correo": "ana.torres@correo.com"
}
```

Respuesta:
```json
{
  "codigo": "USUARIO_NO_ENCONTRADO",
  "mensaje": "No existe un usuario con el nombre y correo electrónico proporcionados."
}
```
