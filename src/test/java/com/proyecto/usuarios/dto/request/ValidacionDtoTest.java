package com.proyecto.usuarios.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.proyecto.usuarios.enums.Catalogos.EstadoCivil;
import com.proyecto.usuarios.enums.Catalogos.Nacionalidad;
import com.proyecto.usuarios.enums.Catalogos.Sexo;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Pruebas de las anotaciones de Bean Validation de los Request DTO:
 * formatos aceptados por REGEX_CORREO y REGEX_TELEFONO, @NotNull y @AssertTrue.
 */
class ValidacionDtoTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void iniciar() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void cerrar() {
        factory.close();
    }

    // ===================== 1. Correo electronico =====================

    @ParameterizedTest
    @ValueSource(strings = {
            "juan.perez@correo.com",
            "ana_92+ventas@empresa.com.mx",
            "JUAN.PEREZ@CORREO.COM",
            "maria-lopez@mi-dominio.org"
    })
    void correoConFormatoValidoSeAcepta(String correo) {
        assertThat(camposConError(solicitudCreacion(correo, "4771234567"))).doesNotContain("correo");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "juan",                   // sin arroba
            "juan@correo",            // sin extension
            "juan@correo.c",          // extension de una letra
            "juan perez@correo.com",  // con espacio
            ".juan@correo.com",       // punto al inicio
            "juan.@correo.com",       // punto al final del usuario
            "juan..perez@correo.com", // puntos consecutivos
            "juan@-correo.com",       // dominio que inicia con guion
            "a,b@correo.com",         // caracter no permitido
            "juan@@correo.com"        // doble arroba
    })
    void correoConFormatoInvalidoSeRechaza(String correo) {
        assertThat(mensajes(solicitudCreacion(correo, "4771234567"), "correo"))
                .containsExactly("El correo electrónico no tiene un formato válido.");
    }

    @Test
    void correoVacioSoloReportaQueEsObligatorio() {
        assertThat(mensajes(solicitudCreacion("", "4771234567"), "correo"))
                .contains("El correo electrónico es obligatorio.");
    }

    // ===================== 2. Telefono =====================

    @Test
    void telefonoDeDiezDigitosSeAcepta() {
        assertThat(camposConError(solicitudCreacion("juan.perez@correo.com", "4771234567"))).doesNotContain("telefono");
    }

    @ParameterizedTest
    @ValueSource(strings = {"477123456", "47712345678", "477-123-4567", "477 123 4567", "47712345ab", "+524771234567"})
    void telefonoConFormatoInvalidoSeRechaza(String telefono) {
        assertThat(mensajes(solicitudCreacion("juan.perez@correo.com", telefono), "telefono"))
                .containsExactly("El teléfono debe contener exactamente 10 dígitos.");
    }

    // ===================== 3. Busqueda dinamica =====================

    @Test
    void busquedaConCriteriosNulosYUnFiltroEsValida() {
        UsuarioBusquedaRequest request = UsuarioBusquedaRequest.builder()
                .nombre("Juan").correo(null).telefono(null).activo(true).build();
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void busquedaSinCriteriosSeRechazaConAssertTrue() {
        assertThat(camposConError(new UsuarioBusquedaRequest())).containsExactly("algunCriterio");
    }

    @Test
    void busquedaConPaginaYTamanioNulosSeRechaza() {
        UsuarioBusquedaRequest request = UsuarioBusquedaRequest.builder()
                .nombre("Juan").pagina(null).tamanio(null).build();
        assertThat(camposConError(request)).containsExactlyInAnyOrder("pagina", "tamanio");
    }

    // ===================== 4. Campos obligatorios =====================

    @Test
    void creacionSinCamposObligatoriosLosReportaTodos() {
        assertThat(camposConError(new UsuarioCreateRequest())).containsExactlyInAnyOrder(
                "nombre", "apellidoPaterno", "apellidoMaterno", "fechaNacimiento", "curp", "rfc",
                "sexo", "nacionalidad", "estadoCivil", "correo", "telefono");
    }

    @Test
    void segundoNombreEsOpcional() {
        UsuarioCreateRequest request = solicitudCreacion("juan.perez@correo.com", "4771234567");
        request.setSegundoNombre(null);
        assertThat(validator.validate(request)).isEmpty();
    }

    // ===================== Utilidades =====================

    private static UsuarioCreateRequest solicitudCreacion(String correo, String telefono) {
        return UsuarioCreateRequest.builder()
                .nombre("Juan").segundoNombre("Carlos").apellidoPaterno("Perez").apellidoMaterno("Gomez")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .curp("PEGJ900101HGTRRN09").rfc("PEGJ900101AB1")
                .sexo(Sexo.HOMBRE).nacionalidad(Nacionalidad.MEXICANA).estadoCivil(EstadoCivil.SOLTERO)
                .correo(correo).telefono(telefono)
                .build();
    }

    private static <T> Set<String> camposConError(T dto) {
        return validator.validate(dto).stream()
                .map(violacion -> violacion.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    private static <T> Set<String> mensajes(T dto, String campo) {
        return validator.validate(dto).stream()
                .filter(violacion -> violacion.getPropertyPath().toString().equals(campo))
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }

}
