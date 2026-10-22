package com.proyecto.usuarios.dto.request;

import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada con los criterios de la búsqueda dinámica.
 *
 * Reglas:
 * - nombre: coincidencia parcial sin distinguir mayusculas.
 * - correo, telefono y activo: coincidencia exacta.
 * - Los criterios vacios o nulos se excluyen; los enviados se combinan con AND.
 * - Se requiere al menos un criterio y el resultado siempre se pagina.
 * - Los criterios aceptan null a proposito (son opcionales); pagina y tamanio no.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Criterios combinables para la busqueda dinamica de usuarios")
public class UsuarioBusquedaRequest {

    @Size(max = 60, message = "El nombre no debe exceder 60 caracteres.")
    @Schema(description = "Texto contenido en el nombre (parcial)", example = "Juan", nullable = true)
    private String nombre;

    @Email(regexp = UsuarioCreateRequest.REGEX_CORREO, message = "El correo electrónico no tiene un formato válido.")
    @Schema(description = "Correo exacto", example = "juan.perez@correo.com", nullable = true)
    private String correo;

    @Pattern(regexp = UsuarioCreateRequest.REGEX_TELEFONO, message = "El teléfono debe contener exactamente 10 dígitos.")
    @Schema(description = "Telefono exacto", example = "4771234567", nullable = true)
    private String telefono;

    @Schema(description = "Estado del usuario", example = "true", nullable = true)
    private Boolean activo;

    // Si se omite toma el valor por defecto; si se envia null explicitamente se rechaza
    @Builder.Default
    @NotNull(message = "La pagina no puede ser nula.")
    @Min(value = 0, message = "La pagina debe ser mayor o igual a 0.")
    @Schema(description = "Numero de pagina (inicia en 0)", example = "0", defaultValue = "0")
    private Integer pagina = 0;

    @Builder.Default
    @NotNull(message = "El tamaño de pagina no puede ser nulo.")
    @Min(value = 1, message = "El tamaño de pagina debe ser al menos 1.")
    @Max(value = 50, message = "El tamaño de pagina no debe exceder 50.")
    @Schema(description = "Registros por pagina (1 a 50)", example = "10", defaultValue = "10")
    private Integer tamanio = 10;

    @JsonIgnore
    @AssertTrue(message = "Debe enviar al menos un criterio de búsqueda (nombre, correo, telefono o activo).")
    public boolean isAlgunCriterio() {
        return activo != null || Stream.of(nombre, correo, telefono)
                .anyMatch(valor -> valor != null && !valor.isBlank());
    }

}
