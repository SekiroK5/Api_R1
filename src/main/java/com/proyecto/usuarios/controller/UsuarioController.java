package com.proyecto.usuarios.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.usuarios.dto.request.UsuarioBusquedaRequest;
import com.proyecto.usuarios.dto.request.UsuarioConsultaRequest;
import com.proyecto.usuarios.dto.request.UsuarioCreateRequest;
import com.proyecto.usuarios.dto.request.UsuarioEliminacionRequest;
import com.proyecto.usuarios.dto.request.UsuarioUpdateRequest;
import com.proyecto.usuarios.dto.response.ApiErrorResponse;
import com.proyecto.usuarios.dto.response.ApiSuccessResponse;
import com.proyecto.usuarios.dto.response.UsuarioResponse;
import com.proyecto.usuarios.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller: recibe las solicitudes, valida los DTOs con @Valid, delega al servicio y devuelve las respuestas HTTP.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Alta, consulta, busqueda dinamica, actualizacion y eliminacion de usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    // 1. Crear usuario
    @PostMapping
    @Operation(summary = "Registrar usuario",
            description = "Registra un usuario nuevo. El id, el estado activo y la fecha de creacion los asigna el servidor.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado"),
            @ApiResponse(responseCode = "400", description = "Datos no validos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Correo, CURP o RFC ya registrados",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ApiSuccessResponse<UsuarioResponse>> crear(@Valid @RequestBody UsuarioCreateRequest request) {
        UsuarioResponse usuario = usuarioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiSuccessResponse<>("Usuario registrado correctamente.", usuario));
    }

    // 2. Consultar usuarios (listado paginado)
    @GetMapping
    @Operation(summary = "Listar usuarios",
            description = "Devuelve los usuarios registrados de forma paginada, del mas reciente al mas antiguo (maximo 50 por pagina).")
    @ApiResponse(responseCode = "200", description = "Pagina de usuarios")
    public ResponseEntity<Page<UsuarioResponse>> listar(@ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(usuarioService.listar(pageable));
    }

    // 3. Busqueda dinamica con criterios combinables
    @PostMapping("/busqueda")
    @Operation(summary = "Buscar usuarios por criterios",
            description = "Combina nombre (parcial), correo, telefono y activo (exactos). Los criterios no enviados se excluyen.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagina de usuarios que cumplen los criterios"),
            @ApiResponse(responseCode = "400", description = "Criterios no validos o sin criterios",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Page<UsuarioResponse>> buscar(@Valid @RequestBody UsuarioBusquedaRequest request) {
        return ResponseEntity.ok(usuarioService.buscar(request));
    }

    // 4. Consultar usuario por llave compuesta (nombre + correo)
    @PostMapping("/consulta")
    @Operation(summary = "Consultar usuario por nombre y correo",
            description = "Localiza al usuario cuyo nombre y correo coinciden con ambos valores.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos no validos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe el usuario",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<UsuarioResponse> consultar(@Valid @RequestBody UsuarioConsultaRequest request) {
        return ResponseEntity.ok(usuarioService.consultar(request));
    }

    // 5. Actualizacion parcial
    @PatchMapping("/actualizacion")
    @Operation(summary = "Actualizar parcialmente un usuario",
            description = "Localiza al usuario por nombre y correo y modifica solo los campos enviados. "
                    + "Use nuevoNombre o nuevoCorreo para cambiar la llave.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos no validos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe el usuario",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El nuevo correo, CURP o RFC ya estan registrados",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ApiSuccessResponse<UsuarioResponse>> actualizar(@Valid @RequestBody UsuarioUpdateRequest request) {
        UsuarioResponse usuario = usuarioService.actualizar(request);
        return ResponseEntity.ok(new ApiSuccessResponse<>("Usuario actualizado correctamente.", usuario));
    }

    // 6. Eliminacion por llave compuesta
    @DeleteMapping("/eliminacion")
    @Operation(summary = "Eliminar usuario",
            description = "Elimina fisicamente al usuario cuyo nombre y correo coinciden con ambos valores.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario eliminado"),
            @ApiResponse(responseCode = "400", description = "Datos no validos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe el usuario",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ApiSuccessResponse<Void>> eliminar(@Valid @RequestBody UsuarioEliminacionRequest request) {
        usuarioService.eliminar(request);
        return ResponseEntity.ok(new ApiSuccessResponse<>("Usuario eliminado correctamente.", null));
    }

}
