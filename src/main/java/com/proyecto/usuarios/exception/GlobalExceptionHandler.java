package com.proyecto.usuarios.exception;

import com.proyecto.usuarios.dto.response.ApiErrorResponse;
import com.proyecto.usuarios.enums.Catalogos.CodigoError;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Set<String> VALIDACIONES_OBLIGATORIO = Set.of("NotNull", "NotEmpty", "NotBlank");

    /**
     * En caso de que alguna validacion no se cumpla para algun DTO se tira MethodArgumentNotValidException
     * este metodo la cacha y regresa los mensajes de error para el cliente, con los message de cada campo.
     * @param ex
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> manejarValidacion(MethodArgumentNotValidException ex) {
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
        Map<String, String> listError = new LinkedHashMap<>();
        // Si un campo incumple varias reglas, primero va la de obligatorio (@NotNull, @NotEmpty, @NotBlank)
        List<FieldError> erroresOrdenados = new ArrayList<>(ex.getBindingResult().getFieldErrors());
        erroresOrdenados.sort(Comparator.comparing(error -> !VALIDACIONES_OBLIGATORIO.contains(error.getCode())));
        for (FieldError fieldError : erroresOrdenados) {
            listError.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        apiErrorResponse.setCodigo(CodigoError.DATOS_INVALIDOS.name());
        apiErrorResponse.setMensaje(CodigoError.DATOS_INVALIDOS.getMensaje());
        apiErrorResponse.setErrores(listError);
        return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Errores de negocio. Gracias al polimorfismo, cada subclase de NegocioException
     * define su propio codigo y su codigo HTTP (404 usuario no encontrado, 409 correo duplicado).
     * @param ex
     * @return
     */
    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ApiErrorResponse> manejarNegocio(NegocioException ex) {
        log.warn("[Negocio] {}: {}", ex.getCodigo(), ex.getMessage());
        return construirRespuesta(ex.getHttpStatus(), ex.getCodigo(), ex.getMessage(), null);
    }

    /**
     * Violaciones de las restricciones de la base de datos (UNIQUE, CHECK).
     * Se traduce el nombre de la restriccion a un mensaje claro sin exponer SQL ni tablas.
     * @param ex
     * @return
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> manejarIntegridad(DataIntegrityViolationException ex) {
        String restriccion = obtenerNombreRestriccion(ex);
        log.warn("[Integridad] Restriccion violada: {}", restriccion);

        if (restriccion.contains("uk_usuarios_correo")) {
            return construirRespuesta(HttpStatus.CONFLICT, CodigoError.CORREO_DUPLICADO.name(),
                    CodigoError.CORREO_DUPLICADO.getMensaje(), null);
        }
        String mensaje = CodigoError.INTEGRIDAD_DATOS.getMensaje();
        if (restriccion.contains("uk_usuarios_curp")) {
            mensaje = "La CURP ya se encuentra registrada.";
        } else if (restriccion.contains("uk_usuarios_rfc")) {
            mensaje = "El RFC ya se encuentra registrado.";
        }
        return construirRespuesta(HttpStatus.CONFLICT, CodigoError.INTEGRIDAD_DATOS.name(), mensaje, null);
    }

    /**
     * JSON mal formado, cuerpo ausente o valores que no se pueden convertir
     * (por ejemplo un valor fuera de los catalogos o una fecha con formato incorrecto).
     * @param ex
     * @return
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> manejarCuerpoIlegible(HttpMessageNotReadableException ex) {
        Map<String, String> errores = null;
        if (ex.getCause() instanceof InvalidFormatException formato && !formato.getPath().isEmpty()) {
            String campo = formato.getPath().stream()
                    .map(referencia -> referencia.getFieldName() != null ? referencia.getFieldName() : "[" + referencia.getIndex() + "]")
                    .collect(Collectors.joining("."));
            Class<?> tipo = formato.getTargetType();
            String detalle = tipo.isEnum()
                    ? "Valor no valido. Valores permitidos: " + Arrays.toString(tipo.getEnumConstants())
                    : "Valor no valido para el tipo de dato esperado" + (tipo == LocalDate.class ? " (yyyy-MM-dd)." : ".");
            errores = new LinkedHashMap<>();
            errores.put(campo, detalle);
        }
        log.warn("[Solicitud] Cuerpo no legible: {}", ex.getMostSpecificCause().getClass().getSimpleName());
        return construirRespuesta(HttpStatus.BAD_REQUEST, CodigoError.SOLICITUD_INVALIDA.name(),
                CodigoError.SOLICITUD_INVALIDA.getMensaje(), errores);
    }

    /**
     * Cualquier otra excepcion. Los errores propios de Spring MVC (ruta inexistente,
     * metodo no permitido, tipo de contenido no soportado) conservan su codigo 4xx;
     * el resto se registra en el log y se responde 500 con un mensaje controlado.
     * @param ex
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> manejarInesperado(Exception ex) {
        if (ex instanceof ErrorResponse errorSpring && errorSpring.getStatusCode().is4xxClientError()) {
            HttpStatus status = HttpStatus.valueOf(errorSpring.getStatusCode().value());
            log.warn("[Solicitud] {}: {}", status.value(), ex.getMessage());
            return construirRespuesta(status, CodigoError.SOLICITUD_INVALIDA.name(), mensajePara(status), null);
        }
        log.error("[Error] Excepcion no controlada", ex);
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, CodigoError.ERROR_INTERNO.name(),
                CodigoError.ERROR_INTERNO.getMensaje(), null);
    }

    private ResponseEntity<ApiErrorResponse> construirRespuesta(HttpStatus status, String codigo, String mensaje,
                                                                Map<String, String> errores) {
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
        apiErrorResponse.setCodigo(codigo);
        apiErrorResponse.setMensaje(mensaje);
        apiErrorResponse.setErrores(errores);
        return new ResponseEntity<>(apiErrorResponse, status);
    }

    // Usa el nombre que extrae Hibernate; si no lo obtiene (mensajes del servidor en otro idioma),
    // busca el nombre de la restriccion dentro de los mensajes de la cadena de causas
    private static String obtenerNombreRestriccion(DataIntegrityViolationException ex) {
        StringBuilder mensajes = new StringBuilder();
        Throwable causa = ex;
        while (causa != null) {
            if (causa instanceof ConstraintViolationException violacion && violacion.getConstraintName() != null) {
                return violacion.getConstraintName().toLowerCase(Locale.ROOT);
            }
            if (causa.getMessage() != null) {
                mensajes.append(causa.getMessage()).append(' ');
            }
            causa = causa.getCause();
        }
        String texto = mensajes.toString().toLowerCase(Locale.ROOT);
        return Stream.of("uk_usuarios_correo", "uk_usuarios_curp", "uk_usuarios_rfc")
                .filter(texto::contains)
                .findFirst()
                .orElse("desconocida");
    }

    private static String mensajePara(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> "El recurso solicitado no existe. Consulte la documentacion en /swagger-ui.html.";
            case METHOD_NOT_ALLOWED -> "El metodo HTTP no esta permitido para este recurso.";
            case UNSUPPORTED_MEDIA_TYPE -> "El tipo de contenido no es soportado. Utilice application/json.";
            default -> CodigoError.SOLICITUD_INVALIDA.getMensaje();
        };
    }

    // ===================== Excepciones de negocio =====================

    /**
     * Excepcion base de negocio. Cada subclase indica su codigo y su codigo HTTP.
     */
    public abstract static class NegocioException extends RuntimeException {

        protected NegocioException(String mensaje) {
            super(mensaje);
        }

        public abstract String getCodigo();

        public abstract HttpStatus getHttpStatus();
    }

    /**
     * No existe un usuario con el nombre y correo proporcionados. HTTP 404.
     */
    public static class UsuarioNoEncontradoException extends NegocioException {

        public UsuarioNoEncontradoException() {
            super(CodigoError.USUARIO_NO_ENCONTRADO.getMensaje());
        }

        @Override
        public String getCodigo() {
            return CodigoError.USUARIO_NO_ENCONTRADO.name();
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.NOT_FOUND;
        }
    }

    /**
     * El correo ya pertenece a otro usuario. HTTP 409.
     */
    public static class CorreoDuplicadoException extends NegocioException {

        public CorreoDuplicadoException() {
            super(CodigoError.CORREO_DUPLICADO.getMensaje());
        }

        @Override
        public String getCodigo() {
            return CodigoError.CORREO_DUPLICADO.name();
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.CONFLICT;
        }
    }

}
