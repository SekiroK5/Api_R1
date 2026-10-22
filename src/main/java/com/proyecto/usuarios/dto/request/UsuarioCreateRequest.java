package com.proyecto.usuarios.dto.request;

import java.time.LocalDate;

import com.proyecto.usuarios.enums.Catalogos.EstadoCivil;
import com.proyecto.usuarios.enums.Catalogos.Nacionalidad;
import com.proyecto.usuarios.enums.Catalogos.Sexo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para registrar un usuario.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para registrar un usuario. El id, el estado activo y la fecha de creacion los asigna el servidor.")
public class UsuarioCreateRequest {

    // Expresiones regulares compartidas con los demas Request DTO
    public static final String REGEX_TEXTO_NOMBRE = "^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+([ '-][A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+)*$";
    // Usuario: letras, digitos y . _ % + - (sin puntos al inicio, al final ni consecutivos).
    // Dominio: etiquetas de letras, digitos y guiones (sin guion al inicio ni al final) y extension de 2+ letras.
    // Ejemplos validos: juan.perez@correo.com, ana_92+ventas@empresa.com.mx
    public static final String REGEX_CORREO =
            "^[A-Za-z0-9_%+-]+(\\.[A-Za-z0-9_%+-]+)*@[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?(\\.[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?)*\\.[A-Za-z]{2,}$";
    // Telefono mexicano de exactamente 10 digitos, sin espacios ni guiones (formato del documento)
    public static final String REGEX_TELEFONO = "^[0-9]{10}$";
    public static final String REGEX_CURP = "^[A-Z][AEIOUX][A-Z]{2}[0-9]{6}[HMX][A-Z]{2}[B-DF-HJ-NP-TV-Z]{3}[A-Z0-9][0-9]$";
    public static final String REGEX_RFC = "^[A-ZÑ&]{4}[0-9]{6}[A-Z0-9]{3}$";

    // --- Datos personales ---
    @NotBlank(message = "El nombre es obligatorio y no puede contener solo espacios.")
    @Size(max = 60, message = "El nombre no debe exceder 60 caracteres.")
    @Pattern(regexp = REGEX_TEXTO_NOMBRE, message = "El nombre solo debe contener letras y espacios.")
    @Schema(description = "Nombre del usuario", example = "Juan")
    private String nombre;

    @Size(max = 60, message = "El segundo nombre no debe exceder 60 caracteres.")
    @Pattern(regexp = REGEX_TEXTO_NOMBRE, message = "El segundo nombre solo debe contener letras y espacios.")
    @Schema(description = "Segundo nombre (opcional)", example = "Carlos")
    private String segundoNombre;

    @NotBlank(message = "El apellido paterno es obligatorio y no puede contener solo espacios.")
    @Size(max = 60, message = "El apellido paterno no debe exceder 60 caracteres.")
    @Pattern(regexp = REGEX_TEXTO_NOMBRE, message = "El apellido paterno solo debe contener letras y espacios.")
    @Schema(description = "Apellido paterno", example = "Perez")
    private String apellidoPaterno;

    @NotBlank(message = "El apellido materno es obligatorio y no puede contener solo espacios.")
    @Size(max = 60, message = "El apellido materno no debe exceder 60 caracteres.")
    @Pattern(regexp = REGEX_TEXTO_NOMBRE, message = "El apellido materno solo debe contener letras y espacios.")
    @Schema(description = "Apellido materno", example = "Gomez")
    private String apellidoMaterno;

    @NotNull(message = "La fecha de nacimiento es obligatoria.")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada.")
    @Schema(description = "Fecha de nacimiento (yyyy-MM-dd)", example = "1990-01-01")
    private LocalDate fechaNacimiento;

    // @NotEmpty rechaza null y ""; los espacios y el formato los rechaza @Pattern
    @NotEmpty(message = "La CURP es obligatoria.")
    @Pattern(regexp = REGEX_CURP, flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "La CURP no tiene un formato valido (18 caracteres).")
    @Schema(description = "CURP de 18 caracteres", example = "PEGJ900101HGTRRN09")
    private String curp;

    @NotEmpty(message = "El RFC es obligatorio.")
    @Pattern(regexp = REGEX_RFC, flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "El RFC no tiene un formato valido (13 caracteres de persona fisica).")
    @Schema(description = "RFC de persona fisica de 13 caracteres", example = "PEGJ900101AB1")
    private String rfc;

    // --- Catalogos (enums) ---
    @NotNull(message = "El sexo es obligatorio.")
    @Schema(description = "Sexo", example = "HOMBRE")
    private Sexo sexo;

    @NotNull(message = "La nacionalidad es obligatoria.")
    @Schema(description = "Nacionalidad", example = "MEXICANA")
    private Nacionalidad nacionalidad;

    @NotNull(message = "El estado civil es obligatorio.")
    @Schema(description = "Estado civil", example = "SOLTERO")
    private EstadoCivil estadoCivil;

    // --- Contacto ---
    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(regexp = REGEX_CORREO, message = "El correo electrónico no tiene un formato válido.")
    @Size(max = 120, message = "El correo electrónico no debe exceder 120 caracteres.")
    @Schema(description = "Correo electronico unico", example = "juan.perez@correo.com")
    private String correo;

    @NotBlank(message = "El teléfono es obligatorio.")
    @Pattern(regexp = REGEX_TELEFONO, message = "El teléfono debe contener exactamente 10 dígitos.")
    @Schema(description = "Telefono de 10 digitos", example = "4771234567")
    private String telefono;

}
