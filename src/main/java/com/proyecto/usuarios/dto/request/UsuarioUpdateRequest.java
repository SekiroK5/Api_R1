package com.proyecto.usuarios.dto.request;

import java.time.LocalDate;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.proyecto.usuarios.enums.Catalogos.EstadoCivil;
import com.proyecto.usuarios.enums.Catalogos.Nacionalidad;
import com.proyecto.usuarios.enums.Catalogos.Sexo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para actualizar parcialmente un usuario (llave: nombre + correo).
 *
 * Reglas:
 * - nombre y correo identifican al usuario y son obligatorios.
 * - nuevoNombre y nuevoCorreo permiten cambiar la llave; el nuevo correo debe ser unico.
 * - Un campo omitido o enviado con null no se modifica.
 * - Excepcion: segundoNombre enviado explicitamente con null se borra; si se omite, no cambia.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Actualizacion parcial. Solo se modifican los campos enviados con valor.")
public class UsuarioUpdateRequest {

    // --- Llave para localizar al usuario ---
    @NotBlank(message = "El nombre es obligatorio y no puede contener solo espacios.")
    @Schema(description = "Nombre actual del usuario (llave)", example = "Juan")
    private String nombre;

    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(regexp = UsuarioCreateRequest.REGEX_CORREO, message = "El correo electrónico no tiene un formato válido.")
    @Schema(description = "Correo actual del usuario (llave)", example = "juan.perez@correo.com")
    private String correo;

    // --- Cambio de llave (opcional) ---
    @Size(max = 60, message = "El nuevo nombre no debe exceder 60 caracteres.")
    @Pattern(regexp = UsuarioCreateRequest.REGEX_TEXTO_NOMBRE, message = "El nuevo nombre solo debe contener letras y espacios.")
    @Schema(description = "Nuevo nombre (opcional)", example = "Juan")
    private String nuevoNombre;

    @Size(min = 6, max = 120, message = "El nuevo correo debe tener entre 6 y 120 caracteres.")
    @Email(regexp = UsuarioCreateRequest.REGEX_CORREO, message = "El nuevo correo electrónico no tiene un formato válido.")
    @Schema(description = "Nuevo correo (opcional, debe ser unico)", example = "juan.perez.nuevo@correo.com")
    private String nuevoCorreo;

    // --- Datos personales (opcionales) ---
    @Size(max = 60, message = "El segundo nombre no debe exceder 60 caracteres.")
    @Pattern(regexp = UsuarioCreateRequest.REGEX_TEXTO_NOMBRE, message = "El segundo nombre solo debe contener letras y espacios.")
    @Schema(description = "Segundo nombre. Enviar null para borrarlo; omitirlo para no cambiarlo.", example = "Carlos", nullable = true)
    private String segundoNombre;

    // Indica si el cliente envio el campo segundoNombre (aunque sea null). No viene del JSON.
    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private boolean segundoNombreEnviado;

    @Size(max = 60, message = "El apellido paterno no debe exceder 60 caracteres.")
    @Pattern(regexp = UsuarioCreateRequest.REGEX_TEXTO_NOMBRE, message = "El apellido paterno solo debe contener letras y espacios.")
    @Schema(description = "Apellido paterno", example = "Perez")
    private String apellidoPaterno;

    @Size(max = 60, message = "El apellido materno no debe exceder 60 caracteres.")
    @Pattern(regexp = UsuarioCreateRequest.REGEX_TEXTO_NOMBRE, message = "El apellido materno solo debe contener letras y espacios.")
    @Schema(description = "Apellido materno", example = "Gomez")
    private String apellidoMaterno;

    @Past(message = "La fecha de nacimiento debe ser una fecha pasada.")
    @Schema(description = "Fecha de nacimiento (yyyy-MM-dd)", example = "1990-01-01")
    private LocalDate fechaNacimiento;

    @Pattern(regexp = UsuarioCreateRequest.REGEX_CURP, flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "La CURP no tiene un formato valido (18 caracteres).")
    @Schema(description = "CURP de 18 caracteres", example = "PEGJ900101HGTRRN09")
    private String curp;

    @Pattern(regexp = UsuarioCreateRequest.REGEX_RFC, flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "El RFC no tiene un formato valido (13 caracteres de persona fisica).")
    @Schema(description = "RFC de persona fisica de 13 caracteres", example = "PEGJ900101AB1")
    private String rfc;

    @Schema(description = "Sexo", example = "HOMBRE")
    private Sexo sexo;

    @Schema(description = "Nacionalidad", example = "MEXICANA")
    private Nacionalidad nacionalidad;

    @Schema(description = "Estado civil", example = "CASADO")
    private EstadoCivil estadoCivil;

    // --- Contacto y estado ---
    @Pattern(regexp = UsuarioCreateRequest.REGEX_TELEFONO, message = "El teléfono debe contener exactamente 10 dígitos.")
    @Schema(description = "Telefono de 10 digitos", example = "4779876543")
    private String telefono;

    @Schema(description = "Estado del usuario", example = "true")
    private Boolean activo;

    // Jackson solo invoca este setter cuando el campo viene en el JSON, incluso con null
    public void setSegundoNombre(String segundoNombre) {
        this.segundoNombre = segundoNombre;
        this.segundoNombreEnviado = true;
    }

    @JsonIgnore
    @AssertTrue(message = "Debe enviar al menos un campo para actualizar.")
    public boolean isAlgunCampoParaActualizar() {
        return segundoNombreEnviado || Stream.of(nuevoNombre, nuevoCorreo, apellidoPaterno, apellidoMaterno,
                        fechaNacimiento, curp, rfc, sexo, nacionalidad, estadoCivil, telefono, activo)
                .anyMatch(valor -> valor != null);
    }

}
