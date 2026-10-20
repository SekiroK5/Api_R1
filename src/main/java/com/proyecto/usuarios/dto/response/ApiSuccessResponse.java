package com.proyecto.usuarios.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de salida para respuestas exitosas con mensaje y datos.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta exitosa con mensaje y datos")
public class ApiSuccessResponse<T> {

    @Schema(example = "Usuario registrado correctamente.")
    private String mensaje;

    private T datos;

}
