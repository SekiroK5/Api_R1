package com.proyecto.usuarios.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.proyecto.usuarios.enums.Catalogos.EstadoCivil;
import com.proyecto.usuarios.enums.Catalogos.Nacionalidad;
import com.proyecto.usuarios.enums.Catalogos.Sexo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de salida con la información del usuario.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Informacion del usuario que expone la API")
public class UsuarioResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Juan")
    private String nombre;

    @Schema(example = "Carlos")
    private String segundoNombre;

    @Schema(example = "Perez")
    private String apellidoPaterno;

    @Schema(example = "Gomez")
    private String apellidoMaterno;

    @Schema(example = "1990-01-01")
    private LocalDate fechaNacimiento;

    @Schema(example = "PEGJ900101HGTRRN09")
    private String curp;

    @Schema(example = "PEGJ900101AB1")
    private String rfc;

    @Schema(example = "HOMBRE")
    private Sexo sexo;

    @Schema(example = "MEXICANA")
    private Nacionalidad nacionalidad;

    @Schema(example = "SOLTERO")
    private EstadoCivil estadoCivil;

    @Schema(example = "juan.perez@correo.com")
    private String correo;

    @Schema(example = "4771234567")
    private String telefono;

    @Schema(example = "true")
    private Boolean activo;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(example = "2026-10-14T10:30:00")
    private LocalDateTime fechaCreacion;

}
