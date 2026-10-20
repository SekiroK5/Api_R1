package com.proyecto.usuarios.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para eliminar un usuario por la llave compuesta nombre + correo.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Llave compuesta (nombre + correo) del usuario a eliminar")
public class UsuarioEliminacionRequest {

    @NotBlank(message = "El nombre es obligatorio y no puede contener solo espacios.")
    @Schema(description = "Nombre del usuario", example = "Juan")
    private String nombre;

    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(regexp = UsuarioCreateRequest.REGEX_CORREO, message = "El correo electrónico no tiene un formato válido.")
    @Schema(description = "Correo electronico del usuario", example = "juan.perez@correo.com")
    private String correo;

}
